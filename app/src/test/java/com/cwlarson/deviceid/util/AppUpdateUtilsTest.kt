package com.cwlarson.deviceid.util

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.cwlarson.deviceid.testutils.CoroutineTestRule
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallErrorCode
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
@RunWith(AndroidJUnit4::class)
class AppUpdateUtilsTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var appUpdateManager: FakeAppUpdateManager
    private lateinit var activity: AppCompatActivity
    private lateinit var lifecycleOwner: TestLifecycleOwner
    private lateinit var testObject: AppUpdateUtils

    @Before
    fun setup() {
        lifecycleOwner = TestLifecycleOwner()
        activity = mock { on { lifecycle } doReturn lifecycleOwner.lifecycle }
        appUpdateManager = spy(FakeAppUpdateManager(ApplicationProvider.getApplicationContext()))
        testObject = AppUpdateUtils(appUpdateManager, activity)
    }

    @Test
    fun `Verify lifecycle adds observer on initialization`() {
        assertEquals(1, lifecycleOwner.lifecycle.observerCount)
    }

    @Test
    fun `Verify lifecycle removes observer on destroy`() {
        lifecycleOwner.onCreate()
        lifecycleOwner.onDestroy()
        assertEquals(0, lifecycleOwner.lifecycle.observerCount)
    }

    @Test
    fun `Verify initial states`() = runBlocking {
        testObject.updateState.test { assertEquals(UpdateState.Initial, awaitItem()) }
        testObject.installState.test { assertEquals(InstallState.Initial, awaitItem()) }
    }

    @Test
    fun `Verify checkForFlexibleUpdate returns yes when update available`() = runBlocking {
        appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
        testObject.updateState.test {
            assertEquals(UpdateState.Initial, awaitItem())
            testObject.checkForFlexibleUpdate()
            assertEquals(UpdateState.Checking, awaitItem())
            assertTrue(awaitItem() is UpdateState.Yes)
        }
    }

    @Test
    fun `Verify checkForFlexibleUpdate returns not allowed when update available`() = runBlocking {
        appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.IMMEDIATE)
        testObject.updateState.test {
            assertEquals(UpdateState.Initial, awaitItem())
            testObject.checkForFlexibleUpdate()
            assertEquals(UpdateState.Checking, awaitItem())
            assertEquals(UpdateState.YesButNotAllowed, awaitItem())
        }
    }

    @Test
    fun `Verify checkForFlexibleUpdate returns when update not available and manual false`() =
        runBlocking {
            appUpdateManager.setUpdateNotAvailable()
            testObject.updateState.test {
                assertEquals(UpdateState.Initial, awaitItem())
                testObject.checkForFlexibleUpdate()
                assertEquals(UpdateState.Checking, awaitItem())
                assertEquals(UpdateState.Initial, awaitItem())
            }
        }

    @Test
    fun `Verify checkForFlexibleUpdate returns no when update not available and manual true`() =
        runBlocking {
            appUpdateManager.setUpdateNotAvailable()
            testObject.updateState.test {
                assertEquals(UpdateState.Initial, awaitItem())
                testObject.checkForFlexibleUpdate(true)
                assertEquals(UpdateState.Checking, awaitItem())
                assertTrue(awaitItem() is UpdateState.No)
            }
        }

    @Test
    fun `Verify checkForFlexibleUpdate returns no when update unknown`() = runBlocking {
        appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
        val updateInfoTask = spy(appUpdateManager.appUpdateInfo)
        val updateInfo = spy(updateInfoTask.result) {
            on { updateAvailability() } doReturn UpdateAvailability.UNKNOWN
        }
        whenever(appUpdateManager.appUpdateInfo).thenReturn(updateInfoTask)
        whenever(updateInfoTask.result).thenReturn(updateInfo)
        testObject.updateState.test {
            assertEquals(UpdateState.Initial, awaitItem())
            testObject.checkForFlexibleUpdate()
            assertEquals(UpdateState.Checking, awaitItem())
            assertTrue(awaitItem() is UpdateState.No)
        }
    }

    @Test
    fun `Verify checkForFlexibleUpdate resets when update exception`() = runBlocking {
        appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
        whenever(appUpdateManager.appUpdateInfo).thenThrow(NullPointerException())
        testObject.updateState.test {
            assertEquals(UpdateState.Initial, awaitItem())
            testObject.checkForFlexibleUpdate()
            assertEquals(UpdateState.Checking, awaitItem())
            assertTrue(awaitItem() is UpdateState.Initial)
        }
    }

    @Test
    fun `Verify startFlexibleUpdate starts update when update yes`(): Unit = runBlocking {
        appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
        testObject.checkForFlexibleUpdate()
        verify(appUpdateManager).registerListener(any())
        verify(appUpdateManager).startUpdateFlow(any(), eq(activity), any())
    }

    @Ignore("Unknown how to do?")
    @Test
    fun `Verify startFlexibleUpdate resets state when failure`(): Unit = runBlocking {
        appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)

        testObject.checkForFlexibleUpdate()
        verify(appUpdateManager).registerListener(any())
        //verify(appUpdateManager).startUpdateFlow(any(), eq(activity), any())
    }

    @Test
    fun `Verify startFlexibleUpdate removes listener when update no`() = runBlocking {
        appUpdateManager.setUpdateNotAvailable()
        testObject.checkForFlexibleUpdate()
        verify(appUpdateManager, never()).registerListener(any())
        verify(appUpdateManager, never()).startUpdateFlow(any(), any(), any())
        verify(appUpdateManager).unregisterListener(any())
    }

    @Test
    fun `Verify startFlexibleUpdate removes listener when update unknown`() = runBlocking {
        appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
        val updateInfoTask = spy(appUpdateManager.appUpdateInfo)
        val updateInfo = spy(updateInfoTask.result) {
            on { updateAvailability() } doReturn UpdateAvailability.UNKNOWN
        }
        whenever(appUpdateManager.appUpdateInfo).thenReturn(updateInfoTask)
        whenever(updateInfoTask.result).thenReturn(updateInfo)
        testObject.checkForFlexibleUpdate()
        verify(appUpdateManager, never()).registerListener(any())
        verify(appUpdateManager, never()).startUpdateFlow(any(), any(), any())
        verify(appUpdateManager).unregisterListener(any())
    }

    @Test
    fun `Verify awaitIsFlexibleUpdateDownloaded returns true when downloaded`() = runBlocking {
        appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
        testObject.checkForFlexibleUpdate()
        appUpdateManager.userAcceptsUpdate()
        appUpdateManager.downloadStarts()
        appUpdateManager.downloadCompletes()
        assertTrue(testObject.awaitIsFlexibleUpdateDownloaded())
    }

    @Test
    fun `Verify awaitIsFlexibleUpdateDownloaded returns false when not downloaded`() = runBlocking {
        appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
        testObject.checkForFlexibleUpdate()
        appUpdateManager.userAcceptsUpdate()
        appUpdateManager.downloadStarts()
        assertFalse(testObject.awaitIsFlexibleUpdateDownloaded())
    }

    @Test
    fun `Verify completeUpdate calls to complete update`() = runBlocking {
        appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
        testObject.checkForFlexibleUpdate()
        appUpdateManager.userAcceptsUpdate()
        appUpdateManager.downloadStarts()
        appUpdateManager.downloadCompletes()
        testObject.completeUpdate()
        verify(appUpdateManager).completeUpdate()
        assertTrue(appUpdateManager.isInstallSplashScreenVisible)
        appUpdateManager.installCompletes()
    }

    @Test
    fun `Verify onStateUpdate returns failed when ERROR_API_NOT_AVAILABLE and not manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate()
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_API_NOT_AVAILABLE)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed("The API is not available on this device.", false),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when ERROR_API_NOT_AVAILABLE and manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate(true)
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_API_NOT_AVAILABLE)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed("The API is not available on this device.", true),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when ERROR_APP_NOT_OWNED and not manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate()
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_APP_NOT_OWNED)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed(
                        "The app is not owned by any user on this device. An app is \"owned\" if it has been acquired from Play.",
                        false
                    ),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when ERROR_APP_NOT_OWNED and manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate(true)
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_APP_NOT_OWNED)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed(
                        "The app is not owned by any user on this device. An app is \"owned\" if it has been acquired from Play.",
                        true
                    ),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when ERROR_DOWNLOAD_NOT_PRESENT and not manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate()
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_DOWNLOAD_NOT_PRESENT)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed(
                        "The install/update has not been (fully) downloaded yet.",
                        false
                    ),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when ERROR_DOWNLOAD_NOT_PRESENT and manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate(true)
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_DOWNLOAD_NOT_PRESENT)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed(
                        "The install/update has not been (fully) downloaded yet.",
                        true
                    ),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when ERROR_INSTALL_NOT_ALLOWED and not manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate()
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_INSTALL_NOT_ALLOWED)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed(
                        "The download/install is not allowed, due to the current device state.",
                        false
                    ),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when ERROR_INSTALL_NOT_ALLOWED and manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate(true)
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_INSTALL_NOT_ALLOWED)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed(
                        "The download/install is not allowed, due to the current device state.",
                        true
                    ),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when ERROR_INSTALL_UNAVAILABLE and not manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate()
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_INSTALL_UNAVAILABLE)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed(
                        "The install is unavailable to this user or device.",
                        false
                    ),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when ERROR_INSTALL_UNAVAILABLE and manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate(true)
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_INSTALL_UNAVAILABLE)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed("The install is unavailable to this user or device.", true),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when ERROR_INTERNAL_ERROR and not manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate()
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_INTERNAL_ERROR)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed("An internal error happened in the Play Store.", false),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when ERROR_INTERNAL_ERROR and manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate(true)
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_INTERNAL_ERROR)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed("An internal error happened in the Play Store.", true),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when ERROR_INVALID_REQUEST and not manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate()
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_INVALID_REQUEST)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed(
                        "The request that was sent by the app is malformed.",
                        false
                    ),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when ERROR_INVALID_REQUEST and manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate(true)
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_INVALID_REQUEST)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed("The request that was sent by the app is malformed.", true),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when ERROR_PLAY_STORE_NOT_FOUND and not manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate()
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_PLAY_STORE_NOT_FOUND)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed("The Play Store is not available on this device.", false),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when ERROR_PLAY_STORE_NOT_FOUND and manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate(true)
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_PLAY_STORE_NOT_FOUND)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed("The Play Store is not available on this device.", true),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when NO_ERROR success`() = runBlocking {
        appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
        testObject.installState.test {
            assertEquals(InstallState.Initial, awaitItem())
            testObject.checkForFlexibleUpdate()
            appUpdateManager.userAcceptsUpdate()
            assertEquals(InstallState.NoError(InstallStatus.PENDING), awaitItem())
            appUpdateManager.downloadStarts()
            assertEquals(InstallState.NoError(InstallStatus.DOWNLOADING), awaitItem())
            appUpdateManager.downloadCompletes()
            assertEquals(InstallState.NoError(InstallStatus.DOWNLOADED), awaitItem())
            appUpdateManager.completeUpdate()
            assertEquals(InstallState.NoError(InstallStatus.INSTALLING), awaitItem())
            appUpdateManager.installCompletes()
            assertEquals(InstallState.NoError(InstallStatus.INSTALLED), awaitItem())
        }
    }

    @Test
    fun `Verify onStateUpdate returns failed when NO_ERROR canceled`() = runBlocking {
        appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
        testObject.installState.test {
            assertEquals(InstallState.Initial, awaitItem())
            testObject.checkForFlexibleUpdate()
            appUpdateManager.userAcceptsUpdate()
            assertEquals(InstallState.NoError(InstallStatus.PENDING), awaitItem())
            appUpdateManager.downloadStarts()
            assertEquals(InstallState.NoError(InstallStatus.DOWNLOADING), awaitItem())
            appUpdateManager.userCancelsDownload()
            assertEquals(InstallState.NoError(InstallStatus.CANCELED), awaitItem())
        }
    }

    @Test
    fun `Verify onStateUpdate returns failed when NO_ERROR failed`() = runBlocking {
        appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
        testObject.installState.test {
            assertEquals(InstallState.Initial, awaitItem())
            testObject.checkForFlexibleUpdate()
            appUpdateManager.userAcceptsUpdate()
            assertEquals(InstallState.NoError(InstallStatus.PENDING), awaitItem())
            appUpdateManager.downloadStarts()
            assertEquals(InstallState.NoError(InstallStatus.DOWNLOADING), awaitItem())
            appUpdateManager.downloadCompletes()
            assertEquals(InstallState.NoError(InstallStatus.DOWNLOADED), awaitItem())
            appUpdateManager.completeUpdate()
            assertEquals(InstallState.NoError(InstallStatus.INSTALLING), awaitItem())
            appUpdateManager.installFails()
            assertEquals(InstallState.NoError(InstallStatus.FAILED), awaitItem())
        }
    }

    @Test
    fun `Verify onStateUpdate returns failed when other and not manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate()
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_UNKNOWN)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed("An unknown error occurred.", false),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Verify onStateUpdate returns failed when other and manual`() =
        runBlocking {
            appUpdateManager.setUpdateAvailable(Int.MAX_VALUE, AppUpdateType.FLEXIBLE)
            testObject.checkForFlexibleUpdate(true)
            appUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_UNKNOWN)
            appUpdateManager.userAcceptsUpdate()
            appUpdateManager.downloadStarts()
            appUpdateManager.downloadCompletes()
            appUpdateManager.completeUpdate()
            testObject.installState.test {
                assertEquals(
                    InstallState.Failed("An unknown error occurred.", true),
                    expectMostRecentItem()
                )
            }
        }

    private class TestLifecycleOwner : LifecycleOwner {
        private val lifecycle = LifecycleRegistry(this)
        fun onCreate() {
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }

        fun onDestroy() {
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }

        override fun getLifecycle() = lifecycle
    }
}