package com.cwlarson.deviceid.tabs

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.data.TabDataStatus
import com.cwlarson.deviceid.ui.util.click
import com.cwlarson.deviceid.util.collectAsStateWithLifecycle
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.ExperimentalCoroutinesApi

@VisibleForTesting
const val TAB_TEST_TAG_LIST = "tab_list"

@VisibleForTesting
const val TAB_TEST_TAG_PROGRESS = "tab_progressbar"

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalPermissionsApi
@Composable
fun TabScreen(
    appBarSize: Int, isTwoPane: Boolean, scaffoldState: ScaffoldState,
    viewModel: TabsViewModel = hiltViewModel(), onItemClick: (item: Item) -> Unit
) {
    val status by viewModel.allItems.collectAsStateWithLifecycle(initial = TabDataStatus.Loading)
    val refreshDisabled by viewModel.refreshDisabled.collectAsStateWithLifecycle(initial = true)
    LoadingScreen(isVisible = status is TabDataStatus.Loading) {
        ErrorScreen(isVisible = status is TabDataStatus.Error) {
            EmptyTabScreen(
                isVisible = status is TabDataStatus.Success &&
                        (status as TabDataStatus.Success).list.isEmpty()
            ) {
                MainContent(appBarSize = appBarSize, isTwoPane = isTwoPane,
                    refreshDisabled = refreshDisabled, scaffoldState = scaffoldState,
                    status = status, onForceRefresh = { viewModel.forceRefresh() },
                    onItemClick = { item -> onItemClick(item) })
            }
        }
    }
}

@ExperimentalCoroutinesApi
@ExperimentalPermissionsApi
@ExperimentalFoundationApi
@Composable
private fun MainContent(
    appBarSize: Int, isTwoPane: Boolean, refreshDisabled: Boolean,
    scaffoldState: ScaffoldState, status: TabDataStatus,
    onForceRefresh: () -> Unit, onItemClick: (item: Item) -> Unit
) {
    val swipeRefreshState = rememberSwipeRefreshState(status is TabDataStatus.Loading)
    var clickedItem by remember { mutableStateOf<Item?>(null) }
    clickedItem?.click(
        snackbarHostState = scaffoldState.snackbarHostState, forceRefresh = onForceRefresh,
    ) { onItemClick(it) }
    SwipeRefresh(state = swipeRefreshState, onRefresh = onForceRefresh,
        swipeEnabled = !refreshDisabled, indicator = { state, trigger ->
            SwipeRefreshIndicator(
                state = state, refreshTriggerDistance = trigger,
                contentColor = MaterialTheme.colors.secondary, scale = true
            )
        }) {
        LazyVerticalGrid(modifier = Modifier.testTag(TAB_TEST_TAG_LIST),
            contentPadding = rememberInsetsPaddingValues(
                if (isTwoPane) LocalWindowInsets.current.systemBars
                else LocalWindowInsets.current.statusBars,
                applyTop = false,
                additionalTop = with(LocalDensity.current) { appBarSize.toDp() }
            ), cells = GridCells.Adaptive(dimensionResource(R.dimen.grid_view_item_width))
        ) {
            when (status) {
                is TabDataStatus.Success -> items(status.list) { item ->
                    ItemListItem(item = item) { clickedItem = item }
                }
                else -> items(emptyList<Item>()) { }
            }
        }
    }
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
                    painter = painterResource(R.drawable.ic_no_items),
                    contentDescription = stringResource(R.string.textview_recyclerview_no_items)
                )
                Text(
                    stringResource(R.string.textview_recyclerview_no_items),
                    style = MaterialTheme.typography.h6
                )
            }
        else content()
    }
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
                    style = MaterialTheme.typography.body1
                )
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(150.dp)
                        .padding(top = dimensionResource(id = R.dimen.activity_vertical_margin))
                        .testTag(TAB_TEST_TAG_PROGRESS),
                    color = MaterialTheme.colors.secondary
                )
            }
        else content()
    }
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
                    colorFilter = ColorFilter.tint(MaterialTheme.colors.error),
                    contentDescription = stringResource(R.string.general_error)
                )
                Text(
                    stringResource(R.string.general_error),
                    style = MaterialTheme.typography.h6
                )
            }
        else content()
    }
}