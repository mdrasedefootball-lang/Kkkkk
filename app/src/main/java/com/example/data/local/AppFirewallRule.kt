package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores the user's firewall rule for each application.
 *
 * @param packageName Unique Android application package name
 * @param appName Human-readable application label
 * @param blockWifi True if app should be prevented from communicating over Wi-Fi
 * @param blockMobile True if app should be prevented from communicating over Cellular/Mobile Data
 * @param isSystemApp Flag indicating whether this is a pre-installed system application
 * @param lastUpdated Timestamp of last rule update
 */
@Entity(tableName = "firewall_rules")
data class AppFirewallRule(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val blockWifi: Boolean = false,
    val blockMobile: Boolean = false,
    val isSystemApp: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
