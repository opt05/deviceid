package com.cwlarson.deviceid

import android.content.Intent
import android.view.View
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class TitleVisibility(val visible: Int, val noFade: Boolean)
data class SearchOpen(val yes: Boolean, val twoPane: Boolean)

class MainActivityViewModel @ViewModelInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val KEY_TWO_PANE = "twoPane"
    }

    @ExperimentalCoroutinesApi
    private val _hideSearchBar = MutableStateFlow(false)

    @ExperimentalCoroutinesApi
    val hideSearchBar: StateFlow<Boolean> = _hideSearchBar

    @ExperimentalCoroutinesApi
    private val _hideBottomBar = MutableStateFlow(false)

    @ExperimentalCoroutinesApi
    val hideBottomBar: StateFlow<Boolean> = _hideBottomBar

    @ExperimentalCoroutinesApi
    private val _isSearchOpen = MutableStateFlow(SearchOpen(false, twoPane))

    @ExperimentalCoroutinesApi
    val isSearchOpen: StateFlow<SearchOpen> = _isSearchOpen

    @ExperimentalCoroutinesApi
    private val _titleVisibility = MutableStateFlow(TitleVisibility(View.VISIBLE, false))

    @ExperimentalCoroutinesApi
    val titleVisibility: StateFlow<TitleVisibility> = _titleVisibility
    private var titleFadeRunning = false
    var twoPane: Boolean
        get() = savedStateHandle.get<Boolean>(KEY_TWO_PANE) ?: false
        set(value) = savedStateHandle.set(KEY_TWO_PANE, value)

    @ExperimentalCoroutinesApi
    fun updateHideSearchBar(hide: Boolean) {
        _hideSearchBar.value = hide
    }

    @ExperimentalCoroutinesApi
    fun updateHideBottomBar(hide: Boolean) {
        _hideBottomBar.value = hide
    }

    @ExperimentalCoroutinesApi
    fun updateIsSearchOpen(open: Boolean) {
        _isSearchOpen.value = SearchOpen(open, twoPane)
    }

    @ExperimentalCoroutinesApi
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