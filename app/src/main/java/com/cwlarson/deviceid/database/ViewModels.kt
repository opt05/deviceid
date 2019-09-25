package com.cwlarson.deviceid.database

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.ItemType
import com.cwlarson.deviceid.util.FakeAppUpdateManagerWrapper
import com.cwlarson.deviceid.util.UpdateState
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AllItemsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private var items: LiveData<PagedList<Item>>? = null
    private val hideUnavailable = MutableLiveData<Boolean>()
    private var itemType: ItemType? = null
    val status = MutableLiveData<Status?>()
    val itemsCount = MutableLiveData<Int>()
    val refreshDisabled = MutableLiveData<Boolean>()
    init {
        hideUnavailable.value = false
    }

    fun initialize(itemType: ItemType) {
        if(itemType == this.itemType) return
        this.itemType = itemType
    }

    /**
     * Used by tab layout
     * @return LiveData of all the items of specified type
     */
    fun getAllItems(): LiveData<PagedList<Item>>? {
        if (items == null) {
            items = Transformations.switchMap(hideUnavailable) { hideUnavailable ->
                if (hideUnavailable) {
                    itemType?.let { database.itemDao().getAllAvailableItems(it).toLiveData(20) }
                } else {
                    itemType?.let { database.itemDao().getAllItems(it).toLiveData(20) }
                }
            }
        }
        return items
    }

    fun setHideUnavailable(hideUnavailable: Boolean) {
        this.hideUnavailable.value = hideUnavailable
    }

    fun refreshData(noStatus: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if(!noStatus) status.postValue(Status.LOADING)
            status.postValue(database.populateAsync(getApplication(),  itemType ?: ItemType.NONE))
        }
    }
}

class SearchItemsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val searchText = MutableLiveData<String?>()
    private val hideUnavailable = MutableLiveData<Boolean>()
    val itemsCount = MutableLiveData<Int>()
    val isLoading = MutableLiveData<Boolean>()
    init {
        hideUnavailable.value = false
        itemsCount.value = 0
        isLoading.value = true
    }
    val searchItems: LiveData<PagedList<Item>> = Transformations.switchMap(DoubleTrigger(searchText, hideUnavailable)) { pair ->
        when {
            pair.first.isNullOrBlank() -> MutableLiveData<PagedList<Item>>().apply { value = null }
            pair.second == true -> database.itemDao().getAllAvailableSearchItems("%${pair.first}%").toLiveData(20)
            else -> database.itemDao().getAllSearchItems("%${pair.first}%").toLiveData(20)
        }
    }

    /**
     * Used by [com.cwlarson.deviceid.MainActivity]
     * to give search text to [com.cwlarson.deviceid.SearchFragment]
     */
    fun setSearchText(searchString: String?) {
        this.searchText.value = searchString
    }

    fun setHideUnavailable(hideUnavailable: Boolean) {
        this.hideUnavailable.value = hideUnavailable
    }

}

class BottomSheetViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val itemDetails = MutableLiveData<Pair<String, ItemType>?>()
    var item: LiveData<Item?> = Transformations.switchMap(itemDetails) { pair ->
        database.itemDao().getItem(pair?.first, pair?.second)
    }
    /**
     * Used by ItemClickDialog
     * @return LiveData of all items
     */
    fun setItem(title: String, type: ItemType) {
        itemDetails.value = Pair(title, type)
    }
}

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val hideSearchBar = MutableLiveData<Boolean>()
    val hideBottomBar = MutableLiveData<Boolean>()
    val isSearchOpen = MutableLiveData<Boolean>()
    val titleVisibility = MutableLiveData<Pair<Boolean, Int>>()
    var twoPane: Boolean = false
        private set

    fun loadAllData() {
        viewModelScope.launch(Dispatchers.IO) { database.populateAsync(getApplication<Application>().applicationContext) }
    }

    fun initialize(twoPane: Boolean) {
        if(this.twoPane == twoPane) return
        this@MainActivityViewModel.twoPane = twoPane
    }
}

class AppUpdateViewModel(application: Application) : AndroidViewModel(application) {
    val checkForFlexibleUpdate = MutableLiveData<Event<Boolean>>()
    val updateStatus = MutableLiveData<UpdateState>()
    val installState = MutableLiveData<InstallState>()
    val useFakeAppUpdateManager = MutableLiveData<Boolean>()
    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(application)
    }
    val fakeAppUpdateManager by lazy {
        FakeAppUpdateManagerWrapper(application, viewModelScope)
    }

    fun getUpdateManager(): AppUpdateManager =
            if(useFakeAppUpdateManager.value == true) fakeAppUpdateManager else appUpdateManager

    fun sendCheckForFlexibleUpdate() {
        checkForFlexibleUpdate.value = Event(true)
    }
}

/**
 * Used as a wrapper for a double LiveData event to trigger on either data change.
 */
class DoubleTrigger<A, B>(a: LiveData<A>, b: LiveData<B>) : MediatorLiveData<Pair<A?, B?>>() {
    init {
        addSource(a) { value = it to b.value }
        addSource(b) { value = a.value to it }
    }
}

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
open class Event<out T>(private val content: T) {
    @Suppress("MemberVisibilityCanBePrivate")
    var hasBeenHandled = false
        private set // Allow external read but not write
    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? = if (hasBeenHandled) null
    else {
        hasBeenHandled = true
        content
    }
    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}