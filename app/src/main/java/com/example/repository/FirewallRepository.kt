package com.example.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import com.example.data.local.AppFirewallRule
import com.example.data.local.AppRuleDao
import com.example.model.AppItem
import com.example.vpn.FirewallStateHolder
import com.example.vpn.FirewallVpnService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class FirewallRepository(
    private val context: Context,
    private val appRuleDao: AppRuleDao
) {
    private val iconCache = ConcurrentHashMap<String, Drawable>()
    private val labelCache = ConcurrentHashMap<String, String>()
    private val systemAppCache = ConcurrentHashMap<String, Boolean>()

    companion object {
        private const val TAG = "FirewallRepository"
    }

    /**
     * Flow of applications merged with their persistent Wi-Fi and Mobile Data blocking rules.
     */
    val appItemsFlow: Flow<List<AppItem>> = appRuleDao.getAllRules().map { rules ->
        rules.map { rule ->
            AppItem(
                packageName = rule.packageName,
                appName = rule.appName,
                isSystemApp = rule.isSystemApp,
                blockWifi = rule.blockWifi,
                blockMobile = rule.blockMobile,
                icon = iconCache[rule.packageName]
            )
        }
    }

    /**
     * Scan installed applications on the device, cache metadata/icons, and populate
     * any missing database rows with default allowed rules.
     */
    suspend fun refreshInstalledApps() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val selfPackage = context.packageName

        try {
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val newRulesToInsert = mutableListOf<AppFirewallRule>()

            for (appInfo in installedApps) {
                val pkg = appInfo.packageName
                if (pkg == selfPackage) continue

                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val label = try {
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    pkg
                }

                labelCache[pkg] = label
                systemAppCache[pkg] = isSystem

                try {
                    val icon = pm.getApplicationIcon(appInfo)
                    iconCache[pkg] = icon
                } catch (e: Exception) {
                    // Fallback to null icon
                }

                // Check if already in DB
                val existing = appRuleDao.getRuleByPackage(pkg)
                if (existing == null) {
                    newRulesToInsert.add(
                        AppFirewallRule(
                            packageName = pkg,
                            appName = label,
                            blockWifi = false,
                            blockMobile = false,
                            isSystemApp = isSystem
                        )
                    )
                } else if (existing.appName != label) {
                    // Update app label if renamed
                    appRuleDao.upsertRule(existing.copy(appName = label, isSystemApp = isSystem))
                }
            }

            if (newRulesToInsert.isNotEmpty()) {
                appRuleDao.upsertRules(newRulesToInsert)
                Log.d(TAG, "Inserted ${newRulesToInsert.size} newly discovered apps into database.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh installed applications", e)
        }
    }

    suspend fun setWifiBlocked(packageName: String, blocked: Boolean) {
        appRuleDao.setWifiBlocked(packageName, blocked)
        notifyServiceIfActive()
    }

    suspend fun setMobileBlocked(packageName: String, blocked: Boolean) {
        appRuleDao.setMobileBlocked(packageName, blocked)
        notifyServiceIfActive()
    }

    suspend fun setAllWifiBlocked(blocked: Boolean) {
        appRuleDao.setAllWifiBlocked(blocked)
        notifyServiceIfActive()
    }

    suspend fun setAllMobileBlocked(blocked: Boolean) {
        appRuleDao.setAllMobileBlocked(blocked)
        notifyServiceIfActive()
    }

    suspend fun resetAllRules() {
        appRuleDao.resetAllRules()
        notifyServiceIfActive()
    }

    private fun notifyServiceIfActive() {
        if (FirewallStateHolder.isFirewallActive.value) {
            FirewallVpnService.reloadRules(context)
        }
    }
}
