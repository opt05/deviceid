package com.cwlarson.deviceid.database

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.text.TextUtils
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.ItemType
import com.cwlarson.deviceid.databinding.UnavailableItem
import com.cwlarson.deviceid.databinding.UnavailableType
import com.cwlarson.deviceid.util.WeakReferenceDelegate

enum class Status {
    LOADING, ERROR, SUCCESS
}

object DatabaseInitializer {
    private var mListener: OnPopulate? by WeakReferenceDelegate()

    interface OnPopulate {
        fun status(status: Status)
    }

    fun populateAsync(activity: Activity?, db: AppDatabase?, type: ItemType?, listener: OnPopulate) {
        if (db == null || type == null) return
        mListener = listener
        PopulateDbAsync(activity, db, type).execute()
    }

    fun populateAsync(activity: Activity?, db: AppDatabase?) {
        if (db == null) return
        PopulateDbAsync(activity, db).execute()
    }

    private class PopulateDbAsync : AsyncTask<Void, Boolean, Void> {
        private val mDb: AppDatabase
        private val mItemType: ItemType?
        private var mActivity: Activity? by WeakReferenceDelegate()

        internal constructor(activity: Activity?, db: AppDatabase, type: ItemType) {
            mActivity = activity
            mDb = db
            mItemType = type
        }

        internal constructor(activity: Activity?, db: AppDatabase) {
            mActivity = activity
            mDb = db
            mItemType = null
        }

        override fun onProgressUpdate(vararg values: Boolean?) {
            if (values.isEmpty() || values[0]?.not() != false) {
                mListener?.status(com.cwlarson.deviceid.database.Status.LOADING)
            } else {
                mListener?.status(com.cwlarson.deviceid.database.Status.SUCCESS)
            }
        }

        override fun doInBackground(vararg voids: Void): Void? {
            publishProgress()
            if (mItemType == null) {
                mActivity?.let {
                    Device(it, mDb)
                    Network(it, mDb)
                    Software(it, mDb)
                    Hardware(it, mDb)
                }
            } else {
                when (mItemType) {
                    ItemType.DEVICE -> mActivity?.let { Device(it, mDb) }
                    ItemType.NETWORK -> mActivity?.let { Network(it, mDb) }
                    ItemType.SOFTWARE -> mActivity?.let { Software(it, mDb) }
                    ItemType.HARDWARE -> mActivity?.let { Hardware(it, mDb) }
                    else -> { }
                }
            }
            publishProgress(true)
            return null
        }
    }
}

class ItemAdder internal constructor(context: Context, database: AppDatabase) {
    private var context : Context? by WeakReferenceDelegate()
    private var db: AppDatabase

    init {
        this.context = context
        this.db = database
    }

    fun addItems(item: Item?) {
        if(context==null || item==null) return
        if (TextUtils.isEmpty(item.subtitle) && item.unavailableitem == null) {
            item.unavailableitem = UnavailableItem(UnavailableType.NOT_FOUND,
                    context?.getString(R.string.not_found))
        } else if(TextUtils.isEmpty(item.subtitle) && item.unavailableitem != null) {
            if(item.unavailableitem?.unavailabletype == UnavailableType.NO_LONGER_POSSIBLE) {
                item.unavailableitem?.unavailablesupporttext =
                        context?.resources?.getString(R.string.no_longer_possible, item.unavailableitem?.unavailablesupporttext)
            } else if (item.unavailableitem?.unavailabletype == UnavailableType.NOT_POSSIBLE_YET) {
                item.unavailableitem?.unavailablesupporttext =
                        context?.resources?.getString(R.string.not_possible_yet, item.unavailableitem?.unavailablesupporttext)
            }
        }
        db.itemDao().insertItems(item)
    }
}
