@file:JvmName("ViewUtils")
package com.cwlarson.deviceid.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.SearchOpen
import com.cwlarson.deviceid.TitleVisibility
import com.cwlarson.deviceid.databinding.SearchbarLayoutBinding
import com.cwlarson.deviceid.search.SuggestionAdapter
import com.cwlarson.deviceid.settings.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import kotlin.math.max


/**
 * For [androidx.recyclerview.widget.GridLayoutManager] to determine number
 * of columns based on screen width
 *
 * @param twoPane is the app currently in two pane mode or not
 * @return number of columns
 */
fun Fragment.calculateNoOfColumns(twoPane: Boolean): Int {
    val displayMetrics = resources.displayMetrics
    val dpWidth = (displayMetrics.widthPixels - if (twoPane)
        resources.getDimensionPixelSize(R.dimen.navigation_list_width) else 0) / displayMetrics.density
    val calcValue = (dpWidth / 300).toInt()
    return if (calcValue < 1) 1 else calcValue
}

/**
 * Determines the bottom sheet's max width should screen size be greater than 750px
 * and updates the dialog as such. This should be run in [BottomSheetDialogFragment.onCreateDialog]
 * @return The same dialog
 */
fun Dialog.calculateBottomSheetMaxWidth(): Dialog {
    setOnShowListener {
        val displayMetrics = context.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        window?.setLayout(
            if (dpWidth > 750) 750 else ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
    return this
}

/**
 * Make a [Snackbar] to display a message with the view to find a parent from. This view is also
 * used to find the anchor view when calling [Snackbar.setAnchorView].
 *
 * @param text The text to show - should be formatted text.
 * @param duration How long to display the message. Can be [Snackbar.LENGTH_SHORT],
 * [Snackbar.LENGTH_LONG], [Snackbar.LENGTH_INDEFINITE], or a custom duration in milliseconds.
 * Default is [Snackbar.LENGTH_LONG] if not specified.
 */
fun View.snackbar(
    text: CharSequence,
    @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_LONG
) {
    createSnackbar(text, duration).show()
}

private typealias onActionClick = () -> Unit

/**
 * Make a [Snackbar] to display a message with the view to find a parent from. This view is also
 * used to find the anchor view when calling [Snackbar.setAnchorView].
 *
 * @param text The text to show - should be String resource.
 * @param duration How long to display the message. Can be [Snackbar.LENGTH_SHORT],
 * [Snackbar.LENGTH_LONG], [Snackbar.LENGTH_INDEFINITE], or a custom duration in milliseconds.
 * Default is [Snackbar.LENGTH_LONG] if not specified.
 * @param action String resource to display for the action
 * @param onActionClick callback to be invoked when the action is clicked
 */
fun View.snackbar(
    @StringRes text: Int,
    @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_LONG,
    @StringRes action: Int,
    onActionClick: onActionClick
) {
    createSnackbar(resources.getText(text), duration)
        .setActionTextColor(ContextCompat.getColor(context, R.color.imageSecondary))
        .setAction(action) { onActionClick.invoke() }.show()
}

/**
 * Make a [Snackbar] to display a message with the view to find a parent from. This view is also
 * used to find the anchor view when calling [Snackbar.setAnchorView].
 *
 * @param text The text to show - should be formatted text.
 * @param duration How long to display the message. Can be [Snackbar.LENGTH_SHORT],
 * [Snackbar.LENGTH_LONG], [Snackbar.LENGTH_INDEFINITE], or a custom duration in milliseconds.
 * Default is [Snackbar.LENGTH_LONG] if not specified.
 * @param action Text to display for the action
 * @param onActionClick callback to be invoked when the action is clicked
 */
fun View.snackbar(
    text: CharSequence,
    @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_LONG,
    action: CharSequence,
    onActionClick: onActionClick
) {
    createSnackbar(text, duration)
        .setActionTextColor(ContextCompat.getColor(context, R.color.imageSecondary))
        .setAction(action) { onActionClick.invoke() }.show()
}

private fun View.createSnackbar(
    text: CharSequence,
    @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_LONG
): Snackbar {
    val v = rootView.findViewById(R.id.bottom_navigation_spacer)
        ?: rootView.findViewById(R.id.coordinator_layout) ?: rootView
    return Snackbar.make(v, text, duration).apply {
        if (v.id == R.id.bottom_navigation_spacer) {
            view.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                anchorId = v.id
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                anchorGravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            }
        }
    }
}

/**
 * Make a standard toast that just contains a text view. The context to use. Usually your
 * [android.app.Application] or [android.app.Activity] object.
 *
 * @param text The text to show - Should be String resource.
 * @param duration How long to display the message.  Either [Toast.LENGTH_SHORT] or
 * [Toast.LENGTH_LONG]. Default is [Toast.LENGTH_SHORT] if not specified.
 */
fun Context?.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

/**
 * Make a standard toast that just contains a text view. The context to use. Usually your
 * [android.app.Application] or [android.app.Activity] object.
 *
 * @param text The text to show - Should be formatted text.
 * @param duration How long to display the message.  Either [Toast.LENGTH_SHORT] or
 * [Toast.LENGTH_LONG]. Default is [Toast.LENGTH_SHORT] if not specified.
 */
fun Context?.toast(@StringRes text: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

/**
 * Modifies the specified view's (search layout) to add extra margin, hide/show back button, and
 * change [SearchView] padding, based on the supplied variables
 *
 * @param searchOpen is the [SearchView] currently open or not and is the app currently in a two
 * pane mode or not
 */
fun SearchbarLayoutBinding.modifySearchLayout(searchOpen: SearchOpen) {
    val layoutParams = root.layoutParams as MarginLayoutParams
    val extraMargin: Int = if (searchOpen.yes && searchOpen.twoPane)
        root.resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin) else 0
    root.updateLayoutParams<MarginLayoutParams> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val start = root.getTag(R.id.origMarginStart) as Int? ?: layoutParams.marginStart
            val end = root.getTag(R.id.origMarginEnd) as Int? ?: layoutParams.marginEnd
            updateMarginsRelative(start = start + extraMargin, end = end + extraMargin)
            if (root.getTag(R.id.origMarginStart) == null) root.setTag(R.id.origMarginStart, start)
            if (root.getTag(R.id.origMarginEnd) == null) root.setTag(R.id.origMarginEnd, end)
        } else {
            val left = root.getTag(R.id.origMarginStart) as Int? ?: layoutParams.leftMargin
            val right = root.getTag(R.id.origMarginEnd) as Int? ?: layoutParams.rightMargin
            updateMargins(left = left + extraMargin, right = right + extraMargin)
            if (root.getTag(R.id.origMarginStart) == null) root.setTag(R.id.origMarginStart, left)
            if (root.getTag(R.id.origMarginEnd) == null) root.setTag(R.id.origMarginEnd, right)
        }
    }
    // Hide/Show back button
    searchBackButton.isVisible = searchOpen.yes
    // Update SearchView padding
    searchBar.updatePadding(
        left = root.context.resources.getDimensionPixelSize(
            if (searchOpen.yes)
                R.dimen.searchbar_padding_active else R.dimen.searchbar_padding_inactive
        )
    )
}

/**
 * Resets view's Y translation if not at edge of screen
 */
fun View.showViewOnNavigationChange() {
    if (translationY != 0f) {
        clearAnimation()
        animate().translationY(0f).duration = 200
    }
}

/**
 * Fades the specified view in and out based on the supplied visibility.
 *
 * @param visible The visibility to change the view to. Can be [View.VISIBLE], [View.GONE] or
 * [View.INVISIBLE].
 */
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
    if (endAnimVisibility != null) startAlpha = alpha
    val endAlpha = if (willBeVisible) 1f else 0f

    // Now create an animator
    ObjectAnimator.ofFloat(this, View.ALPHA, startAlpha, endAlpha).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            setAutoCancel(true)

        addListener(object : AnimatorListenerAdapter() {
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
    }.start()
}

/**
 * Fades the specified view in and out based on the supplied visibility. Should be used with
 * SearchBar Layout's app title
 *
 * @param title The visibility to change the view to
 */
fun SearchbarLayoutBinding.setSearchHintVisibility(title: TitleVisibility) {
    if (title.noFade) {
        appTitle.root.visibility = title.visible
        searchBar.queryHint = root.context.getString(R.string.search_hint_items)
        return
    }
    // Were we animating before? If so, what was the visibility?
    val endAnimVisibility = appTitle.root.getTag(R.id.finalVisibility) as Int?
    val oldVisibility = endAnimVisibility ?: appTitle.root.visibility

    if (oldVisibility == title.visible) {
        // just let it finish any current animation.
        return
    }

    val isVisible = oldVisibility == View.VISIBLE
    val willBeVisible = title.visible == View.VISIBLE

    appTitle.root.visibility = View.VISIBLE
    var startAlpha = if (isVisible) 1f else 0f
    if (endAnimVisibility != null) startAlpha = root.alpha
    val endAlpha = if (willBeVisible) 1f else 0f

    // Now create an animator
    ObjectAnimator.ofFloat(appTitle.root, View.ALPHA, startAlpha, endAlpha).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            setAutoCancel(true)

        addListener(object : AnimatorListenerAdapter() {
            private var isCanceled: Boolean = false

            override fun onAnimationStart(anim: Animator) {
                appTitle.root.setTag(R.id.finalVisibility, title.visible)
            }

            override fun onAnimationCancel(anim: Animator) {
                isCanceled = true
            }

            override fun onAnimationEnd(anim: Animator) {
                appTitle.root.setTag(R.id.finalVisibility, null)
                if (!isCanceled) {
                    appTitle.root.alpha = 1f
                    appTitle.root.visibility = title.visible
                }
                searchBar.queryHint =
                    if (title.visible == View.VISIBLE) null
                    else root.context.getString(R.string.search_hint_items)
            }
        })
    }.start()
}

private typealias onQuery = (query: String?, needsToSubmit: Boolean) -> Unit

fun SearchbarLayoutBinding.setupSearchBarLayout(
    navController: NavController,
    preferenceManager: PreferenceManager,
    onQuery: onQuery
) {
    searchBackButton.setOnClickListener {
        root.isFocusable = false
        root.isFocusableInTouchMode = false
        root.clearFocus()
        navController.navigateUp()
    }
    searchBar.setOnQueryTextFocusChangeListener { _, hasFocus ->
        if (!hasFocus && preferenceManager.searchHistory)
            searchBar.query?.let { preferenceManager.saveSearchHistoryItem(it.toString()) }
    }
    searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            preferenceManager.saveSearchHistoryItem(query)
            onQuery.invoke(query, false)
            if (!query.isNullOrBlank() &&
                navController.currentDestination?.id != R.id.search_fragment_dest
            )
                navController.navigate(R.id.search_fragment_dest)
            return false
        }

        override fun onQueryTextChange(query: String?): Boolean {
            onQuery.invoke(query, false)
            if (!query.isNullOrBlank() &&
                navController.currentDestination?.id != R.id.search_fragment_dest
            )
                navController.navigate(R.id.search_fragment_dest)
            return false
        }
    })
    searchBar.findViewById<SearchView.SearchAutoComplete>(R.id.search_src_text)?.apply {
        @ColorRes val colorRes = TypedValue().run {
            this@setupSearchBarLayout.root.context.theme.resolveAttribute(
                R.attr.colorBackgroundFloating,
                this, true
            )
            resourceId
        }
        setDropDownBackgroundResource(colorRes)
        val searchHistoryAdapter = SuggestionAdapter(
            this@setupSearchBarLayout.root.context,
            R.layout.searchview_history_item,
            preferenceManager.getSearchHistoryItems(),
            android.R.id.text1
        )
        @SuppressLint("RestrictedApi")
        threshold = 0
        setAdapter(searchHistoryAdapter)
        onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            onQuery.invoke(searchHistoryAdapter.getItem(position), true)
        }
    } ?: Timber.wtf("SearchView.SearchAutoComplete id has changed and requires maintenance")
}

/**
 * Updates the history list of the [SearchView]
 * @param list History list of all the items, shown in the supplied order
 */
fun SearchbarLayoutBinding.updateSearchBarAdapter(list: List<String>) {
    searchBar.findViewById<SearchView.SearchAutoComplete?>(R.id.search_src_text)?.apply {
        (adapter as? SuggestionAdapter)?.updateList(list)
    } ?: Timber.wtf("SearchView.SearchAutoComplete id has changed and requires maintenance")
}

/**
 * Applies the fullscreen mode (behind status and navigation bars) & left/right insets to supplied view.
 * This should be the rootView of the layout for best performance.
 */
fun Window.applySystemUiVisibility() {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ->
            WindowCompat.setDecorFitsSystemWindows(this, false)
        Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP -> decorView.fitsSystemWindows = true
    }
}

/**
 * A fix for the hiding and showing of the [com.google.android.material.appbar.AppBarLayout] as
 * transitioning to each state messes with the [SearchView] layout
 *
 * @param appBarIsVisible is the appbar layout currently visible or not
 */
fun View.appBarLayoutHideFix(appBarIsVisible: Boolean) {
    updateLayoutParams<ViewGroup.LayoutParams> {
        height = if (appBarIsVisible) CoordinatorLayout.LayoutParams.WRAP_CONTENT else 0
    }
}

/**
 * A fix for FragmentContainerView not working with NavHostFragment when used on
 * Activity onCreate() - https://issuetracker.google.com/issues/142847973
 *
 * @param viewId The id of the [NavHostFragment]
 * @return The [NavController]
 */
fun AppCompatActivity.findNavControllerFixed(@IdRes viewId: Int): NavController =
    (supportFragmentManager.findFragmentById(viewId) as NavHostFragment).navController

/**
 * Set the bottom padding so that the content bottom is above the nav bar (y-axis).
 * Use such as: app:paddingBottomSystemWindowInsets="@{ true }"
 *
 * @param applyLeft should apply the left side inset or not
 * @param applyTop should apply the top side inset or not
 * @param applyRight should apply the right side inset or not
 * @param applyBottom should apply the bottom side inset or not
 * @param applyActionBarPadding should add extra top inset that is the height of the appbar
 */
fun View.applySystemWindows(
    applyLeft: Boolean = false, applyTop: Boolean = false,
    applyRight: Boolean = false, applyBottom: Boolean = false,
    applyActionBarPadding: Boolean = false
) {
    val extra = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 8F,
        resources.displayMetrics
    ).toInt()
    val tv = TypedValue()
    val abHeight = if (context.theme.resolveAttribute(R.attr.actionBarSize, tv, true)) {
        TypedValue.complexToDimensionPixelSize(tv.data, context.resources.displayMetrics)
    } else 0
    doOnApplyWindowInsets { v, insets, padding, margin ->
        val left =
            if (applyLeft) insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()).left else 0
        val top =
            if (applyTop) insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()).top else 0
        val right =
            if (applyRight) insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()).right else 0
        val bottom =
            if (applyBottom) insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()).bottom else 0
        when (v) { // Fix for custom CardView since it cannot use padding
            is MaterialCardView -> {
                if (getTag(R.id.hasInsetsSet) == null) {
                    setTag(R.id.hasInsetsSet, true)
                    doOnPreDraw {
                        it.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                        val addtlHeight = max(
                            0, (abHeight - (it.measuredHeight - it
                                .paddingTop - it.paddingBottom)) / 2
                        )
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
            else -> {
                if (getTag(R.id.hasInsetsSet) == null) {
                    setTag(R.id.hasInsetsSet, true)
                    updatePadding(
                        left = paddingLeft + left,
                        right = paddingRight + right,
                        top = paddingTop + top + if (applyActionBarPadding) abHeight else 0,
                        bottom = paddingBottom + bottom
                    )
                }
            }
        }
        (parent as? SwipeRefreshLayout)?.setProgressViewOffset(
            false, 0, padding.top + top + extra +
                    (if (applyActionBarPadding) abHeight else 0)
        )
    }
}

private fun View.doOnApplyWindowInsets(f: (View, WindowInsetsCompat, InitialPadding, InitialMargin) -> Unit) {
    // Create a snapshot of the view's padding state
    val initialPadding = recordInitialPaddingForView(this)
    val initialMargin = recordInitialMarginForView(this)
    // Set an actual OnApplyWindowInsetsListener which proxies to the given
    // lambda, also passing in the original padding state
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        f(v, insets, initialPadding, initialMargin)
        // Always return the insets, so that children can also use them
        insets
    }
    // request some insets
    ViewCompat.requestApplyInsets(this)
    requestApplyInsetsWhenAttached()
}

private data class InitialPadding(val left: Int, val top: Int, val right: Int, val bottom: Int)

private fun recordInitialPaddingForView(view: View) = InitialPadding(
    view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
)

private data class InitialMargin(val left: Int, val top: Int, val right: Int, val bottom: Int)

private fun recordInitialMarginForView(view: View) = InitialMargin(
    view.marginStart, view.marginTop, view.marginEnd, view.marginBottom
)

/**
 * Call [View.requestApplyInsets] in a safe away. If we're attached it calls it straight-away.
 * If not it sets an [View.OnAttachStateChangeListener] and waits to be attached before calling
 * [View.requestApplyInsets].
 */
private fun View.requestApplyInsetsWhenAttached() {
    if (ViewCompat.isAttachedToWindow(this)) {
        ViewCompat.requestApplyInsets(this)
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                ViewCompat.requestApplyInsets(v)
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}