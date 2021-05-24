package com.cwlarson.deviceid

import android.content.Intent
import android.view.View
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class TitleVisibility(val visible: Int, val noFade: Boolean)
data class SearchOpen(val yes: Boolean, val twoPane: Boolean)

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val KEY_TWO_PANE = "twoPane"
    }

    private val _hideSearchBar = MutableStateFlow(false)
    val hideSearchBar: StateFlow<Boolean> = _hideSearchBar
    private val _hideBottomBar = MutableStateFlow(false)
    val hideBottomBar: StateFlow<Boolean> = _hideBottomBar
    private val _isSearchOpen = MutableStateFlow(SearchOpen(false, twoPane))
    val isSearchOpen: StateFlow<SearchOpen> = _isSearchOpen
    private val _titleVisibility = MutableStateFlow(TitleVisibility(View.VISIBLE, false))
    val titleVisibility: StateFlow<TitleVisibility> = _titleVisibility
    private var titleFadeRunning = false
    var twoPane: Boolean
        get() = savedStateHandle.get<Boolean>(KEY_TWO_PANE) ?: false
        set(value) = savedStateHandle.set(KEY_TWO_PANE, value)

    fun updateHideSearchBar(hide: Boolean) {
        _hideSearchBar.value = hide
    }

    fun updateHideBottomBar(hide: Boolean) {
        _hideBottomBar.value = hide
    }

    fun updateIsSearchOpen(open: Boolean) {
        _isSearchOpen.value = SearchOpen(open, twoPane)
    }

    fun startTitleFade(intent: Intent) {
        if (titleFadeRunning || titleVisibility.value.visible == View.GONE)
            return
        else if (twoPane || intent.action == Intent.ACTION_SEARCH)
            _titleVisibility.value = TitleVisibility(View.GONE, true)
        else {
            titleFadeRunning = true
            viewModelScope.launch(Dispatchers.IO) {
                delay(TimeUnit.SECONDS.toMillis(2))
                _titleVisibility.value = TitleVisibility(View.GONE, false)
                titleFadeRunning = false
            }
        }
    }
}