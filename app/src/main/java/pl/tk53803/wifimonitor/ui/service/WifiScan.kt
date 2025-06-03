package pl.tk53803.wifimonitor.ui.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.tk53803.wifimonitor.data.WifiRepository
import pl.tk53803.wifimonitor.ui.receiver.WifiScanReceiver
import javax.inject.Inject

class WifiScan @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: WifiRepository
) {
    @Inject lateinit var wifiManager: WifiManager
    private var receiver: BroadcastReceiver? = null

    private var scanJob: Job? = null

    fun start() {
        receiver = WifiScanReceiver(wifiManager, repository)
        context.registerReceiver(receiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        scanJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                /**
                 * używam tego tylko dlatego bo jest to deprecated z tego powodu, że
                 * android chce ograniczyć skanowanie sieciowe do aplikacji i będzie usunięte
                 */
                wifiManager.startScan()
                delay(10000)
            }
        }
    }

    fun stop() {
        receiver?.let {
            context.unregisterReceiver(it)
            receiver = null
        }
        scanJob?.cancel()
        scanJob = null
    }
}