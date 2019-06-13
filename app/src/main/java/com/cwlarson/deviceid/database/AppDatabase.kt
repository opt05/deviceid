package com.cwlarson.deviceid.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cwlarson.deviceid.BuildConfig
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Since this is an in memory database, do not store schema
@Database(entities = [(Item::class)], version = 1)
@TypeConverters(ItemTypeConverter::class, UnavailableTypeConverter::class,
        UnavailablePermissionConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                if (BuildConfig.DEBUG) {
                    //So Stetho can work correctly
                    Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "debug")
                            .fallbackToDestructiveMigration().build()
                } else {
                    Room.inMemoryDatabaseBuilder(context.applicationContext, AppDatabase::class.java)
                            .fallbackToDestructiveMigration().build()
                }.also { INSTANCE = it }
        }
    }
}

enum class Status { LOADING, ERROR, SUCCESS }

suspend fun AppDatabase.populateAsync(context: Context?, type: ItemType? = null): Status =
    withContext(Dispatchers.IO) {
        context?.let {
            when (type) {
                ItemType.DEVICE -> Device(context, this@populateAsync, this)
                ItemType.NETWORK -> Network(context, this@populateAsync, this)
                ItemType.SOFTWARE -> Software(context, this@populateAsync, this)
                ItemType.HARDWARE -> Hardware(context, this@populateAsync, this)
                else -> {
                    Device(context, this@populateAsync, this)
                    Network(context, this@populateAsync, this)
                    Software(context, this@populateAsync, this)
                    Hardware(context, this@populateAsync, this)
                }
            }
            Status.SUCCESS
        } ?: Status.ERROR
    }

suspend fun AppDatabase.addItems(context: Context, vararg items: Item) {
    items.forEach {
        if (it.subtitle.isNullOrBlank()) {
            if (it.unavailableItem == null) {
                it.unavailableItem = UnavailableItem(UnavailableType.NOT_FOUND, context
                        .getString(R.string.not_found))
            } else {
                it.unavailableItem?.unavailableSupportText = when (it.unavailableItem?.unavailableType) {
                    UnavailableType.NOT_POSSIBLE_YET -> context.resources.getString(
                            R.string.not_possible_yet, it.unavailableItem?.unavailableSupportText)
                    UnavailableType.NO_LONGER_POSSIBLE -> context.resources.getString(
                            R.string.no_longer_possible, it.unavailableItem?.unavailableSupportText)
                    else -> it.unavailableItem?.unavailableSupportText
                }
            }
        }
    }
    this@addItems.itemDao().insert(*items)
}
