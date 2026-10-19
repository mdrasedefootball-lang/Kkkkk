package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppRuleDao {

    @Query("SELECT * FROM firewall_rules ORDER BY appName ASC")
    fun getAllRules(): Flow<List<AppFirewallRule>>

    @Query("SELECT * FROM firewall_rules WHERE packageName = :packageName")
    suspend fun getRuleByPackage(packageName: String): AppFirewallRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRule(rule: AppFirewallRule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRules(rules: List<AppFirewallRule>)

    @Query("UPDATE firewall_rules SET blockWifi = :blocked, lastUpdated = :timestamp WHERE packageName = :packageName")
    suspend fun setWifiBlocked(packageName: String, blocked: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE firewall_rules SET blockMobile = :blocked, lastUpdated = :timestamp WHERE packageName = :packageName")
    suspend fun setMobileBlocked(packageName: String, blocked: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE firewall_rules SET blockWifi = :blocked, lastUpdated = :timestamp")
    suspend fun setAllWifiBlocked(blocked: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE firewall_rules SET blockMobile = :blocked, lastUpdated = :timestamp")
    suspend fun setAllMobileBlocked(blocked: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE firewall_rules SET blockWifi = 0, blockMobile = 0, lastUpdated = :timestamp")
    suspend fun resetAllRules(timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM firewall_rules WHERE blockWifi = 1")
    suspend fun getWifiBlockedRules(): List<AppFirewallRule>

    @Query("SELECT * FROM firewall_rules WHERE blockMobile = 1")
    suspend fun getMobileBlockedRules(): List<AppFirewallRule>
}
