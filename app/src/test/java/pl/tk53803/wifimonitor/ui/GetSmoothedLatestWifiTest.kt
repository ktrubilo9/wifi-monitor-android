package pl.tk53803.wifimonitor.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import pl.tk53803.wifimonitor.data.WifiRepository
import pl.tk53803.wifimonitor.data.local.database.WifiInfoDao
import pl.tk53803.wifimonitor.data.local.database.WifiInfoType

class GetSmoothedLatestWifiTest {

    @Test
    fun returnsLatestSmoothedMeasurementForEachNetworkOrderedBySignal() = runTest {
        val measurements = listOf(
            measurement(bssid = "network-a", rssi = -50, timestamp = 1),
            measurement(bssid = "network-a", rssi = -60, timestamp = 2),
            measurement(bssid = "network-a", rssi = -70, timestamp = 3),
            measurement(bssid = "network-b", rssi = -40, timestamp = 1)
        )
        val subject = GetSmoothedLatestWifi(WifiRepository(FakeWifiInfoDao(measurements)))

        val result = subject().first()

        assertEquals(listOf("network-b", "network-a"), result.map { it.bssid })
        assertEquals(listOf(-40, -60), result.map { it.rssi })
    }

    private fun measurement(
        bssid: String,
        rssi: Int,
        timestamp: Long
    ) = WifiInfoType(
        ssid = bssid,
        bssid = bssid,
        rssi = rssi,
        linkSpeed = 100,
        frequency = 2400,
        estimatedDistance = 1f,
        timestamp = timestamp
    )
}

private class FakeWifiInfoDao(
    initialMeasurements: List<WifiInfoType>
) : WifiInfoDao {
    private val measurements = MutableStateFlow(initialMeasurements)

    override suspend fun insert(wifiInfo: WifiInfoType) {
        measurements.value += wifiInfo
    }

    override fun getAll(): Flow<List<WifiInfoType>> = measurements

    override fun getByBssid(bssid: String): Flow<List<WifiInfoType>> =
        measurements.map { items -> items.filter { it.bssid == bssid } }

    override suspend fun deleteNotInBssids(bssids: List<String>) {
        measurements.value = measurements.value.filter { it.bssid in bssids }
    }

    override suspend fun deleteOldData(bssid: String) {
        val latest = measurements.value
            .filter { it.bssid == bssid }
            .maxByOrNull { it.timestamp }

        measurements.value = measurements.value.filter {
            it.bssid != bssid || it == latest
        }
    }
}
