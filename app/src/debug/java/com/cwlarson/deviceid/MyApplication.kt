package com.cwlarson.deviceid

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
        // Normal app init code...
        Stetho.initializeWithDefaults(this)
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false)
        AppCompatDelegate.setDefaultNightMode(
                when(PreferenceManager.getDefaultSharedPreferences(this)
                        .getString(getString(R.string.pref_daynight_mode_key),"")) {
                    getString(R.string.pref_night_mode_off) -> AppCompatDelegate.MODE_NIGHT_NO
                    getString(R.string.pref_night_mode_on) -> AppCompatDelegate.MODE_NIGHT_YES
                    getString(R.string.pref_night_mode_auto) -> AppCompatDelegate.MODE_NIGHT_AUTO
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                })
    }
}
