package pl.tk53803.wifimonitor.ui.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.tk53803.wifimonitor.data.WifiRepository
import pl.tk53803.wifimonitor.ui.receiver.WifiScanReceiver
import javax.inject.Inject

class WifiScan @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wifiManager: WifiManager,
    private val repository: WifiRepository
) {
    private var receiver: BroadcastReceiver? = null

    fun start() {
        receiver = WifiScanReceiver(wifiManager, repository)
        context.registerReceiver(receiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()
    }

    fun stop() {
        receiver?.let {
            context.unregisterReceiver(it)
            receiver = null
        }
    }
}