package com.cwlarson.deviceid.testutils

import androidx.annotation.StringRes
import app.cash.turbine.ReceiveTurbine
import com.cwlarson.deviceid.tabs.Item

suspend inline fun ReceiveTurbine<List<Item>>.awaitItemFromList(
    @StringRes title: Int,
    titleFormatArgs: List<String>? = null
): Item? = awaitItem().firstOrNull {
    it.title == title && titleFormatArgs?.let { a -> it.titleFormatArgs == a } ?: true
}

fun List<Item>.itemFromList(
    @StringRes title: Int,
    titleFormatArgs: List<String>? = null
): Item? = firstOrNull {
    it.title == title && titleFormatArgs?.let { a -> it.titleFormatArgs == a } ?: true
}

fun ReceiveTurbine<List<Item>>.expectMostRecentItemFromList(
    @StringRes title: Int,
    titleFormatArgs: List<String>? = null
): Item? = expectMostRecentItem().firstOrNull {
    it.title == title && titleFormatArgs?.let { a -> it.titleFormatArgs == a } ?: true
}