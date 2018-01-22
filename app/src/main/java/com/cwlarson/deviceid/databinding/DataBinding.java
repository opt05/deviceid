package com.cwlarson.deviceid.databinding;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.cwlarson.deviceid.R;

public class DataBinding {

    @BindingAdapter("buttonImageAbove")
    public static void setButtonImageAbove(Button button, @DrawableRes int drawable) {
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

    @BindingAdapter("animatedVisibility")
    public static void setVisibility(final View view, final int visibility) {
        // Were we animating before? If so, what was the visibility?
        Integer endAnimVisibility = (Integer) view.getTag(R.id.finalVisibility);
        int oldVisibility = endAnimVisibility == null
            ? view.getVisibility()
            : endAnimVisibility;

        if (oldVisibility == visibility) {
            // just let it finish any current animation.
            return;
        }

        boolean isVisibile = oldVisibility == View.VISIBLE;
        boolean willBeVisible = visibility == View.VISIBLE;

        view.setVisibility(View.VISIBLE);
        float startAlpha = isVisibile ? 1f : 0f;
        if (endAnimVisibility != null) {
            startAlpha = view.getAlpha();
        }
        float endAlpha = willBeVisible ? 1f : 0f;

        // Now create an animator
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, View.ALPHA, startAlpha, endAlpha);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            alpha.setAutoCancel(true);
        }

        alpha.addListener(new AnimatorListenerAdapter() {
            private boolean isCanceled;

            @Override
            public void onAnimationStart(Animator anim) {
                view.setTag(R.id.finalVisibility, visibility);
            }

            @Override
            public void onAnimationCancel(Animator anim) {
                isCanceled = true;
            }

            @Override
            public void onAnimationEnd(Animator anim) {
                view.setTag(R.id.finalVisibility, null);
                if (!isCanceled) {
                    view.setAlpha(1f);
                    view.setVisibility(visibility);
                }
            }
        });
        alpha.start();
    }
}
