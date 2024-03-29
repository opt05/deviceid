package com.cwlarson.deviceid.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.androidtestutils.assertHasLongClickAction
import com.cwlarson.deviceid.androidtestutils.assertHasNoLongClickAction
import com.cwlarson.deviceid.androidtestutils.performLongClick
import com.cwlarson.deviceid.ui.theme.AppTheme
import com.cwlarson.deviceid.ui.util.LIST_ITEM_TEST_TAG_ICON
import com.cwlarson.deviceid.ui.util.LIST_ITEM_TEST_TAG_PROGRESS
import com.cwlarson.deviceid.util.AppPermission
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ItemListItemTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var clipboardManager: ClipboardManager

    private fun Item.setContent(onItemClick: (() -> Unit)? = null) {
        composeTestRule.setContent {
            AppTheme {
                clipboardManager = LocalClipboardManager.current
                ItemListItem(item = this) { onItemClick?.invoke() }
            }
        }
    }

    @Test
    fun test_ItemSubtitle_Text_null() = runTest {
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.Text(null)
        ).setContent()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM)
            .assertTextEquals("Device Info", "Unable to determine")
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_ICON, true).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_PROGRESS, true).assertDoesNotExist()
    }

    @Test
    fun test_ItemSubtitle_Text_blank() = runTest {
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.Text("")
        ).setContent()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM)
            .assertTextEquals("Device Info", "Unable to determine")
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_ICON, true).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_PROGRESS, true).assertDoesNotExist()
    }

    @Test
    fun test_ItemSubtitle_Text_nonnull() = runTest {
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.Text("subtitle")
        ).setContent()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM)
            .assertTextEquals("Device Info", "subtitle")
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_ICON, true).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_PROGRESS, true).assertDoesNotExist()
    }

    @Test
    fun test_ItemSubtitle_Text_click() = runTest {
        var clicked = false
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.Text("subtitle")
        ).setContent { clicked = true }

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasClickAction().performClick()
        assert(clicked)
    }

    @Test
    fun test_ItemSubtitle_Text_longClick() = runTest {
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.Text("subtitle")
        ).setContent()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasLongClickAction()
            .performLongClick()
        assert(clipboardManager.getText()?.text == "subtitle")
    }

    @Test
    fun test_ItemSubtitle_Chart_null() = runTest {
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.Chart(
                ChartItem(
                    0f, 0f, Icons.Default.Android, null
                )
            )
        ).setContent()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM)
            .assertTextEquals("Device Info", "Unable to determine")
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_ICON, true).assertIsDisplayed()
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_PROGRESS, true).assertDoesNotExist()
    }

    @Test
    fun test_ItemSubtitle_Chart_blank() = runTest {
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.Chart(
                ChartItem(
                    0f, 0f, Icons.Default.Android, ""
                )
            )
        ).setContent()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM)
            .assertTextEquals("Device Info", "Unable to determine")
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_ICON, true).assertIsDisplayed()
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_PROGRESS, true).assertDoesNotExist()
    }

    @Test
    fun test_ItemSubtitle_Chart_nonnull() = runTest {
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.Chart(
                ChartItem(
                    100f, 50f, Icons.Default.Android, "subtitle"
                )
            )
        ).setContent()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM)
            .assertTextEquals("Device Info", "subtitle")
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_ICON, true).assertIsDisplayed()
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_PROGRESS, true).apply {
            assertIsDisplayed()
            assertRangeInfoEquals(ProgressBarRangeInfo(0.5f, 0f..1f, 0))
        }
    }

    @Test
    fun test_ItemSubtitle_Chart_click() = runTest {
        var clicked = false
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.Chart(
                ChartItem(
                    100f, 50f, Icons.Default.Android, "subtitle"
                )
            )
        ).setContent { clicked = true }

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasClickAction().performClick()
        assert(clicked)
    }

    @Test
    fun test_ItemSubtitle_Chart_longClick() = runTest {
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.Chart(
                ChartItem(
                    100f, 50f, Icons.Default.Android, "subtitle"
                )
            )
        ).setContent()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasLongClickAction()
            .performLongClick()
        assert(clipboardManager.getText()?.text == "subtitle")
    }

    @Test
    fun test_ItemSubtitle_NoLongerPossible() = runTest {
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.NoLongerPossible(1)
        ).setContent()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM)
            .assertTextEquals("Device Info", "Not possible since Android 1.0")
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_ICON, true).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_PROGRESS, true).assertDoesNotExist()
    }

    @Test
    fun test_ItemSubtitle_NoLongerPossible_click() = runTest {
        var clicked = false
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.NoLongerPossible(1)
        ).setContent { clicked = true }

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasClickAction().performClick()
        assert(clicked)
    }

    @Test
    fun test_ItemSubtitle_NoLongerPossible_longClick() = runTest {
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.NoLongerPossible(1)
        ).setContent()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasNoLongClickAction()
    }

    @Test
    fun test_ItemSubtitle_NotPossibleYet() = runTest {
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.NotPossibleYet(1)
        ).setContent()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM)
            .assertTextEquals("Device Info", "Available starting on Android 1.0")
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_ICON, true).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_PROGRESS, true).assertDoesNotExist()
    }

    @Test
    fun test_ItemSubtitle_NotPossibleYet_click() = runTest {
        var clicked = false
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.NotPossibleYet(1)
        ).setContent { clicked = true }

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasClickAction().performClick()
        assert(clicked)
    }

    @Test
    fun test_ItemSubtitle_NotPossibleYet_longClick() = runTest {
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.NotPossibleYet(1)
        ).setContent()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasNoLongClickAction()
    }

    @Test
    fun test_ItemSubtitle_Permission() = runTest {
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.Permission(AppPermission.ReadPhoneState)
        ).setContent()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM)
            .assertTextEquals(
                "Device Info",
                "Please enable the read phone status and identity permission"
            )
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_ICON, true).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_PROGRESS, true).assertDoesNotExist()
    }

    @Test
    fun test_ItemSubtitle_Permission_click() = runTest {
        var clicked = false
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.Permission(AppPermission.ReadPhoneState)
        ).setContent { clicked = true }

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasClickAction().performClick()
        assert(clicked)
    }

    @Test
    fun test_ItemSubtitle_Permission_longClick() = runTest {
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.Permission(AppPermission.ReadPhoneState)
        ).setContent()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasNoLongClickAction()
    }

    @Test
    fun test_ItemSubtitle_other() = runTest {
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.Error
        ).setContent()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM)
            .assertTextEquals("Device Info", "Unable to determine")
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_ICON, true).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LIST_ITEM_TEST_TAG_PROGRESS, true).assertDoesNotExist()
    }

    @Test
    fun test_ItemSubtitle_other_click() = runTest {
        var clicked = false
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.Error
        ).setContent { clicked = true }

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasClickAction().performClick()
        assert(clicked)
    }

    @Test
    fun test_ItemSubtitle_other_longClick() = runTest {
        Item(
            title = R.string.app_name,
            itemType = ItemType.DEVICE,
            subtitle = ItemSubtitle.Error
        ).setContent()

        composeTestRule.onNodeWithTag(TAB_TEST_TAG_LIST_ITEM).assertHasNoLongClickAction()
    }
}