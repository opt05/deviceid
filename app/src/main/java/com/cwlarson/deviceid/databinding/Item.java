package com.cwlarson.deviceid.databinding;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.data.Permissions;
import com.cwlarson.deviceid.dialog.ItemClickDialog;
import com.cwlarson.deviceid.dialog.ItemMoreButtonDialog;
import com.cwlarson.deviceid.util.DataUtil;

/**
 * Helper class for recyclerview items
 */
public class Item extends BaseObservable implements Comparable<Item>, Parcelable {
    private String title, subTitle;

    public Item() {
        // empty
    }

    @Bindable
    public String getTitle(){
        return title;
    }

    @Bindable
    public String getSubTitle(){
        return this.subTitle;
    }

    private String getSubTitleWithContext(Context context){
        return TextUtils.isEmpty(subTitle) ? context.getResources().getString(R.string.not_found) : subTitle;
    }

    public boolean isFavorite(AppCompatActivity activity) {
        return new DataUtil(activity).isFavoriteItem(title);
    }

    public void setFavorite(boolean favorite, AppCompatActivity activity) {
        if(favorite != isFavorite(activity)) {
            if(isFavorite(activity))
                new DataUtil(activity).removeFavoriteItem(title,subTitle);
            else
                new DataUtil(activity).saveFavoriteItem(title,subTitle);
            notifyChange();
        }
    }

    public void setTitle(String title){
        this.title=title;
    }

    public void setSubTitle(String subTitle){
        this.subTitle=subTitle;
    }

    public boolean getHasOptions(Context context) {
        String subTitle = getSubTitleWithContext(context);
        return !(subTitle.equals(context.getResources().getString(R.string.not_found))
                || subTitle.equals(context.getResources().getString(R.string.phone_permission_denied))
                || subTitle.startsWith(context.getResources().getString(R.string.no_longer_possible).replace("%s", ""))
                || subTitle.startsWith(context.getResources().getString(R.string.not_possible_yet).replace("%s", "")));

    }

    public void onOptionsClick(AppCompatActivity appCompatActivity) {
        if(appCompatActivity==null) return;
        ItemMoreButtonDialog.newInstance(this).show(appCompatActivity.getSupportFragmentManager(),"itemMoreButtonDialog");
    }

    public void onClick(AppCompatActivity appCompatActivity) {
        String subTitle = getSubTitleWithContext(appCompatActivity);
        if(subTitle.equals(appCompatActivity.getResources().getString(R.string.phone_permission_denied)))
            new Permissions(appCompatActivity).getPermissionClickAdapter(Permissions.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE, title);
        else if(subTitle.equals(appCompatActivity.getResources().getString(R.string.not_found))
                || subTitle.startsWith(appCompatActivity.getResources().getString(R.string.no_longer_possible).replace("%s", ""))
                || subTitle.startsWith(appCompatActivity.getResources().getString(R.string.not_possible_yet).replace("%s", "")))
            Snackbar.make(appCompatActivity.findViewById(R.id.main_activity_layout), appCompatActivity.getResources().getString(R.string.snackbar_not_found_adapter, title), Snackbar.LENGTH_LONG).show();
        else  // This is a valid body so we should do something and inform the user
            ItemClickDialog.newInstance(this).show(appCompatActivity.getSupportFragmentManager(),"itemClickDialog");
    }

    public boolean onLongClick(AppCompatActivity appCompatActivity) {
        if(getHasOptions(appCompatActivity)) {
            DataUtil dataUtil = new DataUtil(appCompatActivity);
            dataUtil.copyToClipboard(title,subTitle);
            return true;
        }
        return false;
    }

    public boolean matchesSearchText(String searchText, Context context) {
        return searchText.length() > 0 &&
                (title.toLowerCase().contains(searchText.toLowerCase())
                        || getSubTitleWithContext(context).toLowerCase().contains(searchText.toLowerCase()));
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
