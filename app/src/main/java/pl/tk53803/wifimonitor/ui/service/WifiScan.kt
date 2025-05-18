package pl.tk53803.wifimonitor.ui.service

import android.content.Context
import android.net.wifi.WifiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.tk53803.wifimonitor.data.WifiRepository
import javax.inject.Inject

class WifiScan @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wifiManager: WifiManager,
    private val repository: WifiRepository
) {
    //receiver
    private var receiver

    fun start() {

    }

    fun stop() {

    }
}