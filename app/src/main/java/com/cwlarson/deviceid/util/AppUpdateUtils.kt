package com.cwlarson.deviceid.util

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.appupdates.FakeAppUpdateManagerWrapper
import com.cwlarson.deviceid.appupdates.UpdateType
import com.cwlarson.deviceid.settings.SettingsFragment
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallErrorCode
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.installErrorCode
import com.google.android.play.core.ktx.installStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

sealed class UpdateState {
    object Initial : UpdateState()
    object Checking: UpdateState()
    data class Yes(internal val appUpdateInfo: AppUpdateInfo, val manual: Boolean) : UpdateState()
    object YesButNotAllowed : UpdateState()
    data class No(
        @UpdateAvailability val availability: Int, @StringRes val title: Int,
        @StringRes val message: Int, @StringRes val button: Int
    ) : UpdateState()
}

sealed class InstallState {
    object Initial : InstallState()
    data class Failed(val message: String, val manual: Boolean) : InstallState()
    data class NoError(@InstallStatus val status: Int) : InstallState()
}

class AppUpdateUtils @Inject constructor(private val appUpdateManager: AppUpdateManager,
 private val activity: Context) :
    InstallStateUpdatedListener, LifecycleObserver {
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Initial)
    val updateState: StateFlow<UpdateState> = _updateState
    private val _installState = MutableStateFlow<InstallState>(InstallState.Initial)
    val installState: StateFlow<InstallState> = _installState

    init {
        if(activity is LifecycleOwner) activity.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        unregisterListener()
        if(activity is LifecycleOwner) activity.lifecycle.removeObserver(this)
    }

    fun checkForFlexibleUpdate(manual: Boolean = false) {
        if(_updateState.value is UpdateState.Checking) return
        _updateState.value = UpdateState.Checking
        appUpdateManager.appUpdateInfo.addOnCompleteListener { result ->
            if (result.isSuccessful) {
                Timber.d(result.result.updateAvailability().toString())
                _updateState.value = when (val avail = result.result.updateAvailability()) {
                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        if (result.result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                            UpdateState.Yes(result.result, manual)
                        } else UpdateState.YesButNotAllowed
                    }
                    UpdateAvailability.UPDATE_NOT_AVAILABLE ->
                        if (!manual) UpdateState.Initial else
                            UpdateState.No(
                                avail, R.string.update_notavailable_title, R.string
                                    .update_notavailable_message, R.string.update_notavailable_ok
                            )
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
                        if (!manual) UpdateState.Initial else
                            UpdateState.No(
                                avail, R.string.update_inprogress_title, R.string
                                    .update_inprogress_message, R.string.update_inprogress_ok
                            )
                    else -> { // UpdateAvailability.UNKNOWN
                        Timber.e("Unknown update availability type: $avail")
                        _installState.value = InstallState.Initial
                        UpdateState.No(
                            avail, R.string.update_unknown_title, R.string
                                .update_unknown_message, R.string.update_unknown_ok
                        )
                    }
                }
                if(activity is AppCompatActivity) startFlexibleUpdate(activity)
            } else {
                Timber.e(result.exception)
                _updateState.value = UpdateState.Initial
            }
        }
    }

    private fun startFlexibleUpdate(activity: Activity) {
        with(_updateState.value) {
            if (this is UpdateState.Yes) {
                appUpdateManager.registerListener(this@AppUpdateUtils)
                appUpdateManager.startUpdateFlow(
                    appUpdateInfo,
                    activity,
                    AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE)
                ).addOnFailureListener {
                    Timber.d("Flexible update flow failed: ${it?.message}")
                    _installState.value = InstallState.Initial
                }
            } else unregisterListener()
        }
    }

    private fun unregisterListener() {
        appUpdateManager.unregisterListener(this)
    }

    /**
     * Used in [Activity.onResume] to check if flexible update has downloaded
     * while user was away from the app
     */
    @Throws(Throwable::class)
    suspend fun awaitIsFlexibleUpdateDownloaded(): Boolean =
        suspendCoroutine { continuation ->
            appUpdateManager.appUpdateInfo.addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    continuation.resume(
                        result.result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                                && result.result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                                && result.result.installStatus() == InstallStatus.DOWNLOADED
                    )
                } else {
                    Timber.e(result.exception)
                    continuation.resumeWithException(result.exception ?: Throwable("Unknown"))
                }
            }
        }

    /**
     * For a flexible update flow, triggers the completion of the update.
     *
     * You should call this method to complete an update that has already been started and
     * is in the [InstallStatus.DOWNLOADED] state.
     */
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    /**
     * Used by [SettingsFragment] to set [FakeAppUpdateManagerWrapper.setEndState]
     * @return If was successful or not
     */
    fun updateFakeAppUpdateManagerState(type: UpdateType): Boolean =
        (appUpdateManager as? FakeAppUpdateManagerWrapper)?.setEndState(type) != null

    override fun onStateUpdate(state: com.google.android.play.core.install.InstallState) {
        val wasManualUpdate = when(val s = _updateState.value) {
            is UpdateState.Yes -> s.manual
            else -> false
        }
        _installState.value = when (state.installErrorCode) {
            InstallErrorCode.ERROR_API_NOT_AVAILABLE ->
                InstallState.Failed("The API is not available on this device.", wasManualUpdate)
            InstallErrorCode.ERROR_APP_NOT_OWNED ->
                InstallState.Failed(
                    "The app is not owned by any user on this device. An app is \"owned\" if it has been acquired from Play.",
                    wasManualUpdate
                )
            InstallErrorCode.ERROR_DOWNLOAD_NOT_PRESENT ->
                InstallState.Failed(
                    "The install/update has not been (fully) downloaded yet.",
                    wasManualUpdate
                )
            InstallErrorCode.ERROR_INSTALL_NOT_ALLOWED ->
                InstallState.Failed(
                    "The download/install is not allowed, due to the current device state.",
                    wasManualUpdate
                )
            InstallErrorCode.ERROR_INSTALL_UNAVAILABLE ->
                InstallState.Failed(
                    "The install is unavailable to this user or device.",
                    wasManualUpdate
                )
            InstallErrorCode.ERROR_INTERNAL_ERROR ->
                InstallState.Failed(
                    "An internal error happened in the Play Store.",
                    wasManualUpdate
                )
            InstallErrorCode.ERROR_INVALID_REQUEST ->
                InstallState.Failed(
                    "The request that was sent by the app is malformed.",
                    wasManualUpdate
                )
            InstallErrorCode.ERROR_PLAY_STORE_NOT_FOUND ->
                InstallState.Failed(
                    "The Play Store is not available on this device",
                    wasManualUpdate
                )
            InstallErrorCode.NO_ERROR -> {
                Timber.d("No error occurred; all types of update flow are allowed.")
                when (state.installStatus) {
                    InstallStatus.CANCELED -> Timber.d("Canceled")
                    InstallStatus.DOWNLOADING -> Timber.d("Downloading")
                    InstallStatus.INSTALLED -> Timber.d("Installed")
                    InstallStatus.INSTALLING -> Timber.d("Installing")
                    InstallStatus.PENDING -> Timber.d("Pending")
                    InstallStatus.FAILED -> Timber.d("Failed")
                    InstallStatus.DOWNLOADED -> Timber.d("Downloaded")
                    else -> Timber.d("Unknown")
                }
                InstallState.NoError(state.installStatus)
            }
            else -> InstallState.Failed("An unknown error occurred.", wasManualUpdate)
        }
    }
}