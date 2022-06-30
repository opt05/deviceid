package com.cwlarson.deviceid.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cwlarson.deviceid.util.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val preferenceManager: PreferenceManager
) : ViewModel() {
    val userPreferencesFlow = preferenceManager.userPreferencesFlow

    fun setHideUnavailable(value: Boolean) {
        viewModelScope.launch(dispatcherProvider.Main) { preferenceManager.hideUnavailable(value) }
    }

    fun setAutoRefreshRate(value: Int) {
        viewModelScope.launch(dispatcherProvider.Main) { preferenceManager.autoRefreshRate(value) }
    }

    fun setDarkMode(value: String) {
        viewModelScope.launch(dispatcherProvider.Main) { preferenceManager.setDarkTheme(value) }
    }

    fun setSearchHistory(value: Boolean) {
        viewModelScope.launch(dispatcherProvider.Main) { preferenceManager.searchHistory(value) }
    }
}