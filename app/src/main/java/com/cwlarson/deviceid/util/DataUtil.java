package com.cwlarson.deviceid.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cwlarson.deviceid.MainActivity;
import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.data.Permissions;

import java.util.HashSet;
import java.util.Set;

public class DataUtil {
    private final String TAG = "DataUtil";
    private static final String favItemKey = "FAV_ITEMS";
    public static final String BROADCAST_UPDATE_FAV="BROADCAST_UPDATE_FAV";
    private Toast toast;
    private final Activity activity;
    private final Context context;

    public DataUtil(Activity activity){
        this.activity=activity;
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
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(BROADCAST_UPDATE_FAV).putExtra("ACTION", "ADD").putExtra("ITEM_TITLE", itemID).putExtra("ITEM_SUB",itemSub));
    }

    public Set<String> getAllFavoriteItems() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getStringSet(favItemKey,new HashSet<String>());
    }

    public boolean isFavoriteItem(String itemID) {
        Set<String> allFavs = getAllFavoriteItems();
        for (String s:allFavs){
            if (s.equals(itemID)){
                return true;
            }
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
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(BROADCAST_UPDATE_FAV).putExtra("ACTION", "REMOVE").putExtra("ITEM_TITLE",itemID).putExtra("ITEM_SUB",itemSub));
    }
    // Returns true if method already takes care of the click, false if the parent should
    public void onClickAdapter(String itemTitle, String itemSubTitle, final ImageButton itemMoreButton){
        if(itemSubTitle.equals(context.getResources().getString(R.string.phone_permission_denied))) {
            Permissions permissions = new Permissions(activity);
            permissions.getPermissionClickAdapter(Permissions.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE, itemTitle);
        } else if(itemSubTitle.equals(context.getResources().getString(R.string.not_found))||itemSubTitle.startsWith(context.getResources().getString(R.string.no_longer_possible).replace("%s", ""))||itemSubTitle.startsWith(context.getResources().getString(R.string.not_possible_yet).replace("%s", ""))) {
            Snackbar.make(activity.findViewById(R.id.main_activity_layout), activity.getResources().getString(R.string.snackbar_not_found_adapter, itemTitle), Snackbar.LENGTH_LONG).show();
        } else { // This is a valid body so we should do something and inform the user
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(itemTitle);
            builder.setMessage(itemSubTitle);
            builder.setPositiveButton(R.string.dialog_long_press_positive_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    itemMoreButton.performClick();
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton(R.string.dialog_long_press_negative_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            MainActivity.dialog = builder.create();
            MainActivity.dialog.show();
        }
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
