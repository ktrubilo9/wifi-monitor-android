package pl.tk53803.wifimonitor.ui.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.tk53803.wifimonitor.data.WifiRepository
import pl.tk53803.wifimonitor.data.local.database.WifiInfoType

class WifiScanReceiver (
    private val wifiManager: WifiManager,
    private val repository: WifiRepository
) : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val res: List<ScanResult> = wifiManager.scanResults

        CoroutineScope(Dispatchers.IO).launch {
            res.forEach { it ->
                val d = dist(it.level, it.frequency)
                repository.insert(
                    WifiInfoType(
                        ssid = it.SSID.ifEmpty { "Unknown" }, //?
                        bssid = it.BSSID,
                        rssi = it.level,
                        linkSpeed = wifiManager.connectionInfo.linkSpeed, //to fix?
                        frequency = it.frequency,
                        estimatedDistance = d
                    )
                )
            }
        }
    }

    private fun dist(rssi: Int, freq: Int,): Float{
        val d = 0
        return d.toFloat()
    }
}