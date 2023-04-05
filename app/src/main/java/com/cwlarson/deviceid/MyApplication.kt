package com.cwlarson.deviceid

import android.app.Application
import com.cwlarson.deviceid.util.HyperlinkedDebugTree
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(HyperlinkedDebugTree())
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}