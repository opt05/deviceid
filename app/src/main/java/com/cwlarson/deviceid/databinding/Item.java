package com.cwlarson.deviceid.databinding;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.BatteryManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.cwlarson.deviceid.BR;
import com.cwlarson.deviceid.R;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper class for recyclerview items
 */
public class Item extends BaseObservable implements Comparable<Item>, Parcelable {
    private String title, subTitle;
    private int id, permissionCode;
    private ChartItem chartItem;
    private boolean isFavorite;
    public static final String favItemKey = "FAV_ITEMS";
    private BroadcastReceiver infoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BATTERY_CHANGED)) {
                //int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)/10;
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                subTitle = String.valueOf(level)+"%";
                // Are we charging / charged?
                switch (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        subTitle+=" - "+arg0.getString(R.string.BATTERY_STATUS_CHARGING);
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        subTitle+=" - "+arg0.getString(R.string.BATTERY_STATUS_FULL);
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        subTitle+=" - "+arg0.getString(R.string.BATTERY_STATUS_DISCHARGING);
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        subTitle+=" - "+arg0.getString(R.string.BATTERY_STATUS_NOT_CHARGING);
                        break;
                    default:
                        subTitle+=" - "+arg0.getString(R.string.BATTERY_STATUS_UNKNOWN);
                        break;
                }
                // How are we charging?
                int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
                boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
                if(usbCharge)
                    subTitle += " "+arg0.getString(R.string.BATTERY_PLUGGED_USB);
                else if(acCharge)
                    subTitle += " "+arg0.getString(R.string.BATTERY_PLUGGED_AC);
                //subTitle = String.valueOf(temperature+"\u00b0C");
                notifyPropertyChanged(BR.subTitle);
                setChartItem(new ChartItem(100-level,100,R.drawable.ic_battery));
                notifyPropertyChanged(BR.chartItem);
                arg0.unregisterReceiver(infoReceiver);
            }
        }
    };

    public Item(int id, String title, String subTitle) {
        this.id=id;
        this.title=title;
        this.subTitle=subTitle;
    }
    @Bindable
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }
    @Bindable
    public String getTitle(){
        return title;
    }
    @Bindable
    public String getSubTitle(){
        return this.subTitle;
    }
    @Bindable
    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite=isFavorite;
        notifyPropertyChanged(BR.favorite);
    }
    @Bindable
    public int getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(int permissionCode) {
        this.permissionCode = permissionCode;
        notifyPropertyChanged(BR.permissionCode);
    }

    public void setTitle(String title){
        this.title=title;
        notifyPropertyChanged(BR.title);
    }

    public void setSubTitle(String subTitle){
        this.subTitle=subTitle;
        notifyPropertyChanged(BR.subTitle);
    }
    @Bindable
    public ChartItem getChartItem() {
        return chartItem;
    }

    public void setChartItem(ChartItem chartItem) {
        this.chartItem = chartItem;
        notifyPropertyChanged(BR.chartItem);
    }

    public BroadcastReceiver getInfoReceiver() {
        return infoReceiver;
    }

    private void saveFavoriteItem(int itemID, Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> favoriteItem = sharedPref.getStringSet(favItemKey, new HashSet<String>());
        Set<String> in = new HashSet<>(favoriteItem);
        in.add(String.valueOf(itemID));
        sharedPref.edit().putStringSet(favItemKey, in).apply();
        isFavorite=true;
        notifyPropertyChanged(BR.favorite);
    }

    private void removeFavoriteItem(int itemID, Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> favoriteItemsList = sharedPref.getStringSet(favItemKey, new HashSet<String>());
        Set<String> newFavoriteItemsList=new HashSet<>();
        //Loop through to compare
        for (String s:favoriteItemsList){
            if (!s.equals(String.valueOf(itemID))){
                newFavoriteItemsList.add(s);
            }
        }
        sharedPref.edit().putStringSet(favItemKey, newFavoriteItemsList).apply();
        isFavorite=false;
        notifyPropertyChanged(BR.favorite);
    }
    //Copy to clipboard
    private void copyToClipboard(String headerText, String bodyText, Context context){
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(headerText, bodyText);
        clipboard.setPrimaryClip(clip);
        //Prevents multiple times toast issue with the button
        Toast.makeText(context, context.getResources().getString(R.string.copy_to_clipboard, headerText),
                Toast.LENGTH_SHORT).show();
    }

    private boolean getHasOptions(Context context) {
        String subTitle = getSubTitle();
        return !(subTitle.equals(context.getResources().getString(R.string.not_found))
                || subTitle.equals(context.getResources().getString(R.string.phone_permission_denied))
                || subTitle.startsWith(context.getResources().getString(R.string.no_longer_possible).replace("%s", ""))
                || subTitle.startsWith(context.getResources().getString(R.string.not_possible_yet).replace("%s", "")));

    }

    public boolean getIsUnavailable(Context context) {
        String subTitle = getSubTitle();
        return (TextUtils.isEmpty(subTitle))
                || (subTitle.equals(context.getResources().getString(R.string.not_found))
                || subTitle.startsWith(context.getResources().getString(R.string.no_longer_possible).replace("%s", ""))
                || subTitle.startsWith(context.getResources().getString(R.string.not_possible_yet).replace("%s", "")));
    }

    public boolean onLongClick(Context context) {
        if(getHasOptions(context)) {
            copyToClipboard(title,subTitle,context);
            return true;
        }
        return false;
    }

    public boolean onShareClick(Activity activity) {
        Intent shareIntent = ShareCompat.IntentBuilder.from(activity)
                .setType("text/plain")
                .setChooserTitle(R.string.send_to)
                .setText(subTitle)
                .getIntent();
        if (shareIntent.resolveActivity(activity.getPackageManager()) != null)
            activity.startActivity(shareIntent);
        return true;
    }

    public boolean onFavoriteClick(Context context, boolean changeToFavorite) {
        if (isFavorite != changeToFavorite) {
            if (isFavorite)
                removeFavoriteItem(id, context);
            else
                saveFavoriteItem(id, context);
            notifyPropertyChanged(BR.favorite);
            return true;
        }
        return false;
    }

    public boolean matchesSearchText(String searchText) {
        return searchText.length() > 0 &&
                (title.toLowerCase().contains(searchText.toLowerCase())
                        || getSubTitle().toLowerCase().contains(searchText.toLowerCase()));
    }

    @Override
    public boolean equals(Object o) {
        //check for self-comparison
        if ( this == o ) return true;

        //use instanceof instead of getClass here for two reasons
        //1. if need be, it can match any supertype, and not just one class;
        //2. it renders an explict check for "that == null" redundant, since
        //it does the check for null already - "null instanceof [type]" always
        //returns false. (See Effective Java by Joshua Bloch.)
        if ( !(o instanceof Item) ) return false;
        //Alternative to the above line :
        //if ( o == null || o.getClass() != this.getClass()) return false;

        //cast to native object is now safe
        Item that = (Item) o;

        //now a proper field-by-field evaluation can be made
        return that.title!=null
                && that.title.equals(this.title)
                && that.subTitle.equals(this.subTitle);
    }

    @Override
    public int compareTo(@NonNull Item item) {
        return this.title.compareTo(item.title);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.title);
        parcel.writeString(this.subTitle);
    }

    public Item(Parcel in) {
        this.title=in.readString();
        this.subTitle=in.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Item createFromParcel(Parcel in) { return new Item(in); }

        public Item[] newArray(int size) { return new Item[size]; }
    };
}
