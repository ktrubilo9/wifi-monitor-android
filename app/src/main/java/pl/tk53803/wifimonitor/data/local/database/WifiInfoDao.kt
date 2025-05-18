package pl.tk53803.wifimonitor.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WifiInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wifiInfo: WifiInfoType)

    @Query("SELECT * FROM wifi_info ORDER BY timestamp DESC")
    fun getAll(): Flow<List<WifiInfoType>>
}