package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppFirewallRule
import com.example.data.local.AppRuleDao
import com.example.data.local.FirewallDatabase
import com.example.model.AppItem
import com.example.model.NetworkType
import com.example.vpn.FirewallStateHolder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: FirewallDatabase
    private lateinit var dao: AppRuleDao
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FirewallDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.appRuleDao()
        FirewallStateHolder.resetStats()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `verify app name resource is DF Net Control`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("DF Net Control", appName)
    }

    @Test
    fun `verify AppItem blocked state calculations`() {
        val appNone = AppItem("com.test.one", "Test One", isSystemApp = false, blockWifi = false, blockMobile = false)
        assertFalse(appNone.isAnyBlocked)
        assertFalse(appNone.isBothBlocked)

        val appWifiOnly = appNone.copy(blockWifi = true)
        assertTrue(appWifiOnly.isAnyBlocked)
        assertFalse(appWifiOnly.isBothBlocked)

        val appMobileOnly = appNone.copy(blockMobile = true)
        assertTrue(appMobileOnly.isAnyBlocked)
        assertFalse(appMobileOnly.isBothBlocked)

        val appBoth = appNone.copy(blockWifi = true, blockMobile = true)
        assertTrue(appBoth.isAnyBlocked)
        assertTrue(appBoth.isBothBlocked)
    }

    @Test
    fun `verify Room DAO inserts and updates Wi-Fi and Mobile rules independently`() = runBlocking {
        val rule1 = AppFirewallRule(
            packageName = "com.browser.app",
            appName = "Browser",
            blockWifi = false,
            blockMobile = false
        )
        dao.upsertRule(rule1)

        val saved = dao.getRuleByPackage("com.browser.app")
        assertEquals("Browser", saved?.appName)
        assertFalse(saved!!.blockWifi)
        assertFalse(saved.blockMobile)

        // Block on Wi-Fi only
        dao.setWifiBlocked("com.browser.app", true)
        val afterWifi = dao.getRuleByPackage("com.browser.app")
        assertTrue(afterWifi!!.blockWifi)
        assertFalse(afterWifi.blockMobile)

        // Block on Mobile only
        dao.setMobileBlocked("com.browser.app", true)
        val afterBoth = dao.getRuleByPackage("com.browser.app")
        assertTrue(afterBoth!!.blockWifi)
        assertTrue(afterBoth.blockMobile)

        // Reset all
        dao.resetAllRules()
        val afterReset = dao.getRuleByPackage("com.browser.app")
        assertFalse(afterReset!!.blockWifi)
        assertFalse(afterReset.blockMobile)
    }

    @Test
    fun `verify batch blocking and query flows`() = runBlocking {
        val rules = listOf(
            AppFirewallRule("com.app.a", "App A", blockWifi = false, blockMobile = false),
            AppFirewallRule("com.app.b", "App B", blockWifi = false, blockMobile = false),
            AppFirewallRule("com.app.c", "App C", blockWifi = false, blockMobile = false)
        )
        dao.upsertRules(rules)

        dao.setAllWifiBlocked(true)
        val wifiBlocked = dao.getWifiBlockedRules()
        assertEquals(3, wifiBlocked.size)

        dao.setAllMobileBlocked(true)
        val mobileBlocked = dao.getMobileBlockedRules()
        assertEquals(3, mobileBlocked.size)

        val all = dao.getAllRules().first()
        assertEquals(3, all.size)
        assertTrue(all.all { it.blockWifi && it.blockMobile })
    }

    @Test
    fun `verify FirewallStateHolder records telemetry accurately`() {
        assertEquals(0L, FirewallStateHolder.packetsDropped.value)
        assertEquals(0L, FirewallStateHolder.bytesDropped.value)

        FirewallStateHolder.recordDroppedPacket(128, 6) // TCP packet
        assertEquals(1L, FirewallStateHolder.packetsDropped.value)
        assertEquals(128L, FirewallStateHolder.bytesDropped.value)
        assertEquals("TCP", FirewallStateHolder.recentLogs.value.first().protocol)

        FirewallStateHolder.recordDroppedPacket(64, 17) // UDP packet
        assertEquals(2L, FirewallStateHolder.packetsDropped.value)
        assertEquals(192L, FirewallStateHolder.bytesDropped.value)
        assertEquals("UDP", FirewallStateHolder.recentLogs.value.first().protocol)

        FirewallStateHolder.setActiveNetwork(NetworkType.WIFI)
        assertEquals(NetworkType.WIFI, FirewallStateHolder.activeNetworkType.value)

        FirewallStateHolder.setActiveNetwork(NetworkType.MOBILE)
        assertEquals(NetworkType.MOBILE, FirewallStateHolder.activeNetworkType.value)
    }
}
