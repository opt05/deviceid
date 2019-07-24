package com.cwlarson.deviceid

import android.app.Application
import android.os.Build
import androidx.preference.PreferenceManager
import com.cwlarson.deviceid.util.installStetho
import timber.log.Timber

@Suppress("unused")
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        installStetho()
        Timber.plant(Timber.DebugTree())
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false)
        if(BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            PreferenceManager.setDefaultValues(this, R.xml.pref_testing_app_update, true)
        PreferenceManager.getDefaultSharedPreferences(this).setDarkTheme(this)
    }
}
