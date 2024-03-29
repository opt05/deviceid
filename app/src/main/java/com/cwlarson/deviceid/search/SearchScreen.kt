package com.cwlarson.deviceid.search

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.data.TabDataStatus
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemListItem
import com.cwlarson.deviceid.ui.icons.noItemsSearchIcon
import com.cwlarson.deviceid.ui.util.click
import com.cwlarson.deviceid.util.DispatcherProvider

@VisibleForTesting
const val SEARCH_TEST_TAG_LIST = "search_list"

@VisibleForTesting
const val SEARCH_TEST_TAG_PROGRESS = "search_progressbar"

@VisibleForTesting
const val SEARCH_TEST_TAG_DIVIDER = "search_divider"

@Composable
fun SearchScreen(
    appBarSize: Int, query: String, snackbarHostState: SnackbarHostState,
    dispatcherProvider: DispatcherProvider, viewModel: SearchViewModel = hiltViewModel(),
    onItemClick: (item: Item) -> Unit
) {
    viewModel.setSearchText(query)
    val status by viewModel.allItems.collectAsStateWithLifecycle(
        initialValue = TabDataStatus.Loading, context = dispatcherProvider.Main
    )
    LoadingScreen(isVisible = status is TabDataStatus.Loading) {
        ErrorScreen(isVisible = status is TabDataStatus.Error) {
            EmptySearchScreen(
                isVisible = status is TabDataStatus.Success &&
                        (status as TabDataStatus.Success).list.isEmpty()
            ) {
                MainContent(appBarSize = appBarSize, snackbarHostState = snackbarHostState,
                    status = status, onForceRefresh = { viewModel.forceRefresh() },
                    onItemClick = { item -> onItemClick(item) })
            }
        }
    }
}

@Composable
private fun MainContent(
    appBarSize: Int, snackbarHostState: SnackbarHostState, status: TabDataStatus,
    onForceRefresh: () -> Unit, onItemClick: (item: Item) -> Unit
) {
    var clickedItem by remember { mutableStateOf<Item?>(null) }
    clickedItem?.click(
        snackbarHostState = snackbarHostState, forceRefresh = onForceRefresh,
        showItemDetails = { clickedItem = null; onItemClick(it) }
    )
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.activity_horizontal_margin_search))
            .testTag(SEARCH_TEST_TAG_LIST), contentPadding = WindowInsets.systemBars.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        ).asPaddingValues()
    ) {
        item { Spacer(modifier = Modifier.height(with(LocalDensity.current) { appBarSize.toDp() })) }
        item {
            ResultsRow(
                when (status) {
                    is TabDataStatus.Success -> status.list.size
                    else -> 0
                }
            )
        }
        when (status) {
            is TabDataStatus.Success -> items(status.list) { item ->
                ItemListItem(item = item) { clickedItem = null; clickedItem = item }
                Divider(modifier = Modifier.testTag(SEARCH_TEST_TAG_DIVIDER))
            }
            else -> items(emptyList<Item>()) { }
        }
    }
}

@Composable
private fun ResultsRow(count: Int) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.activity_horizontal_margin),
                vertical = 8.dp
            )
    ) {
        Text(
            stringResource(id = R.string.textview_recyclerview_title).toUpperCase(
                Locale.current
            ), style = MaterialTheme.typography.titleSmall
        )
        Text("$count", style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun EmptySearchScreen(isVisible: Boolean, content: @Composable () -> Unit) {
    Crossfade(targetState = isVisible, label= "Search no results") {
        if (it)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    modifier = Modifier.padding(bottom = 8.dp),
                    contentScale = ContentScale.FillHeight,
                    imageVector = noItemsSearchIcon(),
                    contentDescription = stringResource(R.string.textview_recyclerview_no_search_items)
                )
                Text(
                    stringResource(R.string.textview_recyclerview_no_search_items),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        else content()
    }
}

@Composable
private fun LoadingScreen(isVisible: Boolean, content: @Composable () -> Unit) {
    Crossfade(targetState = isVisible, label="Search loading") {
        if (it)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.search_loading),
                    style = MaterialTheme.typography.bodyLarge
                )
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(150.dp)
                        .padding(top = dimensionResource(id = R.dimen.activity_vertical_margin))
                        .testTag(SEARCH_TEST_TAG_PROGRESS),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        else content()
    }
}

@Composable
private fun ErrorScreen(isVisible: Boolean, content: @Composable () -> Unit) {
    Crossfade(targetState = isVisible, label="Search error") {
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