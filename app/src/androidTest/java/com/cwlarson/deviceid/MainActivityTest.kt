package com.cwlarson.deviceid

import android.content.Intent
import android.util.DisplayMetrics
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
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
import com.cwlarson.deviceid.util.InstallState
import com.cwlarson.deviceid.util.UpdateState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ExperimentalPermissionsApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@HiltAndroidTest
class MainActivityTest {
    @get:Rule(order = 0)
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createEmptyComposeRule()

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

    private val installState = MutableStateFlow<InstallState>(InstallState.Initial)
    private val updateState = MutableStateFlow<UpdateState>(UpdateState.Initial)
    private val deviceItem = Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("1"))
    private val networkItem = deviceItem.copy(subtitle = ItemSubtitle.Text("2"))
    private val softwareItem = deviceItem.copy(subtitle = ItemSubtitle.Text("3"))
    private val hardwareItem = deviceItem.copy(subtitle = ItemSubtitle.Text("4"))
    private val allItem = deviceItem.copy(subtitle = ItemSubtitle.Text("5"))
    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() = runBlockingTest {
        hiltAndroidRule.inject()
        whenever(appUpdateUtils.installState).thenReturn(installState)
        whenever(appUpdateUtils.updateState).thenReturn(updateState)
        whenever(appUpdateUtils.awaitIsFlexibleUpdateDownloaded()).thenReturn(false)
        whenever(preferenceManager.searchHistory).thenReturn(flowOf(false))
        whenever(preferenceManager.getSearchHistoryItems(anyOrNull()))
            .thenReturn(flowOf(listOf("history1", "history2")))
        whenever(preferenceManager.darkTheme).thenReturn(flowOf(false))
        whenever(preferenceManager.autoRefreshRate).thenReturn(flowOf(0))
        whenever(preferenceManager.userPreferencesFlow).thenReturn(flowOf(UserPreferences()))
        whenever(allRepository.search(any())).thenReturn(
            flowOf(TabDataStatus.Success(listOf(allItem)))
        )
        whenever(deviceRepository.list()).thenReturn(
            flowOf(TabDataStatus.Success(listOf(deviceItem)))
        )
        whenever(networkRepository.list()).thenReturn(
            flowOf(TabDataStatus.Success(listOf(networkItem)))
        )
        whenever(softwareRepository.list()).thenReturn(
            flowOf(TabDataStatus.Success(listOf(softwareItem)))
        )
        whenever(hardwareRepository.list()).thenReturn(
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
    fun test_initial_singlePane() {
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
        runBlocking { verify(appUpdateUtils).checkForFlexibleUpdate(false) }
    }

    @Test
    fun test_initial_dualPane() {
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
        runBlocking { verify(appUpdateUtils).checkForFlexibleUpdate(false) }
    }

    @Test
    fun test_SearchView_nameFade_singlePane_initial() {
        launchScenario()
        isScreenSw900dp(false)
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Device Info")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Search all")).assertDoesNotExist()
        TimeUnit.SECONDS.sleep(2)
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Device Info")).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Search all")).assertIsDisplayed()
    }

    @Test
    fun test_SearchView_nameFade_singlePane_recreate() {
        launchScenario()
        isScreenSw900dp(false)
        TimeUnit.SECONDS.sleep(2)
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Device Info")).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Search all")).assertIsDisplayed()
        scenario.recreate()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Device Info")).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH).onChildren()
            .filterToOne(hasTextExactly("Search all")).assertIsDisplayed()
    }

    @Test
    fun test_SearchView_nameFade_dualPane() {
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
    fun test_navigation_searchIntent() {
        launchScenario(true)
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertDoesNotExist()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_CLEAR).assertIsDisplayed()
    }

    @Test
    fun test_navigation_network() {
        launchScenario()
        composeTestRule.onNodeWithText("Network").performClick().assertIsSelected()
        composeTestRule.onNodeWithText("1").assertDoesNotExist()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertDoesNotExist()
        composeTestRule.onNodeWithText("4").assertDoesNotExist()
    }

    @Test
    fun test_navigation_software() {
        launchScenario()
        composeTestRule.onNodeWithText("Software").performClick().assertIsSelected()
        composeTestRule.onNodeWithText("1").assertDoesNotExist()
        composeTestRule.onNodeWithText("2").assertDoesNotExist()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
        composeTestRule.onNodeWithText("4").assertDoesNotExist()
    }

    @Test
    fun test_navigation_hardware() {
        launchScenario()
        composeTestRule.onNodeWithText("Hardware").performClick().assertIsSelected()
        composeTestRule.onNodeWithText("1").assertDoesNotExist()
        composeTestRule.onNodeWithText("2").assertDoesNotExist()
        composeTestRule.onNodeWithText("3").assertDoesNotExist()
        composeTestRule.onNodeWithText("4").assertIsDisplayed()
    }

    @Test
    fun test_navigation_settings() {
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
    fun test_navigation_settings_back() {
        launchScenario()
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
    fun test_navigation_search_initial() {
        launchScenario()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT).performClick()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertIsDisplayed()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_CLEAR).assertDoesNotExist()
    }

    @Test
    fun test_navigation_search_hasText() {
        launchScenario()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT).performTextInput("t")
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertDoesNotExist()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_CLEAR).assertIsDisplayed()
    }

    @Test
    fun test_navigation_search_clearText() {
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
    fun test_navigation_search_back() {
        launchScenario()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT).performTextInput("t")
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK).performClick()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_TOOLBAR).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_BOTTOM_NAV).assertIsDisplayed()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_BACK).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_CLEAR).assertDoesNotExist()
    }

    @Test
    fun test_navigation_search_clear() {
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
    fun test_search_searchHistory_displays() {
        whenever(preferenceManager.searchHistory).thenReturn(flowOf(true))
        launchScenario()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT).performTextInput("h")
        composeTestRule.onAllNodesWithText("history", substring = true).assertCountEquals(2)
    }

    @Test
    fun test_search_searchHistory_notDisplays() {
        launchScenario()
        composeTestRule.onNodeWithTag(MAIN_ACTIVITY_TEST_TAG_SEARCH_TEXT).performTextInput("h")
        composeTestRule.onAllNodesWithText("history", substring = true).assertCountEquals(0)
    }

    @Test
    fun test_appUpdate_FlexibleUpdateDownloadedSnackbar_onResume() {
        runBlocking {
            whenever(appUpdateUtils.awaitIsFlexibleUpdateDownloaded()).thenReturn(true)
        }
        launchScenario()
        composeTestRule.onNodeWithText("An update has just been downloaded.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Restart").assertIsDisplayed()
    }

    @Test
    fun test_appUpdate_FlexibleUpdateDownloadedSnackbar_onStateChange() {
        launchScenario()
        runBlocking {
            whenever(appUpdateUtils.awaitIsFlexibleUpdateDownloaded()).thenReturn(true)
        }
        installState.value = InstallState.NoError(status = InstallStatus.DOWNLOADED)
        composeTestRule.onNodeWithText("An update has just been downloaded.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Restart").assertIsDisplayed()
    }

    @Test
    fun test_appUpdate_FlexibleUpdateDownloadedSnackbar_click() {
        runBlocking {
            whenever(appUpdateUtils.awaitIsFlexibleUpdateDownloaded()).thenReturn(true)
        }
        launchScenario()
        composeTestRule.onNodeWithText("Restart").performClick()
        verify(appUpdateUtils).completeUpdate()
    }

    @Test
    fun test_appUpdate_FlexibleInstallFailedSnackbar() {
        runBlocking {
            whenever(appUpdateUtils.awaitIsFlexibleUpdateDownloaded()).thenReturn(true)
        }
        installState.value = InstallState.NoError(status = InstallStatus.FAILED)
        launchScenario()
        composeTestRule.onNodeWithText("Failed to download an update.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun test_appUpdate_FlexibleInstallFailedSnackbar_click() {
        runBlocking {
            whenever(appUpdateUtils.awaitIsFlexibleUpdateDownloaded()).thenReturn(true)
        }
        installState.value = InstallState.NoError(status = InstallStatus.FAILED)
        launchScenario()
        clearInvocations(appUpdateUtils)
        composeTestRule.onNodeWithText("Retry").performClick()
        runBlocking { verify(appUpdateUtils).checkForFlexibleUpdate(false) }
    }

    @Test
    fun test_appUpdate_FlexibleUpdateDialog() {
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
    fun test_appUpdate_FlexibleUpdateDialog_clickButton() {
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
    fun test_appUpdate_FlexibleUpdateDialog_clickOutside() {
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
            val displayMetrics = DisplayMetrics()
            it.display?.getRealMetrics(displayMetrics)
            val widthDp = displayMetrics.widthPixels / displayMetrics.density
            val heightDp = displayMetrics.heightPixels / displayMetrics.density
            with(widthDp.coerceAtMost(heightDp) >= 900) {
                if (assumeTrue) assumeTrue(this) else assumeFalse(this)
            }
        }
    }
}