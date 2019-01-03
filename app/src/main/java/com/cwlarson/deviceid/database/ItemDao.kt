package com.cwlarson.deviceid.database

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.ItemType
import com.cwlarson.deviceid.databinding.ItemTypeConverter

@Dao
abstract class ItemDao {

    @Query("SELECT * FROM item WHERE itemtype = :type order by title")
    @TypeConverters(ItemTypeConverter::class)
    abstract fun getAllItems(type: ItemType): DataSource.Factory<Int, Item>

    @Query("SELECT * FROM item WHERE unavailabletype IS NULL AND itemtype = :type order by title")
    @TypeConverters(ItemTypeConverter::class)
    abstract fun getAllAvailableItems(type: ItemType): DataSource.Factory<Int, Item>

    @Query("SELECT * FROM item WHERE (title LIKE :query OR subtitle LIKE :query) order by title")
    @TypeConverters(ItemTypeConverter::class)
    abstract fun getAllSearchItems(query: String): DataSource.Factory<Int, Item>

    @Query("SELECT * FROM item WHERE" +
            " (title LIKE :query OR subtitle LIKE :query)" +
            " AND unavailabletype IS NULL order by title")
    @TypeConverters(ItemTypeConverter::class)
    abstract fun getAllAvailableSearchItems(query: String): DataSource.Factory<Int, Item>

    @Transaction
    @Query("SELECT * FROM item WHERE title = :title AND itemtype = :type LIMIT 1")
    @TypeConverters(ItemTypeConverter::class)
    abstract fun getItem(title: String, type: ItemType): LiveData<Item>

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun update(vararg items: Item)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insert(vararg items: Item)

    @Transaction
    open fun insertItems(vararg items: Item) {
        insert(*items)
        update(*items)
    }
}
