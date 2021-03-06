package com.foo.flickerimg.views

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import com.foo.flickerimg.R
import com.foo.flickerimg.resToPx
import java.lang.UnsupportedOperationException


/**
 * A lamda that return the headerview. Perhaps we can pass in other params here instead of just
 * [LayoutInflater]? like first fully visible item pos?
 *
 */
typealias HeaderViewDelegate = (LayoutInflater) -> View?

/**
 * A layout that implements the "Quick return pattern"
 */
class QuickReturnLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : OverlayFrameLayout(context, attrs, defStyleAttr) {
    val TAG = this::class.simpleName


    var showing = false
    var hiding = false

    protected var headerView: View? = null
    lateinit var recyclerView: RecyclerView
    lateinit var llm: LinearLayoutManager
    lateinit var delegate: HeaderViewDelegate
    internal val scrollListener = object : OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            this@QuickReturnLayout.onScrolled(recyclerView, dx, dy)
        }

    }


    /**
     * Setup the QuickReturnLayout helper class
     */
    fun setup(recyclerView: RecyclerView, delegate: HeaderViewDelegate) {
        this.recyclerView = recyclerView
        (this.recyclerView.layoutManager as? LinearLayoutManager)?.let {
            llm = it
        } ?: throw UnsupportedOperationException("Only supports LinearLayoutManager atm")

        this.delegate = delegate
        recyclerView.addOnScrollListener(scrollListener)

    }

    /**
     * Just FYI
     * recyclerView.computeVerticalScrollExtent() = visible v-height px
     * recyclerView.computeVerticalScrollOffset() = first item px relative to the first
     */
    internal fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

        val pos = llm.findFirstCompletelyVisibleItemPosition()
        // Log.d(TAG, "[${Thread.currentThread().hashCode()}]onScrolled: $dx,$dy pos=$pos")
        if (pos == 0) {
            hideHeader(false)
        } else {
            if (dy > 0) {
                hideHeader()
            } else if (Math.abs(dy) <= context.resToPx(R.dimen.quick_return_scroll_dy)) {
                showHeader()
            }
        }
    }


    fun showHeader() {

        if (showing) {
            return
        }

        headerView?.let { view ->
            // if there is already a view we want to return
            if (hiding) {
                // but if it is hiding we will cancel the animation
                view.animation?.let {
                    view.clearAnimation()
                }
            }
            return
        }
        // create the view using the delegate and add it to this layout
        headerView = delegate.invoke(layoutInflator)?.apply {
            addInternalView(this, Gravity.FILL_HORIZONTAL or Gravity.TOP)
            showing = true
            hiding = false

            // apply animation
            Animations.animate(context, this, R.anim.slide_in_from_top, View.VISIBLE, View.VISIBLE, object : DefaultAnimationListener() {
                override fun onAnimationEnd(animation: Animation?) {
                    showing = false
                }
            })
        }
    }


    fun hideHeader(animate: Boolean = true) {

        if (!animate) {
            headerView?.let { view ->
                view.animation?.let {
                    view.clearAnimation()
                }
                removeViewSafely(view)
                headerView = null
                hiding = false
            }
            return
        }



        if (hiding) {
            return
        }
        headerView?.let { view ->
            if (showing) {
                view.animation?.let {
                    view.clearAnimation()
                }
            }
            hiding = true
            showing = false
            Animations.animate(context, view, R.anim.slide_out_to_top, View.VISIBLE, View.VISIBLE, object : DefaultAnimationListener() {
                override fun onAnimationEnd(animation: Animation?) {
                    removeViewSafely(view)
                    headerView = null
                    hiding = false
                }
            })
        }
    }

    /**
     * Can't just call [removeView] after the animation end or while scrolling
     * Because this will trigger an [NullPointerException] durning [android.widget.FrameLayout.layoutChildren]
     * As the the underlying implementation of [RecyclerView] does not expect view tree to change
     * during scrolling. So we can simply avoid this by posting a message to queue the action at the
     * end of the message queue.
     */
    private fun removeViewSafely(view: View) {
        handler.post {
            removeView(view)
        }
    }


}