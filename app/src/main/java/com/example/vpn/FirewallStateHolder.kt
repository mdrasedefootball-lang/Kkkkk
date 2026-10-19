package com.example.vpn

import com.example.model.DroppedPacketLog
import com.example.model.NetworkType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong

/**
 * Global live state holder for telemetry, connection state, and coordination between
 * FirewallVpnService, ViewModel, and UI.
 */
object FirewallStateHolder {

    private val _isFirewallActive = MutableStateFlow(false)
    val isFirewallActive: StateFlow<Boolean> = _isFirewallActive.asStateFlow()

    private val _activeNetworkType = MutableStateFlow(NetworkType.NONE)
    val activeNetworkType: StateFlow<NetworkType> = _activeNetworkType.asStateFlow()

    private val _blockedAppsOnCurrentNetworkCount = MutableStateFlow(0)
    val blockedAppsOnCurrentNetworkCount: StateFlow<Int> = _blockedAppsOnCurrentNetworkCount.asStateFlow()

    private val _packetsDropped = MutableStateFlow(0L)
    val packetsDropped: StateFlow<Long> = _packetsDropped.asStateFlow()

    private val _bytesDropped = MutableStateFlow(0L)
    val bytesDropped: StateFlow<Long> = _bytesDropped.asStateFlow()

    private val _recentLogs = MutableStateFlow<List<DroppedPacketLog>>(emptyList())
    val recentLogs: StateFlow<List<DroppedPacketLog>> = _recentLogs.asStateFlow()

    private val atomicPackets = AtomicLong(0)
    private val atomicBytes = AtomicLong(0)

    fun setFirewallActive(active: Boolean) {
        _isFirewallActive.value = active
    }

    fun setActiveNetwork(networkType: NetworkType) {
        _activeNetworkType.value = networkType
    }

    fun setBlockedAppsCount(count: Int) {
        _blockedAppsOnCurrentNetworkCount.value = count
    }

    fun recordDroppedPacket(bytes: Int, protocolNum: Int) {
        val count = atomicPackets.incrementAndGet()
        val totalBytes = atomicBytes.addAndGet(bytes.toLong())

        _packetsDropped.value = count
        _bytesDropped.value = totalBytes

        val protoName = when (protocolNum) {
            6 -> "TCP"
            17 -> "UDP"
            1 -> "ICMP"
            58 -> "ICMPv6"
            else -> "IP-$protocolNum"
        }

        val log = DroppedPacketLog(
            protocol = protoName,
            sizeBytes = bytes,
            note = "Blocked & sinkholed"
        )

        val current = _recentLogs.value.toMutableList()
        current.add(0, log)
        if (current.size > 50) {
            current.removeAt(current.lastIndex)
        }
        _recentLogs.value = current
    }

    fun resetStats() {
        atomicPackets.set(0)
        atomicBytes.set(0)
        _packetsDropped.value = 0L
        _bytesDropped.value = 0L
        _recentLogs.value = emptyList()
    }
}
