package com.cwlarson.deviceid.database

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.ItemType

class AllItemsViewModel(application: Application) : AndroidViewModel(application) {
    private val mDatabase: AppDatabase = AppDatabase.getDatabase(application)
    private var mItems: LiveData<List<Item>>? = null
    private val mHideUnavailable = MutableLiveData<Boolean>()
    private val mStatus = MutableLiveData<Status>()

    val status: LiveData<Status>
        get() = mStatus

    init {
        mHideUnavailable.value = false
    }

    /**
     * Used by tab layout
     * @param itemType The type of tab to fetch correct data
     * @return LiveData of all the items of specified type
     */
    fun getAllItems(itemType: ItemType?): LiveData<List<Item>>? {
        if (mItems == null) {
            mItems = Transformations.switchMap(mHideUnavailable) { hideUnavailable ->
                if (hideUnavailable) {
                    itemType?.let { mDatabase.itemDao().getAllAvailableItems(it) }
                } else {
                    itemType?.let { mDatabase.itemDao().getAllItems(it) }
                }
            }
        }
        return mItems
    }

    fun setHideUnavailable(hideUnavailable: Boolean) {
        this.mHideUnavailable.value = hideUnavailable
    }

    fun setStatus(status: Status) {
        this.mStatus.value = status
    }

}

class SearchItemsViewModel(application: Application) : AndroidViewModel(application) {
    private val mDatabase: AppDatabase = AppDatabase.getDatabase(application)
    private var mItems: LiveData<List<Item>>? = null
    private val mHideUnavailable = MutableLiveData<Boolean>()

    init {
        mHideUnavailable.value = false
    }

    /**
     * Used by SearchActivity
     * @return LiveData of all items
     */
    fun getAllSearchItems(searchString: String): LiveData<List<Item>>? {
        if (mItems == null) {
            mItems = Transformations.switchMap(mHideUnavailable) { hideUnavailable ->
                if (hideUnavailable) {
                    mDatabase.itemDao().getAllAvailableSearchItems("%$searchString%")
                } else {
                    mDatabase.itemDao().getAllSearchItems("%$searchString%")
                }
            }
        }
        return mItems
    }

    fun setHideUnavailable(hideUnavailable: Boolean) {
        this.mHideUnavailable.value = hideUnavailable
    }

}

class BottomSheetViewModel(application: Application) : AndroidViewModel(application) {
    private var item: LiveData<Item>? = null
    private val mDatabase: AppDatabase = AppDatabase.getDatabase(application)

    /**
     * Used by ItemClickDialog
     * @return LiveData of all items
     */
    fun getItem(title: String, type: ItemType): LiveData<Item>? {
        if (item == null) {
            item = mDatabase.itemDao().getItem(title, type)
        }
        return item
    }

}