package com.cwlarson.deviceid.database

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.ItemType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AllItemsViewModel(application: Application) : AndroidViewModel(application) {
    private val database: AppDatabase = AppDatabase.getDatabase(application)
    private var items: LiveData<PagedList<Item>>? = null
    private val hideUnavailable = MutableLiveData<Boolean>()
    val status = MutableLiveData<Status?>()
    val itemsCount = MutableLiveData<Int>()
    init {
        hideUnavailable.value = false
    }

    /**
     * Used by tab layout
     * @param itemType The type of tab to fetch correct data
     * @return LiveData of all the items of specified type
     */
    fun getAllItems(itemType: ItemType?): LiveData<PagedList<Item>>? {
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

    fun loadData(context: Context?, itemType: ItemType?) {
        GlobalScope.launch(Dispatchers.Main) {
            status.value = Status.LOADING
            status.value = database.populateAsync(context, itemType).await()
        }
    }

}

class SearchItemsViewModel(application: Application) : AndroidViewModel(application) {
    private val database: AppDatabase = AppDatabase.getDatabase(application)
    private var items: LiveData<PagedList<Item>>? = null
    private val hideUnavailable = MutableLiveData<Boolean>()
    val itemsCount = MutableLiveData<Int>()
    val isLoading = MutableLiveData<Boolean>()
    init {
        hideUnavailable.value = false
        itemsCount.value = 0
        isLoading.value = true
    }

    /**
     * Used by SearchActivity
     * @return LiveData of all items
     */
    fun getAllSearchItems(searchString: String): LiveData<PagedList<Item>>? {
        if (items == null) {
            items = Transformations.switchMap(hideUnavailable) { hideUnavailable ->
                if (hideUnavailable) {
                    database.itemDao().getAllAvailableSearchItems("%$searchString%").toLiveData(20)
                } else {
                    database.itemDao().getAllSearchItems("%$searchString%").toLiveData(20)
                }
            }
        }
        return items
    }

    fun setHideUnavailable(hideUnavailable: Boolean) {
        this.hideUnavailable.value = hideUnavailable
    }

}

class BottomSheetViewModel(application: Application) : AndroidViewModel(application) {
    private var item: LiveData<Item>? = null
    private val database: AppDatabase = AppDatabase.getDatabase(application)

    /**
     * Used by ItemClickDialog
     * @return LiveData of all items
     */
    fun getItem(title: String, type: ItemType): LiveData<Item>? {
        if (item == null) {
            item = database.itemDao().getItem(title, type)
        }
        return item
    }

}

class MainActivityViewModel: ViewModel() {
    var searchString: String? = null
    var searchFocus: Boolean = false
}