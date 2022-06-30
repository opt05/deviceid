package com.cwlarson.deviceid

import android.content.Intent
import android.util.TypedValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.unit.height
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.cwlarson.deviceid.data.*
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.settings.UserPreferences
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.util.AppUpdateUtils
import com.cwlarson.deviceid.util.DispatcherProvider
import com.cwlarson.deviceid.util.InstallState
import com.cwlarson.deviceid.util.UpdateState
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import javax.inject.Inject

@HiltAndroidTest
class MainActivityTest {
    @get:Rule(order = 0)
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createEmptyComposeRule()

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var appUpdateUtils: AppUpdateUtils

    @Inject
    lateinit var allRepository: AllRepository

    @Inject
    lateinit var deviceRepository: DeviceRepository

    @Inject
    lateinit var networkRepository: NetworkRepository

    @Inject
    lateinit var softwareRepository: SoftwareRepository

    @Inject
    lateinit var hardwareRepository: HardwareRepository
    private val dispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())

    private val installState = MutableStateFlow<InstallState>(InstallState.Initial)
    private val updateState = MutableStateFlow<UpdateState>(UpdateState.Initial)
    private val deviceItem = Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("1"))
    private val networkItem = deviceItem.copy(subtitle = ItemSubtitle.Text("2"))
    private val softwareItem = deviceItem.copy(subtitle = ItemSubtitle.Text("3"))
    private val hardwareItem = deviceItem.copy(subtitle = ItemSubtitle.Text("4"))
    private val allItem = deviceItem.copy(subtitle = ItemSubtitle.Text("5"))
    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() = runTest(dispatcher) {
        hiltAndroidRule.inject()
        dispatcherProvider.provideDispatcher(dispatcher)
        whenever(appUpdateUtils.installState).doReturn(installState)
        whenever(appUpdateUtils.updateState).doReturn(updateState)
        whenever(appUpdateUtils.awaitIsFlexibleUpdateDownloaded()).doReturn(false)
        whenever(preferenceManager.searchHistory).doReturn(flowOf(false))
        whenever(preferenceManager.getSearchHistoryItems(anyOrNull()))
            .doReturn(flowOf(listOf("history1", "history2")))
        whenever(preferenceManager.darkTheme).doReturn(flowOf(false))
        whenever(preferenceManager.autoRefreshRate).doReturn(flowOf(0))
        whenever(preferenceManager.userPreferencesFlow).doReturn(flowOf(UserPreferences()))
        whenever(allRepository.search(any())).doReturn(
            flowOf(TabDataStatus.Success(listOf(allItem)))
        )
        whenever(deviceRepository.list()).doReturn(
            flowOf(TabDataStatus.Success(listOf(deviceItem)))
        )
        whenever(networkRepository.list()).doReturn(
            flowOf(TabDataStatus.Success(listOf(networkItem)))
        )
        whenever(softwareRepository.list()).doReturn(
            flowOf(TabDataStatus.Success(listOf(softwareItem)))
        )
        whenever(hardwareRepository.list()).doReturn(
            flowOf(TabDataStatus.Success(listOf(hardwareItem)))
        )
    }

    private fun launchScenario(hasSearchIntent: Boolean = false) {
        scenario = if (hasSearchIntent)
            ActivityScenario.launch(Intent(
                ApplicationProvider.getApplicationContext(),
                MainActivity::class.java
            ).apply { action = Intent.ACTION_SEARCH })
        else ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun cleanup() {
        if (::scenario.isInitialized) scenario.close()
    }

    @Test
    fun test_initial_singlePane() = runTest(dispatcher) {
        launchScenario()
        isScreenSw900dp(false)
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Navigate up").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Clear search").assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_MENU, useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV_DEVICE)
            .assertIsDisplayed().assertIsSelected()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV_NETWORK)
            .assertIsDisplayed().assertIsNotSelected()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV_SOFTWARE)
            .assertIsDisplayed().assertIsNotSelected()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV_HARDWARE)
            .assertIsDisplayed().assertIsNotSelected()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV).assertDoesNotExist()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertDoesNotExist()
        composeTestRule.onNodeWithText("3").assertDoesNotExist()
        composeTestRule.onNodeWithText("4").assertDoesNotExist()
        verify(appUpdateUtils).checkForFlexibleUpdate(false)
    }

    @Test
    fun test_initial_dualPane() = runTest(dispatcher) {
        launchScenario()
        isScreenSw900dp()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Navigate up").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Clear search").assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_MENU, useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV_DEVICE)
            .assertIsDisplayed().assertIsSelected()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV_NETWORK)
            .assertIsDisplayed().assertIsNotSelected()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV_SOFTWARE)
            .assertIsDisplayed().assertIsNotSelected()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV_HARDWARE)
            .assertIsDisplayed().assertIsNotSelected()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertDoesNotExist()
        composeTestRule.onNodeWithText("3").assertDoesNotExist()
        composeTestRule.onNodeWithText("4").assertDoesNotExist()
        verify(appUpdateUtils).checkForFlexibleUpdate(false)
    }

    @Test
    fun test_SearchView_nameFade_singlePane_initial() = runTest(dispatcher) {
        launchScenario()
        isScreenSw900dp(false)
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Device Info")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Search all")).assertDoesNotExist()
        dispatcher.scheduler.advanceUntilIdle()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Device Info")).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Search all")).assertIsDisplayed()
    }

    @Test
    fun test_SearchView_nameFade_singlePane_recreate() = runTest(dispatcher) {
        launchScenario()
        isScreenSw900dp(false)
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Device Info")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Search all")).assertDoesNotExist()
        dispatcher.scheduler.advanceUntilIdle()
        scenario.recreate()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Device Info")).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Search all")).assertIsDisplayed()
    }

    @Test
    fun test_SearchView_nameFade_dualPane() = runTest(dispatcher) {
        launchScenario()
        isScreenSw900dp()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Device Info")).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Search all")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_DUAL_PANE_NAV).onChildren()
            .filterToOne(hasTextExactly("Device Info")).assertIsDisplayed()
    }

    @Test
    fun test_navigation_searchIntent() = runTest(dispatcher) {
        launchScenario(true)
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertDoesNotExist()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_CLEAR).assertIsDisplayed()
    }

    @Test
    fun test_navigation_network() = runTest(dispatcher) {
        launchScenario()
        composeTestRule.onNodeWithText("Network").performClick().assertIsSelected()
        composeTestRule.onNodeWithText("1").assertDoesNotExist()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertDoesNotExist()
        composeTestRule.onNodeWithText("4").assertDoesNotExist()
    }

    @Test
    fun test_navigation_software() = runTest(dispatcher) {
        launchScenario()
        composeTestRule.onNodeWithText("Software").performClick().assertIsSelected()
        composeTestRule.onNodeWithText("1").assertDoesNotExist()
        composeTestRule.onNodeWithText("2").assertDoesNotExist()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
        composeTestRule.onNodeWithText("4").assertDoesNotExist()
    }

    @Test
    fun test_navigation_hardware() = runTest(dispatcher) {
        launchScenario()
        composeTestRule.onNodeWithText("Hardware").performClick().assertIsSelected()
        composeTestRule.onNodeWithText("1").assertDoesNotExist()
        composeTestRule.onNodeWithText("2").assertDoesNotExist()
        composeTestRule.onNodeWithText("3").assertDoesNotExist()
        composeTestRule.onNodeWithText("4").assertIsDisplayed()
    }

    @Test
    fun test_navigation_settings() = runTest(dispatcher) {
        launchScenario()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_MENU, useUnmergedTree = true)
            .performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR_BACK).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_MENU, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun test_navigation_settings_back_singlePane() = runTest(dispatcher) {
        launchScenario()
        isScreenSw900dp(false)
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_MENU, useUnmergedTree = true)
            .performClick()
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR_BACK).performClick()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertIsDisplayed()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_MENU, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun test_navigation_settings_back_dualPane() = runTest(dispatcher) {
        launchScenario()
        isScreenSw900dp()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_MENU, useUnmergedTree = true)
            .performClick()
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR_BACK).performClick()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertDoesNotExist()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_MENU, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun test_navigation_search_initial_singlePane() = runTest(dispatcher) {
        launchScenario()
        isScreenSw900dp(false)
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT).performClick()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertIsDisplayed()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_CLEAR).assertDoesNotExist()
    }

    @Test
    fun test_navigation_search_initial_dualPane() = runTest(dispatcher) {
        launchScenario()
        isScreenSw900dp()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT).performClick()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertDoesNotExist()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_CLEAR).assertDoesNotExist()
    }

    @Test
    fun test_navigation_search_hasText() = runTest(dispatcher) {
        launchScenario()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT).performTextInput("t")
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertDoesNotExist()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_CLEAR).assertIsDisplayed()
    }

    @Test
    fun test_navigation_search_clearText() = runTest(dispatcher) {
        launchScenario()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT).performTextInput("t")
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT).performTextClearance()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertDoesNotExist()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_CLEAR).assertIsDisplayed()
    }

    @Test
    fun test_navigation_search_back_singlePane() = runTest(dispatcher) {
        launchScenario()
        isScreenSw900dp(false)
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT).performTextInput("t")
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK).performClick()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertIsDisplayed()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_CLEAR).assertDoesNotExist()
    }

    @Test
    fun test_navigation_search_back_dualPane() = runTest(dispatcher) {
        launchScenario()
        isScreenSw900dp()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT).performTextInput("t")
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK).performClick()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertDoesNotExist()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_CLEAR).assertDoesNotExist()
    }

    @Test
    fun test_navigation_search_clear() = runTest(dispatcher) {
        launchScenario()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT).performTextInput("t")
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_CLEAR).performClick()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT).assertTextEquals("")
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertDoesNotExist()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_CLEAR).assertIsDisplayed()
    }

    @Test
    fun test_search_searchHistory_displays() = runTest(dispatcher) {
        whenever(preferenceManager.searchHistory).doReturn(flowOf(true))
        launchScenario()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT).performTextInput("h")
        composeTestRule.onAllNodesWithText("history", substring = true).assertCountEquals(2)
    }

    @Test
    fun test_search_searchHistory_notDisplays() = runTest(dispatcher) {
        launchScenario()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT).performTextInput("h")
        composeTestRule.onAllNodesWithText("history", substring = true).assertCountEquals(0)
    }

    @Test
    fun test_appUpdate_FlexibleUpdateDownloadedSnackbar_onResume() = runTest(dispatcher) {
        whenever(appUpdateUtils.awaitIsFlexibleUpdateDownloaded()).doReturn(true)
        launchScenario()
        composeTestRule.onNodeWithText("An update has just been downloaded.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Restart").assertIsDisplayed()
    }

    @Test
    fun test_appUpdate_FlexibleUpdateDownloadedSnackbar_onStateChange() = runTest(dispatcher) {
        launchScenario()
        whenever(appUpdateUtils.awaitIsFlexibleUpdateDownloaded()).doReturn(true)
        installState.value = InstallState.NoError(status = InstallStatus.DOWNLOADED)
        composeTestRule.onNodeWithText("An update has just been downloaded.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Restart").assertIsDisplayed()
    }

    @Test
    fun test_appUpdate_FlexibleUpdateDownloadedSnackbar_click() = runTest(dispatcher) {
        whenever(appUpdateUtils.awaitIsFlexibleUpdateDownloaded()).doReturn(true)
        launchScenario()
        composeTestRule.onNodeWithText("Restart").performClick()
        verify(appUpdateUtils).completeUpdate()
    }

    @Test
    fun test_appUpdate_FlexibleInstallFailedSnackbar() = runTest(dispatcher) {
        whenever(appUpdateUtils.awaitIsFlexibleUpdateDownloaded()).doReturn(true)
        installState.value = InstallState.NoError(status = InstallStatus.FAILED)
        launchScenario()
        composeTestRule.onNodeWithText("Failed to download an update.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun test_appUpdate_FlexibleInstallFailedSnackbar_click() = runTest(dispatcher) {
        whenever(appUpdateUtils.awaitIsFlexibleUpdateDownloaded()).doReturn(true)
        installState.value = InstallState.NoError(status = InstallStatus.FAILED)
        launchScenario()
        clearInvocations(appUpdateUtils)
        composeTestRule.onNodeWithText("Retry").performClick()
        verify(appUpdateUtils).checkForFlexibleUpdate(false)
    }

    @Test
    fun test_appUpdate_FlexibleUpdateDialog() = runTest(dispatcher) {
        updateState.value = UpdateState.No(
            UpdateAvailability.UNKNOWN,
            R.string.update_unknown_title, R.string.update_unknown_message,
            R.string.update_unknown_ok
        )
        launchScenario()
        composeTestRule.onNodeWithText("Failed to check for updates").assertIsDisplayed()
        composeTestRule.onNodeWithText("An unknown response has been received. Please check your internet connection or try again later.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Got it").assertIsDisplayed()
    }

    @Test
    fun test_appUpdate_FlexibleUpdateDialog_clickButton() = runTest(dispatcher) {
        updateState.value = UpdateState.No(
            UpdateAvailability.UNKNOWN,
            R.string.update_unknown_title, R.string.update_unknown_message,
            R.string.update_unknown_ok
        )
        launchScenario()
        composeTestRule.onNodeWithText("Got it").performClick()
        composeTestRule.onNode(isDialog()).assertDoesNotExist()
    }

    @Test
    fun test_appUpdate_FlexibleUpdateDialog_clickOutside() = runTest(dispatcher) {
        updateState.value = UpdateState.No(
            UpdateAvailability.UNKNOWN,
            R.string.update_unknown_title, R.string.update_unknown_message,
            R.string.update_unknown_ok
        )
        launchScenario()
        val outsideX = 0
        val outsideY = with(composeTestRule.density) {
            composeTestRule.onAllNodes(isRoot()).onFirst().getUnclippedBoundsInRoot().height.roundToPx() / 2
        }
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).click(outsideX, outsideY)
        composeTestRule.onNode(isDialog()).assertDoesNotExist()
    }

    private fun isScreenSw900dp(assumeTrue: Boolean = true) {
        scenario.onActivity {
            val twoPane = TypedValue().apply {
                it.resources.getValue(R.dimen.two_pane_min, this, true)
            }.run { TypedValue.complexToFloat(data) }
            with(it.resources.configuration.screenWidthDp >= twoPane) {
                if (assumeTrue) assumeTrue(this) else assumeFalse(this)
            }
        }
    }
}