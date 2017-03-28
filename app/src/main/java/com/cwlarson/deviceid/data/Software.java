package com.cwlarson.deviceid.data;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.databinding.Item;
import com.cwlarson.deviceid.util.MyAdapter;
import com.cwlarson.deviceid.util.SystemProperty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Software {
    private final String TAG = Software.class.getSimpleName();
    private final Context context;
    // IDs reserved 15001-25000

    public Software(Activity activity){
        this.context = activity.getApplicationContext();
    }

    public void setSoftwareTiles(final MyAdapter mAdapter){
        new AsyncTask<Void, Item, Void>() {
            @Override
            protected void onProgressUpdate(Item... values) {
                if(mAdapter!=null) mAdapter.add(values[0]);
            }

            @Override
            protected Void doInBackground(Void... aVoid) {
                publishProgress(getAndroidVersion());
                publishProgress(getPatchLevel());
                publishProgress(getPreviewSDKInt());
                publishProgress(getDeviceBuildVersion());
                publishProgress(getBuildBaseband());
                publishProgress(getBuildKernel());
                publishProgress(getBuildDate());
                publishProgress(getBuildNumber());
                publishProgress(getBuildBoard());
                publishProgress(getBuildBootloader());
                publishProgress(getBuildBrand());
                publishProgress(getBuildDevice());
                publishProgress(getBuildDisplay());
                publishProgress(getBuildFingerprint());
                publishProgress(getBuildHardware());
                publishProgress(getBuildHost());
                publishProgress(getBuildTags());
                publishProgress(getBuildType());
                publishProgress(getBuildUser());
                publishProgress(getOpenGLVersion());
                return null;
            }
        }.execute();
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
        NOUGAT, NOUGAT_MR1;

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
                case 1000:
                    return CUR_DEVELOPMENT;
                default:
                    return null;
            }
        }
    }

    private Item getAndroidVersion() {
        Item item = new Item(15001,"Android Version",context.getString(R.string.not_found));
        try {
            //noinspection ConstantConditions
            String versionName = (Codenames.getCodename() == null) ? "" : Codenames.getCodename().toString();
            item.setSubTitle(Build.VERSION.RELEASE+" ("+String.valueOf(Build.VERSION.SDK_INT)+") " + versionName);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.w(TAG, "Null in getAndroidVersion");
        }
        return item;
    }

    private Item getPatchLevel() {
        Item item = new Item(15002,"Security Patch Level",context.getString(R.string.not_found));
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    SimpleDateFormat template = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date patchDate = template.parse(Build.VERSION.SECURITY_PATCH);
                    String format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy");
                    item.setSubTitle(DateFormat.format(format, patchDate).toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                    item.setSubTitle(Build.VERSION.SECURITY_PATCH);
                }
            } else
                item.setSubTitle(context.getResources().getString(R.string.not_possible_yet,"6.0"));
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Null in getAndroidVersion");
        }
        return item;
    }

    private Item getPreviewSDKInt(){
        Item item =new Item(15003,"Preview SDK Number", context.getString(R.string.not_found));
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                int sdk = Build.VERSION.PREVIEW_SDK_INT;
                if(sdk==0)
                    item.setSubTitle("Non-Preview");
                else
                    item.setSubTitle("Preview " + Integer.toString(sdk));
            }
            else
                item.setSubTitle(context.getResources().getString(R.string.not_possible_yet,"6.0"));
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Null in getPreviewSDKInt");
        }
        return item;
    }

    private Item getDeviceBuildVersion() {
        //Get Moto specific build version if available
        Item item = new Item(15004,"Build Version",context.getString(R.string.not_found));
        SystemProperty sp = new SystemProperty(context);
        item.setSubTitle(sp.get("ro.build.version.full")==null|| sp.get("ro.build.version.full").equals("") ? Build.DISPLAY : sp.get("ro.build.version.full"));
        return item;
    }

    private Item getBuildBaseband() {
        Item item = new Item(15005,"Build Baseband",context.getString(R.string.not_found));
        item.setSubTitle(Build.getRadioVersion());
        return item;
    }

    private Item getBuildKernel() {
        Item item = new Item(15006,"Kernel Version",context.getString(R.string.not_found));
        item.setSubTitle(System.getProperty("os.version"));
        return item;
    }

    private Item getBuildDate() {
        Item item = new Item(15007,"Build Date",context.getString(R.string.not_found));
        item.setSubTitle(SimpleDateFormat.getInstance().format(new Date(Build.TIME)));
        return item;
    }

    private Item getBuildNumber() {
        Item item = new Item(15008,"Build Number",context.getString(R.string.not_found));
        item.setSubTitle(Build.ID);
        return item;
    }

    private Item getBuildBoard() {
        Item item = new Item(15009,"Build Board",context.getString(R.string.not_found));
        item.setSubTitle(Build.BOARD);
        return item;
    }

    private Item getBuildBootloader() {
        Item item = new Item(15010,"Build Bootloader",context.getString(R.string.not_found));
        item.setSubTitle(Build.BOOTLOADER);
        return item;
    }

    private Item getBuildBrand() {
        Item item = new Item(15011,"Build Brand",context.getString(R.string.not_found));
        item.setSubTitle(Build.BRAND);
        return item;
    }

    private Item getBuildDevice() {
        String software = Build.DEVICE;
        return new Item(15012,"Build Device",software);
    }

    private Item getBuildDisplay() {
        Item item = new Item(15013,"Build Display",context.getString(R.string.not_found));
        item.setSubTitle(Build.DISPLAY);
        return item;
    }

    private Item getBuildFingerprint() {
        Item item = new Item(15014,"Build Fingerprint",context.getString(R.string.not_found));
        item.setSubTitle(Build.FINGERPRINT);
        return item;
    }

    private Item getBuildHardware() {
        Item item = new Item(15015,"Build Hardware",context.getString(R.string.not_found));
        item.setSubTitle(Build.HARDWARE);
        return item;
    }

    private Item getBuildHost() {
        Item item = new Item(15016,"Build Host",context.getString(R.string.not_found));
        item.setSubTitle(Build.HOST);
        return item;
    }

    private Item getBuildTags() {
        Item item = new Item(15017,"Build Tags", context.getString(R.string.not_found));
        item.setSubTitle(Build.TAGS);
        return item;
    }

    private Item getBuildType() {
        Item item = new Item(15018,"Build Type",context.getString(R.string.not_found));
        item.setSubTitle(Build.TYPE);
        return item;
    }

    private Item getBuildUser() {
        Item item = new Item(15019,"Build User",context.getString(R.string.not_found));
        item.setSubTitle(Build.USER);
        return item;
    }

    private Item getOpenGLVersion(){
        Item item = new Item(15020,"OpenGL Version",context.getString(R.string.not_found));
        ConfigurationInfo configurationInfo = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo();
        item.setSubTitle(configurationInfo.getGlEsVersion());
        return item;
    }
}
