package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FirewallViewModel
import com.example.ui.screens.FirewallMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.vpn.FirewallVpnService

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FirewallApp()
                }
            }
        }
    }
}

@Composable
fun FirewallApp() {
    val app = DFNetControlApp.instance
    val viewModel: FirewallViewModel = viewModel(
        factory = FirewallViewModel.Factory(app.applicationContext, app.repository)
    )

    val allApps by viewModel.allApps.collectAsStateWithLifecycle()
    val filteredApps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isBangla by viewModel.isBangla.collectAsStateWithLifecycle()

    val isFirewallActive by viewModel.isFirewallActive.collectAsStateWithLifecycle()
    val activeNetwork by viewModel.activeNetworkType.collectAsStateWithLifecycle()
    val blockedOnNetworkCount by viewModel.blockedAppsOnCurrentNetworkCount.collectAsStateWithLifecycle()
    val totalBlockedWifi by viewModel.totalBlockedWifiCount.collectAsStateWithLifecycle()
    val totalBlockedMobile by viewModel.totalBlockedMobileCount.collectAsStateWithLifecycle()
    val packetsDropped by viewModel.packetsDropped.collectAsStateWithLifecycle()
    val bytesDropped by viewModel.bytesDropped.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()

    // VPN Consent Launcher
    val vpnConsentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            FirewallVpnService.startService(app.applicationContext)
            Toast.makeText(
                app.applicationContext,
                if (isBangla) "ফায়ারওয়াল চালু হয়েছে" else "Firewall started successfully",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                app.applicationContext,
                if (isBangla) "ভিপিএন অনুমতি প্রত্যাখ্যান করা হয়েছে" else "VPN permission was denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Permission handled
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    FirewallMainScreen(
        allApps = allApps,
        filteredApps = filteredApps,
        searchQuery = searchQuery,
        activeFilter = activeFilter,
        selectedTab = selectedTab,
        isBangla = isBangla,
        isFirewallActive = isFirewallActive,
        activeNetwork = activeNetwork,
        blockedOnNetworkCount = blockedOnNetworkCount,
        totalBlockedWifi = totalBlockedWifi,
        totalBlockedMobile = totalBlockedMobile,
        packetsDropped = packetsDropped,
        bytesDropped = bytesDropped,
        recentLogs = recentLogs,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onFilterSelected = viewModel::onFilterSelected,
        onTabSelected = viewModel::onTabSelected,
        onToggleLanguage = viewModel::toggleLanguage,
        onToggleWifi = viewModel::toggleWifi,
        onToggleMobile = viewModel::toggleMobile,
        onBlockAllWifi = viewModel::blockAllWifi,
        onAllowAllWifi = viewModel::allowAllWifi,
        onBlockAllMobile = viewModel::blockAllMobile,
        onAllowAllMobile = viewModel::allowAllMobile,
        onResetAllRules = viewModel::resetAllRules,
        onRefreshApps = viewModel::refreshInstalledApps,
        onResetStats = viewModel::resetStats,
        onStartFirewall = {
            viewModel.startFirewall { intent ->
                vpnConsentLauncher.launch(intent)
            }
        },
        onStopFirewall = viewModel::stopFirewall
    )
}
