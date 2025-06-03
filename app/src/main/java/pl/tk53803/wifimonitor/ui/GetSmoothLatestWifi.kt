package pl.tk53803.wifimonitor.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.tk53803.wifimonitor.data.WifiRepository
import pl.tk53803.wifimonitor.data.local.database.WifiInfoType
import javax.inject.Inject

class GetSmoothedLatestWifi @Inject constructor(
    private val repository: WifiRepository
) {
    operator fun invoke(): Flow<List<WifiInfoType>> {
        return repository.getAll().map { list ->
            list.groupBy { it.bssid }
                .mapNotNull { (_, group) ->
                    val sorted = group.sortedBy { it.timestamp }
                    val smoothed = applyMovingAverage(sorted, 5)
                    smoothed.lastOrNull()
                }
                .sortedByDescending { it.rssi }
        }
    }

    private fun applyMovingAverage(list: List<WifiInfoType>, window: Int): List<WifiInfoType> {
        return list.mapIndexed { index, item ->
            val start = maxOf(0, index - window + 1)
            val windowItems = list.subList(start, index + 1)
            val avgRssi = windowItems.map { it.rssi }.average().toInt()
            item.copy(rssi = avgRssi)
        }
    }
}
