package com.cwlarson.deviceid.databinding;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.AppCompatButton;
import android.widget.ImageView;

public class DataBinding {
    private DataBinding() {
        //empty
    }

    @BindingAdapter("buttonImageAbove")
    public static void setButtonImageAbove(AppCompatButton button, @DrawableRes int drawable) {
        Drawable icon;
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            icon = VectorDrawableCompat.create(button.getContext().getResources(), drawable, button.getContext().getTheme());
        } else {
            icon = button.getContext().getResources().getDrawable(drawable, button.getContext().getTheme());
        }
        button.setCompoundDrawablesWithIntrinsicBounds(null,icon,null,null);
    }

    @BindingAdapter("srcCompatBinding")
    public static void bindSrcCompat(ImageView imageView, @DrawableRes int drawable) {
        Drawable icon;
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            icon = VectorDrawableCompat.create(imageView.getContext().getResources(), drawable, imageView.getContext().getTheme());
        } else {
            icon = imageView.getContext().getResources().getDrawable(drawable, imageView.getContext().getTheme());
        }
        imageView.setImageDrawable(icon);
    }
}
