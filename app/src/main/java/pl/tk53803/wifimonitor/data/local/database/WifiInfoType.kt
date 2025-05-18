package pl.tk53803.wifimonitor.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wifi_info")
data class WifiInfoType(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "ssid") val ssid: String,
    @ColumnInfo(name = "rssi") val rssi: Int,
    @ColumnInfo(name = "link_speed") val linkSpeed: Int,
    @ColumnInfo(name = "frequency") val frequency: Int,
    @ColumnInfo(name = "estimated_distance") val estimatedDistance: Float,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)
