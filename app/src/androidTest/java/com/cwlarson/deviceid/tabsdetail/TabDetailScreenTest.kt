package com.cwlarson.deviceid.tabsdetail

import android.app.Instrumentation.ActivityResult
import android.content.Intent
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
import com.cwlarson.deviceid.ui.theme.AppTheme
import com.cwlarson.deviceid.util.DispatcherProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class TabDetailScreenTest {
    @get:Rule(order = 0)
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    @Inject
    lateinit var deviceRepository: DeviceRepository

    @Inject
    lateinit var networkRepository: NetworkRepository

    @Inject
    lateinit var softwareRepository: SoftwareRepository

    @Inject
    lateinit var hardwareRepository: HardwareRepository
    private val dispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
    private lateinit var dataRepository: MutableStateFlow<TabDetailStatus>
    private lateinit var clipboardManager: ClipboardManager

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        dispatcherProvider.provideDispatcher(dispatcher)
        dataRepository = MutableStateFlow(TabDetailStatus.Loading)
        every { deviceRepository.details(any()) } returns dataRepository
        every { networkRepository.details(any()) } returns dataRepository
        every { softwareRepository.details(any()) } returns dataRepository
        every { hardwareRepository.details(any()) } returns dataRepository
        composeTestRule.setContent {
            AppTheme {
                clipboardManager = LocalClipboardManager.current
                TabDetailScreen(
                    item = Item(
                        title = R.string.app_name,
                        itemType = ItemType.DEVICE,
                        subtitle = ItemSubtitle.Text("subtitle")
                    ), dispatcherProvider = dispatcherProvider
                )
            }
        }
    }

    @Test
    fun test_loading() = runTest(dispatcher) {
        dataRepository.value = TabDetailStatus.Loading

        composeTestRule.onNodeWithTag(TAB_DETAIL_TEST_TAG_PROGRESS).assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Something went wrong").assertDoesNotExist()
        composeTestRule.onNodeWithText("Something went wrong").assertDoesNotExist()

        composeTestRule.onNodeWithTag(TAB_DETAIL_TEST_TAG_RESULTS).assertDoesNotExist()
    }

    @Test
    fun test_error() = runTest(dispatcher) {
        dataRepository.value = TabDetailStatus.Error

        composeTestRule.onNodeWithTag(TAB_DETAIL_TEST_TAG_PROGRESS).assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()

        composeTestRule.onNodeWithTag(TAB_DETAIL_TEST_TAG_RESULTS).assertDoesNotExist()
    }

    @Test
    fun test_mainContent() = runTest(dispatcher) {
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
    fun test_mainContent_share_click() = runTest(dispatcher) {
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
    fun test_mainContent_copy_click() = runTest(dispatcher) {
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