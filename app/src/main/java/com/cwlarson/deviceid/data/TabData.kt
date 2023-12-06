package com.cwlarson.deviceid.data

import android.content.Context
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.util.DispatcherProvider
import kotlinx.coroutines.flow.*

sealed class TabDataStatus {
    data object Loading : TabDataStatus()
    data class Success(val list: List<Item>) : TabDataStatus()
    data object Error : TabDataStatus()
}

sealed class TabDetailStatus {
    data object Loading : TabDetailStatus()
    data class Success(val item: Item) : TabDetailStatus()
    data object Error : TabDetailStatus()
}

abstract class TabData(
    private val dispatcherProvider: DispatcherProvider,
    private val context: Context,
    private val preferenceManager: PreferenceManager
) {
    abstract fun items(): Flow<List<Item>>

    fun list(): Flow<TabDataStatus> = channelFlow {
        trySend(TabDataStatus.Loading)
        preferenceManager.getFilters().collectLatest { filters ->
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
    }.flowOn(dispatcherProvider.IO)

    fun details(item: Item): Flow<TabDetailStatus> = channelFlow {
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
    }.flowOn(dispatcherProvider.IO)

    fun search(searchText: StateFlow<String>): Flow<TabDataStatus> = channelFlow {
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
    }.flowOn(dispatcherProvider.IO)
}