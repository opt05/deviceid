@file:JvmName("SystemUtils") // pretty name for utils class if called from
package com.cwlarson.deviceid.util

import android.content.Context
import android.view.ViewGroup

fun calculateNoOfColumns(context: Context): Int {
    val displayMetrics = context.resources.displayMetrics
    val dpWidth = displayMetrics.widthPixels / displayMetrics.density
    return (dpWidth / 300).toInt()
}

fun calculateBottomSheetMaxWidth(context: Context): Int {
    val displayMetrics = context.resources.displayMetrics
    val dpWidth = displayMetrics.widthPixels / displayMetrics.density
    return if(dpWidth>750) 750 else ViewGroup.LayoutParams.MATCH_PARENT
}