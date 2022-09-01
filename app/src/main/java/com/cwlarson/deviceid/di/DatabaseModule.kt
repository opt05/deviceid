package com.cwlarson.deviceid.di

import android.content.Context
import com.cwlarson.deviceid.data.*
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.util.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun providesDeviceRepository(
        dispatcherProvider: DispatcherProvider, @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ): DeviceRepository = DeviceRepository(dispatcherProvider, context, preferenceManager)

    @Provides
    fun providesNetworkRepository(
        dispatcherProvider: DispatcherProvider, @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ): NetworkRepository = NetworkRepository(dispatcherProvider, context, preferenceManager)

    @Provides
    fun providesSoftwareRepository(
        dispatcherProvider: DispatcherProvider, @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ): SoftwareRepository = SoftwareRepository(dispatcherProvider, context, preferenceManager)

    @Provides
    fun providesHardwareRepository(
        dispatcherProvider: DispatcherProvider, @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ): HardwareRepository = HardwareRepository(dispatcherProvider, context, preferenceManager)

    @Provides
    fun providesAllRepository(
        dispatcherProvider: DispatcherProvider, @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ): AllRepository = AllRepository(dispatcherProvider, context, preferenceManager)
}