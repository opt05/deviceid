package com.cwlarson.deviceid.data;

import android.content.Context;
import android.text.TextUtils;

import com.cwlarson.deviceid.R;

/**
 * Helper class for recyclerview items
 */
public class Item {
    private String title, subTitle;
    private final Context context;

    public Item(Context context){
        this.context=context;
    }

    public String getTitle(){
        return title;
    }

    public String getSubTitle(){
        return TextUtils.isEmpty(subTitle) ? context.getResources().getString(R.string.not_found) : subTitle;
    }

    public void setTitle(String title){
        this.title=title;
    }

    public void setSubTitle(String subTitle){
        this.subTitle=subTitle;
    }
}
