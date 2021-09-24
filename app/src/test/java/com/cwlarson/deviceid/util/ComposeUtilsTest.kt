package com.cwlarson.deviceid.util

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
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
import com.cwlarson.deviceid.ui.util.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@ExperimentalPermissionsApi
@ExperimentalCoroutinesApi
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
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var context: Application

    @Before
    fun setup() {
        context = spy(ApplicationProvider.getApplicationContext() as Application)
    }

    @Test
    fun test_loadPermissionLabel_success() {
        val pi: PermissionInfo = mock()
        val pm: PackageManager = mock()
        whenever(context.packageManager).thenReturn(pm)
        whenever(pm.getPermissionInfo(any(), any())).thenReturn(pi)
        whenever(pi.loadLabel(pm)).thenReturn("test")
        composeTestRule.setContent {
            assert(AppPermission.ReadPhoneState.loadPermissionLabel(context) == "test")
        }
    }

    @Test
    fun test_loadPermissionLabel_fail() {
        whenever(context.packageManager).doThrow(NullPointerException())
        composeTestRule.setContent {
            assert(AppPermission.ReadPhoneState.loadPermissionLabel(context) == "Something went wrong")
        }
    }

    @Test
    fun test_IntentHandler_init() {
        val activity: ComponentActivity = mock()
        val lifecycle: Lifecycle = mock()
        whenever(activity.lifecycle).thenReturn(lifecycle)
        IntentHandler(activity)
        verify(lifecycle).addObserver(any())
    }

    @Test
    fun test_IntentHandler_onCreate_intent() {
        val intent = composeTestRule.activity.intent
        val handler = composeTestRule.runOnUiThread { IntentHandler(composeTestRule.activity) }
        composeTestRule.setContent { handler.OnIntent { assert(it == intent) } }
    }

    @Test
    fun test_IntentHandler_onCreate_intent_multiple() {
        val intent = composeTestRule.activity.intent
        val handler = composeTestRule.runOnUiThread { IntentHandler(composeTestRule.activity) }
        composeTestRule.activity.intent = intent.apply { putExtra(Intent.EXTRA_TEXT, "test") }
        composeTestRule.runOnUiThread {
            handler.onDestroy()
            handler.onCreate()
        }
        composeTestRule.setContent { handler.OnIntent { assert(it == intent) } }
    }

    @Test
    fun test_IntentHandler_onNewIntent() {
        val intent = composeTestRule.activity.intent
            .apply { putExtra(Intent.EXTRA_TEXT, "test") }
        val handler = composeTestRule.runOnUiThread { IntentHandler(composeTestRule.activity) }
        handler.onNewIntent(intent)
        composeTestRule.setContent { handler.OnIntent { assert(it == intent) } }
    }

    @Test
    fun test_copyItemToClipboard_null() {
        composeTestRule.setContent {
            assert(
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text(null))
                    .copyItemToClipboard() == null
            )
        }
    }

    @Test
    fun test_copyItemToClipboard_blank() {
        composeTestRule.setContent {
            assert(
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text(""))
                    .copyItemToClipboard() == null
            )
        }
    }

    @Test
    fun test_copyItemToClipboard_nonnull() {
        composeTestRule.setContent {
            assert(
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("Name"))
                    .copyItemToClipboard() != null
            )
        }
    }

    @Test
    fun test_copyItemToClipboard_click_null() {
        composeTestRule.setContent {
            val clipboardManager = LocalClipboardManager.current
            Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Error)
                .copyItemToClipboard()?.invoke()
            assert(clipboardManager.getText()?.text == null)
        }
        assert(ShadowToast.shownToastCount() == 0)
    }

    @Test
    fun test_copyItemToClipboard_click_blank() {
        composeTestRule.setContent {
            val clipboardManager = LocalClipboardManager.current
            Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text(""))
                .copyItemToClipboard()?.invoke()
            assert(clipboardManager.getText()?.text == null)
        }
        assert(ShadowToast.shownToastCount() == 0)
    }

    @Test
    fun test_copyItemToClipboard_click_nonnull() {
        composeTestRule.setContent {
            val clipboardManager = LocalClipboardManager.current
            Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("Name"))
                .copyItemToClipboard()?.invoke()
            assert(clipboardManager.getText()?.text == "Name")
        }
        assert(ShadowToast.showedToast("Copied Device Info to clipboard!"))
    }

    @Test
    fun test_share_success() {
        val pm: PackageManager = mock()
        val appInfo: ApplicationInfo = mock()
        val activityInfo: ActivityInfo = mock()
        val info: ResolveInfo = mock()
        info.activityInfo = activityInfo
        activityInfo.name = "name"
        activityInfo.applicationInfo = appInfo
        appInfo.packageName = "name"
        doReturn(pm).whenever(context).packageManager
        doReturn(info).whenever(pm).resolveActivity(any(), any())
        doNothing().whenever(context).startActivity(any())
        composeTestRule.setContent {
            Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("Name"))
                .share(context).invoke()
        }
        verify(context).startActivity(any())
    }

    @Test
    fun test_share_failure() {
        val pm: PackageManager = mock()
        doReturn(pm).whenever(context).packageManager
        doReturn(null).whenever(pm).resolveActivity(any(), any())
        doNothing().whenever(context).startActivity(any())
        composeTestRule.setContent {
            Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("Name"))
                .share(context).invoke()
        }
        verify(context, times(0)).startActivity(any())
        assert(ShadowToast.showedToast("No app available"))
    }

    @Test
    fun test_click_permission_hasPermission() {
        var clickedType: ClickedType = ClickedType.None
        shadowOf(context).grantPermissions(Manifest.permission.READ_PHONE_STATE)
        composeTestRule.setContent {
            ComposableUnderTest(
                Item(
                    R.string.app_name, ItemType.DEVICE,
                    ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                )
            ) { clickedType = it }
        }
        assert(clickedType == ClickedType.Refresh)
    }

    @Test
    fun test_click_permission_shouldShowRationale() {
        var clickedType: ClickedType = ClickedType.None
        shadowOf(context).denyPermissions(Manifest.permission.READ_PHONE_STATE)
        shadowOf(context.packageManager)
            .setShouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE, true)
        composeTestRule.setContent {
            ComposableUnderTest(
                Item(
                    R.string.app_name, ItemType.DEVICE,
                    ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                )
            ) { clickedType = it }
        }
        composeTestRule.onNodeWithText("permission is required to display", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        assert(clickedType == ClickedType.None)
    }

    @Test
    fun test_click_permission_shouldShowRationale_clickRetry() {
        var clickedType: ClickedType = ClickedType.None
        shadowOf(context).denyPermissions(Manifest.permission.READ_PHONE_STATE)
        shadowOf(context.packageManager)
            .setShouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE, true)
        composeTestRule.setContent {
            ComposableUnderTest(
                Item(
                    R.string.app_name, ItemType.DEVICE,
                    ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                )
            ) { clickedType = it }
        }
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed().performClick()
        assert(Manifest.permission.READ_PHONE_STATE ==
            shadowOf(composeTestRule.activity).lastRequestedPermission.requestedPermissions[0])
        assert(clickedType == ClickedType.None)
    }

    @Test
    fun test_click_permission_permissionRequested() {
        var clickedType: ClickedType = ClickedType.None
        shadowOf(context).denyPermissions(Manifest.permission.READ_PHONE_STATE)
        composeTestRule.setContent {
            ComposableUnderTest(
                Item(
                    R.string.app_name, ItemType.DEVICE,
                    ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                )
            ) { clickedType = it }
        }
        assert(Manifest.permission.READ_PHONE_STATE ==
                shadowOf(composeTestRule.activity).lastRequestedPermission.requestedPermissions[0])
        assert(clickedType == ClickedType.None)
    }

    @Test
    fun test_click_else_subtitle_null() {
        var clickedType: ClickedType = ClickedType.None
        composeTestRule.setContent {
            ComposableUnderTest(
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text(null))
            ) { clickedType = it }
        }
        composeTestRule.onNodeWithText("Unable to retrieve Device Info from this device")
            .assertIsDisplayed()
        assert(clickedType == ClickedType.None)
    }

    @Test
    fun test_click_else_subtitle_blank() {
        var clickedType: ClickedType = ClickedType.None
        composeTestRule.setContent {
            ComposableUnderTest(
                Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text(""))
            ) { clickedType = it }
        }
        composeTestRule.onNodeWithText("Unable to retrieve Device Info from this device")
            .assertIsDisplayed()
        assert(clickedType == ClickedType.None)
    }

    @Test
    fun test_click_else_subtitle_nonnull() {
        var clickedType: ClickedType = ClickedType.None
        val item = Item(R.string.app_name, ItemType.DEVICE, ItemSubtitle.Text("Test"))
        composeTestRule.setContent { ComposableUnderTest(item) { clickedType = it } }
        assert(clickedType == ClickedType.Details(item))
    }

    private sealed class ClickedType {
        object None: ClickedType()
        object Refresh: ClickedType()
        data class Details(val item: Item): ClickedType()
    }

    @Suppress("TestFunctionName")
    @Composable
    private fun ComposableUnderTest(item: Item, clicked: ((type: ClickedType) -> Unit)) {
        val state = rememberScaffoldState()
        Scaffold(scaffoldState = state) {
            item.click(
                snackbarHostState = state.snackbarHostState,
                forceRefresh = { clicked(ClickedType.Refresh) },
                showItemDetails = { clicked(ClickedType.Details(it)) })
        }
    }
}