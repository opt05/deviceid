package com.cwlarson.deviceid.appupdates

import android.app.Activity
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.cwlarson.deviceid.MainActivity
import com.cwlarson.deviceid.util.toast
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.tasks.Task
import kotlinx.coroutines.*

@Suppress("unused")
enum class UpdateType {
    /** No simulation.  */
    NO_SIMULATION,

    /** No update available.  */
    NONE,

    /** The update will be successful.  */
    SUCCESS,

    /** The update will fail because the user will choose cancel when the Play dialog shows.  */
    FAIL_DIALOG_CANCEL,

    /** The update will fail because the dialog will fail with another unknown reason.  */
    FAIL_DIALOG_UPDATE_FAILED,

    /** The update will fail because the download fails.  */
    FAIL_DOWNLOAD,

    /** The update will fail because the download was cancelled. */
    FAIL_DOWNLOAD_CANCEL,

    /** The update will fail because it failed to install.  */
    FAIL_INSTALL
}

enum class UpdateEvent {
    UPDATE_AVAILABLE,
    USER_ACCEPTS_UPDATE,
    USER_REJECTS_UPDATE,
    TRIGGER_DOWNLOAD,
    DOWNLOAD_STARTS,
    DOWNLOAD_FAILS,
    USER_CANCELS_DOWNLOAD,
    DOWNLOAD_COMPLETES,
    INSTALL_FAILS,
    INSTALL_COMPLETES
}

/**
 * A wrapper of FakeAppUpdateManager meant to help automatically trigger more update scenarios.  The
 * wrapper isn't meant to be used for a full integration test, but simulating all of the possible
 * error cases is a bit easier to do here.
 */
class FakeAppUpdateManagerWrapper(private val context: Context) : FakeAppUpdateManager(context),
        CoroutineScope by MainScope(), DefaultLifecycleObserver {
    companion object {
        private const val RESULT_IN_APP_UPDATE_FAILED = 1
        private const val STEP_DELAY_MS = 5000L
    }

    private var endState: UpdateType = UpdateType.NO_SIMULATION

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        coroutineContext.cancel()
    }

    fun setEndState(endState: UpdateType) {
        launch {
            this@FakeAppUpdateManagerWrapper.endState = endState
            if (endState != UpdateType.NONE) eventHandler(UpdateEvent.UPDATE_AVAILABLE)
        }
    }

    override fun startUpdateFlowForResult(appUpdateInfo: AppUpdateInfo, appUpdateType: Int,
                                          activity: Activity, requestCode: Int): Boolean {
        runBlocking { toast("Starting update flow.") }
        // TODO: Simulate exceptions being thrown or returning false from the super call.
        val success = super.startUpdateFlowForResult(appUpdateInfo, appUpdateType, activity, requestCode)
        if (!success) return false

        val resultCode: Int = when (endState) {
            UpdateType.FAIL_DIALOG_CANCEL -> Activity.RESULT_CANCELED
            UpdateType.FAIL_DIALOG_UPDATE_FAILED -> RESULT_IN_APP_UPDATE_FAILED
            else -> Activity.RESULT_OK
        }
        launch(Dispatchers.IO) { triggerDialogResponse(activity, requestCode, resultCode) }
        return true
    }

    /**
     * A helper class to wrap invocations to the Google Play API.
     */
    private suspend fun eventHandler(updateEvent: UpdateEvent,
                                     withDelay: Boolean = false) = withContext(Dispatchers.IO) {
        if (withDelay) delay(STEP_DELAY_MS)
        when (updateEvent) {
            UpdateEvent.UPDATE_AVAILABLE -> {
                toast("Making app update available.")
                setUpdateAvailable(10000 /* Figure out a better version? */)
            }
            UpdateEvent.USER_ACCEPTS_UPDATE -> {
                toast("User accepts update.")
                userAcceptsUpdate()
            }
            UpdateEvent.USER_REJECTS_UPDATE -> {
                toast("User rejects update.")
                userRejectsUpdate()
            }
            UpdateEvent.TRIGGER_DOWNLOAD -> {
                toast("Triggering download.")
                triggerDownload()
            }
            UpdateEvent.DOWNLOAD_STARTS -> {
                toast("Download has started.")
                downloadStarts()
            }
            UpdateEvent.DOWNLOAD_FAILS -> {
                toast("Triggering download failure.")
                downloadFails()
            }
            UpdateEvent.USER_CANCELS_DOWNLOAD -> {
                toast("Triggering cancellation of download.")
                userCancelsDownload()
            }
            UpdateEvent.DOWNLOAD_COMPLETES -> {
                toast("Download completes.")
                downloadCompletes()
            }
            UpdateEvent.INSTALL_FAILS -> {
                toast("Triggering install failure.")
                installFails()
            }
            UpdateEvent.INSTALL_COMPLETES -> {
                toast("Triggering install completion.")
                installCompletes()
            }
        }
    }

    override fun completeUpdate(): Task<Void> {
        launch {
            toast("Completing update.")
            eventHandler(if (endState == UpdateType.FAIL_INSTALL) UpdateEvent.INSTALL_FAILS
            else UpdateEvent.INSTALL_COMPLETES, true)
        }
        return super.completeUpdate()
    }

    @ExperimentalCoroutinesApi
    private suspend fun triggerDialogResponse(activity: Activity?, requestCode: Int, resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            eventHandler(UpdateEvent.USER_ACCEPTS_UPDATE)
        } else if (resultCode == Activity.RESULT_CANCELED) {
            eventHandler(UpdateEvent.USER_REJECTS_UPDATE)
        }

        if (activity is MainActivity)
            activity.onActivityResult(requestCode, resultCode, null)
        if (resultCode == Activity.RESULT_OK) {
            eventHandler(UpdateEvent.TRIGGER_DOWNLOAD, true)
        }
    }

    private suspend fun triggerDownload() {
        eventHandler(UpdateEvent.DOWNLOAD_STARTS)
        eventHandler(
                when (endState) {
                    UpdateType.FAIL_DOWNLOAD -> UpdateEvent.DOWNLOAD_FAILS
                    UpdateType.FAIL_DOWNLOAD_CANCEL -> UpdateEvent.USER_CANCELS_DOWNLOAD
                    else -> UpdateEvent.DOWNLOAD_COMPLETES
                }, true)
    }

    private suspend fun toast(text: CharSequence) = withContext(Dispatchers.Main) {
        context.toast("Play Store Flow: $text", STEP_DELAY_MS.toInt())
    }
}
