package com.cwlarson.deviceid.di

import com.cwlarson.deviceid.data.*
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.util.AppUpdateUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.kotlin.mock
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class, DatabaseModule::class, ActivityModule::class]
)
object TestAppModule {
    @Provides
    @Singleton
    fun providesPreferences(): PreferenceManager = mock()

    @Provides
    @Singleton
    fun providesAppUpdateUtils(): AppUpdateUtils = mock()

    @Provides
    @Singleton
    fun providesAllRepository(): AllRepository = mock()

    @Provides
    @Singleton
    fun providesDeviceRepository(): DeviceRepository = mock()

    @Provides
    @Singleton
    fun providesHardwareRepository(): HardwareRepository = mock()

    @Provides
    @Singleton
    fun providesNetworkRepository(): NetworkRepository = mock()

    @Provides
    @Singleton
    fun providesSoftwareRepository(): SoftwareRepository = mock()
}