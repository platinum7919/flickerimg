package com.foo.flickerimg.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.foo.flickerimg.R


class StateLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var customLayoutInflater: LayoutInflater? = null

    val layoutInflator: LayoutInflater
        get() {
            return customLayoutInflater ?: LayoutInflater.from(context)
        }

    init {
        this.foregroundGravity = Gravity.CENTER
    }


    fun showLoading(message: CharSequence? = context.getString(R.string.loading)) {
        setOriginalChildsVisibility(View.GONE)
        removeCustomViews()

        var view = layoutInflator.inflate(R.layout.layout_loading, this, false).apply {
            applyTag(this)
            addViewAtCenter(this)
        }


        view.findViewById<TextView>(R.id.textview_message)?.let {
            it.setOptionalText(message)
        }
        // apply some custom visual change
    }


    fun showNormal() {
        setOriginalChildsVisibility(View.VISIBLE)
        removeCustomViews()
    }


    private fun applyTag(view: View) {
        view.setTag(R.id.internal_child_view, true)
    }


    private fun removeCustomViews() {
        removeChildWithTag(R.id.internal_child_view, true)
    }

    private fun setOriginalChildsVisibility(visibility: Int) {
        setChildVisibility(false, visibility)
    }


    private fun setChildVisibility(internalChildView: Boolean, visibility: Int) {
        for (i in 0 until this.childCount) {
            this.getChildAt(i)?.let { v ->
                val internal = v.findTag<Boolean>(R.id.internal_child_view)
                if ((internalChildView && internal == true)
                        || (!internalChildView && internal != true)) {
                    v.visibility = visibility
                }
            }
        }
    }


    fun addViewAtCenter(view: View) {
        addView(view)
        (view.layoutParams as? FrameLayout.LayoutParams)?.let {
            it.gravity = Gravity.FILL
            view.layoutParams = it
        }
    }


}