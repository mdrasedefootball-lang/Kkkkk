package com.example.model

import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val blockWifi: Boolean,
    val blockMobile: Boolean,
    val icon: Drawable? = null
) {
    val isAnyBlocked: Boolean get() = blockWifi || blockMobile
    val isBothBlocked: Boolean get() = blockWifi && blockMobile
}

enum class NetworkType {
    WIFI,
    MOBILE,
    NONE;

    fun getDisplayName(isBangla: Boolean): String {
        return when (this) {
            WIFI -> if (isBangla) "ওয়াই-ফাই" else "Wi-Fi"
            MOBILE -> if (isBangla) "মোবাইল ডেটা" else "Mobile Data"
            NONE -> if (isBangla) "কোনো সংযোগ নেই" else "No Connection"
        }
    }
}

enum class AppFilter {
    ALL,
    BLOCKED,
    USER,
    SYSTEM
}

data class DroppedPacketLog(
    val timestamp: Long = System.currentTimeMillis(),
    val protocol: String, // TCP, UDP, ICMP, OTHER
    val sizeBytes: Int,
    val note: String = ""
)
