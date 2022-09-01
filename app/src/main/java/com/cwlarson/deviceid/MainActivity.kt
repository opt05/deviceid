package com.cwlarson.deviceid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cwlarson.deviceid.search.SearchScreen
import com.cwlarson.deviceid.settings.SettingsScreen
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.tabs.TabScreen
import com.cwlarson.deviceid.tabsdetail.TabDetailScreen
import com.cwlarson.deviceid.ui.theme.AppTheme
import com.cwlarson.deviceid.ui.theme.Ubuntu
import com.cwlarson.deviceid.ui.theme.navigationBackgroundSelected
import com.cwlarson.deviceid.ui.theme.statusBarColor
import com.cwlarson.deviceid.ui.util.IntentHandler
import com.cwlarson.deviceid.util.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.play.core.install.model.InstallStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_SEARCH = "main_activity_search"

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK = "main_activity_search_back"

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT = "main_activity_search_text"

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_SEARCH_CLEAR = "main_activity_search_clear"

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_SEARCH_MENU = "main_activity_search_menu"

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_TOOLBAR = "main_activity_toolbar"

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_TOOLBAR_BACK = "main_activity_toolbar_back"

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV = "main_activity_bottom_nav"

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV_DEVICE = "main_activity_bottom_nav_device"

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV_NETWORK = "main_activity_bottom_nav_network"

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV_SOFTWARE = "main_activity_bottom_nav_software"

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV_HARDWARE = "main_activity_bottom_nav_hardware"

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV = "main_activity_dual_pane_nav"

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV_DEVICE = "main_activity_dual_pane_nav_device"

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV_NETWORK = "main_activity_dual_pane_nav_network"

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV_SOFTWARE = "main_activity_dual_pane_nav_software"

@VisibleForTesting
const val MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV_HARDWARE = "main_activity_dual_pane_nav_hardware"

private sealed class Screen(
    val route: String,
    @StringRes val stringRes: Int,
    val icon: ImageVector
) {
    object Device :
        Screen("device", R.string.bottom_nav_title_device, Icons.Outlined.PermDeviceInformation)

    object Network :
        Screen("network", R.string.bottom_nav_title_network, Icons.Outlined.SettingsEthernet)

    object Software :
        Screen("software", R.string.bottom_nav_title_software, Icons.Outlined.Android)

    object Hardware :
        Screen("hardware", R.string.bottom_nav_title_hardware, Icons.Outlined.DeveloperBoard)

    object Settings : Screen("settings", R.string.menu_settings, Icons.Outlined.Settings)
    object Search : Screen("search", R.string.menu_search, Icons.Outlined.Search)
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    @Inject
    lateinit var appUpdateUtils: AppUpdateUtils

    private val viewModel by viewModels<MainActivityViewModel>()
    private val intentHandler = IntentHandler(this)
    private val navigationItems =
        listOf(Screen.Device, Screen.Network, Screen.Software, Screen.Hardware)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (savedInstanceState == null) {
            onNewIntent(intent)
            lifecycleScope.launch(dispatcherProvider.Main) { appUpdateUtils.checkForFlexibleUpdate() }
        }
        setContent {
            val isTwoPane =
                LocalConfiguration.current.screenWidthDp >= dimensionResource(id = R.dimen.two_pane_min).value
            viewModel.startTitleFade(isTwoPane, intent)
            var showUpdateDialog by rememberSaveable { mutableStateOf(false) }
            var updateDialogTitle by rememberSaveable { mutableStateOf(0) }
            var updateDialogMessage by rememberSaveable { mutableStateOf(0) }
            var updateDialogButton by rememberSaveable { mutableStateOf(0) }
            val installState by appUpdateUtils.installState.collectAsStateWithLifecycle(initial = InstallState.Initial)
            val updateState by appUpdateUtils.updateState.collectAsStateWithLifecycle(initial = UpdateState.Initial)
            with(updateState) {
                if (this is UpdateState.No) {
                    showUpdateDialog = true
                    updateDialogTitle = title
                    updateDialogMessage = message
                    updateDialogButton = button

                }
            }
            val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle(initial = null)
            AppTheme(darkTheme = darkTheme ?: isSystemInDarkTheme()) {
                var appBarVisible by rememberSaveable { mutableStateOf(false) }
                val statusBarColor =
                    if (appBarVisible) MaterialTheme.colors.statusBarColor else Color.Transparent
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = MaterialTheme.colors.isLight && !appBarVisible
                val keyboardController = LocalSoftwareKeyboardController.current
                SideEffect {
                    systemUiController.setNavigationBarColor(
                        Color.Transparent, darkIcons = useDarkIcons
                    )
                    systemUiController.setStatusBarColor(statusBarColor, darkIcons = useDarkIcons)
                }
                var bottomSheetItem by rememberSaveable { mutableStateOf<Item?>(null) }
                val bottomSheetState =
                    rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden) {
                        if (it == ModalBottomSheetValue.Hidden) bottomSheetItem = null
                        keyboardController?.hide()
                        true
                    }

                if (bottomSheetItem != null)
                    LaunchedEffect(bottomSheetState) { bottomSheetState.show() }
                val scaffoldState = rememberScaffoldState()
                var isSideNavVisible by rememberSaveable { mutableStateOf(true) }
                var searchBarQuery by rememberSaveable { mutableStateOf("") }
                var isSearchOpen by rememberSaveable { mutableStateOf(false) }
                var topSearchBarSize by remember { mutableStateOf(0) }
                val navController = rememberNavController()
                ModalBottomSheetLayout(sheetState = bottomSheetState,
                    sheetContent = { TabDetailScreen(item = bottomSheetItem) }) {
                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = {
                            if (appBarVisible)
                                TopAppBar(
                                    modifier = Modifier
                                        .statusBarsPadding()
                                        .testTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR),
                                    title = {
                                        Text(
                                            stringResource(
                                                when (navController.currentDestination?.route) {
                                                    Screen.Settings.route -> Screen.Settings.stringRes
                                                    else -> R.string.app_name
                                                }
                                            ), fontFamily = Ubuntu
                                        )
                                    }, navigationIcon = {
                                        if (navController.backQueue.size > 1)
                                            IconButton(
                                                modifier = Modifier.testTag(
                                                    MAIN_ACTIVITY_TEST_TAG_TOOLBAR_BACK
                                                ),
                                                onClick = { navController.navigateUp() }) {
                                                Icon(
                                                    imageVector = Icons.Outlined.ArrowBack,
                                                    contentDescription = stringResource(R.string.menu_back)
                                                )
                                            }
                                    }
                                )
                        }, bottomBar = {
                            BottomAppBar(
                                appBarVisible = appBarVisible,
                                isSearchOpen = isSearchOpen,
                                isTwoPane = isTwoPane,
                                navController = navController,
                                items = navigationItems
                            )
                        }
                    ) { innerPadding ->
                        Row(modifier = Modifier.fillMaxSize()) {
                            DualPaneNavigationView(
                                items = navigationItems,
                                navController = navController,
                                isVisible = isSideNavVisible && isTwoPane
                            ) {
                                Box(
                                    modifier =
                                    if (!isTwoPane) Modifier.padding(innerPadding) else
                                        Modifier.padding(
                                            top = innerPadding.calculateTopPadding(),
                                            start = innerPadding.calculateStartPadding
                                                (LocalLayoutDirection.current),
                                            end = innerPadding.calculateEndPadding
                                                (LocalLayoutDirection.current)
                                        )
                                ) {
                                    NavHost(
                                        navController, startDestination = Screen.Device.route,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        composable(
                                            Screen.Device.route,
                                            arguments = listOf(navArgument("tab") {
                                                type = NavType.EnumType(ItemType::class.java)
                                                defaultValue = ItemType.DEVICE
                                            })
                                        ) {
                                            appBarVisible = false
                                            isSideNavVisible = true
                                            TabScreen(
                                                appBarSize = topSearchBarSize,
                                                isTwoPane = isTwoPane,
                                                scaffoldState = scaffoldState
                                            ) { item -> bottomSheetItem = item }
                                        }
                                        composable(
                                            Screen.Network.route,
                                            arguments = listOf(navArgument("tab") {
                                                type = NavType.EnumType(ItemType::class.java)
                                                defaultValue = ItemType.NETWORK
                                            })
                                        ) {
                                            appBarVisible = false
                                            isSideNavVisible = true
                                            TabScreen(
                                                appBarSize = topSearchBarSize,
                                                isTwoPane = isTwoPane,
                                                scaffoldState = scaffoldState
                                            ) { item -> bottomSheetItem = item }
                                        }
                                        composable(
                                            Screen.Software.route,
                                            arguments = listOf(navArgument("tab") {
                                                type = NavType.EnumType(ItemType::class.java)
                                                defaultValue = ItemType.SOFTWARE
                                            })
                                        ) {
                                            appBarVisible = false
                                            isSideNavVisible = true
                                            TabScreen(
                                                appBarSize = topSearchBarSize,
                                                isTwoPane = isTwoPane,
                                                scaffoldState = scaffoldState
                                            ) { item -> bottomSheetItem = item }
                                        }
                                        composable(
                                            Screen.Hardware.route,
                                            arguments = listOf(navArgument("tab") {
                                                type = NavType.EnumType(ItemType::class.java)
                                                defaultValue = ItemType.HARDWARE
                                            })
                                        ) {
                                            appBarVisible = false
                                            isSideNavVisible = true
                                            TabScreen(
                                                appBarSize = topSearchBarSize,
                                                isTwoPane = isTwoPane,
                                                scaffoldState = scaffoldState
                                            ) { item -> bottomSheetItem = item }
                                        }
                                        composable(Screen.Search.route) {
                                            appBarVisible = false
                                            isSideNavVisible = false
                                            SearchScreen(
                                                topSearchBarSize, searchBarQuery,
                                                scaffoldState
                                            ) { item ->
                                                bottomSheetItem = item
                                            }
                                        }
                                        composable(Screen.Settings.route) {
                                            appBarVisible = true
                                            isSideNavVisible = false
                                            SettingsScreen(appUpdateUtils)
                                        }
                                    }

                                    if (!appBarVisible)
                                        SearchView(
                                            navController = navController,
                                            modifier = Modifier.onSizeChanged {
                                                topSearchBarSize = it.height
                                            },
                                            isSearchOpen = isSearchOpen,
                                            isTwoPane = isTwoPane,
                                            searchBarQuery = searchBarQuery,
                                            viewModel = viewModel,
                                            keyboardController = keyboardController,
                                            onSearchOpen = {
                                                isSearchOpen = it
                                                if (it && navController.currentBackStackEntry?.destination?.route != Screen.Search.route)
                                                    navController.navigate(Screen.Search.route)
                                                else if (!it && navController.currentBackStackEntry?.destination?.route == Screen.Search.route)
                                                    navController.navigateUp()
                                            }) { searchBarQuery = it }

                                    intentHandler.OnIntent { intent ->
                                        if (intent?.action == Intent.ACTION_SEARCH) {
                                            isSearchOpen = true
                                            navController.navigate(Screen.Search.route)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    FlexibleUpdateDialog(
                        showUpdateDialog, updateDialogTitle,
                        updateDialogMessage, updateDialogButton
                    ) { showUpdateDialog = false }

                    with(installState) {
                        if (this is InstallState.NoError) {
                            when (status) {
                                InstallStatus.DOWNLOADED ->
                                    FlexibleUpdateDownloadedSnackbar(scaffoldState)
                                InstallStatus.FAILED ->
                                    FlexibleInstallFailedSnackbar(scaffoldState)
                                else -> { /* Do nothing */
                                }
                            }
                        } else FlexibleUpdateDownloadedSnackbar(scaffoldState)
                    }
                }
            }
        }
    }

    @Composable
    private fun FlexibleUpdateDownloadedSnackbar(scaffoldState: ScaffoldState) {
        val message = stringResource(R.string.update_download_finished)
        val label = stringResource(R.string.update_restart)
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        LaunchedEffect(lifecycle) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (appUpdateUtils.awaitIsFlexibleUpdateDownloaded()) {
                    scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                    val result = scaffoldState.snackbarHostState.showSnackbar(
                        message = message, actionLabel = label,
                        duration = SnackbarDuration.Indefinite
                    )
                    if (result == SnackbarResult.ActionPerformed)
                        appUpdateUtils.completeUpdate()
                }
            }
        }
    }

    @Composable
    private fun FlexibleInstallFailedSnackbar(scaffoldState: ScaffoldState) {
        val message = stringResource(R.string.update_download_failed)
        val label = stringResource(R.string.update_retry)
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        LaunchedEffect(lifecycle) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (appUpdateUtils.awaitIsFlexibleUpdateDownloaded()) {
                    scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                    val result = scaffoldState.snackbarHostState.showSnackbar(
                        message = message, actionLabel = label,
                        duration = SnackbarDuration.Indefinite
                    )
                    if (result == SnackbarResult.ActionPerformed)
                        appUpdateUtils.checkForFlexibleUpdate()
                }
            }
        }
    }

    @Composable
    private fun FlexibleUpdateDialog(
        showDialog: Boolean, title: Int, message: Int, button: Int, onDismiss: () -> Unit
    ) {
        if (showDialog)
            AlertDialog(onDismissRequest = { onDismiss() },
                title = { Text(stringResource(title)) },
                text = { Text(stringResource(message)) },
                confirmButton = {
                    TextButton(onClick = { onDismiss() }) { Text(stringResource(button)) }
                })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intentHandler.onNewIntent(intent)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SearchView(
    navController: NavController, modifier: Modifier = Modifier, isSearchOpen: Boolean,
    isTwoPane: Boolean, searchBarQuery: String, viewModel: MainActivityViewModel,
    keyboardController: SoftwareKeyboardController?,
    onSearchOpen: (Boolean) -> Unit, onSearchQueryChange: (String) -> Unit
) {
    val titleVisibility by viewModel.titleVisibility.collectAsStateWithLifecycle(
        initial = TitleVisibility(visible = true, noFade = false)
    )
    val state by viewModel.isSearchHistory.collectAsStateWithLifecycle(initial = false)
    val items by viewModel.getSearchHistoryItems(searchBarQuery)
        .collectAsStateWithLifecycle(initial = emptyList())
    val focusRequesterTextField = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var forceCloseDropdown by rememberSaveable { mutableStateOf(false) }
    var rowSize by remember { mutableStateOf(Size.Zero) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                horizontal = if (isTwoPane && !isSearchOpen) 16.dp else dimensionResource(R.dimen.activity_horizontal_margin),
                vertical = 8.dp
            )
            .onGloballyPositioned { layoutCoordinates -> rowSize = layoutCoordinates.size.toSize() }
            .testTag(MAIN_ACTIVITY_TEST_TAG_SEARCH), elevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                modifier = Modifier
                    .clickable(indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { focusRequesterTextField.requestFocus() }),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = isSearchOpen) {
                    IconButton(modifier = Modifier.testTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK),
                        onClick = {
                            viewModel.saveSearchHistory(searchBarQuery)
                            onSearchOpen(false)
                        }) {
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.nav_app_bar_navigate_up_description)
                            )
                        }
                    }
                }
                Spacer(Modifier.width(if (isSearchOpen) 0.dp else 16.dp))
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        modifier = Modifier
                            .testTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT)
                            .focusRequester(focusRequesterTextField)
                            .fillMaxWidth(),
                        cursorBrush = SolidColor(MaterialTheme.colors.secondary),
                        textStyle = MaterialTheme.typography.subtitle1
                            .merge(TextStyle(color = MaterialTheme.colors.onSurface)),
                        value = searchBarQuery, onValueChange = { query ->
                            if (query.isNotBlank() && !isSearchOpen) onSearchOpen(true)
                            forceCloseDropdown = false
                            onSearchQueryChange(query)
                        }, singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            viewModel.saveSearchHistory(searchBarQuery)
                        })
                    )
                    if (searchBarQuery.isEmpty())
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = if (titleVisibility.visible) ""
                                else stringResource(R.string.search_hint_items),
                                style = MaterialTheme.typography.subtitle1
                            )
                        }
                }
                Crossfade(targetState = isSearchOpen) {
                    if (it)
                        IconButton(modifier = Modifier.testTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_CLEAR),
                            onClick = { onSearchQueryChange("") }) {
                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.search_clear)
                                )
                            }
                        }
                }
                SearchbarMenu(navController, rowSize.height)
                if (!isSearchOpen) {
                    onSearchQueryChange("")
                    focusManager.clearFocus(true)
                } else if (!forceCloseDropdown)
                    DisposableEffect(Unit) {
                        focusRequesterTextField.requestFocus()
                        keyboardController?.show()
                        onDispose { }
                    }
            }
            if (state)
                SearchViewHistory(items, rowSize, isSearchOpen && !forceCloseDropdown) { query ->
                    onSearchQueryChange(query)
                    focusManager.clearFocus(force = true)
                    forceCloseDropdown = true
                }
            Crossfade(targetState = titleVisibility.visible) {
                if (it)
                    Text(
                        stringResource(id = R.string.app_name),
                        color = MaterialTheme.colors.secondary,
                        style = MaterialTheme.typography.h6,
                        fontFamily = Ubuntu
                    )
            }
        }
    }
}

@Composable
private fun SearchViewHistory(
    items: List<String>, rowSize: Size, isExpanded: Boolean, onItemClick: (text: String) -> Unit
) {
    DropdownMenu(
        modifier = Modifier.width(with(LocalDensity.current) { rowSize.width.toDp() }),
        offset = DpOffset(0.dp, -(4).dp), expanded = isExpanded && items.isNotEmpty(),
        onDismissRequest = { /* Do nothing */ }, properties = PopupProperties(focusable = false)
    ) {
        items.forEach { text ->
            DropdownMenuItem(onClick = { onItemClick(text) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Icon(imageVector = Icons.Outlined.History, contentDescription = null)
                    }
                    Text(text = text, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun SearchbarMenu(navController: NavController, appBarSize: Float) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart).testTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_MENU)) {
        IconButton(onClick = { expanded = true }) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.menu_overflow)
                )
            }
        }
        DropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, -with(LocalDensity.current) { appBarSize.toDp() })
        ) {
            DropdownMenuItem(onClick = { navController.navigate(Screen.Settings.route) }) {
                Text(stringResource(R.string.menu_settings))
            }
        }
    }
}

@Composable
private fun BottomAppBar(
    appBarVisible: Boolean, isSearchOpen: Boolean, isTwoPane: Boolean,
    navController: NavController, items: List<Screen>
) {
    if (isTwoPane) Spacer(modifier = Modifier.navigationBarsPadding())
    else
        Crossfade(targetState = !appBarVisible && !isSearchOpen) { targetState ->
            if (targetState)
                BottomNavigation(
                    elevation = 0.dp, modifier = Modifier
                        .background(MaterialTheme.colors.primarySurface)
                        .navigationBarsPadding()
                        .testTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV)
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentHierarchy = navBackStackEntry?.destination?.hierarchy
                    items.forEach { screen ->
                        BottomNavigationItem(
                            modifier = Modifier.testTag(
                                when (screen) {
                                    Screen.Device -> MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV_DEVICE
                                    Screen.Hardware -> MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV_HARDWARE
                                    Screen.Network -> MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV_NETWORK
                                    Screen.Software -> MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV_SOFTWARE
                                    else -> ""
                                }
                            ),
                            icon = { Icon(imageVector = screen.icon, contentDescription = null) },
                            label = { Text(stringResource(screen.stringRes)) },
                            selected = currentHierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
        }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DualPaneNavigationView(
    items: List<Screen>, navController: NavController,
    isVisible: Boolean, content: @Composable () -> Unit
) {
    val current = navController.currentBackStackEntryAsState()
    var selectedIndex by remember { mutableStateOf(-1) }
    if (selectedIndex < 0)
        selectedIndex = items.indexOfFirst { current.value?.destination?.route == it.route }
    AnimatedVisibility(
        visible = isVisible, enter = slideInHorizontally(),
        exit = ExitTransition.None
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(dimensionResource(id = R.dimen.navigation_list_width))
                .systemBarsPadding()
                .testTag(MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV)
        ) {
            Row(
                modifier = Modifier
                    .height(dimensionResource(id = R.dimen.navigation_title_height))
                    .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.secondary,
                    fontFamily = Ubuntu
                )
            }
            Spacer(
                modifier = Modifier
                    .height(8.dp)
                    .fillMaxWidth()
            )
            LazyColumn(
                modifier = Modifier
                    .selectableGroup()
                    .padding(horizontal = 8.dp)
            ) {
                itemsIndexed(items) { index, item ->
                    Surface(
                        color = if (index == selectedIndex)
                            MaterialTheme.colors.navigationBackgroundSelected
                        else Color.Transparent, shape = MaterialTheme.shapes.small
                    ) {
                        ListItem(modifier = Modifier
                            .testTag(
                                when (item) {
                                    Screen.Device -> MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV_DEVICE
                                    Screen.Hardware -> MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV_HARDWARE
                                    Screen.Network -> MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV_NETWORK
                                    Screen.Software -> MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV_SOFTWARE
                                    else -> ""
                                }
                            )
                            .requiredHeight(48.dp)
                            .selectable(
                                selected = index == selectedIndex,
                                onClick = {
                                    selectedIndex = index
                                    navController.navigate(item.route)
                                }, role = Role.Tab
                            ),
                            text = {
                                if (index == selectedIndex)
                                    Text(
                                        stringResource(id = item.stringRes),
                                        color = MaterialTheme.colors.primary
                                    )
                                else
                                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                        Text(stringResource(id = item.stringRes))
                                    }
                            },
                            icon = {
                                if (index == selectedIndex)
                                    Icon(
                                        imageVector = item.icon,
                                        tint = MaterialTheme.colors.primary,
                                        contentDescription = null
                                    )
                                else
                                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                        Icon(imageVector = item.icon, contentDescription = null)
                                    }
                            }
                        )
                    }
                }
            }
        }
    }
    content()
}