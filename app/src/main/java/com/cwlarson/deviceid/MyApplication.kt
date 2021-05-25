package com.cwlarson.deviceid

import android.app.Application
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.util.HyperlinkedDebugTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {
    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG) Timber.plant(HyperlinkedDebugTree())
        with(preferenceManager) {
            setDefaultValues()
            setDarkTheme(this@MyApplication)
        }
    }
}
