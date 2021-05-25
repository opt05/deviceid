package com.cwlarson.deviceid.di

import android.content.Context
import android.content.SharedPreferences
import com.cwlarson.deviceid.appupdates.FakeAppUpdateManagerWrapper
import com.cwlarson.deviceid.settings.PreferenceManager
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesPreferences(@ApplicationContext context: Context): SharedPreferences =
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    fun providesAppManger(@ApplicationContext context: Context,
                          preferenceManager: PreferenceManager): AppUpdateManager {
        Timber.d("${preferenceManager.useFakeUpdateManager}")
        return if (preferenceManager.useFakeUpdateManager) FakeAppUpdateManagerWrapper(context)
        else AppUpdateManagerFactory.create(context)
    }
}