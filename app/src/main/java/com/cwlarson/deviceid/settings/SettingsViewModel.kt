package com.cwlarson.deviceid.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {
    val userPreferencesFlow = preferenceManager.userPreferencesFlow

    fun setHideUnavailable(value: Boolean) {
        viewModelScope.launch { preferenceManager.hideUnavailable(value) }
    }

    fun setAutoRefreshRate(value: Int) {
        viewModelScope.launch { preferenceManager.autoRefreshRate(value) }
    }

    fun setDarkMode(value: String) {
        viewModelScope.launch { preferenceManager.setDarkTheme(value) }
    }

    fun setSearchHistory(value: Boolean) {
        viewModelScope.launch { preferenceManager.searchHistory(value) }
    }
}