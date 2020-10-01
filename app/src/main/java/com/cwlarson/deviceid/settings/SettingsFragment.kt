package com.cwlarson.deviceid.settings

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.cwlarson.deviceid.BuildConfig
import com.cwlarson.deviceid.MainActivityViewModel
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.appupdates.AppUpdateViewModel
import com.cwlarson.deviceid.appupdates.FakeAppUpdateManagerWrapper
import com.cwlarson.deviceid.appupdates.UpdateState
import com.cwlarson.deviceid.appupdates.UpdateType
import com.cwlarson.deviceid.util.applySystemWindows
import com.cwlarson.deviceid.util.toast
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.InstallStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var appUpdateManager: AppUpdateManager
    @ExperimentalCoroutinesApi
    private val appUpdateViewModel by activityViewModels<AppUpdateViewModel>()
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = super.onCreateView(inflater, container, savedInstanceState)
        listView.clipToPadding = false
        listView.clipChildren = false
        listView.scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
        if (mainActivityViewModel.twoPane) listView?.updatePadding(left = resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin), right = resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin))
        listView.applySystemWindows(applyBottom = true)
        return layout
    }

    @ExperimentalCoroutinesApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        findPreference<ListPreference>(getString(R.string.pref_daynight_mode_key))?.setOnPreferenceChangeListener { _, newValue ->
            preferenceManager.setDarkTheme(newValue)
            true
        }
        findPreference<Preference>(getString(R.string.pref_check_for_update_key))?.let { pref ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                appUpdateViewModel.updateState.onEach { ua ->
                    pref.title = when (ua) {
                        UpdateState.Yes,
                        UpdateState.YesButNotAllowed ->
                            getString(R.string.pref_check_for_update_title_yes)
                        else -> // is UpdateState.No
                            getString(R.string.pref_check_for_update_title_no)
                    }
                }.launchIn(viewLifecycleOwner.lifecycleScope)
                appUpdateViewModel.installState.onEach { state ->
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
                        InstallStatus.UNKNOWN ->
                            getString(R.string.pref_check_for_update_summary_unknown)
                        InstallStatus.DOWNLOADED ->
                            getString(R.string.pref_check_for_update_summary_downloaded)
                        InstallStatus.FAILED ->
                            getString(R.string.pref_check_for_update_summary_failed)
                        else -> getString(R.string.pref_check_for_update_summary)
                    }
                }.launchIn(viewLifecycleOwner.lifecycleScope)

                pref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    appUpdateViewModel.sendCheckForFlexibleUpdate(true)
                    true
                }
            } else pref.isVisible = false // App Updates are not available on KitKat and below
        }
        // Fix to commit preference so app restart doesn't break saving the value
        findPreference<SwitchPreferenceCompat>(getString(R.string.pref_use_fake_update_manager_key))?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is Boolean) {
                preferenceManager.useFakeUpdateManager = newValue
                false
            } else true
        }
        findPreference<ListPreference>(getString(R.string.pref_fake_update_set_end_state_key))?.setOnPreferenceChangeListener { _, newValue ->
            appUpdateManager.let {
                if (it is FakeAppUpdateManagerWrapper)
                    it.setEndState(UpdateType.valueOf(newValue.toString()))
                else
                    context.toast("AppUpdateManger is not FakeAppUpdateManagerWrapper")
            }
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_general, rootKey)
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            addPreferencesFromResource(R.xml.pref_testing_app_update)
    }
}