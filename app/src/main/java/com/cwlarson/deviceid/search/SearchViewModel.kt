package com.cwlarson.deviceid.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cwlarson.deviceid.data.AllRepository
import com.cwlarson.deviceid.settings.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    @ApplicationContext context: Context,
    preferenceManager: PreferenceManager
) : ViewModel() {
    private var hideUnavailable = preferenceManager.hideUnavailable
    private val tabData = AllRepository(context, hideUnavailable)
    @ExperimentalCoroutinesApi
    val status = tabData.status
    @ExperimentalCoroutinesApi
    val allItems = tabData.subscribe(context)

    @ExperimentalCoroutinesApi
    private val job = viewModelScope.launch {
        preferenceManager.observeHideUnavailable().distinctUntilChanged().collectLatest {
            hideUnavailable = it
            tabData.filterUnavailable = it
        }
    }

    internal fun refresh() {
        tabData.refresh(true)
    }

    fun setSearchText(searchString: String?) {
        tabData.searchText = searchString
    }

    @ExperimentalCoroutinesApi
    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}