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
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hardware {
    String TAG = "Hardware";
    private Activity activity;
    private Context context;

    public Hardware(Activity activity){
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    public void setHardwareTiles(MyAdapter mAdapter){
        mAdapter.addItem(Arrays.asList("Android/Hardware ID", getAndroidID()));
        mAdapter.addItem(Arrays.asList("External Storage", context.getResources().getString(R.string.hardware_storage_output_format, getAvailableExternalMemory(), getTotalExternalMemory())));
        mAdapter.addItem(Arrays.asList("Internal Storage", context.getResources().getString(R.string.hardware_storage_output_format,getAvailableInternalMemory(),getTotalInternalMemory())));
        mAdapter.addItem(Arrays.asList("RAM Size", getRamSize()));
        mAdapter.addItem(Arrays.asList("Screen Density", getDeviceScreenDensity()));
    }

    private String getDeviceScreenDensity() {
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
                    Log.e(TAG, "IllefalArgumentException in getDeviceScreenDensity");
                }
            }
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
            Log.e(TAG, "NoSuchMethodException in getDeviceScreenDensity");
        }
        String sizeInPixels = " ("+Integer.toString(height)+"x"+Integer.toString(width)+")";

        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                return "LDPI"+sizeInPixels;
            case DisplayMetrics.DENSITY_MEDIUM:
                return "MDPI"+sizeInPixels;
            case DisplayMetrics.DENSITY_HIGH:
                return "HDPI"+sizeInPixels;
            case DisplayMetrics.DENSITY_XHIGH:
                return "XHDPI"+sizeInPixels;
            case DisplayMetrics.DENSITY_XXHIGH:
                return "XXHDPI"+sizeInPixels;
            case DisplayMetrics.DENSITY_XXXHIGH:
                return "XXXHDPI"+sizeInPixels;
            case DisplayMetrics.DENSITY_TV:
                return "TVDPI"+sizeInPixels;
            case DisplayMetrics.DENSITY_400:
                return "400DPI"+sizeInPixels;
            case DisplayMetrics.DENSITY_560:
                return "560DPI"+sizeInPixels;
            default:
                return context.getResources().getString(R.string.not_found)+sizeInPixels;
        }
    }

    private String getAndroidID() {
        String android_id="";
        try {
            android_id= Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getAndroidID");
        }
        return android_id==null || android_id.equals("") ? context.getResources().getString(R.string.not_found) : android_id;
    }

    private String getRamSize() {
        RandomAccessFile reader;
        String load;
        String lastValue = "";
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
            lastValue = Formatter.formatFileSize(context,Long.parseLong(value));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return (lastValue.equals("")) ? context.getResources().getString(R.string.not_found) : lastValue;
    }

    private String getAvailableInternalMemory() {
        return Formatter.formatFileSize(context, new File(context.getFilesDir().getAbsoluteFile().toString()).getFreeSpace());
    }

    private String getTotalInternalMemory() {
        return Formatter.formatFileSize(context, new File(context.getFilesDir().getAbsoluteFile().toString()).getTotalSpace());
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
