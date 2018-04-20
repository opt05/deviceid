@file:JvmName("DataBinding")
package com.cwlarson.deviceid.databinding

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.databinding.BindingAdapter
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.graphics.drawable.VectorDrawableCompat
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.cwlarson.deviceid.R

@BindingAdapter("buttonImageAbove")
fun setButtonImageAbove(button: Button, @DrawableRes drawable: Int) {
    val icon = if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        VectorDrawableCompat.create(button.context.resources, drawable, button.context.theme)
    } else {
        button.context.resources.getDrawable(drawable, button.context.theme)
    }
    button.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null)
}

@BindingAdapter("srcCompatBinding")
fun bindSrcCompat(imageView: ImageView, @DrawableRes drawable: Int) {
    val icon = if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        VectorDrawableCompat.create(imageView.context.resources, drawable, imageView.context.theme)
    } else {
        imageView.context.resources.getDrawable(drawable, imageView.context.theme)
    }
    imageView.setImageDrawable(icon)
}

@BindingAdapter("animatedVisibility")
fun setVisibility(view: View, visibility: Int) {
    // Were we animating before? If so, what was the visibility?
    val endAnimVisibility = view.getTag(R.id.finalVisibility) as Int?
    val oldVisibility = endAnimVisibility ?: view.visibility

    if (oldVisibility == visibility) {
        // just let it finish any current animation.
        return
    }

    val isVisible = oldVisibility == View.VISIBLE
    val willBeVisible = visibility == View.VISIBLE

    view.visibility = View.VISIBLE
    var startAlpha = if (isVisible) 1f else 0f
    if (endAnimVisibility != null) {
        startAlpha = view.alpha
    }
    val endAlpha = if (willBeVisible) 1f else 0f

    // Now create an animator
    val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, startAlpha, endAlpha)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
        alpha.setAutoCancel(true)
    }

    alpha.addListener(object : AnimatorListenerAdapter() {
        private var isCanceled: Boolean = false

        override fun onAnimationStart(anim: Animator) {
            view.setTag(R.id.finalVisibility, visibility)
        }

        override fun onAnimationCancel(anim: Animator) {
            isCanceled = true
        }

        override fun onAnimationEnd(anim: Animator) {
            view.setTag(R.id.finalVisibility, null)
            if (!isCanceled) {
                view.alpha = 1f
                view.visibility = visibility
            }
        }
    })
    alpha.start()
}