package com.cwlarson.deviceid

import android.app.Application
import androidx.preference.PreferenceManager
import com.cwlarson.deviceid.util.installStetho
import timber.log.Timber

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        installStetho()
        Timber.plant(Timber.DebugTree())
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false)
        PreferenceManager.getDefaultSharedPreferences(this).setDarkTheme(this)
    }
}
