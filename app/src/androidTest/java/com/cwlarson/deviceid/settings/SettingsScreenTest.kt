package com.cwlarson.deviceid.settings

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.cwlarson.deviceid.HiltTestActivity
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.ui.theme.AppTheme
import com.cwlarson.deviceid.util.AppUpdateUtils
import com.cwlarson.deviceid.util.DispatcherProvider
import com.cwlarson.deviceid.util.InstallState
import com.cwlarson.deviceid.util.UpdateState
import com.google.android.play.core.install.model.InstallStatus
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class SettingsScreenTest {
    @get:Rule(order = 0)
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    @Inject
    lateinit var appUpdateUtils: AppUpdateUtils

    @Inject
    lateinit var preferenceManager: PreferenceManager
    private val dispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())

    private lateinit var dataPreferences: MutableStateFlow<UserPreferences>
    private lateinit var dataUpdateState: MutableStateFlow<UpdateState>
    private lateinit var dataInstallState: MutableStateFlow<InstallState>

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        dispatcherProvider.provideDispatcher(dispatcher)
        dataPreferences = MutableStateFlow(UserPreferences())
        dataUpdateState = MutableStateFlow(UpdateState.Initial)
        dataInstallState = MutableStateFlow(InstallState.Initial)
        every { preferenceManager.userPreferencesFlow } returns dataPreferences
        every { appUpdateUtils.updateState } returns dataUpdateState
        every { appUpdateUtils.installState } returns dataInstallState
        composeTestRule.setContent { AppTheme { SettingsScreen(appUpdateUtils = appUpdateUtils) } }
    }

    //TODO Icon checking...
    @Test
    fun test_initial() = runTest(dispatcher) {
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_TITLE_APP_SETTINGS)
            .assertIsDisplayed().assertHasNoClickAction()
            .onChildren().filterToOne(hasText("App Settings")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_HIDE_UNAVAILABLE)
            .assertIsDisplayed().assertHasClickAction().apply {
                assertTextEquals(
                    "Hide not applicable",
                    "Showing items not applicable to this device"
                )
                onChildren().filterToOne(isToggleable()).assertIsOff()
            }
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_AUTO_REFRESH)
            .assertIsDisplayed().assertHasNoClickAction().apply {
                onChildren().filterToOne(hasText("Auto refresh rate")).assertIsDisplayed()
                onChildren().filterToOne(hasText("In seconds, how often to auto refresh items")).assertIsDisplayed()
                onChildren().filterToOne(hasProgressBarRangeInfo(ProgressBarRangeInfo(0f, 0f..1f, 10))).assertIsDisplayed()
                onChildren().filterToOne(hasText("off")).assertIsDisplayed()
            }
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_THEME)
            .assertIsDisplayed().assertHasClickAction()
            .assertTextEquals("Choose theme", "System default")
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_SEARCH_HISTORY)
            .assertIsDisplayed().assertHasClickAction().apply {
                assertTextEquals("Search history", "Not saving history")
                onChildren().filterToOne(isToggleable()).assertIsOff()
            }
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertIsDisplayed().assertHasClickAction()
            .assertTextEquals("App update not available", "Check for app update")
    }

    @Test
    fun test_hideUnavailable_on() = runTest(dispatcher) {
        dataPreferences.value = UserPreferences(hideUnavailable = true)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_HIDE_UNAVAILABLE).apply {
            assertTextEquals(
                "Hide not applicable",
                "Hiding items not applicable to this device"
            )
            onChildren().filterToOne(isToggleable()).assertIsOn()
        }
    }

    @Test
    fun test_hideUnavailable_click() = runTest(dispatcher) {
        coJustRun { preferenceManager.hideUnavailable(any()) }
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_HIDE_UNAVAILABLE).performClick()
        coVerify { preferenceManager.hideUnavailable(true) }
    }

    @Test
    fun test_autoRefresh_above0() = runTest(dispatcher) {
        dataPreferences.value = UserPreferences(autoRefreshRate = 10)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_AUTO_REFRESH).apply {
            onChildren().filterToOne(hasProgressBarRangeInfo(ProgressBarRangeInfo(1f, 0f..1f, 10))).assertIsDisplayed()
            onChildren().filterToOne(hasText("10")).assertIsDisplayed()
        }
    }

    @Test
    fun test_autoRefresh_click() = runTest(dispatcher) {
        coJustRun { preferenceManager.autoRefreshRate(any()) }
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_AUTO_REFRESH).onChildren()
            .filterToOne(hasProgressBarRangeInfo(ProgressBarRangeInfo(0f, 0f..1f, 10)))
            .performSemanticsAction(SemanticsActions.SetProgress) { it(0.7f) }
        coVerify { preferenceManager.autoRefreshRate(7) }
    }

    @Test
    fun test_theme_light() = runTest(dispatcher) {
        dataPreferences.value = UserPreferences(darkTheme = "mode_off")
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_THEME)
            .assertTextEquals("Choose theme", "Light")
    }

    @Test
    fun test_theme_dark() = runTest(dispatcher) {
        dataPreferences.value = UserPreferences(darkTheme = "mode_on")
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_THEME)
            .assertTextEquals("Choose theme", "Dark")
    }

    @Test
    fun test_theme_click_displaysDialog() = runTest(dispatcher) {
        val array = composeTestRule.activity.resources.getStringArray(R.array.pref_daynight_mode_entries)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_THEME).performClick()
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_DIALOG).assertIsDisplayed()
            .onChildren().filterToOne(hasText("Choose theme")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_DIALOG_LIST).assertIsDisplayed().apply {
            onChildren().filterToOne(hasText(array[0])).assertIsNotSelected()
            onChildren().filterToOne(hasText(array[1])).assertIsNotSelected()
            onChildren().filterToOne(hasText(array[2])).assertIsSelected()
        }
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_DIALOG_BUTTON_CANCEL).assertIsDisplayed()
    }

    @Test
    fun test_theme_click_dialogItem() = runTest(dispatcher) {
        coJustRun { preferenceManager.setDarkTheme(any()) }
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_THEME).performClick()
        composeTestRule.onNodeWithText("Dark").performClick()
        coVerify { preferenceManager.setDarkTheme("mode_on") }
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_DIALOG).assertDoesNotExist()
    }

    @Test
    fun test_theme_click_dialogCancel() = runTest(dispatcher) {
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_THEME).performClick()
        clearMocks(preferenceManager)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_DIALOG_BUTTON_CANCEL).performClick()
        verify { preferenceManager wasNot Called }
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_DIALOG).assertDoesNotExist()
    }

    @Test
    fun test_searchHistory_on() = runTest(dispatcher) {
        dataPreferences.value = UserPreferences(searchHistory = true)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_SEARCH_HISTORY).apply {
            assertTextEquals("Search history", "Saving history (turning off clears history)")
            onChildren().filterToOne(isToggleable()).assertIsOn()
        }
    }

    @Test
    fun test_searchHistory_click() = runTest(dispatcher) {
        coJustRun { preferenceManager.searchHistory(any()) }
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_SEARCH_HISTORY).performClick()
        coVerify { preferenceManager.searchHistory(true) }
    }

    @Test
    fun test_appUpdate_updateAvailable_yes() = runTest(dispatcher) {
        dataUpdateState.value = UpdateState.Yes(mockk(), true)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update available", "Check for app update")
    }

    @Test
    fun test_appUpdate_updateAvailable_yesNoAllowed() = runTest(dispatcher) {
        dataUpdateState.value = UpdateState.YesButNotAllowed
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update available", "Check for app update")
    }

    @Test
    fun test_appUpdate_updateAvailable_other() = runTest(dispatcher) {
        dataUpdateState.value = UpdateState.Checking
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Check for app update")
    }

    @Test
    fun test_appUpdate_installFailed() = runTest(dispatcher) {
        dataInstallState.value = InstallState.Failed("message", true)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Check for app update failed!")
    }

    @Test
    fun test_appUpdate_installNoError_canceled() = runTest(dispatcher) {
        dataInstallState.value = InstallState.NoError(InstallStatus.CANCELED)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Canceled")
    }

    @Test
    fun test_appUpdate_installNoError_downloading() = runTest(dispatcher) {
        dataInstallState.value = InstallState.NoError(InstallStatus.DOWNLOADING)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Downloading…")
    }

    @Test
    fun test_appUpdate_installNoError_installed() = runTest(dispatcher) {
        dataInstallState.value = InstallState.NoError(InstallStatus.INSTALLED)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Installed!")
    }

    @Test
    fun test_appUpdate_installNoError_installing() = runTest(dispatcher) {
        dataInstallState.value = InstallState.NoError(InstallStatus.INSTALLING)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Installing…")
    }

    @Test
    fun test_appUpdate_installNoError_pending() = runTest(dispatcher) {
        dataInstallState.value = InstallState.NoError(InstallStatus.PENDING)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Pending…")
    }

    @Test
    fun test_appUpdate_installNoError_unknown() = runTest(dispatcher) {
        dataInstallState.value = InstallState.NoError(InstallStatus.UNKNOWN)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Unknown")
    }

    @Test
    fun test_appUpdate_installNoError_downloaded() = runTest(dispatcher) {
        dataInstallState.value = InstallState.NoError(InstallStatus.DOWNLOADED)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals(
                "App update not available",
                "Downloaded - pending restart of app"
            )
    }

    @Test
    fun test_appUpdate_installNoError_failed() = runTest(dispatcher) {
        dataInstallState.value = InstallState.NoError(InstallStatus.FAILED)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Check for app update failed!")
    }

    @Test
    fun test_appUpdate_installNoError_other() = runTest(dispatcher) {
        dataInstallState.value = InstallState.NoError(Int.MAX_VALUE)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Check for app update")
    }

    @Test
    fun test_appUpdate_click() = runTest(dispatcher) {
        coJustRun { appUpdateUtils.checkForFlexibleUpdate(any()) }
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE).performClick()
        coVerify { appUpdateUtils.checkForFlexibleUpdate(true) }
    }
}