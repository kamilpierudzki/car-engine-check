package com.kamil.pierudzki.carenginecheck.hilt

import android.bluetooth.BluetoothManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SingletonComponentModule {

    @Singleton
    @Provides
    fun provideFrameworkBluetoothManager(
        @ApplicationContext appContext: Context,
    ): BluetoothManager {
        return appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
}