package com.cwlarson.deviceid.data;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.util.Log;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.util.MyAdapter;
import com.cwlarson.deviceid.util.SystemProperty;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Software {
    String TAG = "Network";
    private Context context;

    public Software(Activity activity){
        this.context = activity.getApplicationContext();
    }

    public void setSoftwareTiles(MyAdapter mAdapter){
        mAdapter.addItem(Arrays.asList("Android Version", getAndroidVersion()));
        mAdapter.addItem(Arrays.asList("Baseband Version", getBuildBaseband()));
        mAdapter.addItem(Arrays.asList("Build Board", getBuildBoard()));
        mAdapter.addItem(Arrays.asList("Build Bootloader", getBuildBootloader()));
        mAdapter.addItem(Arrays.asList("Build Brand", getBuildBrand()));
        mAdapter.addItem(Arrays.asList("Build Date", getBuildDate()));
        mAdapter.addItem(Arrays.asList("Build Device", getBuildDevice()));
        mAdapter.addItem(Arrays.asList("Build Display", getBuildDisplay()));
        mAdapter.addItem(Arrays.asList("Build Fingerprint", getBuildFingerprint()));
        mAdapter.addItem(Arrays.asList("Build Hardware", getBuildHardware()));
        mAdapter.addItem(Arrays.asList("Build Host", getBuildHost()));
        mAdapter.addItem(Arrays.asList("Build Number", getBuildNumber()));
        mAdapter.addItem(Arrays.asList("Build Tags", getBuildTags()));
        mAdapter.addItem(Arrays.asList("Build Type", getBuildType()));
        mAdapter.addItem(Arrays.asList("Build User", getBuildUser()));
        mAdapter.addItem(Arrays.asList("Build Version", getDeviceBuildVersion()));
        mAdapter.addItem(Arrays.asList("Kernel Version", getBuildKernel()));
        mAdapter.addItem(Arrays.asList("OpenGL Version", getOpenGLVersion()));
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
        MARSHMALLOW;

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
                case 1000:
                    return CUR_DEVELOPMENT;
                default:
                    return null;
            }
        }
    }

    private String getAndroidVersion() {
        String version="",api="",versionName="";
        try {
            version = Build.VERSION.RELEASE;
            api = Integer.toString(Build.VERSION.SDK_INT);
            //noinspection ConstantConditions
            versionName = (Codenames.getCodename() == null) ? "" : Codenames.getCodename().toString();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.w(TAG, "Null in getAndroidVersion");
        }
        version=version+ " (" +api+") "+versionName;

        return version.equals("") ? context.getResources().getString(R.string.not_found) : version;
    }

    private String getDeviceBuildVersion() {
        //Get Moto specific build version if available
        SystemProperty sp = new SystemProperty(context);
        return sp.get("ro.build.version.full")==null|| sp.get("ro.build.version.full").equals("") ? Build.DISPLAY : sp.get("ro.build.version.full");
    }

    private String getBuildBaseband() {
        return Build.getRadioVersion().equals("") ? context.getResources().getString(R.string.not_found) : Build.getRadioVersion();
    }

    private String getBuildKernel() {
        return System.getProperty("os.version")==null|| System.getProperty("os.version").equals("") ? context.getResources().getString(R.string.not_found) : System.getProperty("os.version");
    }

    private String getBuildDate() {
        return SimpleDateFormat.getInstance().format(new Date(Build.TIME)).equals("") ? context.getResources().getString(R.string.not_found) : SimpleDateFormat.getInstance().format(new Date(Build.TIME));
    }

    private String getBuildNumber() {
        return Build.ID==null || Build.ID.equals("") ? context.getResources().getString(R.string.not_found) : Build.ID;
    }

    private String getBuildBoard() {
        return Build.BOARD==null || Build.BOARD.equals("") ? context.getResources().getString(R.string.not_found) : Build.BOARD;
    }

    private String getBuildBootloader() {
        return Build.BOOTLOADER==null || Build.BOOTLOADER.equals("") ? context.getResources().getString(R.string.not_found) : Build.BOOTLOADER;
    }

    private String getBuildBrand() {
        return Build.BRAND==null || Build.BRAND.equals("") ? context.getResources().getString(R.string.not_found) : Build.BRAND;
    }

    private String getBuildDevice() {
        return Build.DEVICE==null || Build.DEVICE.equals("") ? context.getResources().getString(R.string.not_found) : Build.DEVICE;
    }

    private String getBuildDisplay() {
        return Build.DISPLAY==null || Build.DISPLAY.equals("") ? context.getResources().getString(R.string.not_found) : Build.DISPLAY;
    }

    private String getBuildFingerprint() {
        return Build.FINGERPRINT==null || Build.FINGERPRINT.equals("") ? context.getResources().getString(R.string.not_found) : Build.FINGERPRINT;
    }

    private String getBuildHardware() {
        return Build.HARDWARE==null || Build.HARDWARE.equals("") ? context.getResources().getString(R.string.not_found) : Build.HARDWARE;
    }

    private String getBuildHost() {
        return Build.HOST==null || Build.HOST.equals("") ? context.getResources().getString(R.string.not_found) : Build.HOST;
    }

    private String getBuildTags() {
        return Build.TAGS==null || Build.TAGS.equals("") ? context.getResources().getString(R.string.not_found) : Build.TAGS;
    }

    private String getBuildType() {
        return Build.TYPE==null || Build.TYPE.equals("") ? context.getResources().getString(R.string.not_found) : Build.TYPE;
    }

    private String getBuildUser() {
        return Build.USER==null || Build.USER.equals("") ? context.getResources().getString(R.string.not_found) : Build.USER;
    }

    private String getOpenGLVersion(){
        ConfigurationInfo configurationInfo = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo();
        return (configurationInfo.getGlEsVersion().equals("")) ? context.getResources().getString(R.string.not_found) : configurationInfo.getGlEsVersion();
    }
}
