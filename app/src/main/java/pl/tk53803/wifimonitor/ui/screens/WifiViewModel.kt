package pl.tk53803.wifimonitor.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pl.tk53803.wifimonitor.data.WifiRepository
import pl.tk53803.wifimonitor.data.local.database.WifiInfoType
import pl.tk53803.wifimonitor.ui.GetSmoothedLatestWifi
import javax.inject.Inject

@HiltViewModel
class WifiViewModel @Inject constructor(
    private val getSmoothedLatestWifi: GetSmoothedLatestWifi,
    private val repository: WifiRepository
) : ViewModel() {

    val smoothedLatest: StateFlow<List<WifiInfoType>> = getSmoothedLatestWifi()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getSmoothedByBssid(bssid: String): Flow<List<WifiInfoType>> =
        repository.getByBssid(bssid).map { list ->
            list.sortedBy { it.timestamp }.let { sorted ->
                sorted.mapIndexed { index, item ->
                    val start = maxOf(0, index - 5 + 1)
                    val window = sorted.subList(start, index + 1)
                    val avg = window.map { it.rssi }.average().toInt()
                    item.copy(rssi = avg)
                }
            }
        }

    suspend fun deleteOldData(bssid: String) = repository.deleteOldData(bssid)
}
