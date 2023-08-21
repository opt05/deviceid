package com.cwlarson.deviceid.search

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.cwlarson.deviceid.HiltTestActivity
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.androidtestutils.assertHasLongClickAction
import com.cwlarson.deviceid.data.AllRepository
import com.cwlarson.deviceid.data.TabDataStatus
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.tabs.TAB_TEST_TAG_LIST_ITEM
import com.cwlarson.deviceid.ui.theme.AppTheme
import com.cwlarson.deviceid.util.DispatcherProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class SearchScreenTest {
    @get:Rule(order = 0)
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    @Inject
    lateinit var repository: AllRepository
    private lateinit var data: MutableStateFlow<TabDataStatus>
    private var clickedItem: Item? = null
    private val dispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        dispatcherProvider.provideDispatcher(dispatcher)
        data = MutableStateFlow(TabDataStatus.Loading)
        every { repository.search(any()) } returns data
        clickedItem = null
        composeTestRule.setContent {
            AppTheme {
                SearchScreen(
                    appBarSize = 0, query = "test",
                    snackbarHostState = remember { SnackbarHostState() },
                    dispatcherProvider = dispatcherProvider
                ) { clickedItem = it }
            }
        }
    }

    @Test
    fun test_loading() = runTest(dispatcher) {
        data.value = TabDataStatus.Loading

        composeTestRule.onNodeWithText("Searching…").assertIsDisplayed()
        composeTestRule.onNodeWithTag(SEARCH_TEST_TAG_PROGRESS).assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("No results").assertDoesNotExist()
        composeTestRule.onNodeWithText("No results").assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("Something went wrong").assertDoesNotExist()
        composeTestRule.onNodeWithText("Something went wrong").assertDoesNotExist()

        composeTestRule.onNodeWithTag(SEARCH_TEST_TAG_LIST).assertDoesNotExist()
    }

    @Test
    fun test_error() = runTest(dispatcher) {
        data.value = TabDataStatus.Error

        composeTestRule.onNodeWithText("Searching…").assertDoesNotExist()
        composeTestRule.onNodeWithTag(SEARCH_TEST_TAG_PROGRESS).assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("No results").assertDoesNotExist()
        composeTestRule.onNodeWithText("No results").assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()

        composeTestRule.onNodeWithTag(SEARCH_TEST_TAG_LIST).assertDoesNotExist()
    }

    @Test
    fun test_noResults() = runTest(dispatcher) {
        data.value = TabDataStatus.Success(emptyList())

        composeTestRule.onNodeWithText("Searching…").assertDoesNotExist()
        composeTestRule.onNodeWithTag(SEARCH_TEST_TAG_PROGRESS).assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("No results").assertIsDisplayed()
        composeTestRule.onNodeWithText("No results").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Something went wrong").assertDoesNotExist()
        composeTestRule.onNodeWithText("Something went wrong").assertDoesNotExist()

        composeTestRule.onNodeWithTag(SEARCH_TEST_TAG_LIST).assertDoesNotExist()
    }

    @Test
    fun test_mainContent() = runTest(dispatcher) {
        data.value = TabDataStatus.Success(
            listOf(
                Item(
                    title = R.string.app_name,
                    itemType = ItemType.DEVICE, subtitle = ItemSubtitle.Text("subtitle")
                )
            )
        )

        composeTestRule.onNodeWithText("Searching…").assertDoesNotExist()
        composeTestRule.onNodeWithTag(SEARCH_TEST_TAG_PROGRESS).assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("No results").assertDoesNotExist()
        composeTestRule.onNodeWithText("No results").assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("Something went wrong").assertDoesNotExist()
        composeTestRule.onNodeWithText("Something went wrong").assertDoesNotExist()

        composeTestRule.onNodeWithTag(SEARCH_TEST_TAG_LIST).assertIsDisplayed()
        composeTestRule.onNodeWithText("RESULTS").assertIsDisplayed()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Device Info").assertIsDisplayed()
        composeTestRule.onNodeWithText("subtitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag(SEARCH_TEST_TAG_DIVIDER).assertIsDisplayed()
    }

    @Test
    fun test_mainContent_onItemClick() = runTest(dispatcher) {
        val item = Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE, subtitle = ItemSubtitle.Text("subtitle")
        )
        data.value = TabDataStatus.Success(listOf(item))
        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasClickAction().performClick()
        composeTestRule.runOnIdle { assert(clickedItem == item) }
    }

    @Test
    fun test_mainContent_onItemLongClick() = runTest(dispatcher) {
        val item = Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE, subtitle = ItemSubtitle.Text("subtitle")
        )
        data.value = TabDataStatus.Success(listOf(item))
        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasLongClickAction()
    }
}