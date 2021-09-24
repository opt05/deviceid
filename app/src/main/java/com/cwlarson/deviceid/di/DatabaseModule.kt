package com.cwlarson.deviceid.di

import android.content.Context
import com.cwlarson.deviceid.data.*
import com.cwlarson.deviceid.settings.PreferenceManager
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
        @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ): DeviceRepository = DeviceRepository(context, preferenceManager)

    @Provides
    fun providesNetworkRepository(
        @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ): NetworkRepository = NetworkRepository(context, preferenceManager)

    @Provides
    fun providesSoftwareRepository(
        @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ): SoftwareRepository = SoftwareRepository(context, preferenceManager)

    @Provides
    fun providesHardwareRepository(
        @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ): HardwareRepository = HardwareRepository(context, preferenceManager)

    @Provides
    fun providesAllRepository(
        @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ): AllRepository = AllRepository(context, preferenceManager)
}