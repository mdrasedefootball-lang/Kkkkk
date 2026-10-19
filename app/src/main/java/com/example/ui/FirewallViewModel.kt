package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.model.AppFilter
import com.example.model.AppItem
import com.example.model.DroppedPacketLog
import com.example.model.NetworkType
import com.example.repository.FirewallRepository
import com.example.vpn.FirewallStateHolder
import com.example.vpn.FirewallVpnService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FirewallViewModel(
    private val context: Context,
    private val repository: FirewallRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeFilter = MutableStateFlow(AppFilter.ALL)
    val activeFilter: StateFlow<AppFilter> = _activeFilter.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Apps, 1: Status & Logs, 2: How It Works
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isBangla = MutableStateFlow(false)
    val isBangla: StateFlow<Boolean> = _isBangla.asStateFlow()

    val isFirewallActive: StateFlow<Boolean> = FirewallStateHolder.isFirewallActive
    val activeNetworkType: StateFlow<NetworkType> = FirewallStateHolder.activeNetworkType
    val blockedAppsOnCurrentNetworkCount: StateFlow<Int> = FirewallStateHolder.blockedAppsOnCurrentNetworkCount
    val packetsDropped: StateFlow<Long> = FirewallStateHolder.packetsDropped
    val bytesDropped: StateFlow<Long> = FirewallStateHolder.bytesDropped
    val recentLogs: StateFlow<List<DroppedPacketLog>> = FirewallStateHolder.recentLogs

    val allApps: StateFlow<List<AppItem>> = repository.appItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredApps: StateFlow<List<AppItem>> = combine(
        repository.appItemsFlow,
        _searchQuery,
        _activeFilter
    ) { apps, query, filter ->
        apps.filter { app ->
            val matchesQuery = query.isBlank() ||
                    app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                AppFilter.ALL -> true
                AppFilter.BLOCKED -> app.isAnyBlocked
                AppFilter.USER -> !app.isSystemApp
                AppFilter.SYSTEM -> app.isSystemApp
            }

            matchesQuery && matchesFilter
        }.sortedWith(
            compareByDescending<AppItem> { it.isAnyBlocked }
                .thenBy { it.appName.lowercase() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBlockedWifiCount: StateFlow<Int> = repository.appItemsFlow.combine(MutableStateFlow(Unit)) { apps, _ ->
        apps.count { it.blockWifi }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalBlockedMobileCount: StateFlow<Int> = repository.appItemsFlow.combine(MutableStateFlow(Unit)) { apps, _ ->
        apps.count { it.blockMobile }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterSelected(filter: AppFilter) {
        _activeFilter.value = filter
    }

    fun onTabSelected(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun toggleLanguage() {
        _isBangla.value = !_isBangla.value
    }

    fun toggleWifi(app: AppItem) {
        viewModelScope.launch {
            repository.setWifiBlocked(app.packageName, !app.blockWifi)
        }
    }

    fun toggleMobile(app: AppItem) {
        viewModelScope.launch {
            repository.setMobileBlocked(app.packageName, !app.blockMobile)
        }
    }

    fun blockAllWifi() {
        viewModelScope.launch {
            repository.setAllWifiBlocked(true)
        }
    }

    fun allowAllWifi() {
        viewModelScope.launch {
            repository.setAllWifiBlocked(false)
        }
    }

    fun blockAllMobile() {
        viewModelScope.launch {
            repository.setAllMobileBlocked(true)
        }
    }

    fun allowAllMobile() {
        viewModelScope.launch {
            repository.setAllMobileBlocked(false)
        }
    }

    fun resetAllRules() {
        viewModelScope.launch {
            repository.resetAllRules()
        }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            repository.refreshInstalledApps()
        }
    }

    fun resetStats() {
        FirewallStateHolder.resetStats()
    }

    fun startFirewall(onNeedsConsent: (Intent) -> Unit) {
        val prepareIntent = VpnService.prepare(context)
        if (prepareIntent != null) {
            onNeedsConsent(prepareIntent)
        } else {
            FirewallVpnService.startService(context)
        }
    }

    fun stopFirewall() {
        FirewallVpnService.stopService(context)
    }

    class Factory(
        private val context: Context,
        private val repository: FirewallRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FirewallViewModel(context, repository) as T
        }
    }
}
