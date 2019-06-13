@file:JvmName("DataBinding")
package com.cwlarson.deviceid.databinding

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.*
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.cwlarson.deviceid.R
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.card.MaterialCardView
import timber.log.Timber

@BindingAdapter("srcCompatBinding")
fun bindSrcCompat(imageView: ImageView, @DrawableRes drawable: Int) {
    val icon = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
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

@BindingAdapter(value=["animatedVisibility","searchView","searchViewHint","noFade"],
        requireAll = true)
fun setSearchHintVisibility(view: TextView, visibility: Int, searchView: SearchView,
                            searchViewHint: String?, noFade: Boolean) {
    if(noFade) {
        view.visibility = visibility
        searchView.queryHint = searchViewHint
        return
    }
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
            searchView.queryHint = if(visibility == View.VISIBLE) null else searchViewHint
        }
    })
    alpha.start()
}

/**
 * Applies the fullscreen mode (behind status and navigation bars) & left/right insets to supplied view.
 * This should be the rootView of the layout for best performance.
 * FIXME: This may not be needed after Android Q release with new support libraries
 */
@BindingAdapter("systemUIVisibility")
fun applySystemUiVisibility(view: View, applyVisibility: Boolean) {
    if(applyVisibility && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        view.systemUiVisibility =
                // Tells the system that you wish to be laid out as if the navigation bar was hidden
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                // Optional, if we want to be laid out fullscreen, behind the status bar
                //View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                // Tells the system that you wish to be laid out at the most extreme scenario of any other flags
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    } else if(applyVisibility && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        view.fitsSystemWindows = true
    }
}

@BindingAdapter("hideFix")
fun appBarLayoutHideFix(view: AppBarLayout, visibility: Int) {
    view.updateLayoutParams<CoordinatorLayout.LayoutParams> {
        height = if(visibility == View.VISIBLE) CoordinatorLayout.LayoutParams.WRAP_CONTENT else 0
    }
}

/**
 * Set the bottom padding so that the content bottom is above the nav bar (y-axis).
 * Use such as: app:paddingBottomSystemWindowInsets="@{ true }"
 * FIXME: This may not be needed after Android Q release with new support libraries
 */
@BindingAdapter("paddingLeftSystemWindowInsets", "paddingTopSystemWindowInsets",
        "paddingRightSystemWindowInsets", "paddingBottomSystemWindowInsets", "paddingActionBar",
        requireAll = false)
fun applySystemWindows(view: View, applyLeft: Boolean, applyTop: Boolean, applyRight: Boolean,
                       applyBottom: Boolean, applyActionBarPadding: Boolean) {
    val extra = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,8F,
            view.resources.displayMetrics).toInt()
    val tv = TypedValue()
    val abHeight = if (view.context.theme.resolveAttribute(R.attr.actionBarSize, tv, true)) {
        TypedValue.complexToDimensionPixelSize(tv.data, view.context.resources.displayMetrics)
    } else 0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
        view.doOnApplyWindowInsets { v, insets, padding, margin ->
            val left = if (applyLeft) insets.systemWindowInsetLeft else 0
            val top = if (applyTop) insets.systemWindowInsetTop else 0
            val right = if (applyRight) insets.systemWindowInsetRight else 0
            val bottom = if (applyBottom) insets.systemWindowInsetBottom else 0
            when (v) { // Fix for custom CardView since it cannot use padding
                is MaterialCardView -> {
                    if(v.getTag(R.id.hasMarginSet) == null) {
                        v.setTag(R.id.hasMarginSet, true)
                        v.doOnPreDraw {
                            it.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                            val addtlHeight = Math.max(0, (abHeight - (it.measuredHeight - it
                                    .paddingTop - it.paddingBottom)) / 2)
                            it.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                                updateMargins(
                                        top = margin.top + top + addtlHeight,
                                        bottom = margin.bottom + bottom + addtlHeight,
                                        left = margin.left + left,
                                        right = margin.right + right
                                )
                            }
                            Timber.d("${margin.top} / $top / $addtlHeight - $abHeight / ${it.measuredHeight}")
                        }
                    }
                }
                else -> {
                    v.setPadding(padding.left + left, padding.top + top + if(applyActionBarPadding)
                        abHeight else 0, padding.right + right, padding.bottom + bottom)
                }
            }
            if (view.parent is SwipeRefreshLayout) (view.parent as SwipeRefreshLayout)
                    .setProgressViewOffset(false, 0, padding.top + top + extra +
                            (if(applyActionBarPadding) abHeight else 0))
        }
    } else {
        when (view) { // Fix for custom CardView since it cannot use padding
            is MaterialCardView -> {
                if(view.getTag(R.id.hasMarginSet) == null) {
                    view.setTag(R.id.hasMarginSet, true)
                    view.doOnPreDraw {
                        it.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                        val addtlHeight = Math.max(0,(abHeight - (it.measuredHeight - it
                                .paddingTop - it.paddingBottom)) / 2)
                        it.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            updateMargins(top = topMargin + addtlHeight, bottom = bottomMargin + addtlHeight)
                        }
                        Timber.d("${it.marginTop} / $addtlHeight - $abHeight / ${view
                                .measuredHeight} / ${it.paddingTop} / ${it.paddingBottom}")
                    }
                }
            }
            else -> {
                view.updatePadding(top = view.paddingTop + if(applyActionBarPadding) abHeight else 0)
            }
        }
        if (view.parent is SwipeRefreshLayout) (view.parent as SwipeRefreshLayout)
                .setProgressViewOffset(false, 0, view.paddingTop + extra)
    }
}

@RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
private fun View.doOnApplyWindowInsets(f: (View, WindowInsets, InitialPadding, InitialMargin) -> Unit) {
    // Create a snapshot of the view's padding state
    val initialPadding = recordInitialPaddingForView(this)
    val initialMargin = recordInitialMarginForView(this)
    // Set an actual OnApplyWindowInsetsListener which proxies to the given
    // lambda, also passing in the original padding state
    setOnApplyWindowInsetsListener { v, insets ->
        f(v, insets, initialPadding, initialMargin)
        // Always return the insets, so that children can also use them
        insets
    }
    // request some insets
    requestApplyInsetsWhenAttached()
}
data class InitialPadding(val left: Int, val top: Int, val right: Int, val bottom: Int)
private fun recordInitialPaddingForView(view: View) = InitialPadding(
        view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)
data class InitialMargin(val left: Int, val top: Int, val right: Int, val bottom: Int)
private fun recordInitialMarginForView(view: View) = InitialMargin(
        view.marginStart, view.marginTop, view.marginEnd, view.marginBottom)
/**
 * Call [View.requestApplyInsets] in a safe away. If we're attached it calls it straight-away.
 * If not it sets an [View.OnAttachStateChangeListener] and waits to be attached before calling
 * [View.requestApplyInsets].
 */
@RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
private fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) { requestApplyInsets()
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.requestApplyInsets()
            }
            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}