package com.example

import android.app.Application
import com.example.data.local.FirewallDatabase
import com.example.repository.FirewallRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DFNetControlApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { FirewallDatabase.getInstance(this) }
    val repository by lazy { FirewallRepository(this, database.appRuleDao()) }

    companion object {
        lateinit var instance: DFNetControlApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        applicationScope.launch {
            repository.refreshInstalledApps()
        }
    }
}
