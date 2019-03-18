package com.cwlarson.deviceid

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
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
