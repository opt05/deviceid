package com.cwlarson.deviceid.tabs

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.data.TabDataStatus
import com.cwlarson.deviceid.ui.icons.noItemsIcon
import com.cwlarson.deviceid.ui.theme.AppTheme
import com.cwlarson.deviceid.ui.util.click
import com.cwlarson.deviceid.util.DispatcherProvider
import com.cwlarson.deviceid.util.collectAsStateWithLifecycle

@VisibleForTesting
const val TAB_TEST_TAG_LIST = "tab_list"

@VisibleForTesting
const val TAB_TEST_TAG_PROGRESS = "tab_progressbar"

@Composable
fun TabScreen(
    appBarSize: Int, isTwoPane: Boolean, snackbarHostState: SnackbarHostState,
    dispatcherProvider: DispatcherProvider, viewModel: TabsViewModel = hiltViewModel(),
    onItemClick: (item: Item) -> Unit
) {
    val status by viewModel.allItems.collectAsStateWithLifecycle(
        dispatcherProvider = dispatcherProvider, initial = TabDataStatus.Loading
    )
    val refreshDisabled by viewModel.refreshDisabled.collectAsStateWithLifecycle(
        dispatcherProvider = dispatcherProvider, initial = true
    )
    LoadingScreen(isVisible = status is TabDataStatus.Loading) {
        ErrorScreen(isVisible = status is TabDataStatus.Error) {
            EmptyTabScreen(
                isVisible = status is TabDataStatus.Success &&
                        (status as TabDataStatus.Success).list.isEmpty()
            ) {
                MainContent(appBarSize = appBarSize, isTwoPane = isTwoPane,
                    refreshDisabled = refreshDisabled, snackbarHostState = snackbarHostState,
                    status = status, onForceRefresh = { viewModel.forceRefresh() },
                    onItemClick = { item -> onItemClick(item) })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TabScreenMainPreview() = AppTheme {
    MainContent(
        appBarSize = 0,
        isTwoPane = false,
        refreshDisabled = true,
        snackbarHostState = SnackbarHostState(),
        status = TabDataStatus.Success(
            listOf(
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("subtitle 1")),
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("subtitle 2")),
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("subtitle 3")),
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("subtitle 4")),
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("subtitle 5")),
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("subtitle 6")),
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("subtitle 7")),
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("subtitle 8")),
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("subtitle 9")),
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("subtitle 10"))
            )
        ),
        onForceRefresh = { }
    ) { }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MainContent(
    appBarSize: Int, isTwoPane: Boolean, refreshDisabled: Boolean,
    snackbarHostState: SnackbarHostState, status: TabDataStatus,
    onForceRefresh: () -> Unit, onItemClick: (item: Item) -> Unit
) {
    //FIXME: Bug with pullRefresh staying on screen, will be fixed with Compose 1.4.0-alpha03+
    val swipeRefreshState = rememberPullRefreshState(
        refreshing = status is TabDataStatus.Loading, onRefresh = onForceRefresh
    )
    var clickedItem by remember { mutableStateOf<Item?>(null) }
    clickedItem?.click(
        snackbarHostState = snackbarHostState, forceRefresh = onForceRefresh,
        showItemDetails = { onItemClick(it) }
    )
    Box(modifier = Modifier.pullRefresh(state = swipeRefreshState, enabled = !refreshDisabled)) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxHeight()
                .testTag(TAB_TEST_TAG_LIST),
            contentPadding = if (isTwoPane) WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                .asPaddingValues() else PaddingValues(),
            columns = GridCells.Adaptive(dimensionResource(R.dimen.grid_view_item_width))
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(LocalDensity.current) { appBarSize.toDp() })
                )
            }
            when (status) {
                is TabDataStatus.Success -> items(status.list) { item ->
                    ItemListItem(item = item) { clickedItem = null; clickedItem = item }
                }
                else -> items(emptyList<Item>()) { }
            }
        }
        PullRefreshIndicator(
            refreshing = status is TabDataStatus.Loading, state = swipeRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = MaterialTheme.colorScheme.secondary, scale = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TabScreenEmptyPreview() = AppTheme {
    EmptyTabScreen(isVisible = true) { }
}

@Composable
private fun EmptyTabScreen(isVisible: Boolean, content: @Composable () -> Unit) {
    Crossfade(targetState = isVisible) {
        if (it)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    modifier = Modifier.padding(bottom = 8.dp),
                    contentScale = ContentScale.FillHeight,
                    imageVector = noItemsIcon(),
                    contentDescription = stringResource(R.string.textview_recyclerview_no_items)
                )
                Text(
                    stringResource(R.string.textview_recyclerview_no_items),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        else content()
    }
}

@Preview(showBackground = true)
@Composable
fun TabScreenLoadingPreview() = AppTheme {
    LoadingScreen(isVisible = true) { }
}

@Composable
private fun LoadingScreen(isVisible: Boolean, content: @Composable () -> Unit) {
    Crossfade(targetState = isVisible) {
        if (it)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.general_loading),
                    style = MaterialTheme.typography.bodyLarge
                )
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(150.dp)
                        .padding(top = dimensionResource(id = R.dimen.activity_vertical_margin))
                        .testTag(TAB_TEST_TAG_PROGRESS),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        else content()
    }
}

@Preview(showBackground = true)
@Composable
fun TabScreenErrorPreview() = AppTheme {
    ErrorScreen(isVisible = true) { }
}

@Composable
private fun ErrorScreen(isVisible: Boolean, content: @Composable () -> Unit) {
    Crossfade(targetState = isVisible) {
        if (it)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(120.dp),
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
        else content()
    }
}