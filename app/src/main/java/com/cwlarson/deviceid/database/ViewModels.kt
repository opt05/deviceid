package com.cwlarson.deviceid.database

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.ItemType
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
        viewModelScope.launch {
            if(!noStatus) status.postValue(Status.LOADING)
            status.postValue(database.populateAsync(getApplication(), itemType ?: ItemType.NONE))
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

    fun loadAllData(context: Context?) {
        viewModelScope.launch { database.populateAsync(context) }
    }
}

class DoubleTrigger<A, B>(a: LiveData<A>, b: LiveData<B>) : MediatorLiveData<Pair<A?, B?>>() {
    init {
        addSource(a) { value = it to b.value }
        addSource(b) { value = a.value to it }
    }
}