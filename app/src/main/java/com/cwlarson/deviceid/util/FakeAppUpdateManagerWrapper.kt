package com.cwlarson.deviceid.util

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.cwlarson.deviceid.MainActivity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.tasks.Task
import kotlinx.coroutines.*

/**
 * A wrapper of FakeAppUpdateManager meant to help automatically trigger more update scenarios.  The
 * wrapper isn't meant to be used for a full integration test, but simulating all of the possible
 * error cases is a bit easier to do here.
 */
class FakeAppUpdateManagerWrapper(private val context: Context, private val scope:
        CoroutineScope) :
        FakeAppUpdateManager(context.applicationContext) {
    private var endState: Type = Type.NO_SIMULATION
    companion object {
        private const val RESULT_IN_APP_UPDATE_FAILED = 1
        private const val STEP_DELAY_MS = 5000L

        @Suppress("unused")
        enum class Type {
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

        enum class Event {
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
    }

    fun setEndState(endState: Type) {
        this.endState = endState
        if (endState != Type.NONE) scope.launch(Dispatchers.IO) { eventHandler(Event.UPDATE_AVAILABLE) }
    }

    override fun startUpdateFlowForResult(appUpdateInfo: AppUpdateInfo, @AppUpdateType appUpdateType: Int,
                                          activity: Activity?, requestCode: Int): Boolean {
        scope.launch { toast("Starting update flow.") }
        // TODO: Simulate exceptions being thrown or returning false from the super call.
        val success = super.startUpdateFlowForResult(appUpdateInfo, appUpdateType, activity, requestCode)
        if (!success) return false

        val resultCode: Int = when (endState) {
            Type.FAIL_DIALOG_CANCEL -> Activity.RESULT_CANCELED
            Type.FAIL_DIALOG_UPDATE_FAILED -> RESULT_IN_APP_UPDATE_FAILED
            else -> Activity.RESULT_OK
        }

        scope.launch(Dispatchers.IO) { triggerDialogResponse(activity, requestCode, resultCode) }
        return true
    }

    /**
     * A helper class to wrap invocations to the Google Play API.
     */
    private suspend fun eventHandler(event: Event, withDelay: Boolean = false) = coroutineScope {
        if(withDelay) delay(STEP_DELAY_MS)
        when (event) {
            Event.UPDATE_AVAILABLE -> {
                toast("Making app update available.")
                setUpdateAvailable(10000 /* Figure out a better version? */)
            }
            Event.USER_ACCEPTS_UPDATE -> {
                toast("User accepts update.")
                userAcceptsUpdate()
            }
            Event.USER_REJECTS_UPDATE -> {
                toast("User rejects update.")
                userRejectsUpdate()
            }
            Event.TRIGGER_DOWNLOAD -> {
                toast("Triggering download.")
                triggerDownload()
            }
            Event.DOWNLOAD_STARTS -> {
                toast("Download has started.")
                downloadStarts()
            }
            Event.DOWNLOAD_FAILS -> {
                toast("Triggering download failure.")
                downloadFails()
            }
            Event.USER_CANCELS_DOWNLOAD -> {
                toast("Triggering cancellation of download.")
                userCancelsDownload()
            }
            Event.DOWNLOAD_COMPLETES -> {
                toast("Download completes.")
                downloadCompletes()
            }
            Event.INSTALL_FAILS -> {
                toast("Triggering install failure.")
                installFails()
            }
            Event.INSTALL_COMPLETES -> {
                toast("Triggering install completion.")
                installCompletes()
            }
        }
    }

    override fun completeUpdate(): Task<Void> {
        scope.launch(Dispatchers.IO) {
            toast("Completing update.")
            eventHandler(if(endState == Type.FAIL_INSTALL) Event.INSTALL_FAILS
            else Event.INSTALL_COMPLETES, true)
        }
        return super.completeUpdate()
    }

    private suspend fun triggerDialogResponse(activity: Activity?, requestCode: Int, resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            eventHandler(Event.USER_ACCEPTS_UPDATE)
        } else if (resultCode == Activity.RESULT_CANCELED) {
            eventHandler(Event.USER_REJECTS_UPDATE)
        }

        if (activity is MainActivity)
            activity.onActivityResult(requestCode, resultCode, null)
        if (resultCode == Activity.RESULT_OK) {
            eventHandler(Event.TRIGGER_DOWNLOAD, true)
        }
    }

    private suspend fun triggerDownload() {
        eventHandler(Event.DOWNLOAD_STARTS)
        eventHandler(
                when (endState) {
                    Type.FAIL_DOWNLOAD -> Event.DOWNLOAD_FAILS
                    Type.FAIL_DOWNLOAD_CANCEL -> Event.USER_CANCELS_DOWNLOAD
                    else -> Event.DOWNLOAD_COMPLETES
                }, true)
    }

    private suspend fun toast(text: CharSequence) = withContext(Dispatchers.Main) {
        Toast.makeText(context, "Play Store Flow: $text", STEP_DELAY_MS.toInt()).show()
    }
}
