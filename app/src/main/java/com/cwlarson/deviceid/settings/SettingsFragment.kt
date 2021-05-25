package com.cwlarson.deviceid.settings

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.cwlarson.deviceid.BuildConfig
import com.cwlarson.deviceid.MainActivityViewModel
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.appupdates.UpdateType
import com.cwlarson.deviceid.util.*
import com.google.android.play.core.install.model.InstallStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var appUpdateUtils: AppUpdateUtils
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<ListPreference>(getString(R.string.pref_daynight_mode_key))?.setOnPreferenceChangeListener { _, newValue ->
            preferenceManager.setDarkTheme(newValue)
            true
        }
        findPreference<Preference>(getString(R.string.pref_check_for_update_key))?.let { pref ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                viewLifecycleOwner.addRepeatingJob(Lifecycle.State.STARTED) {
                    appUpdateUtils.updateState.collect { ua ->
                        pref.title = when (ua) {
                            is UpdateState.Yes,
                            UpdateState.YesButNotAllowed ->
                                getString(R.string.pref_check_for_update_title_yes)
                            is UpdateState.No ->
                                getString(R.string.pref_check_for_update_title_no)
                            else -> pref.title
                        }
                    }
                }
                viewLifecycleOwner.addRepeatingJob(Lifecycle.State.STARTED) {
                    appUpdateUtils.installState.collect { state ->
                        pref.summary = when(state) {
                            is InstallState.Failed -> getString(R.string.pref_check_for_update_summary_failed)
                            InstallState.Initial -> getString(R.string.pref_check_for_update_summary)
                            is InstallState.NoError -> {
                                when(state.status) {
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
                            }
                        }
                    }
                }

                pref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    appUpdateUtils.checkForFlexibleUpdate(true)
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
            if(!appUpdateUtils.updateFakeAppUpdateManagerState(UpdateType.valueOf(newValue.toString())))
                context.toast("AppUpdateManger is not FakeAppUpdateManagerWrapper")
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_general, rootKey)
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            addPreferencesFromResource(R.xml.pref_testing_app_update)
    }
}