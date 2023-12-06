package com.cwlarson.deviceid.settings

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.ui.theme.AppTheme
import com.cwlarson.deviceid.ui.util.ListItem
import com.cwlarson.deviceid.util.*
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.InstallStatus
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@VisibleForTesting
const val SETTINGS_TEST_TAG_LIST_ITEM_TITLE_APP_SETTINGS = "settings_list_item_title_app_settings"

@VisibleForTesting
const val SETTINGS_TEST_TAG_LIST_ITEM_HIDE_UNAVAILABLE = "settings_list_item_hide_unavailable"

@VisibleForTesting
const val SETTINGS_TEST_TAG_LIST_ITEM_AUTO_REFRESH = "settings_list_item_auto_refresh"

@VisibleForTesting
const val SETTINGS_TEST_TAG_LIST_ITEM_THEME = "settings_list_item_theme"

@VisibleForTesting
const val SETTINGS_TEST_TAG_LIST_ITEM_SEARCH_HISTORY = "settings_list_item_search_history"

@VisibleForTesting
const val SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE = "settings_list_item_app_update"

@VisibleForTesting
const val SETTINGS_TEST_TAG_DIALOG = "settings_dialog"

@VisibleForTesting
const val SETTINGS_TEST_TAG_DIALOG_LIST = "settings_dialog_list"

@VisibleForTesting
const val SETTINGS_TEST_TAG_DIALOG_BUTTON_CANCEL = "settings_dialog_button_cancel"

@Preview(showBackground = true)
@Composable
fun SettingsPreview() = AppTheme {
    Column {
        val context = LocalContext.current
        SettingsScreen(
            dispatcherProvider = DispatcherProvider, appUpdateUtils = AppUpdateUtils(
                DispatcherProvider, FakeAppUpdateManager(context), context
            ), viewModel = SettingsViewModel(
                DispatcherProvider,
                PreferenceManager(
                    DispatcherProvider, context,
                    PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("user_preferences") })
            )
        )
    }
}

@Composable
fun SettingsScreen(
    dispatcherProvider: DispatcherProvider, appUpdateUtils: AppUpdateUtils,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val values by viewModel.userPreferencesFlow.collectAsStateWithLifecycle(
        initialValue = UserPreferences(), context = dispatcherProvider.Main
    )
    val appUpdateState by appUpdateUtils.updateState.collectAsStateWithLifecycle(
        initialValue = UpdateState.Initial, context = dispatcherProvider.Main
    )
    val appInstallState by appUpdateUtils.installState.collectAsStateWithLifecycle(
        initialValue = InstallState.Initial, context = dispatcherProvider.Main
    )
    LazyColumn(
        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.activity_horizontal_margin_search))
    ) {
        item {
            Title(
                SETTINGS_TEST_TAG_LIST_ITEM_TITLE_APP_SETTINGS,
                R.string.pref_title_app_settings
            )
        }
        item {
            SwitchPreference(
                SETTINGS_TEST_TAG_LIST_ITEM_HIDE_UNAVAILABLE,
                Icons.Outlined.VisibilityOff, R.string.pref_hide_unavailable_title,
                R.string.pref_hide_unavailable_summary_on, R.string
                    .pref_hide_unavailable_summary_off, values.hideUnavailable
            ) { viewModel.setHideUnavailable(it) }
        }
        item {
            SeekBarPreference(
                SETTINGS_TEST_TAG_LIST_ITEM_AUTO_REFRESH,
                Icons.Outlined.Sync, R.string.pref_auto_refresh_rate_title,
                R.string.pref_auto_refresh_rate_summary, values.autoRefreshRate
            ) { viewModel.setAutoRefreshRate(it) }
        }
        item {
            ListPreference(
                SETTINGS_TEST_TAG_LIST_ITEM_THEME,
                Icons.Outlined.DarkMode, R.string.pref_daynight_mode_title,
                R.array.pref_daynight_mode_entries, R.array.pref_daynight_mode_values,
                values.darkTheme
            ) { viewModel.setDarkMode(it) }
        }
        item {
            SwitchPreference(
                SETTINGS_TEST_TAG_LIST_ITEM_SEARCH_HISTORY,
                Icons.Outlined.History, R.string.pref_search_history_title,
                R.string.pref_search_history_summary_on, R.string.pref_search_history_summary_off,
                values.searchHistory
            ) { viewModel.setSearchHistory(it) }
        }
        item {
            Preference(
                SETTINGS_TEST_TAG_LIST_ITEM_APP_UPDATE,
                Icons.Outlined.SystemUpdateAlt,
                when (appUpdateState) {
                    is UpdateState.Yes,
                    UpdateState.YesButNotAllowed -> R.string.pref_check_for_update_title_yes
                    else -> R.string.pref_check_for_update_title_no
                },
                when (val state = appInstallState) {
                    is InstallState.Failed -> R.string.pref_check_for_update_summary_failed
                    InstallState.Initial -> R.string.pref_check_for_update_summary
                    is InstallState.NoError -> {
                        when (state.status) {
                            InstallStatus.CANCELED -> R.string.pref_check_for_update_summary_canceled
                            InstallStatus.DOWNLOADING -> R.string.pref_check_for_update_summary_downloading
                            InstallStatus.INSTALLED -> R.string.pref_check_for_update_summary_installed
                            InstallStatus.INSTALLING -> R.string.pref_check_for_update_summary_installing
                            InstallStatus.PENDING -> R.string.pref_check_for_update_summary_pending
                            InstallStatus.UNKNOWN -> R.string.pref_check_for_update_summary_unknown
                            InstallStatus.DOWNLOADED -> R.string.pref_check_for_update_summary_downloaded
                            InstallStatus.FAILED -> R.string.pref_check_for_update_summary_failed
                            else -> R.string.pref_check_for_update_summary
                        }
                    }
                }
            ) { scope.launch { appUpdateUtils.checkForFlexibleUpdate(true) } }
        }
    }
}

@Suppress("SameParameterValue")
@Composable
private fun Title(testTag: String, @StringRes title: Int) {
    Row(
        modifier = Modifier
            .padding(start = 72.dp, top = 8.dp, bottom = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.Bottom
    ) {
        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
            Text(text = stringResource(title), color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun SwitchPreference(
    testTag: String,
    icon: ImageVector, @StringRes title: Int, @StringRes summaryOn: Int,
    @StringRes summaryOff: Int, value: Boolean, onValueChanged: (Boolean) -> Unit
) {
    ListItem(modifier = Modifier
        .clickable { onValueChanged(!value) }
        .testTag(testTag),
        icon = icon, isSmallIcon = true, text = stringResource(id = title),
        secondaryText = stringResource(if (value) summaryOn else summaryOff),
        trailing = { Switch(checked = value, onCheckedChange = { onValueChanged(!value) }) })
}

@Suppress("SameParameterValue")
@Composable
private fun SeekBarPreference(
    testTag: String, icon: ImageVector, @StringRes title: Int,
    @StringRes subtitle: Int, value: Int, onValueChanged: (Int) -> Unit
) {
    ListItem(modifier = Modifier.testTag(testTag), icon = icon, isSmallIcon = true,
        text = stringResource(id = title), secondaryText = stringResource(subtitle),
        secondaryTrailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    valueRange = 0f..1f, steps = 10, value = (value.toFloat() / 10),
                    onValueChange = { onValueChanged((it * 10).roundToInt()) },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary
                    )
                )
                Text(
                    text = if (value == 0) "off" else value.toString(),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    )
}

@Suppress("SameParameterValue")
@Composable
private fun ListPreference(
    testTag: String, icon: ImageVector, @StringRes title: Int,
    @ArrayRes subtitle: Int, @ArrayRes subtitleValues: Int, value: String,
    onValueChanged: (String) -> Unit
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    ListItem(
        modifier = Modifier
            .clickable { showDialog = true }
            .testTag(testTag), icon = icon,
        isSmallIcon = true, text = stringResource(id = title),
        secondaryText = stringArrayResource(subtitle)[stringArrayResource(subtitleValues).indexOf(
            value
        )]
    )

    @Composable
    fun dialog(subtitles: Array<String>, subtitleValues: Array<String>) {
        AlertDialog(modifier = Modifier.testTag(SETTINGS_TEST_TAG_DIALOG),
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(title)) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .selectableGroup()
                        .testTag(SETTINGS_TEST_TAG_DIALOG_LIST)
                ) {
                    itemsIndexed(subtitleValues) { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = item == value,
                                    onClick = {
                                        onValueChanged(subtitleValues[index])
                                        showDialog = false
                                    }, role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = item == value,
                                onClick = null
                            )
                            Text(
                                text = subtitles[index],
                                style = MaterialTheme.typography.bodyLarge.merge(),
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(modifier = Modifier.testTag(SETTINGS_TEST_TAG_DIALOG_BUTTON_CANCEL),
                    onClick = { showDialog = false }) {
                    Text(stringResource(android.R.string.cancel).toUpperCase(Locale.current))
                }
            })
    }
    if (showDialog) dialog(stringArrayResource(subtitle), stringArrayResource(subtitleValues))
}

@Suppress("SameParameterValue")
@Composable
private fun Preference(
    testTag: String,
    icon: ImageVector, @StringRes title: Int, @StringRes subtitle: Int,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag), icon = icon,
        isSmallIcon = true, text = stringResource(title), secondaryText = stringResource(subtitle)
    )
}