package com.cwlarson.deviceid.tabsdetail

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.data.TabDetailStatus
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.ui.util.copyItemToClipboard
import com.cwlarson.deviceid.ui.util.share
import com.cwlarson.deviceid.util.DispatcherProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabDetailScreen(
    item: Item?, dispatcherProvider: DispatcherProvider,
    viewModel: TabsDetailViewModel = hiltViewModel(), onDismissRequest: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    LaunchedEffect(key1 = item) { viewModel.updateCurrentItem(item) }
    ModalBottomSheet(
        sheetState = bottomSheetState, onDismissRequest = onDismissRequest
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), contentAlignment = Alignment.TopCenter
        ) {
            val status by viewModel.item.collectAsStateWithLifecycle(
                initialValue = TabDetailStatus.Loading, context = dispatcherProvider.Main
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
}

@Composable
private fun ResultsScreen(item: Item) {
    Column {
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
    Crossfade(targetState = isVisible, label="Details loading") {
        if (it) Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
        } else content()
    }
}

@Composable
private fun ErrorScreen(isVisible: Boolean, content: @Composable () -> Unit) {
    Crossfade(targetState = isVisible, label="Details error") {
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