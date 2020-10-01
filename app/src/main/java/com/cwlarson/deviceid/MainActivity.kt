package com.cwlarson.deviceid

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.cwlarson.deviceid.appupdates.*
import com.cwlarson.deviceid.search.SearchViewModel
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.util.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallErrorCode
import com.google.android.play.core.install.model.InstallStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.appbar_layout.view.*
import kotlinx.android.synthetic.main.searchbar_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main), InstallStateUpdatedListener {
    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var appUpdateManager: AppUpdateManager
    private val topLevelDestinations = setOf(
        R.id.tab_device_dest, R.id.tab_network_dest, R.id.tab_software_dest, R.id.tab_hardware_dest
    )

    @ExperimentalCoroutinesApi
    private val appUpdateViewModel by viewModels<AppUpdateViewModel>()
    private val mainActivityViewModel by viewModels<MainActivityViewModel>()

    @ExperimentalCoroutinesApi
    private val searchItemsViewModel by viewModels<SearchViewModel>()
    private lateinit var navController: NavController

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme) //Removes splash screen
        super.onCreate(savedInstanceState)
        mainActivityViewModel.twoPane = navigation_list != null
        navController = findNavControllerFixed(R.id.nav_host_fragment)
        if(savedInstanceState == null) lifecycleScope.launch { setTaskDescription() }
        mainActivityViewModel.startTitleFade(intent)
        mainActivityViewModel.hideSearchBar.onEach { hidden ->
            setTranslucentStatus(!hidden)
            searchbar_layout.isVisible = !hidden
            appbar_layout.appBarLayoutHideFix(hidden)
        }.launchIn(lifecycleScope)
        mainActivityViewModel.isSearchOpen.onEach { open ->
            searchbar_layout.modifySearchLayout(open)
        }.launchIn(lifecycleScope)
        mainActivityViewModel.titleVisibility.onEach { visibility ->
            searchbar_layout.setSearchHintVisibility(visibility)
        }.launchIn(lifecycleScope)
        mainActivityViewModel.hideBottomBar.onEach { hidden ->
            bottom_navigation_spacer?.isVisible = !hidden
            bottom_navigation?.isVisible = !hidden
            navigation_list?.isVisible = !hidden
        }.launchIn(lifecycleScope)
        setSupportActionBar(appbar_layout.toolbar)
        coordinator_layout.applySystemUiVisibility()
        coordinator_layout.applySystemWindows(applyLeft = true, applyRight = true)
        appbar_layout.applySystemWindows(applyTop = true)
        bottom_navigation?.applySystemWindows(applyBottom = true)
        navigation_list?.applySystemWindows(applyBottom = true, applyTop = true)
        searchbar_layout.applySystemWindows(applyTop = true)
        //Setup menu
        searchbar_layout.menu_toolbar.run {
            menuInflater.inflate(R.menu.base_menu, menu)
            setOnMenuItemClickListener { item ->
                item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
            }
        }
        setupActionBarWithNavController(
            navController,
            AppBarConfiguration.Builder(topLevelDestinations).build()
        )
        bottom_navigation?.setupWithNavController(navController)
        navigation_list?.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.tabs_detail_dialog_dest || destination.id == R.id.app_update_dialog_dest)
                return@addOnDestinationChangedListener
            mainActivityViewModel.updateHideSearchBar(
                !topLevelDestinations.contains(destination.id)
                        && destination.id != R.id.search_fragment_dest
            )
            mainActivityViewModel.updateHideBottomBar(
                !topLevelDestinations.contains(
                    destination
                        .id
                ) || destination.id == R.id.search_fragment_dest
            )
            (destination.id == R.id.search_fragment_dest).apply {
                if (this) supportActionBar?.setDisplayHomeAsUpEnabled(false)
                mainActivityViewModel.updateIsSearchOpen(this)
                if (!this && searchbar_layout.search_bar.query.isNotBlank())
                    searchbar_layout.search_bar.setQuery("", false)
            }
            // Reshow search bar and bottom bar onBackPressed()
            bottom_navigation_spacer?.showViewOnNavigationChange()
            bottom_navigation?.showViewOnNavigationChange()
            searchbar_layout?.showViewOnNavigationChange()
        }
        searchbar_layout.setupSearchBarLayout(navController, preferenceManager) { query, submit ->
            if (submit) searchbar_layout.search_bar.setQuery(query, true)
            else searchItemsViewModel.setSearchText(query)
        }
        appUpdateViewModel.checkForFlexibleUpdate.onEach {
            if (it?.processed == false) withContext(Dispatchers.IO) {
                val updateInfo = appUpdateManager.appUpdateInfo
                val updateAvail = updateInfo.awaitIsUpdateAvailable(AppUpdateType.FLEXIBLE)
                appUpdateViewModel.setUpdateState(updateAvail)
                when (updateAvail) {
                    UpdateState.Yes -> {
                        appUpdateManager.registerListener(this@MainActivity)
                        appUpdateManager.startUpdateFlowForResult(
                            updateInfo.result,
                            AppUpdateType.FLEXIBLE,
                            this@MainActivity,
                            UPDATE_FLEXIBLE_REQUEST_CODE
                        )
                    }
                    is UpdateState.YesButNotAllowed -> { /* Do nothing... */ }
                    is UpdateState.No -> {
                        appUpdateViewModel.setInstallState(null)
                        withContext(Dispatchers.Main) {
                            navController.navigate(
                                AppUpdateDialogDirections
                                    .actionGlobalAppUpdateDialog(
                                        updateAvail.title,
                                        updateAvail.message,
                                        updateAvail.button
                                    )
                            )
                        }
                    }
                }
            }
        }.launchIn(lifecycleScope)
        lifecycleScope.launch {
            preferenceManager.observeSearchHistoryData().distinctUntilChanged().collectLatest {
                searchbar_layout.updateSearchBarAdapter(it)
            }
        }
        lifecycleScope.launch {
            preferenceManager.observeUseFakeUpdateManager().distinctUntilChanged().collectLatest {
                // Restart activity to use different AppUpdateManager
                val intent =
                    baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
                ActivityCompat.finishAffinity(this@MainActivity)
                startActivity(intent)
                exitProcess(0)
            }
        }
        if (savedInstanceState == null) {
            intent?.handle()
            lifecycleScope.launch(Dispatchers.IO) {
                val updateInfo = appUpdateManager.appUpdateInfo
                val updateAvail = updateInfo.awaitIsUpdateAvailable(AppUpdateType.FLEXIBLE)
                appUpdateViewModel.setUpdateState(updateAvail)
                if (updateAvail == UpdateState.Yes) {
                    appUpdateManager.registerListener(this@MainActivity)
                    appUpdateManager.startUpdateFlowForResult(
                        updateInfo.result,
                        AppUpdateType.FLEXIBLE, this@MainActivity, UPDATE_FLEXIBLE_REQUEST_CODE
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            if (appUpdateManager.appUpdateInfo.awaitIsFlexibleUpdateDownloaded()) {
                // If the update is downloaded but not installed, notify the user to complete the update.
                coordinator_layout.snackbar(
                    R.string.update_download_finished,
                    Snackbar.LENGTH_INDEFINITE, R.string.update_restart
                ) {
                    appUpdateManager.completeUpdate()
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_FLEXIBLE_REQUEST_CODE && resultCode != Activity.RESULT_OK) {
            Timber.d("Flexible update flow failed! Results code: $resultCode")
            appUpdateViewModel.resetState()
            appUpdateManager.unregisterListener(this)
        }

    }

    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        super.onDestroy()
        appUpdateViewModel.resetState()
        appUpdateManager.unregisterListener(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.handle()
    }

    private fun Intent.handle() {
        if (this.action == Intent.ACTION_SEARCH) {
            searchbar_layout.search_bar.apply {
                isFocusable = true
                isFocusableInTouchMode = true
                requestFocus()
                setQuery("", false)
            }
            if (navController.currentDestination?.id != R.id.search_fragment_dest)
                navController.navigate(R.id.search_fragment_dest)
        }
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp()

    @SuppressLint("SwitchIntDef")
    @ExperimentalCoroutinesApi
    override fun onStateUpdate(state: InstallState) {
        appUpdateViewModel.setInstallState(state)
        val wasManualUpdate = appUpdateViewModel.checkForFlexibleUpdate.value?.manual ?: false
        if ((state.installErrorCode() != InstallErrorCode.NO_ERROR
                    || state.installStatus() == InstallStatus.FAILED) && wasManualUpdate
        ) {
            Timber.d("Failed")
            coordinator_layout.snackbar(
                R.string.update_download_failed,
                Snackbar.LENGTH_INDEFINITE, R.string.update_retry
            ) {
                appUpdateViewModel.sendCheckForFlexibleUpdate()
            }
        } else
            when (state.installErrorCode()) {
                InstallErrorCode.ERROR_API_NOT_AVAILABLE ->
                    Timber.e("The API is not available on this device.")
                InstallErrorCode.ERROR_APP_NOT_OWNED ->
                    Timber.e("The app is not owned by any user on this device. An app is \"owned\" if it has been acquired from Play.")
                InstallErrorCode.ERROR_DOWNLOAD_NOT_PRESENT ->
                    Timber.e("The install/update has not been (fully) downloaded yet.")
                InstallErrorCode.ERROR_INSTALL_NOT_ALLOWED ->
                    Timber.e("The download/install is not allowed, due to the current device state.")
                InstallErrorCode.ERROR_INSTALL_UNAVAILABLE ->
                    Timber.e("The install is unavailable to this user or device.")
                InstallErrorCode.ERROR_INTERNAL_ERROR ->
                    Timber.e("An internal error happened in the Play Store.")
                InstallErrorCode.ERROR_INVALID_REQUEST ->
                    Timber.e("The request that was sent by the app is malformed.")
                InstallErrorCode.ERROR_UNKNOWN ->
                    Timber.e("An unknown error occurred.")
                InstallErrorCode.ERROR_PLAY_STORE_NOT_FOUND ->
                    Timber.e("The Play Store is not available on this device")
                InstallErrorCode.NO_ERROR -> {
                    Timber.e("No error occurred; all types of update flow are allowed.")
                    when (state.installStatus()) {
                        InstallStatus.CANCELED -> Timber.d("Canceled")
                        InstallStatus.DOWNLOADING -> Timber.d("Downloading")
                        InstallStatus.INSTALLED -> Timber.d("Installed")
                        InstallStatus.INSTALLING -> Timber.d("Installing")
                        InstallStatus.PENDING -> Timber.d("Pending")
                        InstallStatus.FAILED -> Timber.d("Failed")
                        InstallStatus.UNKNOWN -> Timber.d("Unknown")
                        InstallStatus.DOWNLOADED -> {
                            Timber.d("Downloaded")
                            // After the update is downloaded, show a notification
                            // and request user confirmation to restart the app.
                            coordinator_layout.snackbar(
                                R.string.update_download_finished,
                                Snackbar.LENGTH_INDEFINITE, R.string.update_restart
                            ) {
                                appUpdateManager.completeUpdate()
                            }
                        }
                    }
                }
            }
    }
}
