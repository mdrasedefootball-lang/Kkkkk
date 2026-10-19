package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DroppedPacketLog
import com.example.model.NetworkType
import com.example.ui.theme.BlockedRose
import com.example.ui.theme.MobileOrange
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.ShieldEmerald
import com.example.ui.theme.WifiBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusLogsScreen(
    isFirewallActive: Boolean,
    activeNetwork: NetworkType,
    blockedOnNetworkCount: Int,
    totalBlockedWifi: Int,
    totalBlockedMobile: Int,
    packetsDropped: Long,
    bytesDropped: Long,
    recentLogs: List<DroppedPacketLog>,
    isBangla: Boolean,
    onResetStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedBytes = remember(bytesDropped) {
        formatBytes(bytesDropped)
    }

    val timeFormatter = remember {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("status_logs_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Network Interface Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isFirewallActive) PrimaryCyan.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (activeNetwork) {
                                            NetworkType.WIFI -> WifiBlue.copy(alpha = 0.15f)
                                            NetworkType.MOBILE -> MobileOrange.copy(alpha = 0.15f)
                                            NetworkType.NONE -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (activeNetwork) {
                                        NetworkType.WIFI -> Icons.Default.Wifi
                                        NetworkType.MOBILE -> Icons.Default.CellTower
                                        NetworkType.NONE -> Icons.Default.WifiOff
                                    },
                                    contentDescription = null,
                                    tint = when (activeNetwork) {
                                        NetworkType.WIFI -> WifiBlue
                                        NetworkType.MOBILE -> MobileOrange
                                        NetworkType.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = if (isBangla) "সক্রিয় নেটওয়ার্ক ইন্টারফেস" else "Active Network Interface",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = activeNetwork.getDisplayName(isBangla),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // State chip
                        Surface(
                            shape = CircleShape,
                            color = if (isFirewallActive) ShieldEmerald.copy(alpha = 0.15f) else BlockedRose.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isFirewallActive) ShieldEmerald.copy(alpha = 0.4f) else BlockedRose.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = if (isFirewallActive) {
                                    if (isBangla) "ফিল্টারিং সক্রিয়" else "Enforcing"
                                } else {
                                    if (isBangla) "নিষ্ক্রিয়" else "Inactive"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isFirewallActive) ShieldEmerald else BlockedRose,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Technical Details Row
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            DetailRow(
                                label = if (isBangla) "ভার্চুয়াল টানেল (TUN)" else "Virtual Tunnel (TUN)",
                                value = if (isFirewallActive) "10.120.0.1 / fd00:df:1::1" else if (isBangla) "বন্ধ" else "Closed"
                            )
                            DetailRow(
                                label = if (isBangla) "ডিএনএস ব্ল্যাকহোল" else "DNS Sinkhole",
                                value = if (isFirewallActive) "10.120.0.1 (Local drop)" else if (isBangla) "বন্ধ" else "Inactive"
                            )
                            DetailRow(
                                label = if (isBangla) "বর্তমানে ব্লকলিস্টে অ্যাপ" else "Apps in Sinkhole",
                                value = if (isBangla) "$blockedOnNetworkCount টি অ্যাপ" else "$blockedOnNetworkCount apps"
                            )
                        }
                    }
                }
            }
        }

        // Live Telemetry Grid
        item {
            Text(
                text = if (isBangla) "লাইভ পরিসংখ্যান ও পরিমাপ" else "Live Telemetry & Metrics",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = if (isBangla) "ড্রপকৃত প্যাকেট" else "Dropped Packets",
                    value = packetsDropped.toString(),
                    icon = Icons.Default.Shield,
                    tint = BlockedRose,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = if (isBangla) "সংরক্ষিত ডেটা" else "Data Saved",
                    value = formattedBytes,
                    icon = Icons.Default.Lan,
                    tint = PrimaryCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = if (isBangla) "ওয়াই-ফাইতে অবরুদ্ধ" else "Blocked on Wi-Fi",
                    value = "$totalBlockedWifi",
                    icon = Icons.Default.Wifi,
                    tint = WifiBlue,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = if (isBangla) "মোবাইলে অবরুদ্ধ" else "Blocked on Mobile",
                    value = "$totalBlockedMobile",
                    icon = Icons.Default.CellTower,
                    tint = MobileOrange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Real-time packet log stream
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "প্যাকেট ফিল্টারিং লাইভ লগ" else "Live Packet Filter Logs",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                FilledTonalButton(
                    onClick = onResetStats,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("reset_stats_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBangla) "রিসেট" else "Reset",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (recentLogs.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (isBangla) "এখনও কোনো প্যাকেট ড্রপ হয়নি" else "No packets intercepted yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isBangla)
                                "অবরুদ্ধ কোনো অ্যাপ সংযোগ করার চেষ্টা করলে এখানে তাৎক্ষণিক প্যাকেট লগ প্রদর্শিত হবে।"
                            else
                                "Packets intercepted from blocked applications on the TUN interface will stream here in real time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(recentLogs) { log ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BlockedRose.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = log.protocol,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BlockedRose,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = if (isBangla) "প্যাকেট সাইজ: ${log.sizeBytes} বাইট" else "Packet size: ${log.sizeBytes} bytes",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = log.note,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = timeFormatter.format(Date(log.timestamp)),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
        else -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
