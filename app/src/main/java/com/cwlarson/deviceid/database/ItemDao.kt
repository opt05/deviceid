package com.cwlarson.deviceid.database

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.ItemType

@Dao
interface ItemDao {
    @Query("SELECT * FROM item WHERE itemType = :type order by title")
    fun getAllItems(type: ItemType): DataSource.Factory<Int, Item>

    @Query("SELECT * FROM item WHERE unavailableType IS NULL AND itemType = :type order by title")
    fun getAllAvailableItems(type: ItemType): DataSource.Factory<Int, Item>

    @Query("SELECT * FROM item WHERE (title LIKE :query OR subtitle LIKE :query) order by title")
    fun getAllSearchItems(query: String): DataSource.Factory<Int, Item>

    @Query("SELECT * FROM item WHERE (title LIKE :query OR subtitle LIKE :query)" +
            " AND unavailableType IS NULL order by title")
    fun getAllAvailableSearchItems(query: String): DataSource.Factory<Int, Item>

    @Query("SELECT * FROM item WHERE title = :title AND itemType = :type")
    fun getItem(title: String?, type: ItemType?): LiveData<Item?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg items: Item)
}
