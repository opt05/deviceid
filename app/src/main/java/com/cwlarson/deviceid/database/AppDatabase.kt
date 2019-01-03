package com.cwlarson.deviceid.database

import android.content.Context
import android.text.TextUtils
import androidx.annotation.WorkerThread
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cwlarson.deviceid.BuildConfig
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.ItemType
import com.cwlarson.deviceid.databinding.UnavailableItem
import com.cwlarson.deviceid.databinding.UnavailableType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

// Since this is an in memory database, do not store schema
@Database(entities = [(Item::class)], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = if (BuildConfig.DEBUG) {
                    //So Stetho can work correctly
                    Room.databaseBuilder(context, AppDatabase::class.java, "debug")
                            .fallbackToDestructiveMigration().build()
                } else {
                    Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
                }
            }
            return INSTANCE as AppDatabase
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

}

enum class Status { LOADING, ERROR, SUCCESS }

fun AppDatabase.populateAsync(context: Context?, type: ItemType? = null) = GlobalScope.async(Dispatchers.IO) {
    return@async context?.let {
        when (type) {
            ItemType.DEVICE -> Device(context, this@populateAsync)
            ItemType.NETWORK -> Network(context, this@populateAsync)
            ItemType.SOFTWARE -> Software(context, this@populateAsync)
            ItemType.HARDWARE -> Hardware(context, this@populateAsync)
            else -> {
                Device(context, this@populateAsync)
                Network(context, this@populateAsync)
                Software(context, this@populateAsync)
                Hardware(context, this@populateAsync)
            }
        }
        Status.SUCCESS
    } ?: Status.ERROR
}

@WorkerThread
fun AppDatabase.addItems(context: Context?, vararg items: Item) {
    context?.let { c ->
        for (item in items) {
            if (TextUtils.isEmpty(item.subtitle) && item.unavailableitem == null) {
                item.unavailableitem = UnavailableItem(UnavailableType.NOT_FOUND,
                        c.getString(R.string.not_found))
            } else if (TextUtils.isEmpty(item.subtitle) && item.unavailableitem != null) {
                if (item.unavailableitem?.unavailabletype == UnavailableType.NO_LONGER_POSSIBLE) {
                    item.unavailableitem?.unavailablesupporttext =
                            c.resources?.getString(R.string.no_longer_possible, item
                                    .unavailableitem?.unavailablesupporttext)
                } else if (item.unavailableitem?.unavailabletype == UnavailableType.NOT_POSSIBLE_YET) {
                    item.unavailableitem?.unavailablesupporttext =
                            c.resources?.getString(R.string.not_possible_yet, item
                                    .unavailableitem?.unavailablesupporttext)
                }
            }
            this@addItems.itemDao().insertItems(item)
        }
    }
}
