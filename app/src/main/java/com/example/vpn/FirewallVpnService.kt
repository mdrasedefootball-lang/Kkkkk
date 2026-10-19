package com.example.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.FirewallDatabase
import com.example.model.NetworkType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class FirewallVpnService : VpnService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var vpnInterface: ParcelFileDescriptor? = null
    private var packetReaderJob: Job? = null
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private var currentNetworkType: NetworkType = NetworkType.NONE

    companion object {
        private const val TAG = "FirewallVpnService"
        const val ACTION_START = "com.example.dfnetcontrol.START"
        const val ACTION_STOP = "com.example.dfnetcontrol.STOP"
        const val ACTION_RELOAD_RULES = "com.example.dfnetcontrol.RELOAD_RULES"

        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "df_firewall_service_channel"

        fun startService(context: Context) {
            val intent = Intent(context, FirewallVpnService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, FirewallVpnService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun reloadRules(context: Context) {
            val intent = Intent(context, FirewallVpnService::class.java).apply {
                action = ACTION_RELOAD_RULES
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupNetworkCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                Log.d(TAG, "Stopping Firewall Service")
                shutdownVpn()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_RELOAD_RULES -> {
                Log.d(TAG, "Reloading Firewall Rules")
                serviceScope.launch {
                    rebuildVpn()
                }
            }
            ACTION_START, null -> {
                Log.d(TAG, "Starting Firewall Service")
                startForeground(NOTIFICATION_ID, buildNotification("Initializing firewall…"))
                FirewallStateHolder.setFirewallActive(true)
                serviceScope.launch {
                    detectInitialNetwork()
                    rebuildVpn()
                }
            }
        }
        return START_STICKY
    }

    private fun setupNetworkCallback() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val isWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                val isMobile = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                val detectedType = when {
                    isWifi -> NetworkType.WIFI
                    isMobile -> NetworkType.MOBILE
                    else -> NetworkType.NONE
                }

                if (detectedType != currentNetworkType) {
                    Log.d(TAG, "Network switched to: $detectedType")
                    currentNetworkType = detectedType
                    FirewallStateHolder.setActiveNetwork(detectedType)
                    serviceScope.launch {
                        rebuildVpn()
                    }
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost")
                currentNetworkType = NetworkType.NONE
                FirewallStateHolder.setActiveNetwork(NetworkType.NONE)
                serviceScope.launch {
                    rebuildVpn()
                }
            }
        }

        try {
            connectivityManager?.registerDefaultNetworkCallback(networkCallback!!)
        } catch (e: Exception) {
            Log.w(TAG, "registerDefaultNetworkCallback failed, falling back to registerNetworkCallback", e)
            try {
                connectivityManager?.registerNetworkCallback(request, networkCallback!!)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to register network callback", e2)
            }
        }
    }

    private fun detectInitialNetwork() {
        val cm = connectivityManager ?: return
        val activeNet = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNet)
        currentNetworkType = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.MOBILE
            else -> NetworkType.NONE
        }
        FirewallStateHolder.setActiveNetwork(currentNetworkType)
    }

    private val vpnMutex = Mutex()

    private suspend fun rebuildVpn() {
        vpnMutex.withLock {
            val database = FirewallDatabase.getInstance(applicationContext)
            val dao = database.appRuleDao()

            // Fetch blocked packages for current active network
            val blockedPackages = when (currentNetworkType) {
                NetworkType.WIFI -> dao.getWifiBlockedRules().map { it.packageName }
                NetworkType.MOBILE -> dao.getMobileBlockedRules().map { it.packageName }
                NetworkType.NONE -> {
                    // If offline, consider any rule marked blocked
                    (dao.getWifiBlockedRules() + dao.getMobileBlockedRules()).map { it.packageName }.distinct()
                }
            }.filter { it != packageName } // Never intercept our own app

            FirewallStateHolder.setBlockedAppsCount(blockedPackages.size)
            updateNotification(blockedPackages.size)

            // Close prior reader loop and prior VPN interface
            packetReaderJob?.cancel()
            packetReaderJob = null
            try {
                vpnInterface?.close()
            } catch (e: IOException) {
                Log.w(TAG, "Error closing previous vpn interface", e)
            }
            vpnInterface = null

            if (blockedPackages.isEmpty()) {
                Log.d(TAG, "No applications blocked on current network ($currentNetworkType). VPN in standby.")
                return@withLock
            }

        try {
            val builder = Builder()
            builder.setSession("DF Net Control")
            builder.setMtu(1500)

            // Sinkhole tunnel addressing
            builder.addAddress("10.120.0.1", 32)
            builder.addRoute("0.0.0.0", 0) // Capture IPv4 for targeted apps

            try {
                builder.addAddress("fd00:df:1::1", 128)
                builder.addRoute("::", 0) // Capture IPv6 for targeted apps
            } catch (e: Exception) {
                Log.w(TAG, "IPv6 address setup not supported on this device/ROM", e)
            }

            // Route DNS for targeted apps to local sinkhole so they cannot resolve hostnames
            builder.addDnsServer("10.120.0.1")
            builder.setBlocking(true)

            var actualAdded = 0
            val pm = packageManager
            for (pkg in blockedPackages) {
                try {
                    // Check if package still installed
                    pm.getPackageInfo(pkg, 0)
                    builder.addAllowedApplication(pkg)
                    actualAdded++
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.d(TAG, "Package $pkg not installed, skipping")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to add allowed application $pkg", e)
                }
            }

            if (actualAdded > 0) {
                val pfd = builder.establish()
                if (pfd != null) {
                    vpnInterface = pfd
                    Log.i(TAG, "VPN TUN established successfully. Intercepting $actualAdded blocked apps on $currentNetworkType.")
                    startPacketReader(pfd)
                } else {
                    Log.e(TAG, "Builder.establish() returned null. Consent may have been revoked.")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to establish VPN interface", e)
        }
    }
}

    private fun startPacketReader(pfd: ParcelFileDescriptor) {
        packetReaderJob = serviceScope.launch(Dispatchers.IO) {
            val inputStream = FileInputStream(pfd.fileDescriptor)
            val buffer = ByteArray(32768)
            Log.d(TAG, "Packet reader loop started for blocked applications")

            try {
                while (isActive && pfd.fileDescriptor.valid()) {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        // Inspect packet header
                        val version = (buffer[0].toInt() shr 4) and 0x0F
                        val protocol = when {
                            version == 4 && bytesRead >= 20 -> buffer[9].toInt() and 0xFF
                            version == 6 && bytesRead >= 40 -> buffer[6].toInt() and 0xFF
                            else -> 0
                        }

                        // Record real-time dropped packet telemetry
                        FirewallStateHolder.recordDroppedPacket(bytesRead, protocol)

                        // Do not forward packet anywhere: it is silently dropped / sinkholed!
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    Log.d(TAG, "Packet reader loop ended: ${e.message}")
                }
            } finally {
                try {
                    inputStream.close()
                } catch (ignored: Exception) {}
            }
        }
    }

    private fun shutdownVpn() {
        packetReaderJob?.cancel()
        packetReaderJob = null

        try {
            vpnInterface?.close()
        } catch (e: IOException) {
            Log.w(TAG, "Error closing VPN interface", e)
        }
        vpnInterface = null

        FirewallStateHolder.setFirewallActive(false)
        FirewallStateHolder.setBlockedAppsCount(0)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, FirewallVpnService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(statusText)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.notification_action_stop),
                stopPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(blockedCount: Int) {
        val netStr = when (currentNetworkType) {
            NetworkType.WIFI -> getString(R.string.network_wifi)
            NetworkType.MOBILE -> getString(R.string.network_mobile)
            NetworkType.NONE -> getString(R.string.network_none)
        }
        val text = getString(R.string.notification_text_running, blockedCount, netStr)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        networkCallback?.let {
            try {
                connectivityManager?.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering network callback", e)
            }
        }
        shutdownVpn()
        serviceScope.cancel()
        Log.d(TAG, "FirewallVpnService destroyed")
    }
}
