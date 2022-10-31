package com.cwlarson.deviceid.tabs

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.data.sdkToVersion
import com.cwlarson.deviceid.ui.theme.AppTheme
import com.cwlarson.deviceid.ui.util.ListItem
import com.cwlarson.deviceid.ui.util.copyItemToClipboard
import com.cwlarson.deviceid.ui.util.loadPermissionLabel
import com.cwlarson.deviceid.util.AppPermission

@VisibleForTesting
const val TAB_TEST_TAG_LIST_ITEM = "tab_list_item"

@Preview(showBackground = true)
@Composable
fun ItemListItemTextPreview() = AppTheme {
    ItemListItem(item = Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("subtitle"))) { }
}

@Preview(showBackground = true)
@Composable
fun ItemListItemChartPreview() = AppTheme {
    ItemListItem(
        item = Item(
            R.string.app_name, ItemType.DEVICE,
            ItemSubtitle.Chart(
                ChartItem(50f, 100f, Icons.Default.Android, "subtitle")
            )
        )
    ) { }
}

@Preview(showBackground = true)
@Composable
fun ItemListItemNoLongerPreview() = AppTheme {
    ItemListItem(
        item = Item(
            R.string.app_name,
            ItemType.DEVICE,
            ItemSubtitle.NoLongerPossible(15)
        )
    ) { }
}

@Preview(showBackground = true)
@Composable
fun ItemListItemNotPossiblePreview() = AppTheme {
    ItemListItem(
        item = Item(
            R.string.app_name,
            ItemType.DEVICE,
            ItemSubtitle.NotPossibleYet(21)
        )
    ) { }
}

@Preview(showBackground = true)
@Composable
fun ItemListItemPermissionPreview() = AppTheme {
    ItemListItem(
        item = Item(
            R.string.app_name,
            ItemType.DEVICE,
            ItemSubtitle.Permission(AppPermission.ReadPhoneState)
        )
    ) { }
}

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