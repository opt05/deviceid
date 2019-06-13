package com.cwlarson.deviceid

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat

fun SharedPreferences.setDarkTheme(context: Context?, newValue: Any? = null) {
    AppCompatDelegate.setDefaultNightMode(
            when(newValue ?: getString(context?.getString(R.string.pref_daynight_mode_key),
                    context?.getString(R.string.pref_night_mode_system))) {
                context?.getString(R.string.pref_night_mode_off) -> AppCompatDelegate.MODE_NIGHT_NO
                context?.getString(R.string.pref_night_mode_on) -> AppCompatDelegate.MODE_NIGHT_YES
                else -> if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            })
}

class SettingsFragment: PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findPreference<ListPreference>(getString(R.string.pref_daynight_mode_key))?.setOnPreferenceChangeListener { _, newValue ->
            preferenceManager.sharedPreferences.setDarkTheme(context, newValue)
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_general, rootKey)
    }
}