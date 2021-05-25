package com.cwlarson.deviceid

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.cwlarson.deviceid.appupdates.*
import com.cwlarson.deviceid.databinding.ActivityMainBinding
import com.cwlarson.deviceid.search.SearchViewModel
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.util.*
import com.cwlarson.deviceid.util.InstallState.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.install.model.InstallStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var appUpdateUtils: AppUpdateUtils
    private val topLevelDestinations = setOf(
        R.id.tab_device_dest, R.id.tab_network_dest, R.id.tab_software_dest, R.id.tab_hardware_dest
    )
    private val mainActivityViewModel by viewModels<MainActivityViewModel>()
    private val searchItemsViewModel by viewModels<SearchViewModel>()
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme) //Removes splash screen
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainActivityViewModel.twoPane = binding.navigationList != null
        navController = findNavControllerFixed(R.id.nav_host_fragment)
        if (savedInstanceState == null) lifecycleScope.launch { setTaskDescription() }
        mainActivityViewModel.startTitleFade(intent)
        addRepeatingJob(Lifecycle.State.STARTED) {
            mainActivityViewModel.hideSearchBar.collect { hidden ->
                setTranslucentStatus(!hidden)
                binding.searchbarLayout.root.isVisible = !hidden
                binding.appbarLayout.root.appBarLayoutHideFix(hidden)
            }
        }
        addRepeatingJob(Lifecycle.State.STARTED) {
            mainActivityViewModel.isSearchOpen.collect { open ->
                binding.searchbarLayout.modifySearchLayout(open)
            }
        }
        addRepeatingJob(Lifecycle.State.STARTED) {
            mainActivityViewModel.titleVisibility.collect { visibility ->
                binding.searchbarLayout.setSearchHintVisibility(visibility)
            }
        }
        addRepeatingJob(Lifecycle.State.STARTED) {
            mainActivityViewModel.hideBottomBar.collect { hidden ->
                binding.bottomNavigationSpacer?.isVisible = !hidden
                binding.bottomNavigation?.isVisible = !hidden
                binding.navigationList?.isVisible = !hidden
            }
        }
        setSupportActionBar(binding.appbarLayout.toolbar)
        window.applySystemUiVisibility()
        binding.coordinatorLayout.applySystemWindows(applyLeft = true, applyRight = true)
        binding.appbarLayout.root.applySystemWindows(applyTop = true)
        binding.bottomNavigation?.applySystemWindows(applyBottom = true)
        binding.navigationList?.applySystemWindows(applyBottom = true, applyTop = true)
        binding.searchbarLayout.root.applySystemWindows(applyTop = true)
        //Setup menu
        binding.searchbarLayout.menuToolbar.run {
            menuInflater.inflate(R.menu.base_menu, menu)
            setOnMenuItemClickListener { item ->
                item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
            }
        }
        setupActionBarWithNavController(
            navController,
            AppBarConfiguration.Builder(topLevelDestinations).build()
        )
        binding.bottomNavigation?.setupWithNavController(navController)
        binding.navigationList?.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.tabs_detail_dialog_dest || destination.id == R.id.app_update_dialog_dest)
                return@addOnDestinationChangedListener
            mainActivityViewModel.updateHideSearchBar(
                !topLevelDestinations.contains(destination.id)
                        && destination.id != R.id.search_fragment_dest
            )
            mainActivityViewModel.updateHideBottomBar(
                !topLevelDestinations.contains(destination.id)
                        || destination.id == R.id.search_fragment_dest
            )
            (destination.id == R.id.search_fragment_dest).apply {
                if (this) supportActionBar?.setDisplayHomeAsUpEnabled(false)
                mainActivityViewModel.updateIsSearchOpen(this)
                if (!this && binding.searchbarLayout.searchBar.query.isNotBlank())
                    binding.searchbarLayout.searchBar.setQuery("", false)
            }
            // Reshow search bar and bottom bar onBackPressed()
            binding.bottomNavigationSpacer?.showViewOnNavigationChange()
            binding.bottomNavigation?.showViewOnNavigationChange()
            binding.searchbarLayout.root.showViewOnNavigationChange()
        }
        binding.searchbarLayout.setupSearchBarLayout(
            navController,
            preferenceManager
        ) { query, submit ->
            if (submit) binding.searchbarLayout.searchBar.setQuery(query, true)
            else searchItemsViewModel.setSearchText(query)
        }
        addRepeatingJob(Lifecycle.State.STARTED, Dispatchers.IO) {
            appUpdateUtils.updateState.collect { state ->
                when(state) {
                    is UpdateState.No -> {
                        withContext(Dispatchers.Main) {
                            navController.navigate(
                                AppUpdateDialogDirections
                                    .actionGlobalAppUpdateDialog(
                                        state.title,
                                        state.message,
                                        state.button
                                    )
                            )
                        }
                    }
                    else -> { /* Do nothing... */
                    }
                }
            }
        }
        addRepeatingJob(Lifecycle.State.STARTED) {
            appUpdateUtils.installState.collect { state ->
                if(state is NoError) {
                    when(state.status) {
                        InstallStatus.DOWNLOADED -> {
                            binding.coordinatorLayout.snackbar(
                                R.string.update_download_finished,
                                Snackbar.LENGTH_INDEFINITE, R.string.update_restart
                            ) {
                                appUpdateUtils.completeUpdate()
                            }
                        }
                        InstallStatus.FAILED -> {
                            binding.coordinatorLayout.snackbar(
                                R.string.update_download_failed,
                                Snackbar.LENGTH_INDEFINITE, R.string.update_retry
                            ) {
                                appUpdateUtils.checkForFlexibleUpdate()
                            }
                        }
                        else -> { /* Do nothing */
                        }
                    }
                }
            }
        }
        addRepeatingJob(Lifecycle.State.STARTED) {
            preferenceManager.observeSearchHistoryData().distinctUntilChanged().collectLatest {
                binding.searchbarLayout.updateSearchBarAdapter(it)
            }
        }
        addRepeatingJob(Lifecycle.State.STARTED) {
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
            appUpdateUtils.checkForFlexibleUpdate()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if (appUpdateUtils.awaitIsFlexibleUpdateDownloaded()) {
                // If the update is downloaded but not installed, notify the user to complete the update.
                binding.coordinatorLayout.snackbar(
                    R.string.update_download_finished,
                    Snackbar.LENGTH_INDEFINITE, R.string.update_restart
                ) {
                    appUpdateUtils.completeUpdate()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.handle()
    }

    private fun Intent.handle() {
        if (this.action == Intent.ACTION_SEARCH) {
            binding.searchbarLayout.searchBar.apply {
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
}
