package com.cwlarson.deviceid.database;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;

import com.cwlarson.deviceid.databinding.Item;
import com.cwlarson.deviceid.databinding.ItemType;
import com.cwlarson.deviceid.databinding.UnavailableItem;
import com.cwlarson.deviceid.databinding.UnavailableType;
import com.cwlarson.deviceid.util.SystemProperty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class Software {
    private final String TAG = Software.class.getSimpleName();
    private final Context context;

    Software(Activity activity, AppDatabase db){
        this.context = activity.getApplicationContext();
        //Set Software Tiles
        ItemAdder itemAdder = new ItemAdder(context, db);
        itemAdder.addItems(getAndroidVersion());
        itemAdder.addItems(getPatchLevel());
        itemAdder.addItems(getPreviewSDKInt());
        itemAdder.addItems(getDeviceBuildVersion());
        itemAdder.addItems(getBuildBaseband());
        itemAdder.addItems(getBuildKernel());
        itemAdder.addItems(getBuildDate());
        itemAdder.addItems(getBuildNumber());
        itemAdder.addItems(getBuildBoard());
        itemAdder.addItems(getBuildBootloader());
        itemAdder.addItems(getBuildBrand());
        itemAdder.addItems(getBuildDevice());
        itemAdder.addItems(getBuildDisplay());
        itemAdder.addItems(getBuildFingerprint());
        itemAdder.addItems(getBuildHardware());
        itemAdder.addItems(getBuildHost());
        itemAdder.addItems(getBuildTags());
        itemAdder.addItems(getBuildType());
        itemAdder.addItems(getBuildUser());
        itemAdder.addItems(getOpenGLVersion());
        itemAdder.addItems(getGooglePlayServicesVersion());
        itemAdder.addItems(getGooglePlayServicesInstallDate());
        itemAdder.addItems(getGooglePlayServicesUpdatedDate());
    }

    private enum Codenames {
        BASE, BASE_1_1,
        CUPCAKE,
        CUR_DEVELOPMENT,
        DONUT,
        ECLAIR, ECLAIR_MR1, ECLAIR_MR2,
        FROYO,
        GINGERBREAD, GINGERBREAD_MR1,
        HONEYCOMB, HONEYCOMB_MR1, HONEYCOMB_MR2,
        ICE_CREAM_SANDWICH, ICE_CREAM_SANDWICH_MR1,
        JELLY_BEAN, JELLY_BEAN_MR1, JELLY_BEAN_MR2,
        KITKAT, KITKAT_WATCH,
        LOLLIPOP, LOLLIPOP_MR1,
        MARSHMALLOW,
        NOUGAT, NOUGAT_MR1,
        OREO, OREO_MR1;

        public static Codenames getCodename()
        {
            int api = Build.VERSION.SDK_INT;
            switch (api) {
                case 1:
                    return BASE;
                case 2:
                    return BASE_1_1;
                case 3:
                    return CUPCAKE;
                case 4:
                    return DONUT;
                case 5:
                    return ECLAIR;
                case 6:
                    return ECLAIR_MR1;
                case 7:
                    return ECLAIR_MR2;
                case 8:
                    return FROYO;
                case 9:
                    return GINGERBREAD;
                case 10:
                    return GINGERBREAD_MR1;
                case 11:
                    return HONEYCOMB;
                case 12:
                    return HONEYCOMB_MR1;
                case 13:
                    return HONEYCOMB_MR2;
                case 14:
                    return ICE_CREAM_SANDWICH;
                case 15:
                    return ICE_CREAM_SANDWICH_MR1;
                case 16:
                    return JELLY_BEAN;
                case 17:
                    return JELLY_BEAN_MR1;
                case 18:
                    return JELLY_BEAN_MR2;
                case 19:
                    return KITKAT;
                case 20:
                    return KITKAT_WATCH;
                case 21:
                    return LOLLIPOP;
                case 22:
                    return LOLLIPOP_MR1;
                case 23:
                    return MARSHMALLOW;
                case 24:
                    return NOUGAT;
                case 25:
                    return NOUGAT_MR1;
                case 26:
                    return OREO;
                case 27:
                    return OREO_MR1;
                case 1000:
                    return CUR_DEVELOPMENT;
                default:
                    return null;
            }
        }
    }

    private Item getAndroidVersion() {
        Item item = new Item("Android Version", ItemType.SOFTWARE);
        try {
            //noinspection ConstantConditions
            String versionName = (Codenames.getCodename() == null) ? "" : Codenames.getCodename().toString();
            item.setSubtitle(Build.VERSION.RELEASE+" ("+String.valueOf(Build.VERSION.SDK_INT)+") " +
                versionName);
        } catch (Exception e) {
            Log.w(TAG, "Null in getAndroidVersion");
        }
        return item;
    }

    private Item getPatchLevel() {
        Item item = new Item("Security Patch Level", ItemType.SOFTWARE);
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    SimpleDateFormat template = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date patchDate = template.parse(Build.VERSION.SECURITY_PATCH);
                    String format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy");
                    item.setSubtitle(DateFormat.format(format, patchDate).toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                    item.setSubtitle(Build.VERSION.SECURITY_PATCH);
                }
            } else {
                item.setUnavailableitem(
                    new UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,"6.0"));
            }
        } catch (Exception e) {
            Log.w(TAG, "Null in getAndroidVersion");
        }
        return item;
    }

    private Item getPreviewSDKInt(){
        Item item =new Item("Preview SDK Number", ItemType.SOFTWARE);
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                int sdk = Build.VERSION.PREVIEW_SDK_INT;
                if(sdk==0)
                    item.setSubtitle("Non-Preview");
                else
                    item.setSubtitle("Preview " + Integer.toString(sdk));
            }
            else {
                item.setUnavailableitem(
                    new UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,"6.0"));
            }
        } catch (Exception e) {
            Log.w(TAG, "Null in getPreviewSDKInt");
        }
        return item;
    }

    private Item getDeviceBuildVersion() {
        //Get Moto specific build version if available
        Item item = new Item("Build Version", ItemType.SOFTWARE);
        SystemProperty sp = new SystemProperty(context);
        item.setSubtitle(sp.get("ro.build.version.full")==null|| sp.get("ro.build.version.full").equals("") ? Build.DISPLAY : sp.get("ro.build.version.full"));
        return item;
    }

    private Item getBuildBaseband() {
        Item item = new Item("Build Baseband", ItemType.SOFTWARE);
        item.setSubtitle(Build.getRadioVersion());
        return item;
    }

    private Item getBuildKernel() {
        Item item = new Item("Kernel Version", ItemType.SOFTWARE);
        item.setSubtitle(System.getProperty("os.version"));
        return item;
    }

    private Item getBuildDate() {
        Item item = new Item("Build Date", ItemType.SOFTWARE);
        item.setSubtitle(SimpleDateFormat.getInstance().format(new Date(Build.TIME)));
        return item;
    }

    private Item getBuildNumber() {
        Item item = new Item("Build Number", ItemType.SOFTWARE);
        item.setSubtitle(Build.ID);
        return item;
    }

    private Item getBuildBoard() {
        Item item = new Item("Build Board", ItemType.SOFTWARE);
        item.setSubtitle(Build.BOARD);
        return item;
    }

    private Item getBuildBootloader() {
        Item item = new Item("Build Bootloader", ItemType.SOFTWARE);
        item.setSubtitle(Build.BOOTLOADER);
        return item;
    }

    private Item getBuildBrand() {
        Item item = new Item("Build Brand", ItemType.SOFTWARE);
        item.setSubtitle(Build.BRAND);
        return item;
    }

    private Item getBuildDevice() {
        Item item = new Item("Build Brand", ItemType.SOFTWARE);
        item.setSubtitle(Build.DEVICE);
        return item;
    }

    private Item getBuildDisplay() {
        Item item = new Item("Build Display", ItemType.SOFTWARE);
        item.setSubtitle(Build.DISPLAY);
        return item;
    }

    private Item getBuildFingerprint() {
        Item item = new Item("Build Fingerprint", ItemType.SOFTWARE);
        item.setSubtitle(Build.FINGERPRINT);
        return item;
    }

    private Item getBuildHardware() {
        Item item = new Item("Build Hardware", ItemType.SOFTWARE);
        item.setSubtitle(Build.HARDWARE);
        return item;
    }

    private Item getBuildHost() {
        Item item = new Item("Build Host", ItemType.SOFTWARE);
        item.setSubtitle(Build.HOST);
        return item;
    }

    private Item getBuildTags() {
        Item item = new Item("Build Tags", ItemType.SOFTWARE);
        item.setSubtitle(Build.TAGS);
        return item;
    }

    private Item getBuildType() {
        Item item = new Item("Build Type", ItemType.SOFTWARE);
        item.setSubtitle(Build.TYPE);
        return item;
    }

    private Item getBuildUser() {
        Item item = new Item("Build User", ItemType.SOFTWARE);
        item.setSubtitle(Build.USER);
        return item;
    }

    private Item getOpenGLVersion(){
        Item item = new Item("OpenGL Version", ItemType.SOFTWARE);
        try {
            ConfigurationInfo configurationInfo = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo();
            item.setSubtitle(configurationInfo.getGlEsVersion());
        } catch (Exception e) {
            Log.e(TAG, "Exception in getOpenGLVersion");
        }
        return item;
    }

    private Item getGooglePlayServicesVersion() {
        Item item = new Item("Google Play Services Version", ItemType.SOFTWARE);
        try {
            String n = context.getPackageManager().getPackageInfo("com.google.android.gms", 0).versionName;
            int v = context.getPackageManager().getPackageInfo("com.google.android.gms", 0).versionCode;
            item.setSubtitle(n+" ("+String.valueOf(v)+")");
        } catch (Exception e) {
            Log.e(TAG, "Exception in getGooglePlayServicesVersion");
        }
        return item;
    }

    private Item getGooglePlayServicesInstallDate() {
        Item item = new Item("Google Play Services Installed", ItemType.SOFTWARE);
        try {
            long t = context.getPackageManager().getPackageInfo("com.google.android.gms", 0).firstInstallTime;
            item.setSubtitle(DateFormat.getDateFormat(context).format(t));
        } catch (Exception e) {
            Log.e(TAG, "Exception in getGooglePlayServicesVersion");
        }
        return item;
    }

    private Item getGooglePlayServicesUpdatedDate() {
        Item item = new Item("Google Play Services Updated", ItemType.SOFTWARE);
        try {
            long t = context.getPackageManager().getPackageInfo("com.google.android.gms", 0).lastUpdateTime;
            item.setSubtitle(DateFormat.getDateFormat(context).format(t));
        } catch (Exception e) {
            Log.e(TAG, "Exception in getGooglePlayServicesVersion");
        }
        return item;
    }
}
