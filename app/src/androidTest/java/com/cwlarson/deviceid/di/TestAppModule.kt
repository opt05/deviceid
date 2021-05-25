package com.cwlarson.deviceid.di

import android.content.Context
import com.cwlarson.deviceid.appupdates.FakeAppUpdateManagerWrapper
import com.cwlarson.deviceid.settings.PreferenceManager
import com.google.android.play.core.appupdate.AppUpdateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.mockito.Mockito
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {
    @Singleton
    @Provides
    fun providesPreferences(): PreferenceManager = Mockito.mock(PreferenceManager::class.java)

    @Singleton
    @Provides
    fun providesAppManger(@ApplicationContext context: Context,
                          preferenceManager: PreferenceManager): AppUpdateManager =
        FakeAppUpdateManagerWrapper(context)
}