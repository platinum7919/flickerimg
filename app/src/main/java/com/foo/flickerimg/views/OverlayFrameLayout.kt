package com.foo.flickerimg.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.foo.flickerimg.R

/**
 * A [FrameLayout] extension that helps with adding / removing
 * [View] created during runtime. These views be marked with
 * [R.id.internal_child_view] and can be remove by [removeInternalViews]
 *
 * Can hide or show the original childern (defined in XML layout) using
 * [setChildVisibility]
 */
open class OverlayFrameLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var customLayoutInflater: LayoutInflater? = null

    val layoutInflator: LayoutInflater
        get() {
            return customLayoutInflater ?: LayoutInflater.from(context)
        }


    fun showNormal() {
        setChildVisibility(false, View.VISIBLE)
        removeInternalViews()
    }


    protected fun markViewAsInternal(view: View) {
        view.setTag(R.id.internal_child_view, true)
    }


    protected fun removeInternalViews() {
        removeChildWithTag(R.id.internal_child_view, true)
    }


    protected fun setChildVisibility(internalChildView: Boolean, visibility: Int) {
        this.getChildren().filter {
            it.getTag() == if (internalChildView) true else null
        }.forEach {
            it.visibility = visibility
        }
    }


    protected fun addInternalView(view: View, gravity: Int) {
        markViewAsInternal(view)
        addView(view)
        (view.layoutParams as? FrameLayout.LayoutParams)?.let {
            it.gravity = gravity
            view.layoutParams = it
        }
    }

}