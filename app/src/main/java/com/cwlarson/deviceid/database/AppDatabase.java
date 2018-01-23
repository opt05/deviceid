package com.cwlarson.deviceid.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.cwlarson.deviceid.BuildConfig;
import com.cwlarson.deviceid.databinding.Item;
// Since this is an in memory database, do not store schema
@Database(entities = {Item.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
  private static AppDatabase INSTANCE;

  public abstract ItemDao itemDao();

  public static AppDatabase getDatabase(Context context) {
    if(INSTANCE == null) {
      if(BuildConfig.DEBUG) //So Stetho can work correctly
        INSTANCE = Room.databaseBuilder(context,AppDatabase.class,"debug")
            .fallbackToDestructiveMigration().build();
      else
        INSTANCE = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    }
    return INSTANCE;
  }

  public static void destroyInstance() {
    INSTANCE = null;
  }

}
