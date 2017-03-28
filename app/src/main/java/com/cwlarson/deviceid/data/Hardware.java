package com.cwlarson.deviceid.data;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.databinding.ChartItem;
import com.cwlarson.deviceid.databinding.Item;
import com.cwlarson.deviceid.util.MyAdapter;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.content.Context.ACTIVITY_SERVICE;

public class Hardware {
    private final String TAG = Hardware.class.getSimpleName();
    private final Activity activity;
    private final Context context;
    // IDs reserved 25001-30000

    public Hardware(Activity activity){
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    public void setHardwareTiles(final MyAdapter mAdapter){
        new AsyncTask<Void, Item, Void>() {
            @Override
            protected void onProgressUpdate(Item... values) {
                if(mAdapter!=null) mAdapter.add(values[0]);
            }

            @Override
            protected Void doInBackground(Void... aVoid) {
                publishProgress(getDeviceScreenDensity());
                publishProgress(getRamSize());
                publishProgress(getFormattedInternalMemory());
                publishProgress(getFormattedExternalMemory());
                publishProgress(getBattery());
                return null;
            }
        }.execute();
    }

    private Item getDeviceScreenDensity() {
        Item item = new Item(25001,"Screen Density",context.getResources().getString(R.string.not_found));
        int density = context.getResources().getDisplayMetrics().densityDpi;
        int width = 0, height = 0;
        final DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Method mGetRawH, mGetRawW;

        try {
            // For JellyBean 4.2 (API 17) and onward
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(metrics);

                width = metrics.widthPixels;
                height = metrics.heightPixels;
            } else {
                mGetRawH = Display.class.getMethod("getRawHeight");
                mGetRawW = Display.class.getMethod("getRawWidth");

                try {
                    width = (Integer) mGetRawW.invoke(display);
                    height = (Integer) mGetRawH.invoke(display);
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                    Log.e(TAG, "IllegalArgumentException in getDeviceScreenDensity");
                }
            }
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
            Log.e(TAG, "NoSuchMethodException in getDeviceScreenDensity");
        }
        String sizeInPixels = " ("+Integer.toString(height)+"x"+Integer.toString(width)+")";

        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                item.setSubTitle("LDPI"+sizeInPixels);
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                item.setSubTitle("MDPI"+sizeInPixels);
                break;
            case DisplayMetrics.DENSITY_HIGH:
                item.setSubTitle("HDPI"+sizeInPixels);
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                item.setSubTitle("XHDPI"+sizeInPixels);
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                item.setSubTitle("XXHDPI"+sizeInPixels);
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                item.setSubTitle("XXXHDPI"+sizeInPixels);
                break;
            case DisplayMetrics.DENSITY_TV:
                item.setSubTitle("TVDPI"+sizeInPixels);
                break;
            case DisplayMetrics.DENSITY_400:
                item.setSubTitle("400DPI"+sizeInPixels);
                break;
            case DisplayMetrics.DENSITY_560:
                item.setSubTitle("560DPI"+sizeInPixels);
                break;
            default:
                item.setSubTitle(context.getResources().getString(R.string.not_found));
                break;
        }
        return item;
    }

    private Item getRamSize() {
        Item item = new Item(25003,"Memory",context.getString(R.string.not_found));
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        item.setSubTitle(mi.totalMem<=0?context.getResources().getString(R.string.not_found):context.getResources().getString(R.string.hardware_storage_output_format, Formatter.formatFileSize(context,mi.availMem), Formatter.formatFileSize(context,mi.totalMem)));
        item.setChartItem(new ChartItem(mi.availMem,mi.totalMem,R.drawable.ic_memory));
        return item;
    }

    private Item getFormattedInternalMemory() {
        Item item = new Item(25004,"Internal Storage",context.getString(R.string.not_found));
        long available = new File(context.getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
        long total = new File(context.getFilesDir().getAbsoluteFile().toString()).getTotalSpace();
        item.setSubTitle(total<=0L?context.getResources().getString(R.string.not_found):context.getResources().getString(R.string.hardware_storage_output_format, Formatter.formatFileSize(context,available), Formatter.formatFileSize(context,total)));
        item.setChartItem(new ChartItem(available,total,R.drawable.ic_storage));
        return item;
    }

    private Item getFormattedExternalMemory() {
        Item item =  new Item(25005,"External Storage",context.getString(R.string.not_found));
        long availSize=0L,totalSize=0L;
        File[] appsDir = ContextCompat.getExternalFilesDirs(activity,null);
        for(File file : appsDir) {
            availSize += file.getParentFile().getParentFile().getParentFile().getParentFile().getFreeSpace();
            totalSize += file.getParentFile().getParentFile().getParentFile().getParentFile().getTotalSpace();
        }
        availSize-=new File(context.getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
        totalSize-=new File(context.getFilesDir().getAbsoluteFile().toString()).getTotalSpace();
        item.setSubTitle(totalSize<=0L?context.getResources().getString(R.string.not_found):context.getResources().getString(R.string.hardware_storage_output_format, Formatter.formatFileSize(context, availSize), Formatter.formatFileSize(context, totalSize)));
        item.setChartItem(new ChartItem(availSize,totalSize,R.drawable.ic_storage));
        return item;
    }

    private Item getBattery() {
        Item item = new Item(25006,"Battery",context.getResources().getString(R.string.not_found));
        context.registerReceiver(item.getInfoReceiver(),new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return item;
    }
}