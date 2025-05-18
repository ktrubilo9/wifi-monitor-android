package pl.tk53803.wifimonitor.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WifiInfoType::class], version = 1)
abstract class WifiDatabase : RoomDatabase() {
    abstract fun wifiInfoDao(): WifiInfoDao
}