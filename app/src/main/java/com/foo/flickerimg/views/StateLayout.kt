package com.foo.flickerimg.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.foo.flickerimg.R
import com.foo.flickerimg.getStringRes


/**
 * A layout that can hide or overlay its child to
 * show a loading [ProgressBar] while its content loads.
 *
 * Other "state" can also be implemented such as a message with
 * a actionable button
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


    fun showError(throwable: Throwable, actionText: CharSequence = getStringRes(R.string.button_retry), onRetryClicked: View.OnClickListener) {
        showMessageAction(throwable.message ?: "???", actionText, onRetryClicked)
    }

    fun showMessageAction(message: CharSequence, actionText: CharSequence, onActionClicked: View.OnClickListener) {
        setChildVisibility(false, View.GONE)
        removeInternalViews()

        var view = layoutInflator.inflate(R.layout.layout_message_action, this, false).apply {
            addInternalView(this, Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL)
        }


        view.findViewById<TextView>(R.id.textview_message)?.let {
            it.setOptionalText(message)
        }

        view.findViewById<Button>(R.id.button_action1)?.let {
            it.text = actionText
            it.setOnClickListener(onActionClicked)
        }
        // apply some custom visual change
    }
}