package com.cwlarson.deviceid.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.util.DispatcherProvider
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun providesDispatcherProvider(): DispatcherProvider = DispatcherProvider

    @Provides
    @Singleton
    fun providesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("user_preferences") }

    @Provides
    @Singleton
    fun providesPreferences(
        dispatcherProvider: DispatcherProvider, @ApplicationContext context: Context,
        dataStore: DataStore<Preferences>
    ): PreferenceManager =
        PreferenceManager(dispatcherProvider, context, dataStore)

    @Provides
    @Singleton
    fun providesAppManger(@ApplicationContext context: Context): AppUpdateManager =
        AppUpdateManagerFactory.create(context)
}