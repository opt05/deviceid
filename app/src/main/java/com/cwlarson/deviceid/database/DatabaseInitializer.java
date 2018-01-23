package com.cwlarson.deviceid.database;

import android.app.Activity;
import android.os.AsyncTask;

import com.cwlarson.deviceid.databinding.ItemType;

import java.lang.ref.WeakReference;

public class DatabaseInitializer {
  public interface OnPopulate {
    void status(Status status);
  }
  private static WeakReference<OnPopulate> mListener = new WeakReference<>(null);

  public static void populateAsync(Activity activity, final AppDatabase db, final ItemType type,
                                   OnPopulate listener) {
    if(db==null || type==null) return;
    mListener = new WeakReference<>(listener);
    new PopulateDbAsync(activity, db, type).execute();
  }

  public static void populateAsync(Activity activity, final AppDatabase db) {
    if(db==null) return;
    new PopulateDbAsync(activity, db).execute();
  }

  private static class PopulateDbAsync extends AsyncTask<Void, Boolean, Void> {
    private final AppDatabase mDb;
    private final ItemType mItemType;
    private WeakReference<Activity> mActivity = new WeakReference<>(null);

    PopulateDbAsync(Activity activity, AppDatabase db, ItemType type) {
      mActivity = new WeakReference<>(activity);
      mDb = db;
      mItemType = type;
    }

    PopulateDbAsync(Activity activity, AppDatabase db) {
      mActivity = new WeakReference<>(activity);
      mDb = db;
      mItemType = null;
    }

    @Override
    protected void onProgressUpdate(Boolean... values) {
      if(values == null || values.length <=0 || !values[0]) {
        if(mListener.get()!=null) mListener.get().status(com.cwlarson.deviceid.database.Status.LOADING);
      } else {
        if(mListener.get()!=null) mListener.get().status(com.cwlarson.deviceid.database.Status.SUCCESS);
      }
    }

    @Override
    protected Void doInBackground(Void... voids) {
      publishProgress();
      if(mItemType == null) {
        if(mActivity.get()!=null) new Device(mActivity.get(), mDb);
        if(mActivity.get()!=null) new Network(mActivity.get(), mDb);
        if(mActivity.get()!=null) new Software(mActivity.get(), mDb);
        if(mActivity.get()!=null) new Hardware(mActivity.get(), mDb);
      } else {
        switch (mItemType) {
          case DEVICE:
            if (mActivity.get() != null) new Device(mActivity.get(), mDb);
            break;
          case NETWORK:
            if (mActivity.get() != null) new Network(mActivity.get(), mDb);
            break;
          case SOFTWARE:
            if (mActivity.get() != null) new Software(mActivity.get(), mDb);
            break;
          case HARDWARE:
            if (mActivity.get() != null) new Hardware(mActivity.get(), mDb);
            break;
        }
      }
      publishProgress(true);
      return null;
    }
  }
}
