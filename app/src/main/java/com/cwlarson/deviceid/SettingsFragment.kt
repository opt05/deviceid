package com.cwlarson.deviceid

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*

class SettingsFragment: PreferenceFragmentCompat() {
    companion object {
        @Suppress("FieldCanBeLocal","unused")
        private const val TAG = "SettingsFragment"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
        findPreference<ListPreference?>(getString(R.string.pref_daynight_mode_key))
                ?.bindPreferenceSummaryToValue()
        findPreference<SwitchPreferenceCompat?>(getString(R.string.pref_hide_unavailable_key))
                ?.bindPreferenceSummaryToValue()
        findPreference<SeekBarPreference?>(getString(R.string.pref_auto_refresh_rate_key))
                ?.bindPreferenceSummaryToValue()
        findPreference<SwitchPreferenceCompat?>(getString(R.string.pref_search_history_key))
                ?.bindPreferenceSummaryToValue()
    }
    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     */
    private fun Preference.bindPreferenceSummaryToValue() {
        setOnPreferenceChangeListener { preference, newValue ->
            Log.d(TAG, "${preference.key}: $newValue")
            when(preference) {
                is ListPreference -> {
                    if(key == getString(R.string.pref_daynight_mode_key) && newValue != preference.value) {
                        AppCompatDelegate.setDefaultNightMode(
                                when(newValue) {
                                    getString(R.string.pref_night_mode_off) -> AppCompatDelegate.MODE_NIGHT_NO
                                    getString(R.string.pref_night_mode_on) -> AppCompatDelegate.MODE_NIGHT_YES
                                    getString(R.string.pref_night_mode_auto) -> AppCompatDelegate.MODE_NIGHT_AUTO
                                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                                })
                        activity?.recreate()
                    }
                }
                is SwitchPreference -> preference.isChecked = newValue as? Boolean ?: false
                is SwitchPreferenceCompat -> preference.isChecked = newValue as? Boolean ?: false
                is CheckBoxPreference -> preference.isChecked = newValue as? Boolean ?: false
                is SeekBarPreference -> preference.value = newValue as? Int ?: 0
                else -> {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    preference.summary = if(newValue?.toString().isNullOrBlank())
                        context.getString(R.string.empty_preference_default) else newValue?.toString()
                }
            }
            true
        }
        // Trigger the listener immediately with the preference's current value.
        val preferences = preferenceManager.sharedPreferences
        onPreferenceChangeListener.onPreferenceChange(this, when(this) {
            is SwitchPreference -> preferences.getBoolean(key, false)
            is SwitchPreferenceCompat -> preferences.getBoolean(key, false)
            is CheckBoxPreference -> preferences.getBoolean(key, false)
            is SeekBarPreference -> preferences.getInt(key, 0)
            else -> preferences.getString(key, "")
        })
    }
}