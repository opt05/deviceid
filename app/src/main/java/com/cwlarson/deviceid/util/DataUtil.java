package com.cwlarson.deviceid.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.cwlarson.deviceid.R;

import java.util.HashSet;
import java.util.Set;

public class DataUtil {
    private final String TAG = "DataUtil";
    private static final String favItemKey = "FAV_ITEMS";
    //public static final String BROADCAST_UPDATE_FAV="BROADCAST_UPDATE_FAV";
    private Toast toast;
    private final Context context;

    public DataUtil(Activity activity){
        this.context=activity.getApplicationContext();
    }

    public void saveFavoriteItem(String itemID, String itemSub) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> favoriteItem = sharedPref.getStringSet(favItemKey, new HashSet<String>());
        Set<String> in = new HashSet<>(favoriteItem);
        in.add(itemID);
        sharedPref.edit().putStringSet(favItemKey, in).apply();
        Log.i(TAG, "saveFavoriteItems = "+ getAllFavoriteItems());
        //Send a broadcast that a fav was removed
        //LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(BROADCAST_UPDATE_FAV).putExtra("ACTION", "ADD").putExtra("ITEM_TITLE", itemID).putExtra("ITEM_SUB",itemSub));
    }

    private Set<String> getAllFavoriteItems() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getStringSet(favItemKey,new HashSet<String>());
    }

    public boolean isFavoriteItem(String itemID) {
        Set<String> allFavs = getAllFavoriteItems();
        for (String s:allFavs){
            if (s.equals(itemID)) return true;
        }
        return false;
    }

    public void removeFavoriteItem(String itemID, String itemSub) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> favoriteItemsList = sharedPref.getStringSet(favItemKey, new HashSet<String>());
        Set<String> newFavoriteItemsList=new HashSet<>();
        //Loop through to compare
        for (String s:favoriteItemsList){
            if (!s.equals(itemID)){
                newFavoriteItemsList.add(s);
                //Log.i(TAG,"Item to delete: "+itemID);
            }
        }
        sharedPref.edit().putStringSet(favItemKey, newFavoriteItemsList).apply();
        Log.i(TAG, "removeFavoriteItems = " + getAllFavoriteItems());
        //Send a broadcast that a fav was removed
        //LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(BROADCAST_UPDATE_FAV).putExtra("ACTION", "REMOVE").putExtra("ITEM_TITLE",itemID).putExtra("ITEM_SUB",itemSub));
    }
    //Copy to clipboard
    public void copyToClipboard(String headerText, String bodyText){
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(headerText, bodyText);
        clipboard.setPrimaryClip(clip);
        //Prevents multiple times toast issue with the button
        if (toast != null) toast.cancel();
        toast = Toast.makeText(context,
                context.getResources().getString(R.string.copy_to_clipboard, headerText),
                Toast.LENGTH_SHORT);
        toast.show();
    }
}
