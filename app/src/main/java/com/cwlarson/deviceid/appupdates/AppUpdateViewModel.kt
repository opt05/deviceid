package com.cwlarson.deviceid.appupdates

import android.app.Activity
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cwlarson.deviceid.R
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class CheckForUpdate(val manual: Boolean = false, val processed: Boolean = false)

@ExperimentalCoroutinesApi
class AppUpdateViewModel : ViewModel() {
    private val _checkForFlexibleUpdate = MutableStateFlow<CheckForUpdate?>(null)
    val checkForFlexibleUpdate: StateFlow<CheckForUpdate?> = _checkForFlexibleUpdate
    private val _updateState = MutableStateFlow<UpdateState?>(null)
    val updateState: StateFlow<UpdateState?> = _updateState
    private val _installState = MutableStateFlow<InstallState?>(null)
    val installState: StateFlow<InstallState?> = _installState

    fun sendCheckForFlexibleUpdate(manual: Boolean = false) {
        viewModelScope.launch { _checkForFlexibleUpdate.value = CheckForUpdate(manual) }
    }

    fun setUpdateState(state: UpdateState?) {
        viewModelScope.launch { _updateState.value = state }
    }

    fun setInstallState(state: InstallState?) {
        viewModelScope.launch { _installState.value = state }
    }

    // If the update is cancelled or fails, you can request to start the update again.
    fun resetState() {
        viewModelScope.launch {
            _installState.value = null
            _checkForFlexibleUpdate.value = null
        }
    }
}

const val UPDATE_FLEXIBLE_REQUEST_CODE = 8831

sealed class UpdateState {
    object Yes : UpdateState()
    object YesButNotAllowed : UpdateState()
    data class No(
        @UpdateAvailability val availability: Int, @StringRes val title: Int,
        @StringRes val message: Int, @StringRes val button: Int
    ) : UpdateState()
}

/**
 * Used to check if flexible update is available and perform action if so
 */
@Throws(Exception::class)
suspend fun Task<AppUpdateInfo>.awaitIsUpdateAvailable(@AppUpdateType appUpdateType: Int): UpdateState {
    return suspendCoroutine { continuation ->
        addOnCompleteListener { result ->
            if (result.isSuccessful) {
                Timber.d(result.result.updateAvailability().toString())
                continuation.resume(
                    when (val avail = result.result.updateAvailability()) {
                        UpdateAvailability.UPDATE_AVAILABLE -> {
                            if (result.result.isUpdateTypeAllowed(appUpdateType)) UpdateState.Yes
                            else UpdateState.YesButNotAllowed
                        }
                        UpdateAvailability.UPDATE_NOT_AVAILABLE ->
                            UpdateState.No(
                                avail, R.string.update_notavailable_title, R.string
                                    .update_notavailable_message, R.string.update_notavailable_ok
                            )
                        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
                            UpdateState.No(
                                avail, R.string.update_inprogress_title, R.string
                                    .update_inprogress_message, R.string.update_inprogress_ok
                            )
                        else -> { // UpdateAvailability.UNKNOWN
                            Timber.e("Unknown update availability type: $avail")
                            UpdateState.No(
                                avail, R.string.update_unknown_title, R.string
                                    .update_unknown_message, R.string.update_unknown_ok
                            )
                        }
                    }
                )
            } else {
                Timber.e(result.exception)
            }
        }
    }
}

/**
 * Used in [Activity.onResume] to check if flexible update has downloaded
 * while user was away from the app
 */
@Throws(Exception::class)
suspend fun Task<AppUpdateInfo>.awaitIsFlexibleUpdateDownloaded(): Boolean {
    return suspendCoroutine { continuation ->
        addOnCompleteListener { result ->
            if (result.isSuccessful) {
                continuation.resume(
                    result.result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            && result.result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                            && result.result.installStatus() == InstallStatus.DOWNLOADED
                )
            } else Timber.e(result.exception)
        }
    }
}