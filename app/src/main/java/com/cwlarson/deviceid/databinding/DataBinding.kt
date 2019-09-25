@file:JvmName("DataBinding")
package com.cwlarson.deviceid.databinding

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowInsets
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.*
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.cwlarson.deviceid.R
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.card.MaterialCardView
import timber.log.Timber
import kotlin.math.max
import kotlin.math.roundToInt

@BindingAdapter("srcCompatBinding")
fun ImageView.bindSrcCompat(@DrawableRes drawable: Int) {
    setImageDrawable(if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        VectorDrawableCompat.create(context.resources, drawable, context.theme)
    else context.resources.getDrawable(drawable, context.theme))
}

@BindingAdapter(value=["searchBarOpen","searchBarExtraHorizontalMargin"], requireAll = true)
fun View.searchBarExtraHorizontalMargin(isSearchOpen: Boolean, extraMargin: Float) {
    val layoutParams = layoutParams as MarginLayoutParams
    updateLayoutParams<MarginLayoutParams> {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val start = getTag(R.id.origMarginStart) as Int? ?: layoutParams.marginStart
            val end = getTag(R.id.origMarginEnd) as Int? ?: layoutParams.marginEnd
            val extra = if(isSearchOpen) extraMargin.roundToInt() else 0
            updateMarginsRelative(start = start + extra, end = end + extra)
            if(getTag(R.id.origMarginStart) == null) setTag(R.id.origMarginStart, start)
            if(getTag(R.id.origMarginEnd) == null) setTag(R.id.origMarginEnd, end)
        } else {
            val left = getTag(R.id.origMarginStart) as Int? ?: layoutParams.leftMargin
            val right = getTag(R.id.origMarginEnd) as Int? ?: layoutParams.rightMargin
            val extra = if(isSearchOpen) extraMargin.roundToInt() else 0
            updateMargins(left = left + extra, right = right + extra)
            if(getTag(R.id.origMarginStart) == null) setTag(R.id.origMarginStart, left)
            if(getTag(R.id.origMarginEnd) == null) setTag(R.id.origMarginEnd, right)
        }
    }
}

@BindingAdapter("animatedVisibility")
fun View.setAnimatedVisibility(visible: Int) {
    // Were we animating before? If so, what was the visibility?
    val endAnimVisibility = getTag(R.id.finalVisibility) as Int?
    val oldVisibility = endAnimVisibility ?: visibility

    if (oldVisibility == visible) {
        // just let it finish any current animation.
        return
    }

    val isVisible = oldVisibility == View.VISIBLE
    val willBeVisible = visible == View.VISIBLE

    visibility = View.VISIBLE
    var startAlpha = if (isVisible) 1f else 0f
    if (endAnimVisibility != null) {
        startAlpha = alpha
    }
    val endAlpha = if (willBeVisible) 1f else 0f

    // Now create an animator
    val a = ObjectAnimator.ofFloat(this, View.ALPHA, startAlpha, endAlpha)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
        a.setAutoCancel(true)
    }

    a.addListener(object : AnimatorListenerAdapter() {
        private var isCanceled: Boolean = false

        override fun onAnimationStart(anim: Animator) {
            setTag(R.id.finalVisibility, visible)
        }

        override fun onAnimationCancel(anim: Animator) {
            isCanceled = true
        }

        override fun onAnimationEnd(anim: Animator) {
            setTag(R.id.finalVisibility, null)
            if (!isCanceled) {
                alpha = 1f
                visibility = visible
            }
        }
    })
    a.start()
}

@BindingAdapter(value=["animatedVisibility","searchView","searchViewHint","noFade"], requireAll = true)
fun View.setSearchHintVisibility(visible: Int, searchView: SearchView, searchViewHint: String?,
                                 noFade: Boolean) {
    if(noFade) {
        visibility = visible
        searchView.queryHint = searchViewHint
        return
    }
    // Were we animating before? If so, what was the visibility?
    val endAnimVisibility = getTag(R.id.finalVisibility) as Int?
    val oldVisibility = endAnimVisibility ?: visibility

    if (oldVisibility == visible) {
        // just let it finish any current animation.
        return
    }

    val isVisible = oldVisibility == View.VISIBLE
    val willBeVisible = visible == View.VISIBLE

    visibility = View.VISIBLE
    var startAlpha = if (isVisible) 1f else 0f
    if (endAnimVisibility != null) {
        startAlpha = alpha
    }
    val endAlpha = if (willBeVisible) 1f else 0f

    // Now create an animator
    val a = ObjectAnimator.ofFloat(this, View.ALPHA, startAlpha, endAlpha)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
        a.setAutoCancel(true)
    }

    a.addListener(object : AnimatorListenerAdapter() {
        private var isCanceled: Boolean = false

        override fun onAnimationStart(anim: Animator) {
            setTag(R.id.finalVisibility, visible)
        }

        override fun onAnimationCancel(anim: Animator) {
            isCanceled = true
        }

        override fun onAnimationEnd(anim: Animator) {
            setTag(R.id.finalVisibility, null)
            if (!isCanceled) {
                alpha = 1f
                visibility = visible
            }
            searchView.queryHint = if(visible == View.VISIBLE) null else searchViewHint
        }
    })
    a.start()
}

/**
 * Applies the fullscreen mode (behind status and navigation bars) & left/right insets to supplied view.
 * This should be the rootView of the layout for best performance.
 */
@BindingAdapter("systemUIVisibility")
fun View.applySystemUiVisibility(applyVisibility: Boolean) {
    if(applyVisibility && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        systemUiVisibility =
                // Tells the system that you wish to be laid out as if the navigation bar was hidden
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                // Optional, if we want to be laid out fullscreen, behind the status bar
                //View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                // Tells the system that you wish to be laid out at the most extreme scenario of any other flags
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    } else if(applyVisibility && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        fitsSystemWindows = true
    }
}

@BindingAdapter("hideFix")
fun AppBarLayout.appBarLayoutHideFix(visibility: Int) {
    updateLayoutParams<CoordinatorLayout.LayoutParams> {
        height = if(visibility == View.VISIBLE) CoordinatorLayout.LayoutParams.WRAP_CONTENT else 0
    }
}

/**
 * Set the bottom padding so that the content bottom is above the nav bar (y-axis).
 * Use such as: app:paddingBottomSystemWindowInsets="@{ true }"
 */
@BindingAdapter("paddingLeftSystemWindowInsets", "paddingTopSystemWindowInsets",
        "paddingRightSystemWindowInsets", "paddingBottomSystemWindowInsets", "paddingActionBar",
        requireAll = false)
fun View.applySystemWindows(applyLeft: Boolean = false, applyTop: Boolean = false,
                       applyRight: Boolean = false, applyBottom: Boolean = false,
                       applyActionBarPadding: Boolean = false) {
    val extra = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,8F,
            resources.displayMetrics).toInt()
    val tv = TypedValue()
    val abHeight = if (context.theme.resolveAttribute(R.attr.actionBarSize, tv, true)) {
        TypedValue.complexToDimensionPixelSize(tv.data, context.resources.displayMetrics)
    } else 0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
        doOnApplyWindowInsets { v, insets, padding, margin ->
            val left = if (applyLeft) insets.systemWindowInsetLeft else 0
            val top = if (applyTop) insets.systemWindowInsetTop else 0
            val right = if (applyRight) insets.systemWindowInsetRight else 0
            val bottom = if (applyBottom) insets.systemWindowInsetBottom else 0
            when (v) { // Fix for custom CardView since it cannot use padding
                is MaterialCardView -> {
                    if(v.getTag(R.id.hasInsetsSet) == null) {
                        v.setTag(R.id.hasInsetsSet, true)
                        v.doOnPreDraw {
                            it.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                            val addtlHeight = max(0, (abHeight - (it.measuredHeight - it
                                    .paddingTop - it.paddingBottom)) / 2)
                            it.updateLayoutParams<MarginLayoutParams> {
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
                is RecyclerView -> {
                    v.setPadding(padding.left + left, padding.top + top + if (applyActionBarPadding)
                        abHeight else 0, padding.right + right, padding.bottom + bottom)
                }
                else -> {
                    if(v.getTag(R.id.hasInsetsSet) == null) {
                        v.setTag(R.id.hasInsetsSet, true)
                        v.setPadding(padding.left + left, padding.top + top + if (applyActionBarPadding)
                            abHeight else 0, padding.right + right, padding.bottom + bottom)
                    }
                }
            }
            if (parent is SwipeRefreshLayout) (parent as SwipeRefreshLayout)
                    .setProgressViewOffset(false, 0, padding.top + top + extra +
                            (if(applyActionBarPadding) abHeight else 0))
        }
    } else {
        when (this) { // Fix for custom CardView since it cannot use padding
            is MaterialCardView -> {
                if(getTag(R.id.hasInsetsSet) == null) {
                    setTag(R.id.hasInsetsSet, true)
                    doOnPreDraw {
                        it.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                        val addtlHeight = max(0, (abHeight - (it.measuredHeight - it
                                .paddingTop - it.paddingBottom)) / 2)
                        it.updateLayoutParams<MarginLayoutParams> {
                            updateMargins(top = topMargin + addtlHeight, bottom = bottomMargin + addtlHeight)
                        }
                        Timber.d("${it.marginTop} / $addtlHeight - $abHeight / $measuredHeight / ${it.paddingTop} / ${it.paddingBottom}")
                    }
                }
            }
            else -> {
                if(getTag(R.id.hasInsetsSet) == null) {
                    setTag(R.id.hasInsetsSet, true)
                    updatePadding(top = paddingTop + if (applyActionBarPadding) abHeight else 0)
                }
            }
        }
        if (parent is SwipeRefreshLayout) (parent as SwipeRefreshLayout)
                .setProgressViewOffset(false, 0, paddingTop + extra)
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