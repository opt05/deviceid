package com.cwlarson.deviceid.data

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.cwlarson.deviceid.tabs.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

@Keep
enum class Status { LOADING, ERROR, SUCCESS }
internal typealias CallbackData = () -> Unit
internal typealias CallbackStatus = (Status) -> Unit

abstract class TabData(filterUnavailable: Boolean = false) {
    private var callbackData: CallbackData? = null
    private var callbackStatus: CallbackStatus? = null
    var filterUnavailable: Boolean = filterUnavailable
        set(value) {
            if (value != field) {
                field = value
                refresh(true)
            }
        }
    var searchText: String? = null
        set(value) {
            if (value != field) {
                field = value
                refresh(true)
            }
        }

    @ExperimentalCoroutinesApi
    val status: Flow<Status> = channelFlow {
        callbackStatus = { if (!isClosedForSend) offer(it) }
        callbackStatus?.invoke(Status.SUCCESS)
        awaitClose { callbackStatus = null }
    }.flowOn(Dispatchers.IO)

    fun refresh(noStatus: Boolean = false) {
        if (!noStatus) callbackStatus?.invoke(Status.LOADING)
        callbackData?.invoke()
    }

    abstract suspend fun list(): List<Item>

    @ExperimentalCoroutinesApi
    fun subscribe(context: Context): Flow<List<Item>> = channelFlow {
        callbackData = {
            launch {
                try {
                    if (!isClosedForSend) offer(list()
                            .sortedBy { item -> item.getFormattedString(context) }.filter { item ->
                                if (filterUnavailable) !item.subtitle?.getSubTitleText().isNullOrBlank()
                                else true
                            }.filter { item ->
                                searchText?.let { text ->
                                    if (text.isNotBlank()) {
                                        item.getFormattedString(context).contains(text, true) ||
                                                item.subtitle?.getSubTitleText()?.contains(text, true) == true
                                    } else false
                                } ?: true
                            })
                    callbackStatus?.invoke(Status.SUCCESS)
                } catch (e: Throwable) {
                    callbackStatus?.invoke(Status.ERROR)
                }
            }
        }
        callbackData?.invoke()
        awaitClose { callbackData = null }
    }.flowOn(Dispatchers.IO)

    @ExperimentalCoroutinesApi
    fun subscribe(@StringRes title: Int?, titleFormatArgs: Array<String>?): Flow<Item?> =
            channelFlow {
                callbackData = {
                    launch {
                        try {
                            if (!isClosedForSend) offer(list()
                                    .firstOrNull { item ->
                                        item.title == title &&
                                                titleFormatArgs?.run { item.titleFormatArgs?.contentEquals(this) } ?: true
                                    })
                            callbackStatus?.invoke(Status.SUCCESS)
                        } catch (e: Throwable) {
                            callbackStatus?.invoke(Status.ERROR)
                        }
                    }
                }
                callbackData?.invoke()
                awaitClose { callbackData = null }
            }.flowOn(Dispatchers.IO)
}