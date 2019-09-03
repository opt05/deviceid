package com.cwlarson.deviceid.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.marginTop
import com.google.android.material.animation.AnimationUtils

class SearchViewBehavior<V: View>@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null):
        CoordinatorLayout.Behavior<V>(context, attrs) {
    companion object {
        private const val ENTER_ANIMATION_DURATION = 225
        private const val EXIT_ANIMATION_DURATION = 175
        private const val STATE_SCROLLED_DOWN = 1
        private const val STATE_SCROLLED_UP = 2
    }
    private var height: Int = 0
    private var currentState = STATE_SCROLLED_UP
    private var currentAnimator: ViewPropertyAnimator? = null

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        height = child.measuredHeight + child.marginTop
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
        if (currentState != STATE_SCROLLED_UP  && dyConsumed < 0) {
            slideDown(child)
        } else if (currentState != STATE_SCROLLED_DOWN && dyConsumed > 0) {
            slideUp(child)
        }
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
    }

    private fun slideUp(child: View) {
        currentAnimator?.let {
            it.cancel()
            child.clearAnimation()
        }
        currentState = STATE_SCROLLED_DOWN
        animateChildTo(child, -height, ENTER_ANIMATION_DURATION.toLong(),
                AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)
    }

    private fun slideDown(child: View) {
        currentAnimator?.let {
            it.cancel()
            child.clearAnimation()
        }
        currentState = STATE_SCROLLED_UP
        animateChildTo( child, 0, EXIT_ANIMATION_DURATION.toLong(),
                AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR)
    }

    private fun animateChildTo(child: View, targetY: Int, duration: Long, interpolator: TimeInterpolator) {
        currentAnimator = child
                .animate()
                .translationY(targetY.toFloat())
                .setInterpolator(interpolator)
                .setDuration(duration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        currentAnimator = null
                    }
                })
    }
}