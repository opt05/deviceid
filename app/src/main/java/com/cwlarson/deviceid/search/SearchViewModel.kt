package com.cwlarson.deviceid.search

import androidx.lifecycle.ViewModel
import com.cwlarson.deviceid.data.AllRepository
import com.cwlarson.deviceid.settings.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    repository: AllRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {
    private val searchText = MutableStateFlow("")

    @ExperimentalCoroutinesApi
    val allItems = repository.search(searchText)

    fun setSearchText(searchString: String) {
        searchText.value = searchString
    }

    fun forceRefresh() = preferenceManager.forceRefresh()
}