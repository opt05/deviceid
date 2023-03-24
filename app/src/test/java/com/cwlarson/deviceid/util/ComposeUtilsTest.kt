package com.cwlarson.deviceid.util

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.testutils.CoroutineTestRule
import com.cwlarson.deviceid.ui.util.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
@Config(
    instrumentedPackages = [
        // open issue: https://github.com/robolectric/robolectric/issues/6593
        // required to access final members on androidx.loader.content.ModernAsyncTask
        "androidx.loader.content"
    ]
)
class ComposeUtilsTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var context: Application
    private lateinit var dispatcherProvider: DispatcherProvider


    @Before
    fun setup() {
        context = spyk(ApplicationProvider.getApplicationContext() as Application)
        dispatcherProvider = DispatcherProvider.provideDispatcher(coroutineRule.dispatcher)
    }

    @Test
    fun test_loadPermissionLabel_success() {
        val pi: PermissionInfo = mockk()
        val pm: PackageManager = mockk()
        every { context.packageManager } returns pm
        every { pm.getPermissionInfo(any(), any()) } returns pi
        every { pi.loadLabel(pm) } returns "test"
        composeTestRule.setContent {
            assertEquals("test", AppPermission.ReadPhoneState.loadPermissionLabel(context))
        }
    }

    @Test
    fun test_loadPermissionLabel_fail() {
        every { context.packageManager} throws NullPointerException()
        composeTestRule.setContent {
            assertEquals(
                "Something went wrong",
                AppPermission.ReadPhoneState.loadPermissionLabel(context)
            )
        }
    }

    @Test
    fun test_IntentHandler_init() {
        val activity: ComponentActivity = mockk()
        val lifecycle: Lifecycle = mockk()
        every { activity.lifecycle } returns lifecycle
        justRun { lifecycle.addObserver(any()) }
        IntentHandler(activity)
        verify { lifecycle.addObserver(any()) }
    }

    @Test
    fun test_IntentHandler_onCreate_intent() {
        val intent = composeTestRule.activity.intent
        val handler = composeTestRule.runOnUiThread { IntentHandler(composeTestRule.activity) }
        composeTestRule.setContent { handler.OnIntent { assertEquals(intent, it) } }
    }

    @Test
    fun test_IntentHandler_onCreate_intent_multiple() {
        val intent = composeTestRule.activity.intent
        val handler = composeTestRule.runOnUiThread { IntentHandler(composeTestRule.activity) }
        composeTestRule.activity.intent = intent.apply { putExtra(Intent.EXTRA_TEXT, "test") }
        composeTestRule.runOnUiThread {
            handler.onDestroy(composeTestRule.activity)
            handler.onCreate(composeTestRule.activity)
        }
        composeTestRule.setContent { handler.OnIntent { assertEquals(intent, it) } }
    }

    @Test
    fun test_IntentHandler_onNewIntent() = runTest {
        val intent = composeTestRule.activity.intent
            .apply { putExtra(Intent.EXTRA_TEXT, "test") }
        val handler = composeTestRule.runOnUiThread { IntentHandler(composeTestRule.activity) }
        handler.onNewIntent(dispatcherProvider, intent)
        composeTestRule.setContent { handler.OnIntent { assertEquals(intent, it) } }
    }

    @Test
    fun test_copyItemToClipboard_null() {
        composeTestRule.setContent {
            assertNull(
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text(null))
                    .copyItemToClipboard()
            )
        }
    }

    @Test
    fun test_copyItemToClipboard_blank() {
        composeTestRule.setContent {
            assertNull(
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text(""))
                    .copyItemToClipboard()
            )
        }
    }

    @Test
    fun test_copyItemToClipboard_nonnull() {
        composeTestRule.setContent {
            assertNotNull(
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("Name"))
                    .copyItemToClipboard()
            )
        }
    }

    @Test
    fun test_copyItemToClipboard_click_null() {
        composeTestRule.setContent {
            val clipboardManager = LocalClipboardManager.current
            Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Error)
                .copyItemToClipboard()?.invoke()
            assertNull(clipboardManager.getText()?.text)
        }
        assertTrue(ShadowToast.shownToastCount() == 0)
    }

    @Test
    fun test_copyItemToClipboard_click_blank() {
        composeTestRule.setContent {
            val clipboardManager = LocalClipboardManager.current
            Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text(""))
                .copyItemToClipboard()?.invoke()
            assertNull(clipboardManager.getText()?.text)
        }
        assertTrue(ShadowToast.shownToastCount() == 0)
    }

    @Test
    fun test_copyItemToClipboard_click_nonnull() {
        composeTestRule.setContent {
            val clipboardManager = LocalClipboardManager.current
            Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("Name"))
                .copyItemToClipboard()?.invoke()
            assertEquals("Name", clipboardManager.getText()?.text)
        }
        assertTrue(ShadowToast.showedToast("Copied Device Info to clipboard!"))
    }

    @Test
    fun test_share_success() {
        val pm: PackageManager = mockk()
        mockkConstructor(Intent::class)
        every {
            constructedWith<Intent>(EqMatcher(Intent.ACTION_SEND)).resolveActivity(pm)
        } returns mockk()
        every { context.packageManager } returns pm
        every { context.getString(R.string.send_to) } returns "test"
        every { context.getString(R.string.send_to_no_apps) } returns "No app available"
        mockkStatic(Intent::class)
        every { Intent.createChooser(any(), null) } returns mockk()
        justRun { context.startActivity(any()) }
        composeTestRule.setContent {
            Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("Name"))
                .share(context)()
        }
        verify { constructedWith<Intent>(EqMatcher(Intent.ACTION_SEND)).type = "text/plain" }
        verify {
            constructedWith<Intent>(EqMatcher(Intent.ACTION_SEND)).putExtra(
                Intent.EXTRA_TITLE, "test"
            )
        }
        verify {
            constructedWith<Intent>(EqMatcher(Intent.ACTION_SEND)).putExtra(
                Intent.EXTRA_TEXT, "Name"
            )
        }
        verify { context.startActivity(any()) }
        assertFalse(ShadowToast.showedToast("No app available"))
    }

    @Test
    fun test_share_failure() {
        val pm: PackageManager = mockk()
        mockkConstructor(Intent::class)
        every {
            constructedWith<Intent>(EqMatcher(Intent.ACTION_SEND)).resolveActivity(pm)
        } returns null
        every { context.packageManager } returns pm
        every { context.getString(R.string.send_to) } returns "test"
        every { context.getString(R.string.send_to_no_apps) } returns "No app available"
        mockkStatic(Intent::class)
        every { Intent.createChooser(any(), null) } returns null
        justRun { context.startActivity(any()) }
        composeTestRule.setContent {
            Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("Name"))
                .share(context)()
        }
        verify { constructedWith<Intent>(EqMatcher(Intent.ACTION_SEND)).type = "text/plain" }
        verify {
            constructedWith<Intent>(EqMatcher(Intent.ACTION_SEND)).putExtra(
                Intent.EXTRA_TITLE, "test"
            )
        }
        verify {
            constructedWith<Intent>(EqMatcher(Intent.ACTION_SEND)).putExtra(
                Intent.EXTRA_TEXT, "Name"
            )
        }
        verify(inverse = true) { context.startActivity(any()) }
        assertTrue(ShadowToast.showedToast("No app available"))
    }

    @Test
    fun test_click_permission_hasPermission() {
        val clickedRefresh: () -> Unit = mockk(relaxed = true)
        val clickedDetails: (Item) -> Unit = mockk()
        shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
        composeTestRule.setContent {
            ComposableUnderTest(
                Item(
                    R.string.app_name, ItemType.DEVICE,
                    ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                ), clickedRefresh = clickedRefresh, clickedDetails = clickedDetails
            )
        }
        verify { clickedRefresh() }
        verify { clickedDetails wasNot Called }
    }

    @Test
    fun test_click_permission_shouldShowRationale() {
        val clickedRefresh: () -> Unit = mockk()
        val clickedDetails: (Item) -> Unit = mockk()
        shadowOf(context).denyPermissions(Manifest.permission.READ_PHONE_STATE)
        shadowOf(context.packageManager)
            .setShouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE, true)
        composeTestRule.setContent {
            ComposableUnderTest(
                Item(
                    R.string.app_name, ItemType.DEVICE,
                    ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                ), clickedRefresh = clickedRefresh, clickedDetails = clickedDetails
            )
        }
        composeTestRule.onNodeWithText("permission is required to display", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        verify { clickedRefresh wasNot Called }
        verify { clickedDetails wasNot Called }
    }

    @Test
    fun test_click_permission_shouldShowRationale_clickRetry() {
        val clickedRefresh: () -> Unit = mockk()
        val clickedDetails: (Item) -> Unit = mockk()
        shadowOf(context).denyPermissions(Manifest.permission.READ_PHONE_STATE)
        shadowOf(context.packageManager)
            .setShouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE, true)
        composeTestRule.setContent {
            ComposableUnderTest(
                Item(
                    R.string.app_name, ItemType.DEVICE,
                    ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                ), clickedRefresh = clickedRefresh, clickedDetails = clickedDetails
            )
        }
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed().performClick()
        assertEquals(
            Manifest.permission.READ_PHONE_STATE,
            shadowOf(composeTestRule.activity).lastRequestedPermission.requestedPermissions[0]
        )
        verify { clickedRefresh wasNot Called }
        verify { clickedDetails wasNot Called }
    }

    @Test
    fun test_click_permission_permissionRequested() {
        val clickedRefresh: () -> Unit = mockk()
        val clickedDetails: (Item) -> Unit = mockk()
        shadowOf(context).denyPermissions(Manifest.permission.READ_PHONE_STATE)
        composeTestRule.setContent {
            ComposableUnderTest(
                Item(
                    R.string.app_name, ItemType.DEVICE,
                    ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                ), clickedRefresh = clickedRefresh, clickedDetails = clickedDetails
            )
        }
        assertEquals(
            Manifest.permission.READ_PHONE_STATE,
            shadowOf(composeTestRule.activity).lastRequestedPermission.requestedPermissions[0]
        )
        verify { clickedRefresh wasNot Called }
        verify { clickedDetails wasNot Called }
    }

    @Test
    fun test_click_else_subtitle_null() {
        val clickedRefresh: () -> Unit = mockk()
        val clickedDetails: (Item) -> Unit = mockk()
        composeTestRule.setContent {
            ComposableUnderTest(
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text(null)),
                clickedRefresh = clickedRefresh, clickedDetails = clickedDetails
            )
        }
        composeTestRule.onNodeWithText("Unable to retrieve Device Info from this device")
            .assertIsDisplayed()
        verify { clickedRefresh wasNot Called }
        verify { clickedDetails wasNot Called }
    }

    @Test
    fun test_click_else_subtitle_blank() {
        val clickedRefresh: () -> Unit = mockk()
        val clickedDetails: (Item) -> Unit = mockk()
        composeTestRule.setContent {
            ComposableUnderTest(
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("")),
                clickedRefresh = clickedRefresh, clickedDetails = clickedDetails
            )
        }
        composeTestRule.onNodeWithText("Unable to retrieve Device Info from this device")
            .assertIsDisplayed()
        verify { clickedRefresh wasNot Called }
        verify { clickedDetails wasNot Called }
    }

    @Test
    fun test_click_else_subtitle_nonnull() {
        val clickedRefresh: () -> Unit = mockk()
        val clickedDetails: (Item) -> Unit = mockk(relaxed = true)
        val item = Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("Test"))
        composeTestRule.setContent {
            ComposableUnderTest(
                item, clickedRefresh = clickedRefresh, clickedDetails = clickedDetails
            )
        }
        verify { clickedRefresh wasNot Called }
        verify { clickedDetails(item) }
    }

    @Suppress("TestFunctionName")
    @Composable
    private fun ComposableUnderTest(
        item: Item, clickedRefresh: (() -> Unit), clickedDetails: ((Item) -> Unit)
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                item.click(
                    snackbarHostState = snackbarHostState, forceRefresh = clickedRefresh,
                    showItemDetails = clickedDetails
                )
            }
        }
    }
}