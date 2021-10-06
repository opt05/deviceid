package com.cwlarson.deviceid

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cwlarson.deviceid.settings.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal data class TitleVisibility(val visible: Boolean, val noFade: Boolean)

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val preferenceManager: PreferenceManager) :
    ViewModel() {
    private val _titleVisibility = MutableStateFlow(TitleVisibility(visible = true, noFade = false))
    internal val titleVisibility = _titleVisibility.asStateFlow()
    private var titleFadeRunning = false

    internal val isSearchHistory = preferenceManager.searchHistory

    internal val darkTheme = preferenceManager.darkTheme

    fun startTitleFade(twoPane: Boolean, intent: Intent) {
        if (titleFadeRunning || !titleVisibility.value.visible)
            return
        else if (twoPane || intent.action == Intent.ACTION_SEARCH)
            _titleVisibility.value = TitleVisibility(visible = false, noFade = true)
        else {
            titleFadeRunning = true
            viewModelScope.launch(Dispatchers.IO) {
                delay(TimeUnit.SECONDS.toMillis(2))
                _titleVisibility.value = TitleVisibility(visible = false, noFade = false)
                titleFadeRunning = false
            }
        }
    }

    fun saveSearchHistory(query: String) {
        viewModelScope.launch { preferenceManager.saveSearchHistoryItem(query) }
    }

    fun getSearchHistoryItems(query: String) = preferenceManager.getSearchHistoryItems(query)
}