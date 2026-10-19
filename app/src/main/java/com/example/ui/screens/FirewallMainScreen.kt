package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppFilter
import com.example.model.AppItem
import com.example.model.DroppedPacketLog
import com.example.model.NetworkType
import com.example.ui.theme.BlockedRose
import com.example.ui.theme.MobileOrange
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.ShieldEmerald
import com.example.ui.theme.WifiBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirewallMainScreen(
    // State
    allApps: List<AppItem>,
    filteredApps: List<AppItem>,
    searchQuery: String,
    activeFilter: AppFilter,
    selectedTab: Int,
    isBangla: Boolean,
    isFirewallActive: Boolean,
    activeNetwork: NetworkType,
    blockedOnNetworkCount: Int,
    totalBlockedWifi: Int,
    totalBlockedMobile: Int,
    packetsDropped: Long,
    bytesDropped: Long,
    recentLogs: List<DroppedPacketLog>,
    // Callbacks
    onSearchQueryChanged: (String) -> Unit,
    onFilterSelected: (AppFilter) -> Unit,
    onTabSelected: (Int) -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleWifi: (AppItem) -> Unit,
    onToggleMobile: (AppItem) -> Unit,
    onBlockAllWifi: () -> Unit,
    onAllowAllWifi: () -> Unit,
    onBlockAllMobile: () -> Unit,
    onAllowAllMobile: () -> Unit,
    onResetAllRules: () -> Unit,
    onRefreshApps: () -> Unit,
    onResetStats: () -> Unit,
    onStartFirewall: () -> Unit,
    onStopFirewall: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isBangla) "ডিএফ নেট কন্ট্রোল" else "DF Net Control",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBangla) "ওয়াই-ফাই ও মোবাইল ফায়ারওয়াল" else "Wi-Fi & Mobile Data Firewall",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Language Switcher Chip
                    Surface(
                        onClick = onToggleLanguage,
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("language_toggle_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isBangla) "English" else "বাংলা",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Refresh Button
                    IconButton(
                        onClick = onRefreshApps,
                        modifier = Modifier.testTag("refresh_apps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh apps",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    icon = { Icon(Icons.Default.Security, contentDescription = null) },
                    label = { Text(if (isBangla) "অ্যাপসমূহ" else "Apps", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_apps")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    icon = { Icon(Icons.Default.Lan, contentDescription = null) },
                    label = { Text(if (isBangla) "স্ট্যাটাস ও লগ" else "Status & Logs", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_status")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { onTabSelected(2) },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text(if (isBangla) "কাজের নিয়ম" else "How It Works", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_how_it_works")
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> AppsListTab(
                allApps = allApps,
                filteredApps = filteredApps,
                searchQuery = searchQuery,
                activeFilter = activeFilter,
                isBangla = isBangla,
                isFirewallActive = isFirewallActive,
                activeNetwork = activeNetwork,
                blockedOnNetworkCount = blockedOnNetworkCount,
                onSearchQueryChanged = onSearchQueryChanged,
                onFilterSelected = onFilterSelected,
                onToggleWifi = onToggleWifi,
                onToggleMobile = onToggleMobile,
                onBlockAllWifi = onBlockAllWifi,
                onAllowAllWifi = onAllowAllWifi,
                onBlockAllMobile = onBlockAllMobile,
                onAllowAllMobile = onAllowAllMobile,
                onResetAllRules = onResetAllRules,
                onStartFirewall = onStartFirewall,
                onStopFirewall = onStopFirewall,
                modifier = Modifier.padding(innerPadding)
            )
            1 -> StatusLogsScreen(
                isFirewallActive = isFirewallActive,
                activeNetwork = activeNetwork,
                blockedOnNetworkCount = blockedOnNetworkCount,
                totalBlockedWifi = totalBlockedWifi,
                totalBlockedMobile = totalBlockedMobile,
                packetsDropped = packetsDropped,
                bytesDropped = bytesDropped,
                recentLogs = recentLogs,
                isBangla = isBangla,
                onResetStats = onResetStats,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> HowItWorksScreen(
                isBangla = isBangla,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun AppsListTab(
    allApps: List<AppItem>,
    filteredApps: List<AppItem>,
    searchQuery: String,
    activeFilter: AppFilter,
    isBangla: Boolean,
    isFirewallActive: Boolean,
    activeNetwork: NetworkType,
    blockedOnNetworkCount: Int,
    onSearchQueryChanged: (String) -> Unit,
    onFilterSelected: (AppFilter) -> Unit,
    onToggleWifi: (AppItem) -> Unit,
    onToggleMobile: (AppItem) -> Unit,
    onBlockAllWifi: () -> Unit,
    onAllowAllWifi: () -> Unit,
    onBlockAllMobile: () -> Unit,
    onAllowAllMobile: () -> Unit,
    onResetAllRules: () -> Unit,
    onStartFirewall: () -> Unit,
    onStopFirewall: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuickActions by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("apps_list_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Master Firewall Hero Card
        item {
            MasterFirewallCard(
                isFirewallActive = isFirewallActive,
                activeNetwork = activeNetwork,
                blockedOnNetworkCount = blockedOnNetworkCount,
                isBangla = isBangla,
                onStartFirewall = onStartFirewall,
                onStopFirewall = onStopFirewall
            )
        }

        // Search Bar & Filter Row
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = {
                    Text(
                        text = if (isBangla) "অ্যাপ বা প্যাকেজের নাম খুঁজুন…" else "Search by app or package name…",
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_search_field")
            )
        }

        // Quick Actions & Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "ফিল্টার সমূহ" else "Filters",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        onClick = { showQuickActions = !showQuickActions },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (showQuickActions) {
                                    if (isBangla) "ব্যবস্থা লুকান" else "Hide Actions"
                                } else {
                                    if (isBangla) "একসাথ ব্যবস্থা" else "Batch Actions"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Batch Actions expandable drawer
                AnimatedVisibility(visible = showQuickActions) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isBangla) "এক ক্লিকে সকল অ্যাপের নিয়ন্ত্রণ:" else "Batch Control Across All Apps:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onBlockAllWifi,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("action_block_all_wifi"),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(if (isBangla) "ওয়াই-ফাইতে সব বন্ধ" else "Block All Wi-Fi", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = onAllowAllWifi,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("action_allow_all_wifi"),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(if (isBangla) "ওয়াই-ফাইতে সব অনুমতি" else "Allow All Wi-Fi", fontSize = 11.sp)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onBlockAllMobile,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("action_block_all_mobile"),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(if (isBangla) "মোবাইলে সব বন্ধ" else "Block All Mobile", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = onAllowAllMobile,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("action_allow_all_mobile"),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(if (isBangla) "মোবাইলে সব অনুমতি" else "Allow All Mobile", fontSize = 11.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = onResetAllRules,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("action_reset_all_rules"),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBangla) "সব নিয়ম স্বাভাবিক অবস্থায় আনুন" else "Reset All Rules to Allowed", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Filter Chips Horizontal Scroll
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val allCount = allApps.size
                    val blockedCount = allApps.count { it.isAnyBlocked }
                    val userCount = allApps.count { !it.isSystemApp }
                    val systemCount = allApps.count { it.isSystemApp }

                    FilterChip(
                        selected = activeFilter == AppFilter.ALL,
                        onClick = { onFilterSelected(AppFilter.ALL) },
                        label = { Text(if (isBangla) "সকল ($allCount)" else "All ($allCount)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("filter_chip_all")
                    )

                    FilterChip(
                        selected = activeFilter == AppFilter.BLOCKED,
                        onClick = { onFilterSelected(AppFilter.BLOCKED) },
                        label = { Text(if (isBangla) "অবরুদ্ধ ($blockedCount)" else "Blocked ($blockedCount)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BlockedRose.copy(alpha = 0.2f),
                            selectedLabelColor = BlockedRose
                        ),
                        modifier = Modifier.testTag("filter_chip_blocked")
                    )

                    FilterChip(
                        selected = activeFilter == AppFilter.USER,
                        onClick = { onFilterSelected(AppFilter.USER) },
                        label = { Text(if (isBangla) "ব্যবহারকারী ($userCount)" else "User Apps ($userCount)", fontSize = 12.sp) },
                        modifier = Modifier.testTag("filter_chip_user")
                    )

                    FilterChip(
                        selected = activeFilter == AppFilter.SYSTEM,
                        onClick = { onFilterSelected(AppFilter.SYSTEM) },
                        label = { Text(if (isBangla) "সিস্টেম ($systemCount)" else "System ($systemCount)", fontSize = 12.sp) },
                        modifier = Modifier.testTag("filter_chip_system")
                    )
                }
            }
        }

        // App List Content
        if (allApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (isBangla) "ইনস্টল করা অ্যাপ লোড হচ্ছে…" else "Scanning installed apps…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else if (filteredApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (isBangla) "কোনো অ্যাপ খুঁজে পাওয়া যায়নি" else "No matching apps found",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBangla) "অনুসন্ধান বা ফিল্টার পরিবর্তন করে দেখুন।" else "Try adjusting your search terms or filter selection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(
                items = filteredApps,
                key = { it.packageName }
            ) { app ->
                AppListItem(
                    app = app,
                    isBangla = isBangla,
                    onToggleWifi = { onToggleWifi(app) },
                    onToggleMobile = { onToggleMobile(app) }
                )
            }
        }
    }
}

@Composable
private fun MasterFirewallCard(
    isFirewallActive: Boolean,
    activeNetwork: NetworkType,
    blockedOnNetworkCount: Int,
    isBangla: Boolean,
    onStartFirewall: () -> Unit,
    onStopFirewall: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isFirewallActive) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val cardColor by animateColorAsState(
        targetValue = if (isFirewallActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant,
        label = "cardColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFirewallActive) ShieldEmerald.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        label = "borderColor"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("master_firewall_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                if (isFirewallActive) ShieldEmerald.copy(alpha = 0.18f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFirewallActive) Icons.Default.Shield else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (isFirewallActive) ShieldEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isFirewallActive) {
                                if (isBangla) "ফায়ারওয়াল চালু আছে" else "Firewall is Active"
                            } else {
                                if (isBangla) "ফায়ারওয়াল বন্ধ রয়েছে" else "Firewall is Stopped"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = if (isFirewallActive) ShieldEmerald else MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = if (isFirewallActive) {
                                if (isBangla)
                                    "${activeNetwork.getDisplayName(isBangla)}-তে $blockedOnNetworkCount টি অ্যাপ অবরুদ্ধ"
                                else
                                    "$blockedOnNetworkCount apps blocked on ${activeNetwork.getDisplayName(isBangla)}"
                            } else {
                                if (isBangla) "চালু করতে নিচের বোতামে চাপুন" else "Tap start to activate local packet filter"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Active Network Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (activeNetwork) {
                        NetworkType.WIFI -> WifiBlue.copy(alpha = 0.15f)
                        NetworkType.MOBILE -> MobileOrange.copy(alpha = 0.15f)
                        NetworkType.NONE -> MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = when (activeNetwork) {
                                NetworkType.WIFI -> Icons.Default.Wifi
                                NetworkType.MOBILE -> Icons.Default.CellTower
                                NetworkType.NONE -> Icons.Default.WifiOff
                            },
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = when (activeNetwork) {
                                NetworkType.WIFI -> WifiBlue
                                NetworkType.MOBILE -> MobileOrange
                                NetworkType.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = activeNetwork.getDisplayName(isBangla),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Start / Stop Master Button
            if (isFirewallActive) {
                OutlinedButton(
                    onClick = onStopFirewall,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BlockedRose
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, BlockedRose),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("firewall_stop_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "ফায়ারওয়াল বন্ধ করুন" else "Stop Firewall",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            } else {
                Button(
                    onClick = onStartFirewall,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShieldEmerald,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("firewall_start_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "ফায়ারওয়াল চালু করুন" else "Start Firewall",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
