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
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

class WifiScanReceiver (
    private val wifiManager: WifiManager,
    private val repository: WifiRepository
) : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val res: List<ScanResult> = wifiManager.scanResults

        CoroutineScope(Dispatchers.IO).launch {
            val currentBssids = res.map{ it.BSSID }
            repository.deleteNotInBssids(currentBssids)

            res.forEach { it ->
                val d = dist(it.level, it.frequency)
                repository.insert(
                    WifiInfoType(
                        /*
                            używam tego aby dostać ssid wszystkich sieci,
                            kotlin proponuje getWifiSsid, które zwraca tylko SSID aktualnej sieci
                         */
                        ssid = it.SSID.ifEmpty { "Unknown" },
                        bssid = it.BSSID,
                        rssi = it.level,
                        /*
                            podobnie jak dla ssid, można dostać linkSpeed wszystkich sieci,
                            a kotlin proponuje rozwiazanie które zwraca tylko linkSpeed aktualnej sieci
                         */
                        linkSpeed = wifiManager.connectionInfo.linkSpeed,
                        frequency = it.frequency,
                        estimatedDistance = d
                    )
                )
            }
        }
    }

    // metoda do wyliczania odległości
    private fun dist(rssi: Int, freq: Int,): Float{
        val freqMhz = freq.toDouble()
        val exponent = (27.55 - (20 * log10(freqMhz)) + abs(rssi)) / 20.0
        return 10.0.pow(exponent).toFloat()
    }
}