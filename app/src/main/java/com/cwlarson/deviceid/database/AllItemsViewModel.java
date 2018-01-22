package com.cwlarson.deviceid.database;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.cwlarson.deviceid.databinding.Item;
import com.cwlarson.deviceid.databinding.ItemType;

import java.util.List;

public class AllItemsViewModel extends AndroidViewModel {
  private final AppDatabase mDatabase;
  private LiveData<List<Item>> mItems;
  private final MutableLiveData<Boolean> mHideUnavailable = new MutableLiveData<>();
  private final MutableLiveData<Status> mStatus = new MutableLiveData<>();

  public AllItemsViewModel(@NonNull Application application) {
    super(application);
    mDatabase = AppDatabase.getDatabase(application);
    mHideUnavailable.setValue(false);
  }

  /**
   * Used by tab layout
   * @param itemType The type of tab to fetch correct data
   * @return LiveData of all the items of specified type
   */
  public LiveData<List<Item>> getAllItems(final ItemType itemType) {
    if(mItems == null) {
      mItems = Transformations.switchMap(mHideUnavailable, new Function<Boolean,
          LiveData<List<Item>>>() {
        @Override
        public LiveData<List<Item>> apply(Boolean hideUnavailable) {
          if (hideUnavailable) {
            return mDatabase.itemDao().getAllAvailableItems(itemType);
          } else {
            return mDatabase.itemDao().getAllItems(itemType);
          }
        }
      });
    }
    return mItems;
  }

  public void setHideUnavailable(boolean hideUnavailable) {
    this.mHideUnavailable.setValue(hideUnavailable);
  }

  public LiveData<Status> getStatus() {
    return mStatus;
  }

  public void setStatus(Status status) {
    this.mStatus.setValue(status);
  }

}
