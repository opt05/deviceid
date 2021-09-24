package com.cwlarson.deviceid.tabs

import android.content.Context
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.cwlarson.deviceid.util.AppPermission
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Keep
enum class ItemType {
    DEVICE, NETWORK, SOFTWARE, HARDWARE
}

@Parcelize
data class Item(
    @StringRes val title: Int = 0,
    val itemType: ItemType,
    val subtitle: ItemSubtitle,
    val titleFormatArgs: List<String>? = null
) : Parcelable {
    @Composable
    fun getFormattedString(): String =
        titleFormatArgs?.let { stringResource(id = title, *it.toTypedArray()) }
            ?: stringResource(id = title)

    fun getFormattedString(context: Context): String =
        titleFormatArgs?.let { context.getString(title, *it.toTypedArray()) }
            ?: context.getString(title)
}

sealed class ItemSubtitle : Parcelable {
    @Parcelize
    data class Text(val data: String?) : ItemSubtitle()

    @Parcelize
    data class Chart(val chart: ChartItem) : ItemSubtitle()

    @Parcelize
    data class NoLongerPossible(val version: Int) : ItemSubtitle()

    @Parcelize
    data class NotPossibleYet(val version: Int) : ItemSubtitle()

    @Parcelize
    data class Permission(val permission: AppPermission) : ItemSubtitle()

    @Parcelize
    object Error : ItemSubtitle()

    fun getSubTitleText(): String? =
        when (this) {
            is Text -> this.data
            is Chart -> this.chart.chartSubtitle
            else -> null
        }

    fun getIcon(): ImageVector? =
        when (this) {
            is Chart -> this.chart.chartDrawable
            else -> null
        }

    fun getChartPercentage(): Float? =
        when (this) {
            is Chart -> with(this.chart) {
                if (chartSubtitle.isNullOrBlank()) null
                else if (chartAxis1 == 0f && chartAxis2 == 0f) 0f else 1f -
                        ((if (chartAxis1 > chartAxis2)
                            chartAxis2 / chartAxis1 else chartAxis1 / chartAxis2))
            }
            else -> null
        }

}

@Parcelize
data class ChartItem(
    val chartAxis1: Float,
    val chartAxis2: Float,
    val chartDrawable: @RawValue ImageVector,
    val chartSubtitle: String?
) : Parcelable
