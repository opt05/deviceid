package com.cwlarson.deviceid.di

import com.cwlarson.deviceid.data.*
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.util.AppUpdateUtils
import com.cwlarson.deviceid.util.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class, DatabaseModule::class, ActivityModule::class]
)
object TestAppModule {
    @Provides
    @Singleton
    fun providesDispatcherProvider(): DispatcherProvider = DispatcherProvider

    @Provides
    @Singleton
    fun providesPreferences(): PreferenceManager = mockk()

    @Provides
    @Singleton
    fun providesAppUpdateUtils(): AppUpdateUtils = mockk()

    @Provides
    @Singleton
    fun providesAllRepository(): AllRepository = mockk()

    @Provides
    @Singleton
    fun providesDeviceRepository(): DeviceRepository = mockk()

    @Provides
    @Singleton
    fun providesHardwareRepository(): HardwareRepository = mockk()

    @Provides
    @Singleton
    fun providesNetworkRepository(): NetworkRepository = mockk()

    @Provides
    @Singleton
    fun providesSoftwareRepository(): SoftwareRepository = mockk()
}