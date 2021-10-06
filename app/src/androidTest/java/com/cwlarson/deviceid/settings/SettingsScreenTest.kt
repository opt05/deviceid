package com.cwlarson.deviceid.settings

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.cwlarson.deviceid.HiltTestActivity
import com.cwlarson.deviceid.util.AppUpdateUtils
import com.cwlarson.deviceid.util.InstallState
import com.cwlarson.deviceid.util.UpdateState
import com.google.android.play.core.install.model.InstallStatus
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import javax.inject.Inject

@HiltAndroidTest
class SettingsScreenTest {
    @get:Rule(order = 0)
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var appUpdateUtils: AppUpdateUtils

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private lateinit var dataPreferences: MutableStateFlow<UserPreferences>
    private lateinit var dataUpdateState: MutableStateFlow<UpdateState>
    private lateinit var dataInstallState: MutableStateFlow<InstallState>

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        dataPreferences = MutableStateFlow(UserPreferences())
        dataUpdateState = MutableStateFlow(UpdateState.Initial)
        dataInstallState = MutableStateFlow(InstallState.Initial)
        whenever(preferenceManager.userPreferencesFlow).thenReturn(dataPreferences)
        whenever(appUpdateUtils.updateState).thenReturn(dataUpdateState)
        whenever(appUpdateUtils.installState).thenReturn(dataInstallState)
        composeTestRule.setContent { SettingsScreen(appUpdateUtils = appUpdateUtils) }
    }

    //TODO Icon checking...
    @Test
    fun test_initial() = runBlockingTest {
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
    fun test_hideUnavailable_on() = runBlockingTest {
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
    fun test_hideUnavailable_click() = runBlockingTest {
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_HIDE_UNAVAILABLE).performClick()
        verify(preferenceManager).hideUnavailable(true)
    }

    @Test
    fun test_autoRefresh_above0() = runBlockingTest {
        dataPreferences.value = UserPreferences(autoRefreshRate = 10)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_AUTO_REFRESH).apply {
            onChildren().filterToOne(hasProgressBarRangeInfo(ProgressBarRangeInfo(1f, 0f..1f, 10))).assertIsDisplayed()
            onChildren().filterToOne(hasText("10")).assertIsDisplayed()
        }
    }

    @Test
    fun test_autoRefresh_click() = runBlockingTest {
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_AUTO_REFRESH).onChildren()
            .filterToOne(hasProgressBarRangeInfo(ProgressBarRangeInfo(0f, 0f..1f, 10)))
            .performSemanticsAction(SemanticsActions.SetProgress) { it(0.7f) }
        verify(preferenceManager).autoRefreshRate(7)
    }

    @Test
    fun test_theme_light() = runBlockingTest {
        dataPreferences.value = UserPreferences(darkTheme = "mode_off")
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_THEME)
            .assertTextEquals("Choose theme", "Light")
    }

    @Test
    fun test_theme_dark() = runBlockingTest {
        dataPreferences.value = UserPreferences(darkTheme = "mode_on")
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_THEME)
            .assertTextEquals("Choose theme", "Dark")
    }

    @Test
    fun test_theme_click_displaysDialog() = runBlockingTest {
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_THEME).performClick()
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_DIALOG).assertIsDisplayed()
            .onChildren().filterToOne(hasText("Choose theme")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_DIALOG_LIST).assertIsDisplayed().apply {
            onChildren().filterToOne(hasText("Light")).assertIsNotSelected()
            onChildren().filterToOne(hasText("Dark")).assertIsNotSelected()
            onChildren().filterToOne(hasText("System default")).assertIsSelected()
        }
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_DIALOG_BUTTON_CANCEL).assertIsDisplayed()
    }

    @Test
    fun test_theme_click_dialogItem() = runBlockingTest {
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_THEME).performClick()
        composeTestRule.onNodeWithText("Dark").performClick()
        verify(preferenceManager).setDarkTheme("mode_on")
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_DIALOG).assertDoesNotExist()
    }

    @Test
    fun test_theme_click_dialogCancel() = runBlockingTest {
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_THEME).performClick()
        reset(preferenceManager)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_DIALOG_BUTTON_CANCEL).performClick()
        verifyNoMoreInteractions(preferenceManager)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_DIALOG).assertDoesNotExist()
    }

    @Test
    fun test_searchHistory_on() = runBlockingTest {
        dataPreferences.value = UserPreferences(searchHistory = true)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_SEARCH_HISTORY).apply {
            assertTextEquals("Search history", "Saving history (turning off clears history)")
            onChildren().filterToOne(isToggleable()).assertIsOn()
        }
    }

    @Test
    fun test_searchHistory_click() = runBlockingTest {
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_SEARCH_HISTORY).performClick()
        verify(preferenceManager).searchHistory(true)
    }

    @Test
    fun test_appUpdate_updateAvailable_yes() = runBlockingTest {
        dataUpdateState.value = UpdateState.Yes(mock(), true)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update available", "Check for app update")
    }

    @Test
    fun test_appUpdate_updateAvailable_yesNoAllowed() = runBlockingTest {
        dataUpdateState.value = UpdateState.YesButNotAllowed
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update available", "Check for app update")
    }

    @Test
    fun test_appUpdate_updateAvailable_other() = runBlockingTest {
        dataUpdateState.value = UpdateState.Checking
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Check for app update")
    }

    @Test
    fun test_appUpdate_installFailed() = runBlockingTest {
        dataInstallState.value = InstallState.Failed("message", true)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Check for app update failed!")
    }

    @Test
    fun test_appUpdate_installNoError_canceled() = runBlockingTest {
        dataInstallState.value = InstallState.NoError(InstallStatus.CANCELED)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Canceled")
    }

    @Test
    fun test_appUpdate_installNoError_downloading() = runBlockingTest {
        dataInstallState.value = InstallState.NoError(InstallStatus.DOWNLOADING)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Downloading…")
    }

    @Test
    fun test_appUpdate_installNoError_installed() = runBlockingTest {
        dataInstallState.value = InstallState.NoError(InstallStatus.INSTALLED)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Installed!")
    }

    @Test
    fun test_appUpdate_installNoError_installing() = runBlockingTest {
        dataInstallState.value = InstallState.NoError(InstallStatus.INSTALLING)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Installing…")
    }

    @Test
    fun test_appUpdate_installNoError_pending() = runBlockingTest {
        dataInstallState.value = InstallState.NoError(InstallStatus.PENDING)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Pending…")
    }

    @Test
    fun test_appUpdate_installNoError_unknown() = runBlockingTest {
        dataInstallState.value = InstallState.NoError(InstallStatus.UNKNOWN)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Unknown")
    }

    @Test
    fun test_appUpdate_installNoError_downloaded() = runBlockingTest {
        dataInstallState.value = InstallState.NoError(InstallStatus.DOWNLOADED)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals(
                "App update not available",
                "Downloaded - pending restart of app"
            )
    }

    @Test
    fun test_appUpdate_installNoError_failed() = runBlockingTest {
        dataInstallState.value = InstallState.NoError(InstallStatus.FAILED)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Check for app update failed!")
    }

    @Test
    fun test_appUpdate_installNoError_other() = runBlockingTest {
        dataInstallState.value = InstallState.NoError(Int.MAX_VALUE)
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE)
            .assertTextEquals("App update not available", "Check for app update")
    }

    @Test
    fun test_appUpdate_click() = runBlockingTest {
        composeTestRule.onNodeWithTag(SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE).performClick()
        verify(appUpdateUtils).checkForFlexibleUpdate(true)
    }
}