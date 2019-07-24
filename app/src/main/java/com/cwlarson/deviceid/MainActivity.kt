package com.cwlarson.deviceid

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.cwlarson.deviceid.database.AppUpdateViewModel
import com.cwlarson.deviceid.database.MainActivityViewModel
import com.cwlarson.deviceid.database.SearchItemsViewModel
import com.cwlarson.deviceid.databinding.ActivityMainBinding
import com.cwlarson.deviceid.databinding.UnavailablePermission
import com.cwlarson.deviceid.dialog.AppUpdateDialogDirections
import com.cwlarson.deviceid.util.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallErrorCode
import com.google.android.play.core.install.model.InstallStatus
import kotlinx.coroutines.*
import org.json.JSONArray
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope, SharedPreferences
.OnSharedPreferenceChangeListener, InstallStateUpdatedListener {
    private val topLevelDestinations = hashSetOf(R.id.tab_device_dest,
            R.id.tab_network_dest, R.id.tab_software_dest, R.id.tab_hardware_dest)
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var searchHistoryAdapter: SuggestionAdapter<String>
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private lateinit var appUpdateViewModel: AppUpdateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme) //Removes splash screen
        super.onCreate(savedInstanceState)
        setTaskDescription()
        preferences = PreferenceManager.getDefaultSharedPreferences(this).also {
            it.registerOnSharedPreferenceChangeListener(this)
        }
        appUpdateViewModel = ViewModelProviders.of(this).get<AppUpdateViewModel>().apply {
            useFakeAppUpdateManager.value = preferences.getBoolean(
                    getString(R.string.pref_use_fake_update_manager_key), false)
        }
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            lifecycleOwner = this@MainActivity
            model = ViewModelProviders.of(this@MainActivity).get<MainActivityViewModel>().apply {
                hideSearchBar.observe(this@MainActivity, Observer {
                    setTranslucentStatus(!(it ?: false))
                })
            }
            setSupportActionBar(toolbar)
        }
        findNavController(R.id.nav_host_fragment).apply {
            //Setup menu
            binding.menuToolbar.run {
                menuInflater.inflate(R.menu.base_menu, menu)
                setOnMenuItemClickListener { item ->
                    item.onNavDestinationSelected(this@apply) ||
                            super.onOptionsItemSelected(item)
                }
            }
            setupActionBarWithNavController(this@apply,
                    AppBarConfiguration.Builder(topLevelDestinations).build())
            binding.bottomNavigation.setupWithNavController(this@apply)
            addOnDestinationChangedListener { _, destination, _ ->
                if(destination.id == R.id.itemClickDialog || destination.id == R.id.appUpdateDialog)
                    return@addOnDestinationChangedListener
                binding.model?.hideSearchBar?.value = !topLevelDestinations.contains(destination.id)
                        && destination.id != R.id.search_fragment_dest
                binding.model?.hideBottomBar?.value = !topLevelDestinations.contains(destination
                        .id) || destination.id == R.id.search_fragment_dest
                (destination.id == R.id.search_fragment_dest).apply {
                    if (this) supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    binding.model?.isSearchOpen?.value = this
                    if (!this && binding.searchBar.query.isNotBlank())
                        binding.searchBar.setQuery("", false)
                }
            }
            binding.searchHandler = SearchClickHandler(this, binding.searchBar)
        }
        val searchModel = ViewModelProviders.of(this).get<SearchItemsViewModel>()
        binding.searchBar.apply {
            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if(!hasFocus && preferences.getBoolean(
                                getString(R.string.pref_search_history_key), false))
                    query?.let { saveSearchHistoryItem(it.toString()) }
            }
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { saveSearchHistoryItem(it) }
                    searchModel.setSearchText(query)
                    binding.searchHandler?.onSearchSubmit(query)
                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    searchModel.setSearchText(query)
                    binding.searchHandler?.onSearchSubmit(query)
                    return false
                }
            })
            // Load history items in searchview
            findViewById<SearchView.SearchAutoComplete?>(R.id.search_src_text)?.apply {
                @ColorRes val colorRes = TypedValue().run {
                    this@MainActivity.theme.resolveAttribute(R.attr.colorBackgroundFloating, this, true)
                    resourceId
                }
                setDropDownBackgroundResource(colorRes)
                searchHistoryAdapter = SuggestionAdapter(this@MainActivity,
                        R.layout.searchview_history_item,
                        getSearchHistoryItems(preferences), android.R.id.text1)
                @SuppressLint("RestrictedApi")
                threshold = 0
                setAdapter(searchHistoryAdapter)
                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    setQuery(searchHistoryAdapter.getItem(position), true)
                }
            } ?: Timber.wtf("SearchView.SearchAutoComplete id has changed and requires maintenance")

        }
        if (savedInstanceState == null) {
            launch(Dispatchers.IO) {
                (intent.action != Intent.ACTION_SEARCH).apply {
                    if(this) delay(TimeUnit.SECONDS.toMillis(2))
                    binding.model?.titleVisibility?.postValue(Pair(!this, View.GONE))
                }
            }
            binding.model?.loadAllData(this@MainActivity)
            intent?.handle()
            launch(Dispatchers.IO) {
                val updateInfo = appUpdateViewModel.getUpdateManager().appUpdateInfo
                val updateAvail = updateInfo.awaitIsUpdateAvailable(AppUpdateType.FLEXIBLE)
                appUpdateViewModel.updateStatus.postValue(updateAvail)
                if(updateAvail == UpdateState.Yes) {
                    appUpdateViewModel.getUpdateManager().registerListener(this@MainActivity)
                    appUpdateViewModel.getUpdateManager().startUpdateFlowForResult(updateInfo.result,
                            AppUpdateType.FLEXIBLE, this@MainActivity, UPDATE_FLEXIBLE_REQUEST_CODE)
                }
            }
        }
        appUpdateViewModel.checkForFlexibleUpdate.observe(this, Observer {
                    it?.getContentIfNotHandled()?.let { // Only proceed if the event has never been handled
                        launch(Dispatchers.IO) {
                            val updateInfo = appUpdateViewModel.getUpdateManager().appUpdateInfo
                            val updateAvail = updateInfo.awaitIsUpdateAvailable(AppUpdateType.FLEXIBLE)
                            appUpdateViewModel.updateStatus.postValue(updateAvail)
                            when (updateAvail) {
                                UpdateState.Yes -> {
                                    appUpdateViewModel.getUpdateManager().registerListener(this@MainActivity)
                                    appUpdateViewModel.getUpdateManager().startUpdateFlowForResult(updateInfo.result,
                                            AppUpdateType.FLEXIBLE, this@MainActivity, UPDATE_FLEXIBLE_REQUEST_CODE)
                                }
                                is UpdateState.No -> {
                                    appUpdateViewModel.installState.postValue(null)
                                    withContext(Dispatchers.Main) {
                                        findNavController(R.id.nav_host_fragment).navigate(AppUpdateDialogDirections
                                                .actionGlobalAppUpdateDialog(updateAvail.title, updateAvail.message, updateAvail.button))
                                    }
                                }
                            }
                        }
                    }
                })
    }

    override fun onResume() {
        super.onResume()
        launch(Dispatchers.IO) {
            if(appUpdateViewModel.getUpdateManager().appUpdateInfo.awaitIsFlexibleUpdateDownloaded()) {
                // If the update is downloaded but not installed, notify the user to complete the update.
                Snackbar.make(binding.coordinatorLayout,
                        getString(R.string.update_download_finished), Snackbar.LENGTH_INDEFINITE)
                        .setAnchorView(R.id.bottom_navigation)
                        .setActionTextColor(ContextCompat.getColor(this@MainActivity, R.color.imageSecondary))
                        .setAction(getString(R.string.update_restart))
                        { appUpdateViewModel.getUpdateManager().completeUpdate()}.show()
            }
        }

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == UPDATE_FLEXIBLE_REQUEST_CODE && resultCode != Activity.RESULT_OK) {
            Timber.d("Flexible update flow failed! Results code: $resultCode")
            // If the update is cancelled or fails, you can request to start the update again.
            appUpdateViewModel.installState.value = null
            appUpdateViewModel.checkForFlexibleUpdate.value = null
            appUpdateViewModel.getUpdateManager().unregisterListener(this)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
        appUpdateViewModel.installState.value = null
        appUpdateViewModel.checkForFlexibleUpdate.value = null
        appUpdateViewModel.getUpdateManager().unregisterListener(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE.value) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // GRANTED: Force new data updates
                binding.model?.loadAllData(this@MainActivity)
            } else {
                // DENIED: We do nothing (it is handled by the ViewAdapter)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.handle()
    }

    private fun Intent.handle() {
        if(this.action == Intent.ACTION_SEARCH) {
            binding.searchBar.apply {
                isFocusable = true
                isFocusableInTouchMode = true
                requestFocus()
            }
            binding.searchHandler?.onSearchSubmitIntent()
        }
    }

    override fun onSupportNavigateUp(): Boolean  =
            findNavController(R.id.nav_host_fragment).navigateUp()

    private inner class SuggestionAdapter<String>(context: Context, resource: Int, objects: MutableList<String>, textViewResourceId: Int) :
            ArrayAdapter<String>(context, resource, textViewResourceId, objects) {
        private val items = ArrayList<String>(objects)
        private var filterItems = mutableListOf<String>()
        private var filter = object: Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults =
                FilterResults().apply {
                    filterItems.clear()
                    filterItems.addAll(if(constraint != null) {
                        items.filter { s -> s.toString().contains(constraint, true) }
                    } else items)
                    values = filterItems
                    count = filterItems.size
                }
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(objects.isNotEmpty()) clear()
                if(results != null && results.count > 0) {
                    addAll(filterItems)
                    notifyDataSetChanged()
                } else notifyDataSetInvalidated()
            }
        }

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getItem(position: Int): String? = filterItems[position]

        override fun getCount(): Int = filterItems.size

        override fun getFilter(): Filter = filter

        fun updateList(newList: List<String>?) {
            items.clear()
            newList?.let { items.addAll(it) }
            notifyDataSetChanged()
        }
    }

    private fun saveSearchHistoryItem(item: String) {
        if(preferences.getBoolean(getString(R.string.pref_search_history_key), false) && item.isNotBlank()) {
            val jsonString = JSONArray(getSearchHistoryItems(preferences).run {
                // Remove item in the list if already in history
                removeAll { s -> s == item }
                // Prepend item to top of list and remove older ones if more than 10 items
                (listOf(item).plus(this)).take(10)
            }).toString()
            preferences.edit {
                putString(getString(R.string.pref_search_history_data_key), jsonString)
            }
        }
    }

    private fun getSearchHistoryItems(preferences: SharedPreferences?) = mutableListOf<String>().apply {
        val json = JSONArray(preferences?.getString(getString(R.string.pref_search_history_data_key), "[]"))
        (0 until json.length()).forEach {
            add(json.get(it).toString())
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key == getString(R.string.pref_search_history_data_key))
            searchHistoryAdapter.updateList(getSearchHistoryItems(sharedPreferences))
        else if(key == getString(R.string.pref_search_history_key) &&
                sharedPreferences?.getBoolean(key, false) == false)
            sharedPreferences.edit { remove(getString(R.string.pref_search_history_data_key)) }
        else if(key == getString(R.string.pref_use_fake_update_manager_key)) {
            // Restart activity to use different AppUpdateManager
            baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }?.run { startActivity(this) }
        }
    }

    override fun onStateUpdate(state: InstallState?) {
        appUpdateViewModel.installState.postValue(state)
        val wasManualUpdate = appUpdateViewModel.checkForFlexibleUpdate.value?.peekContent() ?: false
        if((state?.installErrorCode() != InstallErrorCode.NO_ERROR ||
                state.installErrorCode() != InstallErrorCode.NO_ERROR_PARTIALLY_ALLOWED
                || state.installStatus() == InstallStatus.FAILED) && wasManualUpdate) {
            Timber.d("Failed")
            Snackbar.make(binding.coordinatorLayout,
                    getString(R.string.update_download_failed), Snackbar.LENGTH_INDEFINITE)
                    .setAnchorView(R.id.bottom_navigation)
                    .setActionTextColor(ContextCompat.getColor(this@MainActivity, R.color.imageSecondary))
                    .setAction(R.string.update_retry)
                    { appUpdateViewModel.sendCheckForFlexibleUpdate() }.show()
        } else
            when(state?.installErrorCode()) {
                InstallErrorCode.ERROR_API_NOT_AVAILABLE -> Timber.e("The API is not available on this device.")
                InstallErrorCode.ERROR_DOWNLOAD_NOT_PRESENT -> Timber.e("The install/update has not been (fully) downloaded yet.")
                InstallErrorCode.ERROR_INSTALL_NOT_ALLOWED -> Timber.e("The download/install is not allowed, due to the current device state.")
                InstallErrorCode.ERROR_INSTALL_UNAVAILABLE -> Timber.e("The install is unavailable to this user or device.")
                InstallErrorCode.ERROR_INTERNAL_ERROR -> Timber.e("An internal error happened in the Play Store.")
                InstallErrorCode.ERROR_INVALID_REQUEST -> Timber.e("The request that was sent by the app is malformed.")
                InstallErrorCode.ERROR_UNKNOWN -> Timber.e("An unknown error occurred.")
                InstallErrorCode.NO_ERROR, InstallErrorCode.NO_ERROR_PARTIALLY_ALLOWED -> {
                    val text = if(state.installErrorCode() == InstallErrorCode.NO_ERROR)
                        "No error occurred; all types of update flow are allowed."
                    else "No error occurred; only some types of update flow are allowed, while others are forbidden."
                        Timber.e(text)
                    when(state.installStatus()) {
                        InstallStatus.CANCELED -> Timber.d("Canceled")
                        InstallStatus.DOWNLOADING -> Timber.d("Downloading")
                        InstallStatus.INSTALLED -> Timber.d("Installed")
                        InstallStatus.INSTALLING -> Timber.d("Installing")
                        InstallStatus.PENDING -> Timber.d("Pending")
                        InstallStatus.REQUIRES_UI_INTENT -> Timber.d("Required UI Intent")
                        InstallStatus.UNKNOWN -> Timber.d("Unknown")
                        InstallStatus.DOWNLOADED -> {
                            Timber.d("Downloaded")
                            // After the update is downloaded, show a notification
                            // and request user confirmation to restart the app.
                            Snackbar.make(binding.coordinatorLayout,
                                    getString(R.string.update_download_finished), Snackbar.LENGTH_INDEFINITE)
                                    .setAnchorView(R.id.bottom_navigation)
                                    .setActionTextColor(ContextCompat.getColor(this@MainActivity, R.color.imageSecondary))
                                    .setAction(getString(R.string.update_restart))
                                    { appUpdateViewModel.getUpdateManager().completeUpdate()}.show()
                        }
                    }
                }
            }
    }
}
