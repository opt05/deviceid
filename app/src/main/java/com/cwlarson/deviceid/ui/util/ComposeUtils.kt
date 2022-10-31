package com.cwlarson.deviceid.ui.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.ui.theme.AppTheme
import com.cwlarson.deviceid.util.AppPermission
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription

@Composable
fun AppPermission.loadPermissionLabel(context: Context = LocalContext.current): CharSequence =
    try {
        context.packageManager.getPermissionInfo(permissionName, 0)
            .loadLabel(context.packageManager)
    } catch (e: Throwable) {
        stringResource(id = R.string.general_error)
    }

private const val KEY_STATE_RESTORED = "com.cwlarson.deviceid.STATE_RESTORED"

/**
 * Helper for delivering deep link intents from your Activity to your compose tree.
 */
class IntentHandler(private val activity: ComponentActivity) : DefaultLifecycleObserver {
    private val intentFlow = MutableSharedFlow<Intent>()
    private lateinit var intent: Flow<Intent>

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        with(activity.savedStateRegistry) {
            initDeepLink(consumeRestoredStateForKey(KEY_STATE_RESTORED) == null)
            registerSavedStateProvider(KEY_STATE_RESTORED) { Bundle() }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        activity.savedStateRegistry.unregisterSavedStateProvider(KEY_STATE_RESTORED)
        activity.lifecycle.removeObserver(this)
    }

    private fun initDeepLink(isInitial: Boolean) {
        intent = if (isInitial) intentFlow.onSubscription { activity.intent?.let { emit(it) } }
        else intentFlow
    }

    /**
     * Call in [android.app.Activity.onNewIntent]
     */
    fun onNewIntent(intent: Intent?) {
        activity.intent = intent
        activity.lifecycleScope.launchWhenCreated { intent?.let { intentFlow.emit(it) } }
    }


    /**
     * Triggers the callback whenever a new deep link is delivered.
     */
    @Composable
    fun OnIntent(callback: FlowCollector<Intent?>) {
        LaunchedEffect(null) { intent.collect(callback) }
    }
}

@Composable
fun Item.copyItemToClipboard(): (() -> Unit)? {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val string = stringResource(R.string.copy_to_clipboard, getFormattedString())
    return subtitle.getSubTitleText()?.let {
        if (it.isBlank()) null else ({
            clipboardManager.setText(AnnotatedString(it))
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
        })
    }
}

@Composable
fun Item.share(context: Context = LocalContext.current): () -> Unit = {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, context.getString(R.string.send_to))
        putExtra(Intent.EXTRA_TEXT, subtitle.getSubTitleText())
    }
    shareIntent.resolveActivity(context.packageManager)?.let {
        context.startActivity(Intent.createChooser(shareIntent, null))
    } ?:
    Toast.makeText(context, context.getString(R.string.send_to_no_apps), Toast.LENGTH_LONG)
        .show()
}

@SuppressLint("ComposableNaming")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Item.click(
    snackbarHostState: SnackbarHostState, forceRefresh: () -> Unit,
    showItemDetails: (item: Item) -> Unit
) {
    when (val sub = subtitle) {
        is ItemSubtitle.Permission -> {
            val permissionState = rememberPermissionState(sub.permission.permissionName)
            when (permissionState.status) {
                PermissionStatus.Granted -> forceRefresh()
                is PermissionStatus.Denied -> {
                    if (permissionState.status.shouldShowRationale) {
                        val message = stringResource(
                            id = R.string.permission_snackbar_retry,
                            sub.permission.loadPermissionLabel(),
                            getFormattedString()
                        )
                        val action = stringResource(id = R.string.permission_snackbar_button)
                        LaunchedEffect(snackbarHostState, this) {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            val result = snackbarHostState.showSnackbar(
                                message = message, actionLabel = action,
                                duration = SnackbarDuration.Indefinite
                            )
                            if (result == SnackbarResult.ActionPerformed)
                                permissionState.launchPermissionRequest()
                        }
                    } else LaunchedEffect(permissionState) { permissionState.launchPermissionRequest() }
                }
            }
        }
        else -> {
            if (sub.getSubTitleText().isNullOrBlank()) {
                // Unavailable for another reason
                val message = stringResource(
                    R.string.snackbar_not_found_adapter,
                    getFormattedString()
                )
                LaunchedEffect(snackbarHostState, this) {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(message = message)
                }
            } else {
                LaunchedEffect(snackbarHostState, this) {
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
                showItemDetails(this)
            }
        }
    }
}

@Preview
@Composable
private fun ListItemPreviewDefaultIcon() = AppTheme {
    ListItem(
        icon = Icons.Default.Android, text = "This is a title", chartPercentage = 75f,
        secondaryText = "This is a subtitle", trailing = { Switch(true, { }) }
    )
}

@Preview
@Composable
private fun ListItemPreviewSmallIcon() = AppTheme {
    ListItem(
        icon = Icons.Default.Android, isSmallIcon = true, text = "This is a title",
        secondaryText = "This is a subtitle", trailing = { Switch(true, { }) }
    )
}

@Preview
@Composable
private fun ListItemPreviewNoIcon() = AppTheme {
    ListItem(
        text = "This is a title", secondaryText = "This is a subtitle",
        trailing = { Switch(true, { }) }
    )
}

@Preview
@Composable
private fun ListItemPreviewNoIconNoTrailing() = AppTheme {
    ListItem(text = "This is a title", secondaryText = "This is a subtitle")
}

@Preview
@Composable
private fun ListItemPreviewNoIconNoTrailingMultiLine() = AppTheme {
    ListItem(
        text = "This is a title",
        secondaryText = "This is a subtitle that is really long and will go over to the next line" +
                " and continue on and on and one and on"
    )
}

@VisibleForTesting
const val LIST_ITEM_TEST_TAG_ICON = "list_item_icon"

@VisibleForTesting
const val LIST_ITEM_TEST_TAG_PROGRESS = "list_item_progress"

@Composable
fun ListItem(
    modifier: Modifier = Modifier, icon: ImageVector? = null, isSmallIcon: Boolean = false,
    text: String, chartPercentage: Float? = null, secondaryText: String? = null,
    secondaryTrailing: @Composable (() -> Unit)? = null, trailing: @Composable (() -> Unit)? = null
) {
    var multiLineSecondaryText by remember { mutableStateOf(false) }
    val minHeight = when {
        multiLineSecondaryText -> 88.dp
        secondaryText != null && icon == null -> 64.dp
        secondaryText == null && icon != null -> 56.dp
        secondaryText != null && icon != null -> 72.dp
        else -> 48.dp
    }
    val topPadding = when {
        chartPercentage != null -> 0.dp
        icon != null -> 32.dp
        secondaryText != null -> 28.dp
        else -> 0.dp
    }
    val secondaryPadding = when {
        chartPercentage != null -> 0.dp
        else -> 16.dp
    }
    Surface {
        Row(
            modifier = modifier
                .heightIn(min = minHeight)
                .padding(end = 16.dp)
        ) {
            icon?.let { i ->
                if (isSmallIcon) Box(
                    modifier = Modifier
                        .align(Alignment.Top)
                        .padding(start = 16.dp, top = 16.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Icon(
                        imageVector = i, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = null, modifier = Modifier
                            .size(24.dp)
                            .testTag(LIST_ITEM_TEST_TAG_ICON)
                    )
                }
                else Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 16.dp), contentAlignment = Alignment.CenterStart
                ) {
                    Icon(
                        imageVector = i, contentDescription = null, modifier = Modifier
                            .size(40.dp)
                            .testTag(LIST_ITEM_TEST_TAG_ICON),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Column(
                modifier = Modifier
                    .heightIn(min = minHeight)
                    .weight(1f)
                    .padding(start = if (isSmallIcon) 32.dp else 16.dp),
                verticalArrangement = if (topPadding == 0.dp) Arrangement.Center else Arrangement.Top
            ) {
                Text(
                    modifier = Modifier.paddingFromBaseline(top = topPadding),
                    text = text, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (chartPercentage != null) LinearProgressIndicator(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth()
                        .testTag(LIST_ITEM_TEST_TAG_PROGRESS),
                    color = MaterialTheme.colorScheme.secondary, trackColor = Color.Transparent,
                    progress = chartPercentage
                )
                if (secondaryText != null) Text(
                    modifier = Modifier.paddingFromBaseline(top = secondaryPadding),
                    text = secondaryText, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    onTextLayout = { multiLineSecondaryText = it.lineCount > 1 })
                secondaryTrailing?.invoke()
            }
            if (trailing != null) Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 16.dp)
            ) { trailing() }
        }
    }
}