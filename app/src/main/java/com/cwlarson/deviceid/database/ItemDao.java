package com.cwlarson.deviceid.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.Update;

import com.cwlarson.deviceid.databinding.Item;
import com.cwlarson.deviceid.databinding.ItemType;
import com.cwlarson.deviceid.databinding.ItemTypeConverter;

import java.util.List;

@Dao
public abstract class ItemDao {

  @Query("SELECT * FROM item WHERE itemtype = :type order by title")
  @TypeConverters(ItemTypeConverter.class)
  abstract LiveData<List<Item>> getAllItems(ItemType type);

  @Query("SELECT * FROM item WHERE unavailabletype IS NULL AND itemtype = :type order by title")
  @TypeConverters(ItemTypeConverter.class)
  abstract LiveData<List<Item>> getAllAvailableItems(ItemType type);

  @Query("SELECT * FROM item WHERE (title LIKE :query OR subtitle LIKE :query) order by title")
  @TypeConverters(ItemTypeConverter.class)
  abstract LiveData<List<Item>> getAllSearchItems(String query);

  @Query("SELECT * FROM item WHERE" +
      " (title LIKE :query OR subtitle LIKE :query)" +
      " AND unavailabletype IS NULL order by title")
  @TypeConverters(ItemTypeConverter.class)
  abstract LiveData<List<Item>> getAllAvailableSearchItems(String query);

  @Transaction
  @Query("SELECT * FROM item WHERE title = :title AND itemtype = :type LIMIT 1")
  @TypeConverters(ItemTypeConverter.class)
  abstract LiveData<Item> getItem(String title, ItemType type);

  @Update(onConflict  = OnConflictStrategy.IGNORE)
  protected abstract void update(Item... items);

  @Insert(onConflict  = OnConflictStrategy.IGNORE)
  protected abstract void insert(Item... items);

  @Transaction
  public void insertItems(Item... items) {
    insert(items);
    update(items);
  }
}
