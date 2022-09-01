package com.cwlarson.deviceid.util

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Suppress("MemberVisibilityCanBePrivate")
object DispatcherProvider {
    var IO : CoroutineDispatcher = Dispatchers.IO
        private set
    var Default : CoroutineDispatcher = Dispatchers.Default
        private set
    var Main : CoroutineDispatcher = Dispatchers.Main
        private set
    var Unconfined : CoroutineDispatcher = Dispatchers.Unconfined
        private set

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun provideDispatcher(dispatcher: CoroutineDispatcher): DispatcherProvider {
        IO = dispatcher
        Default = dispatcher
        Main = dispatcher
        Unconfined = dispatcher
        return this
    }
}