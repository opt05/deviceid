package com.cwlarson.deviceid.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

import com.cwlarson.deviceid.BuildConfig
import com.cwlarson.deviceid.databinding.Item

// Since this is an in memory database, do not store schema
@Database(entities = [(Item::class)], version = 1, exportSchema = false)
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
