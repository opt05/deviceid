package com.cwlarson.deviceid.tabsdetail

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.data.TabDetailStatus
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.ui.util.copyItemToClipboard
import com.cwlarson.deviceid.ui.util.share
import com.cwlarson.deviceid.util.DispatcherProvider
import com.cwlarson.deviceid.util.collectAsStateWithLifecycle

@VisibleForTesting
const val TAB_DETAIL_TEST_TAG_PROGRESS = "tab_detail_progress"

@VisibleForTesting
const val TAB_DETAIL_TEST_TAG_RESULTS = "tab_detail_results"

@Composable
fun TabDetailScreen(
    item: Item?, dispatcherProvider: DispatcherProvider,
    viewModel: TabsDetailViewModel = hiltViewModel())
{
    LaunchedEffect(key1 = item) { viewModel.updateCurrentItem(item) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp)
            .navigationBarsPadding(), contentAlignment = Alignment.TopCenter
    ) {
        val status by viewModel.item.collectAsStateWithLifecycle(
            dispatcherProvider = dispatcherProvider, initial = TabDetailStatus.Loading
        )
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .heightIn(min = 150.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
                ErrorScreen(isVisible = status is TabDetailStatus.Error) {
                    LoadingScreen(isVisible = status is TabDetailStatus.Loading) {
                        if (status is TabDetailStatus.Success)
                            ResultsScreen(item = (status as TabDetailStatus.Success).item)
                    }
            }
        }
    }
}

@Composable
private fun ResultsScreen(item: Item) {
    Column(modifier = Modifier.testTag(TAB_DETAIL_TEST_TAG_RESULTS)) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = item.getFormattedString(),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = item.subtitle.getSubTitleText() ?: "",
            style = MaterialTheme.typography.bodyMedium
        )
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = item.share(),
                modifier = Modifier.weight(0.5f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    modifier = Modifier.padding(end = 8.dp),
                    contentDescription = stringResource(R.string.menu_share)
                )
                Text(
                    stringResource(R.string.menu_share).toUpperCase(Locale.current),
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
            }
            TextButton(
                onClick = item.copyItemToClipboard() ?: { },
                modifier = Modifier.weight(0.5f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    modifier = Modifier.padding(end = 8.dp),
                    contentDescription = stringResource(R.string.menu_copy_to_clipboard)
                )
                Text(
                    stringResource(R.string.menu_copy_to_clipboard).toUpperCase(Locale.current),
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen(isVisible: Boolean, content: @Composable () -> Unit) {
    Crossfade(targetState = isVisible) {
        if (it) Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.testTag(TAB_DETAIL_TEST_TAG_PROGRESS),
                color = MaterialTheme.colorScheme.secondary
            )
        } else content()
    }
}

@Composable
private fun ErrorScreen(isVisible: Boolean, content: @Composable () -> Unit) {
    Crossfade(targetState = isVisible) {
        if (it) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(36.dp),
                    contentScale = ContentScale.FillHeight,
                    imageVector = Icons.Outlined.ErrorOutline,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error),
                    contentDescription = stringResource(R.string.general_error)
                )
                Text(
                    stringResource(R.string.general_error),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else content()
    }
}