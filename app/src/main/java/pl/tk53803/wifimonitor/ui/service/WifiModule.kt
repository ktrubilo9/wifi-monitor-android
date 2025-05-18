package pl.tk53803.wifimonitor.ui.service

import android.content.Context
import android.net.wifi.WifiManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WifiModule {
    @Provides
    @Singleton
    fun provideWifiManager(@ApplicationContext appContext: Context): WifiManager {
        return appContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
}