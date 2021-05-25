package com.cwlarson.deviceid.tabs

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.cwlarson.deviceid.util.AppPermission

@Keep
enum class ItemType(val value: Int) {
    DEVICE(1), NETWORK(2), SOFTWARE(3), HARDWARE(4)
}

data class Item(
        @StringRes var title: Int = 0,
        var itemType: ItemType,
        var subtitle: ItemSubtitle? = null
) {
    var titleFormatArgs: Array<String>? = null

    fun getFormattedString(context: Context): String =
            titleFormatArgs?.let { context.getString(title, *it) } ?: context.getString(title)
}

sealed class ItemSubtitle {
    data class Text(val data: String?) : ItemSubtitle()
    data class Chart(val chart: ChartItem) : ItemSubtitle()
    data class NoLongerPossible(val version: Int) : ItemSubtitle()
    data class NotPossibleYet(val version: Int) : ItemSubtitle()
    data class Permission(val permission: AppPermission) : ItemSubtitle()

    fun getSubTitleText(): String? =
            when (this) {
                is Text -> this.data
                is Chart -> this.chart.chartSubtitle
                else -> null
            }
}

data class ChartItem(val chartAxis1: Float,
                     val chartAxis2: Float,
                     @DrawableRes
                     val chartDrawable: Int,
                     val chartSubtitle: String?) {
    val chartPercentage: Int = if (chartAxis1 == 0f && chartAxis2 == 0f) 0 else 100 -
            ((if (chartAxis1 > chartAxis2) chartAxis2 / chartAxis1
            else chartAxis1 / chartAxis2) * 100).toInt()
}
