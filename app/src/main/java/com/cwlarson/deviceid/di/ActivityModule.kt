package com.cwlarson.deviceid.di

import android.content.Context
import com.cwlarson.deviceid.util.AppUpdateUtils
import com.cwlarson.deviceid.util.DispatcherProvider
import com.google.android.play.core.appupdate.AppUpdateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {

    @Provides
    @ActivityScoped
    fun providesAppUpdateUtils(
        dispatcherProvider: DispatcherProvider,
        manager: AppUpdateManager,
        @ActivityContext activity: Context
    ): AppUpdateUtils = AppUpdateUtils(dispatcherProvider, manager, activity)
}