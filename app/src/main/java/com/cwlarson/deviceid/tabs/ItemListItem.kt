package com.cwlarson.deviceid.tabs

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.data.sdkToVersion
import com.cwlarson.deviceid.ui.util.ListItem
import com.cwlarson.deviceid.ui.util.copyItemToClipboard
import com.cwlarson.deviceid.ui.util.loadPermissionLabel

@VisibleForTesting
const val TAB_TEST_TAG_LIST_ITEM = "tab_list_item"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemListItem(item: Item, onItemClick: () -> Unit) {
    ListItem(
        modifier = Modifier
            .testTag(TAB_TEST_TAG_LIST_ITEM)
            .combinedClickable(
                onClick = { onItemClick() },
                onLongClick = item.copyItemToClipboard()
            ),
        icon = item.subtitle.getIcon(),
        text = item.getFormattedString(),
        chartPercentage = item.subtitle.getChartPercentage(),
        secondaryText = when (val sub = item.subtitle) {
            is ItemSubtitle.Text -> {
                if (sub.data.isNullOrBlank()) stringResource(R.string.not_found)
                else sub.data
            }
            is ItemSubtitle.Chart -> {
                if (sub.chart.chartSubtitle.isNullOrBlank()) stringResource(R.string.not_found)
                else sub.chart.chartSubtitle
            }
            is ItemSubtitle.NoLongerPossible -> {
                stringResource(R.string.no_longer_possible, sub.version.sdkToVersion())
            }
            is ItemSubtitle.NotPossibleYet -> {
                stringResource(R.string.not_possible_yet, sub.version.sdkToVersion())
            }
            is ItemSubtitle.Permission -> {
                stringResource(
                    R.string.permission_item_subtitle,
                    sub.permission.loadPermissionLabel()
                )
            }
            else -> stringResource(R.string.not_found)
        }
    )
}