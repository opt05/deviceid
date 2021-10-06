package com.cwlarson.deviceid.search

import androidx.compose.material.rememberScaffoldState
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
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import javax.inject.Inject

@HiltAndroidTest
class SearchScreenTest {
    @get:Rule(order = 0)
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var repository: AllRepository

    @Inject
    lateinit var preferenceManager: PreferenceManager
    private lateinit var data: MutableStateFlow<TabDataStatus>
    private var clickedItem: Item? = null

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        data = MutableStateFlow(TabDataStatus.Loading)
        whenever(repository.search(any())).thenReturn(data)
        clickedItem = null
        composeTestRule.setContent {
            SearchScreen(
                appBarSize = 0, query = "test",
                scaffoldState = rememberScaffoldState()
            ) { clickedItem = it }
        }
    }

    @Test
    fun test_loading() = runBlockingTest {
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
    fun test_error() = runBlockingTest {
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
    fun test_noResults() = runBlockingTest {
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
    fun test_mainContent() = runBlockingTest {
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
    fun test_mainContent_onItemClick() = runBlockingTest {
        val item = Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE, subtitle = ItemSubtitle.Text("subtitle")
        )
        data.value = TabDataStatus.Success(listOf(item))
        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasClickAction().performClick()
        composeTestRule.runOnIdle { assert(clickedItem == item) }
    }

    @Test
    fun test_mainContent_onItemLongClick() = runBlockingTest {
        val item = Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE, subtitle = ItemSubtitle.Text("subtitle")
        )
        data.value = TabDataStatus.Success(listOf(item))
        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasLongClickAction()
    }
}