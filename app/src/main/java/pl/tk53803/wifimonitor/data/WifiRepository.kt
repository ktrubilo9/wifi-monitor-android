package pl.tk53803.wifimonitor.data

import kotlinx.coroutines.flow.Flow
import pl.tk53803.wifimonitor.data.local.database.WifiInfoDao
import pl.tk53803.wifimonitor.data.local.database.WifiInfoType
import javax.inject.Inject

class WifiRepository @Inject constructor(
    private val dao: WifiInfoDao
) {
    suspend fun insert(wifi: WifiInfoType) = dao.insert(wifi)
    suspend fun deleteNotInBssids(bssids: List<String>) = dao.deleteNotInBssids(bssids)
    fun getAll(): Flow<List<WifiInfoType>> = dao.getAll()
    fun getByBssid(bssid: String): Flow<List<WifiInfoType>> = dao.getByBssid(bssid)
}
