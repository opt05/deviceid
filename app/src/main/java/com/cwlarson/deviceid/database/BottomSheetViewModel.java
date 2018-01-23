package com.cwlarson.deviceid.database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.cwlarson.deviceid.databinding.Item;
import com.cwlarson.deviceid.databinding.ItemType;

public class BottomSheetViewModel extends AndroidViewModel {
  private LiveData<Item> item;
  private final AppDatabase mDatabase;

  public BottomSheetViewModel(@NonNull Application application) {
    super(application);
    mDatabase = AppDatabase.getDatabase(application);
  }

  public LiveData<Item> getItem(String title, ItemType type) {
    if(item == null) {
      item = mDatabase.itemDao().getItem(title,type);
    }
    return item;
  }

}
