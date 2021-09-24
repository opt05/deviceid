package com.cwlarson.deviceid.tabsdetail

import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import com.cwlarson.deviceid.HiltTestActivity
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.androidtestutils.intentsSafeRelease
import com.cwlarson.deviceid.data.*
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
class TabDetailScreenTest {
    @get:Rule(order = 0)
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var deviceRepository: DeviceRepository

    @Inject
    lateinit var networkRepository: NetworkRepository

    @Inject
    lateinit var softwareRepository: SoftwareRepository

    @Inject
    lateinit var hardwareRepository: HardwareRepository
    private lateinit var dataRepository: MutableStateFlow<TabDetailStatus>
    private lateinit var clipboardManager: ClipboardManager

    @ExperimentalPermissionsApi
    @ExperimentalFoundationApi
    @Before
    fun setup() {
        hiltAndroidRule.inject()
        dataRepository = MutableStateFlow(TabDetailStatus.Loading)
        whenever(deviceRepository.details(any())).thenReturn(dataRepository)
        whenever(networkRepository.details(any())).thenReturn(dataRepository)
        whenever(softwareRepository.details(any())).thenReturn(dataRepository)
        whenever(hardwareRepository.details(any())).thenReturn(dataRepository)
        composeTestRule.setContent {
            clipboardManager = LocalClipboardManager.current
            TabDetailScreen(
                Item(
                    title = R.string.app_name,
                    itemType = ItemType.DEVICE,
                    subtitle = ItemSubtitle.Text("subtitle")
                )
            )
        }
    }

    @Test
    fun test_loading() = runBlockingTest {
        dataRepository.value = TabDetailStatus.Loading

        composeTestRule.onNodeWithTag(TAB_DETAIL_TEST_TAG_PROGRESS).assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Something went wrong").assertDoesNotExist()
        composeTestRule.onNodeWithText("Something went wrong").assertDoesNotExist()

        composeTestRule.onNodeWithTag(TAB_DETAIL_TEST_TAG_RESULTS).assertDoesNotExist()
    }

    @Test
    fun test_error() = runBlockingTest {
        dataRepository.value = TabDetailStatus.Error

        composeTestRule.onNodeWithTag(TAB_DETAIL_TEST_TAG_PROGRESS).assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()

        composeTestRule.onNodeWithTag(TAB_DETAIL_TEST_TAG_RESULTS).assertDoesNotExist()
    }

    @Test
    fun test_mainContent() = runBlockingTest {
        dataRepository.value = TabDetailStatus.Success(
            Item(
                title = R.string.app_name,
                itemType = ItemType.DEVICE,
                subtitle = ItemSubtitle.Text("subtitle")
            )
        )

        composeTestRule.onNodeWithTag(TAB_DETAIL_TEST_TAG_PROGRESS).assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("Something went wrong").assertDoesNotExist()
        composeTestRule.onNodeWithText("Something went wrong").assertDoesNotExist()

        composeTestRule.onNodeWithTag(TAB_DETAIL_TEST_TAG_RESULTS).assertIsDisplayed()
        composeTestRule.onNodeWithText("Device Info").assertIsDisplayed()
        composeTestRule.onNodeWithText("subtitle").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Copy").assertIsDisplayed()
    }

    @Test
    fun test_mainContent_share_click() = runBlockingTest {
        dataRepository.value = TabDetailStatus.Success(
            Item(
                title = R.string.app_name,
                itemType = ItemType.DEVICE,
                subtitle = ItemSubtitle.Text("subtitle")
            )
        )
        intentsSafeRelease {
            intending(anyIntent()).respondWith(ActivityResult(0, null))
            composeTestRule.onNodeWithContentDescription("Share").assertHasClickAction().performClick()
            intended(allOf(hasAction(Intent.ACTION_CHOOSER), hasExtraWithKey(Intent.EXTRA_INTENT)))
        }
    }

    @Test
    fun test_mainContent_copy_click() = runBlockingTest {
        dataRepository.value = TabDetailStatus.Success(
            Item(
                title = R.string.app_name,
                itemType = ItemType.DEVICE,
                subtitle = ItemSubtitle.Text("subtitle")
            )
        )
        composeTestRule.onNodeWithContentDescription("Copy").assertHasClickAction().performClick()
        assert(clipboardManager.getText()?.text == "subtitle")
    }
}