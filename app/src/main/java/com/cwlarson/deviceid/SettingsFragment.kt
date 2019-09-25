package com.cwlarson.deviceid

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.cwlarson.deviceid.database.AppUpdateViewModel
import com.cwlarson.deviceid.database.MainActivityViewModel
import com.cwlarson.deviceid.databinding.applySystemWindows
import com.cwlarson.deviceid.util.FakeAppUpdateManagerWrapper
import com.cwlarson.deviceid.util.UpdateState
import com.google.android.play.core.install.model.InstallStatus

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
    private val appUpdateViewModel by activityViewModels<AppUpdateViewModel>()
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = super.onCreateView(inflater, container, savedInstanceState)
        listView.clipToPadding = false
        listView.clipChildren = false
        listView.scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
        if(mainActivityViewModel.twoPane) listView?.updatePadding(left = resources.getDimensionPixelSize(R.dimen
                    .activity_horizontal_margin), right = resources.
                    getDimensionPixelSize(R.dimen.activity_horizontal_margin))
        listView.applySystemWindows(applyBottom = true)
        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        findPreference<ListPreference>(getString(R.string.pref_daynight_mode_key))?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    preferenceManager.sharedPreferences.setDarkTheme(context, newValue)
                    true
                }
        findPreference<Preference>(getString(R.string.pref_check_for_update_key))?.let { pref ->
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                appUpdateViewModel.updateStatus.observe(viewLifecycleOwner, Observer { ua ->
                    pref.title = when (ua) {
                        UpdateState.Yes,
                        UpdateState.YesButNotAllowed ->
                            getString(R.string.pref_check_for_update_title_yes)
                        else -> // is UpdateState.No
                            getString(R.string.pref_check_for_update_title_no)
                    }
                })
                appUpdateViewModel.installState.observe(viewLifecycleOwner, Observer { state ->
                    pref.summary = when (state?.installStatus()) {
                        InstallStatus.CANCELED ->
                            getString(R.string.pref_check_for_update_summary_canceled)
                        InstallStatus.DOWNLOADING ->
                            getString(R.string.pref_check_for_update_summary_downloading)
                        InstallStatus.INSTALLED ->
                            getString(R.string.pref_check_for_update_summary_installed)
                        InstallStatus.INSTALLING ->
                            getString(R.string.pref_check_for_update_summary_installing)
                        InstallStatus.PENDING ->
                            getString(R.string.pref_check_for_update_summary_pending)
                        InstallStatus.REQUIRES_UI_INTENT ->
                            getString(R.string.pref_check_for_update_summary_ui_intent)
                        InstallStatus.UNKNOWN ->
                            getString(R.string.pref_check_for_update_summary_unknown)
                        InstallStatus.DOWNLOADED ->
                            getString(R.string.pref_check_for_update_summary_downloaded)
                        InstallStatus.FAILED ->
                            getString(R.string.pref_check_for_update_summary_failed)
                        else -> getString(R.string.pref_check_for_update_summary)
                    }
                })

                pref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    appUpdateViewModel.sendCheckForFlexibleUpdate()
                    true
                }
            } else pref.isVisible = false // App Updates are not available on KitKat and below
        }
        findPreference<ListPreference>(getString(R.string.pref_fake_update_set_end_state_key))?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {  _, newValue ->
            appUpdateViewModel.fakeAppUpdateManager.setEndState(FakeAppUpdateManagerWrapper.Companion.Type.valueOf(newValue.toString()))
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_general, rootKey)
        if(BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            addPreferencesFromResource(R.xml.pref_testing_app_update)
    }
}