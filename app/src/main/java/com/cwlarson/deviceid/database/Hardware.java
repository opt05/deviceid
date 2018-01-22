package com.cwlarson.deviceid.database;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.databinding.ChartItem;
import com.cwlarson.deviceid.databinding.Item;
import com.cwlarson.deviceid.databinding.ItemType;

import java.io.File;
import java.lang.reflect.Method;

import static android.content.Context.ACTIVITY_SERVICE;

class Hardware {
    private final String TAG = Hardware.class.getSimpleName();
    private final Context context;

    Hardware(Activity activity, AppDatabase db){
        this.context = activity.getApplicationContext();
        //Set Hardware Tiles
        ItemAdder itemAdder = new ItemAdder(context, db);
        itemAdder.addItems(getDeviceScreenDensity());
        itemAdder.addItems(getRamSize());
        itemAdder.addItems(getFormattedInternalMemory());
        itemAdder.addItems(getFormattedExternalMemory());
        getBattery();
    }

    private Item getDeviceScreenDensity() {
        Item item = new Item("Screen Density", ItemType.HARDWARE);
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
                } catch (Exception e) {
                    Log.e(TAG, "IllegalArgumentException in getDeviceScreenDensity");
                }
            }
        } catch (Exception e3) {
            Log.e(TAG, "NoSuchMethodException in getDeviceScreenDensity");
        }
        String sizeInPixels = " ("+Integer.toString(height)+"x"+Integer.toString(width)+")";

        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                item.setSubtitle("LDPI"+sizeInPixels);
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                item.setSubtitle("MDPI"+sizeInPixels);
                break;
            case DisplayMetrics.DENSITY_HIGH:
                item.setSubtitle("HDPI"+sizeInPixels);
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                item.setSubtitle("XHDPI"+sizeInPixels);
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                item.setSubtitle("XXHDPI"+sizeInPixels);
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                item.setSubtitle("XXXHDPI"+sizeInPixels);
                break;
            case DisplayMetrics.DENSITY_TV:
                item.setSubtitle("TVDPI"+sizeInPixels);
                break;
            case DisplayMetrics.DENSITY_400:
                item.setSubtitle("400DPI"+sizeInPixels);
                break;
            case DisplayMetrics.DENSITY_560:
                item.setSubtitle("560DPI"+sizeInPixels);
                break;
            default:
                break;
        }
        return item;
    }

    private Item getRamSize() {
        Item item = new Item("Memory", ItemType.HARDWARE);
        try {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                //Total memory is only available on API 16+
                item.setSubtitle(context.getResources().getString(R.string
                    .hardware_storage_output_format_api15,
                    Formatter.formatFileSize(context, mi.availMem)));
                item.setChartitem(new ChartItem(mi.availMem,0, R.drawable.ic_memory));
            }else {
                item.setSubtitle(mi.totalMem <= 0 ? context.getResources().getString(R.string.not_found) : context.getResources
                    ().getString(R.string.hardware_storage_output_format,
                    Formatter.formatFileSize(context, mi.availMem),
                    Formatter.formatFileSize(context, mi.totalMem)));
                item.setChartitem(new ChartItem(mi.availMem, mi.totalMem, R.drawable.ic_memory));
            }
        } catch (Exception e) {
            Log.w(TAG, "Exception in getRamSize");
        }
        return item;
    }

    private Item getFormattedInternalMemory() {
        Item item = new Item("Internal Storage", ItemType.HARDWARE);
        long available = new File(context.getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
        long total = new File(context.getFilesDir().getAbsoluteFile().toString()).getTotalSpace();
        if(total>0L)
            item.setSubtitle(context.getResources().getString(R.string.hardware_storage_output_format, Formatter.formatFileSize(context,available), Formatter.formatFileSize(context,total)));
        item.setChartitem(new ChartItem(available,total,R.drawable.ic_storage));
        return item;
    }

    private Item getFormattedExternalMemory() {
        Item item =  new Item("External Storage", ItemType.HARDWARE);
        long availSize=0L,totalSize=0L;
        File[] appsDir = ContextCompat.getExternalFilesDirs(context,null);
        for(File file : appsDir) {
            availSize += file.getParentFile().getParentFile().getParentFile().getParentFile().getFreeSpace();
            totalSize += file.getParentFile().getParentFile().getParentFile().getParentFile().getTotalSpace();
        }
        availSize-=new File(context.getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
        totalSize-=new File(context.getFilesDir().getAbsoluteFile().toString()).getTotalSpace();
        if(totalSize>0L)
            item.setSubtitle(context.getResources().getString(R.string.hardware_storage_output_format, Formatter.formatFileSize(context, availSize), Formatter.formatFileSize(context, totalSize)));
        item.setChartitem(new ChartItem(availSize,totalSize,R.drawable.ic_storage));
        return item;
    }

    private void getBattery() {
        //Item item = new Item("Battery", ItemType.HARDWARE);
        context.registerReceiver(new InfoReceiver(),new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
    //int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)/10;
    // Are we charging / charged?
    // How are we charging?
    //subTitle = String.valueOf(temperature+"\u00b0C");
    private class InfoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent==null || intent.getAction()==null) return;
            final PendingResult result = goAsync();
            new ProcessInfoBroadcastAsync(context,intent,result).execute();
            context.unregisterReceiver(this);
        }
    }
    private static class ProcessInfoBroadcastAsync extends AsyncTask<Void, Void, Void> {
        private final Context context;
        private final Intent intent;
        private final BroadcastReceiver.PendingResult result;

        ProcessInfoBroadcastAsync(Context context, Intent intent, BroadcastReceiver
            .PendingResult result) {
            this.context = context;
            this.intent = intent;
            this.result = result;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BATTERY_CHANGED)) {
                //int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)/10;
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                StringBuilder subtitle = new StringBuilder();
                subtitle.append(level).append("% - ");
                // Are we charging / charged?
                switch (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        subtitle.append(context.getString(R.string.BATTERY_STATUS_CHARGING));
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        subtitle.append(context.getString(R.string.BATTERY_STATUS_FULL));
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        subtitle.append(context.getString(R.string.BATTERY_STATUS_DISCHARGING));
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        subtitle.append(context.getString(R.string.BATTERY_STATUS_NOT_CHARGING));
                        break;
                    default:
                        subtitle.append(context.getString(R.string.BATTERY_STATUS_UNKNOWN));
                        break;
                }
                // How are we charging?
                int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
                boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
                subtitle.append(" ");
                if(usbCharge)
                    subtitle.append(context.getString(R.string.BATTERY_PLUGGED_USB));
                else if(acCharge)
                    subtitle.append(context.getString(R.string.BATTERY_PLUGGED_AC));
                //subTitle = String.valueOf(temperature+"\u00b0C");
                Item item = new Item("Battery", ItemType.HARDWARE);
                item.setSubtitle(subtitle.toString());
                item.setChartitem(new ChartItem(100-level,100,R.drawable.ic_battery));
                AppDatabase.getDatabase(context).itemDao().insertItems(item);
                // Must call finish() so the BroadcastReceiver can be recycled.
                result.finish();
            }
            return null;
        }
    }
}