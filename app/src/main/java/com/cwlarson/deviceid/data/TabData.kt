package com.cwlarson.deviceid.data

import android.content.Context
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.tabs.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

sealed class TabDataStatus {
    object Loading : TabDataStatus()
    data class Success(val list: List<Item>) : TabDataStatus()
    object Error : TabDataStatus()
}

sealed class TabDetailStatus {
    object Loading : TabDetailStatus()
    data class Success(val item: Item) : TabDetailStatus()
    object Error : TabDetailStatus()
}

abstract class TabData(
    private val context: Context,
    private val preferenceManager: PreferenceManager
) {
    internal abstract fun items(): Flow<List<Item>>

    @OptIn(ExperimentalCoroutinesApi::class)
    open fun list(): Flow<TabDataStatus> = channelFlow {
        trySend(TabDataStatus.Loading)
        preferenceManager.getFilters().collectLatest { filters ->
            if(!filters.swipeRefreshDisabled) trySend(TabDataStatus.Loading)
            items().collectLatest { items ->
                try {
                    val result = items.filter { item ->
                        if (filters.hideUnavailable) !item.subtitle.getSubTitleText()
                            .isNullOrBlank()
                        else true
                    }.sortedBy { item -> item.getFormattedString(context) }
                    trySend(TabDataStatus.Success(result))
                } catch (e: Throwable) {
                    trySend(TabDataStatus.Error)
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    open fun details(item: Item): Flow<TabDetailStatus> = channelFlow {
        trySend(TabDetailStatus.Loading)
        items().collectLatest { items ->
            try {
                val result = items.first { i ->
                    i.title == item.title &&
                            item.titleFormatArgs?.run {
                                i.titleFormatArgs?.containsAll(this)
                            } ?: true
                }
                trySend(TabDetailStatus.Success(result))
            } catch (e: Throwable) {
                trySend(TabDetailStatus.Error)
            }
        }
    }.flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    open fun search(searchText: StateFlow<String>): Flow<TabDataStatus> = channelFlow {
        trySend(TabDataStatus.Loading)
        preferenceManager.getFilters().collectLatest { filters ->
            items().collectLatest { items ->
                searchText.collectLatest { text ->
                    try {
                        val result = items.filter { item ->
                            if (filters.hideUnavailable) !item.subtitle.getSubTitleText()
                                .isNullOrBlank()
                            else true
                        }.filter { item ->
                            if (text.isNotBlank()) {
                                item.getFormattedString(context).contains(text, true) ||
                                        item.subtitle.getSubTitleText()
                                            ?.contains(text, true) == true
                            } else false
                        }.sortedBy { item -> item.getFormattedString(context) }
                        trySend(TabDataStatus.Success(result))
                    } catch (e: Throwable) {
                        trySend(TabDataStatus.Error)
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}