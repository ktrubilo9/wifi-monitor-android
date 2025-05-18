package pl.tk53803.wifimonitor.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import pl.tk53803.wifimonitor.data.WifiRepository
import pl.tk53803.wifimonitor.data.local.database.WifiInfoType
import javax.inject.Inject

@HiltViewModel
class WifiViewModel @Inject constructor(
    private val repository: WifiRepository
) : ViewModel() {
    val wifiHistory: StateFlow<List<WifiInfoType>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), emptyList())

    suspend fun insert(wifi: WifiInfoType) = repository.insert(wifi)
    fun getByBssid(bssid: String) = repository.getByBssid(bssid)
}