package com.foo.flickerimg.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.foo.flickerimg.R


/**
 * A layout that helps show a loading [ProgressBar] while its content loads
 */
class StateLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : OverlayFrameLayout(context, attrs, defStyleAttr) {


    fun showLoading(message: CharSequence? = context.getString(R.string.loading)) {
        setChildVisibility(false, View.GONE)
        removeInternalViews()

        var view = layoutInflator.inflate(R.layout.layout_loading, this, false).apply {
            addInternalView(this, Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL)
        }


        view.findViewById<TextView>(R.id.textview_message)?.let {
            it.setOptionalText(message)
        }
        // apply some custom visual change
    }


}