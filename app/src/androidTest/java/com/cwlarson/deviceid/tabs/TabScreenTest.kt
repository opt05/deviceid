package com.cwlarson.deviceid.tabs

import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.os.bundleOf
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cwlarson.deviceid.HiltTestActivity
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.androidtestutils.assertHasLongClickAction
import com.cwlarson.deviceid.data.*
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.settings.UserPreferences
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.whenever
import javax.inject.Inject

@HiltAndroidTest
class TabScreenTest {
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

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private lateinit var dataRepository: MutableStateFlow<TabDataStatus>
    private lateinit var dataPreferences: MutableStateFlow<UserPreferences>
    private var clickedItem: Item? = null

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        dataPreferences = MutableStateFlow(UserPreferences())
        dataRepository = MutableStateFlow(TabDataStatus.Loading)
        whenever(preferenceManager.userPreferencesFlow).thenReturn(dataPreferences)
        whenever(deviceRepository.list()).thenReturn(dataRepository)
        whenever(networkRepository.list()).thenReturn(dataRepository)
        whenever(softwareRepository.list()).thenReturn(dataRepository)
        whenever(hardwareRepository.list()).thenReturn(dataRepository)
        whenever(preferenceManager.autoRefreshRate).thenReturn(flowOf(0))
        clickedItem = null
        composeTestRule.setContent {
            NavHost(navController = rememberNavController(), startDestination = "tab") {
                composable("tab") {
                    it.arguments = bundleOf("tab" to ItemType.DEVICE)
                    TabScreen(0, false, rememberScaffoldState()) { item -> clickedItem = item }
                }
            }
        }
    }

    @Test
    fun test_loading() = runBlockingTest {
        dataRepository.value = TabDataStatus.Loading

        composeTestRule.onNodeWithText("Loading…").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAB_TEST_TAG_PROGRESS).assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("No items").assertDoesNotExist()
        composeTestRule.onNodeWithText("No items").assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("Something went wrong").assertDoesNotExist()
        composeTestRule.onNodeWithText("Something went wrong").assertDoesNotExist()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST).assertDoesNotExist()
    }

    @Test
    fun test_error() = runBlockingTest {
        dataRepository.value = TabDataStatus.Error

        composeTestRule.onNodeWithText("Loading…").assertDoesNotExist()
        composeTestRule.onNodeWithTag(TAB_TEST_TAG_PROGRESS).assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("No items").assertDoesNotExist()
        composeTestRule.onNodeWithText("No items").assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST).assertDoesNotExist()

    }

    @Test
    fun test_noResults() = runBlockingTest {
        dataRepository.value = TabDataStatus.Success(emptyList())

        composeTestRule.onNodeWithText("Loading…").assertDoesNotExist()
        composeTestRule.onNodeWithTag(TAB_TEST_TAG_PROGRESS).assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("No items").assertIsDisplayed()
        composeTestRule.onNodeWithText("No items").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Something went wrong").assertDoesNotExist()
        composeTestRule.onNodeWithText("Something went wrong").assertDoesNotExist()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST).assertDoesNotExist()
    }

    @Test
    fun test_mainContent() = runBlockingTest {
        dataRepository.value = TabDataStatus.Success(
            listOf(
                Item(
                    title = R.string.app_name,
                    itemType = ItemType.DEVICE, subtitle = ItemSubtitle.Text("subtitle")
                )
            )
        )

        composeTestRule.onNodeWithText("Loading…").assertDoesNotExist()
        composeTestRule.onNodeWithTag(TAB_TEST_TAG_PROGRESS).assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("No items").assertDoesNotExist()
        composeTestRule.onNodeWithText("No items").assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("Something went wrong").assertDoesNotExist()
        composeTestRule.onNodeWithText("Something went wrong").assertDoesNotExist()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST).apply {
            assertIsDisplayed()
            onChildren().assertCountEquals(1)
        }
        composeTestRule.onNodeWithText("Device Info").assertIsDisplayed()
        composeTestRule.onNodeWithText("subtitle").assertIsDisplayed()
    }

    @Test
    fun test_mainContent_onItemClick() = runBlockingTest {
        val item = Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE, subtitle = ItemSubtitle.Text("subtitle")
        )
        dataRepository.value = TabDataStatus.Success(listOf(item))
        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasClickAction()
            .performGesture { click(topRight) }
        composeTestRule.runOnIdle { assert(clickedItem == item) }
    }

    @Test
    fun test_mainContent_onItemLongClick() = runBlockingTest {
        val item = Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE, subtitle = ItemSubtitle.Text("subtitle")
        )
        dataRepository.value = TabDataStatus.Success(listOf(item))
        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasLongClickAction()
    }
}