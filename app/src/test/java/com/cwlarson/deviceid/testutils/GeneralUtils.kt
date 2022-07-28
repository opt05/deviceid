package com.cwlarson.deviceid.testutils

import androidx.annotation.StringRes
import app.cash.turbine.FlowTurbine
import com.cwlarson.deviceid.tabs.Item

suspend inline fun FlowTurbine<List<Item>>.awaitItemFromList(
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

fun FlowTurbine<List<Item>>.expectMostRecentItemFromList(
    @StringRes title: Int,
    titleFormatArgs: List<String>? = null
): Item? = expectMostRecentItem().firstOrNull {
    it.title == title && titleFormatArgs?.let { a -> it.titleFormatArgs == a } ?: true
}