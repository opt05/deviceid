package com.cwlarson.deviceid.database;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.cwlarson.deviceid.databinding.Item;

import java.util.List;

public class SearchItemsViewModel extends AndroidViewModel {
  private final AppDatabase mDatabase;
  private LiveData<List<Item>> mItems;
  private final MutableLiveData<Boolean> mHideUnavailable = new MutableLiveData<>();

  public SearchItemsViewModel(@NonNull Application application) {
    super(application);
    mDatabase = AppDatabase.getDatabase(application);
    mHideUnavailable.setValue(false);
  }

  /**
   * Used by SearchActivityOld
   * @return LiveData of all items
   */
  public LiveData<List<Item>> getAllSearchItems(final String searchString) {
    if(mItems == null) {
      mItems = Transformations.switchMap(mHideUnavailable, new Function<Boolean, LiveData<List<Item>>>() {
        @Override
        public LiveData<List<Item>> apply(Boolean hideUnavailable) {
          if (hideUnavailable) {
            return mDatabase.itemDao().getAllAvailableSearchItems(searchString);
          } else {
            return mDatabase.itemDao().getAllSearchItems(searchString);
          }
        }
      });
    }
    return mItems;
  }

  public void setHideUnavailable(boolean hideUnavailable) {
    this.mHideUnavailable.setValue(hideUnavailable);
  }

}
