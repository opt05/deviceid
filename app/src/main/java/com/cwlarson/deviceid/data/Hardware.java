package com.cwlarson.deviceid.data;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.util.MyAdapter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hardware {
    private final String TAG = "Hardware";
    private final Activity activity;
    private final Context context;

    public Hardware(Activity activity){
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    public List<Item> setHardwareTiles(MyAdapter mAdapter){
        List<Item> items = new ArrayList<>();
        items.add(getDeviceScreenDensity());
        items.add(getAndroidID());
        items.add(getRamSize());
        items.add(getFormattedInternalMemory());
        items.add(getFormattedExternalMemory());
        if(mAdapter!=null) mAdapter.addAll(items);
        return items;
    }

    private Item getDeviceScreenDensity() {
        String hardware;
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
                hardware = "LDPI"+sizeInPixels;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                hardware = "MDPI"+sizeInPixels;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                hardware = "HDPI"+sizeInPixels;
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                hardware = "XHDPI"+sizeInPixels;
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                hardware = "XXHDPI"+sizeInPixels;
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                hardware = "XXXHDPI"+sizeInPixels;
                break;
            case DisplayMetrics.DENSITY_TV:
                hardware = "TVDPI"+sizeInPixels;
                break;
            case DisplayMetrics.DENSITY_400:
                hardware = "400DPI"+sizeInPixels;
                break;
            case DisplayMetrics.DENSITY_560:
                hardware = "560DPI"+sizeInPixels;
                break;
            default:
                hardware = context.getResources().getString(R.string.not_found);
                break;
        }
        Item item = new Item(context);
        item.setTitle("Screen Density");
        item.setSubTitle(hardware); 
        return item;
    }

    private Item getAndroidID() {
        String hardware="";
        try {
            hardware= Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getAndroidID");
        }
        Item item = new Item(context);
        item.setTitle("Android/Hardware ID");
        item.setSubTitle(hardware); 
        return item;
    }

    private Item getRamSize() {
        RandomAccessFile reader;
        String load;
        String hardware = "";
        try {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            load = reader.readLine();

            // Get the Number value from the string
            Pattern p = Pattern.compile("(\\d+)");
            Matcher m = p.matcher(load);
            String value = "";
            while (m.find()) {
                value = m.group(1);
            }
            reader.close();
            hardware = Formatter.formatFileSize(context, Long.parseLong(value));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Item item = new Item(context);
        item.setTitle("RAM Size");
        item.setSubTitle(hardware); 
        return item;
    }

    private Item getFormattedInternalMemory() {
        String hardware = context.getResources().getString(R.string.hardware_storage_output_format, getAvailableInternalMemory(), getTotalInternalMemory());
        Item item = new Item(context);
        item.setTitle("Internal Storage");
        item.setSubTitle(hardware); 
        return item;
    }

    private String getAvailableInternalMemory() {
        return Formatter.formatFileSize(context, new File(context.getFilesDir().getAbsoluteFile().toString()).getFreeSpace());
    }

    private String getTotalInternalMemory() {
        return Formatter.formatFileSize(context, new File(context.getFilesDir().getAbsoluteFile().toString()).getTotalSpace());
    }

    private Item getFormattedExternalMemory() {
        String hardware = context.getResources().getString(R.string.hardware_storage_output_format, getAvailableExternalMemory(), getTotalExternalMemory());
        Item item = new Item(context);
        item.setTitle("External Storage");
        item.setSubTitle(hardware); 
        return item;
    }

    private String getAvailableExternalMemory() {
        Long size=0L;
        File[] appsDir = ContextCompat.getExternalFilesDirs(activity,null);
        for(File file : appsDir) {
            size += file.getParentFile().getParentFile().getParentFile().getParentFile().getFreeSpace();
        }
        size-=new File(context.getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
        return Formatter.formatFileSize(context, size);
    }

    private String getTotalExternalMemory() {
        Long size=0L;
        File[] appsDir = ContextCompat.getExternalFilesDirs(activity,null);
        for(File file : appsDir) {
            size += file.getParentFile().getParentFile().getParentFile().getParentFile().getTotalSpace();
        }
        size-=new File(context.getFilesDir().getAbsoluteFile().toString()).getTotalSpace();
        return Formatter.formatFileSize(context, size);
    }
}
