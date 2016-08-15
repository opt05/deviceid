package com.cwlarson.deviceid.data;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.util.MyAdapter;
import com.cwlarson.deviceid.util.SystemProperty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Software {
    private final String TAG = "Network";
    private final Context context;

    public Software(Activity activity){
        this.context = activity.getApplicationContext();
    }

    public List<Item> setSoftwareTiles(MyAdapter mAdapter){
        List<Item> items = new ArrayList<>();
        items.add(getAndroidVersion());
        items.add(getPatchLevel());
        items.add(getPreviewSDKInt());
        items.add(getDeviceBuildVersion());
        items.add(getBuildBaseband());
        items.add(getBuildKernel());
        items.add(getBuildDate());
        items.add(getBuildNumber());
        items.add(getBuildBoard());
        items.add(getBuildBootloader());
        items.add(getBuildBrand());
        items.add(getBuildDevice());
        items.add(getBuildDisplay());
        items.add(getBuildFingerprint());
        items.add(getBuildHardware());
        items.add(getBuildHost());
        items.add(getBuildTags());
        items.add(getBuildType());
        items.add(getBuildUser());
        items.add(getOpenGLVersion());
        if(mAdapter!=null) mAdapter.addAll(items);
        return items;
    }

    enum Codenames {
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
        NOUGAT;

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
                case 1000:
                    return CUR_DEVELOPMENT;
                default:
                    return null;
            }
        }
    }

    private Item getAndroidVersion() {
        String software="",api="",versionName="";
        try {
            software = Build.VERSION.RELEASE;
            api = Integer.toString(Build.VERSION.SDK_INT);
            //noinspection ConstantConditions
            versionName = (Codenames.getCodename() == null) ? "" : Codenames.getCodename().toString();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.w(TAG, "Null in getAndroidVersion");
        }
        software=software+ " (" +api+") "+versionName;
        Item item = new Item(context);
        item.setTitle("Android Version");
        item.setSubTitle(software); 
        return item;
    }

    private Item getPatchLevel() {
        String software="";
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    SimpleDateFormat template = new SimpleDateFormat("yyyy-MM-dd");
                    Date patchDate = template.parse(Build.VERSION.SECURITY_PATCH);
                    String format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy");
                    software = DateFormat.format(format, patchDate).toString();
                } catch (ParseException e) {
                    e.printStackTrace();
                    software = Build.VERSION.SECURITY_PATCH;
                }
            } else
                software = context.getResources().getString(R.string.not_possible_yet,"6.0");
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Null in getAndroidVersion");
        }
        Item item = new Item(context);
        item.setTitle("Security Patch Level");
        item.setSubTitle(software);
        return item;
    }

    private Item getPreviewSDKInt(){
        String software="";
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                int sdk = Build.VERSION.PREVIEW_SDK_INT;
                if(sdk==0)
                    software = "Non-Preview";
                else
                    software = "Preview " + Integer.toString(sdk);
            }
            else
                software = context.getResources().getString(R.string.not_possible_yet,"6.0");
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Null in getPreviewSDKInt");
        }
        Item item = new Item(context);
        item.setTitle("Preview SDK Number");
        item.setSubTitle(software);
        return item;
    }

    private Item getDeviceBuildVersion() {
        //Get Moto specific build version if available
        SystemProperty sp = new SystemProperty(context);
        String software=sp.get("ro.build.version.full")==null|| sp.get("ro.build.version.full").equals("") ? Build.DISPLAY : sp.get("ro.build.version.full");
        Item item = new Item(context);
        item.setTitle("Build Version");
        item.setSubTitle(software); 
        return item;
    }

    private Item getBuildBaseband() {
        String software = Build.getRadioVersion();
        Item item = new Item(context);
        item.setTitle("Build Baseband");
        item.setSubTitle(software); 
        return item;
    }

    private Item getBuildKernel() {
        String software = System.getProperty("os.version");
        Item item = new Item(context);
        item.setTitle("Kernel Version");
        item.setSubTitle(software); 
        return item;
    }

    private Item getBuildDate() {
        String software = SimpleDateFormat.getInstance().format(new Date(Build.TIME));
        Item item = new Item(context);
        item.setTitle("Build Date");
        item.setSubTitle(software); 
        return item;
    }

    private Item getBuildNumber() {
        String software = Build.ID;
        Item item = new Item(context);
        item.setTitle("Build Number");
        item.setSubTitle(software); 
        return item;
    }

    private Item getBuildBoard() {
        String software = Build.BOARD;
        Item item = new Item(context);
        item.setTitle("Build Board");
        item.setSubTitle(software); 
        return item;
    }

    private Item getBuildBootloader() {
        String software = Build.BOOTLOADER;
        Item item = new Item(context);
        item.setTitle("Build Bootloader");
        item.setSubTitle(software); 
        return item;
    }

    private Item getBuildBrand() {
        String software = Build.BRAND;
        Item item = new Item(context);
        item.setTitle("Build Brand");
        item.setSubTitle(software); 
        return item;
    }

    private Item getBuildDevice() {
        String software = Build.DEVICE;
        Item item = new Item(context);
        item.setTitle("Build Device");
        item.setSubTitle(software); 
        return item;
    }

    private Item getBuildDisplay() {
        String software = Build.DISPLAY;
        Item item = new Item(context);
        item.setTitle("Build Display");
        item.setSubTitle(software); 
        return item;
    }

    private Item getBuildFingerprint() {
        String software = Build.FINGERPRINT;
        Item item = new Item(context);
        item.setTitle("Build Fingerprint");
        item.setSubTitle(software); 
        return item;
    }

    private Item getBuildHardware() {
        String software = Build.HARDWARE;
        Item item = new Item(context);
        item.setTitle("Build Hardware");
        item.setSubTitle(software); 
        return item;
    }

    private Item getBuildHost() {
        String software = Build.HOST;
        Item item = new Item(context);
        item.setTitle("Build Host");
        item.setSubTitle(software); 
        return item;
    }

    private Item getBuildTags() {
        String software = Build.TAGS;
        Item item = new Item(context);
        item.setTitle("Build Tags");
        item.setSubTitle(software); 
        return item;
    }

    private Item getBuildType() {
        String software = Build.TYPE;
        Item item = new Item(context);
        item.setTitle("Build Type");
        item.setSubTitle(software); 
        return item;
    }

    private Item getBuildUser() {
        String software = Build.USER;
        Item item = new Item(context);
        item.setTitle("Build User");
        item.setSubTitle(software); 
        return item;
    }

    private Item getOpenGLVersion(){
        ConfigurationInfo configurationInfo = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo();
        String software = configurationInfo.getGlEsVersion();
        Item item = new Item(context);
        item.setTitle("OpenGL Version");
        item.setSubTitle(software); 
        return item;
    }
}
