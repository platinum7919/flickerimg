package com.foo.flickerimg.views

import android.support.annotation.IdRes
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.foo.common.utils.isNullOrEmptyOrBlank

/**
 * Remove a child from parent if there is a tag with a certain value in the child
 */
fun ViewGroup.removeChildWithTag(@IdRes tagId: Int, tagValue: Any) {
    var childern = mutableListOf<View>()
    if (this.childCount == 0)
        return
    for (i in 0 until this.childCount) {
        childern.add(this.getChildAt(i))
    }
    childern.forEach { view ->
        if (view.getTag(tagId)?.equals(tagValue) == true) {
            this.removeView(view)
        }
    }
}


/**
 * Get a tag value from a view with a specific type
 */
fun <O : Any> View.findTag(@IdRes id: Int): O? {
    this.getTag(id)?.let {
        return it as? O
    }
    return null;
}


/**
 * @param id
 * @param defaultValue
 * @param <V>
 * @return
</V> */
fun <V> View.getTag(id: Int, defaultValue: V): V {
    return (getTag(id) as? V) ?: defaultValue
}


fun TextView.setOptionalText(text: CharSequence?, visibilityIfEmpty: Int = View.GONE) {
    if (text.isNullOrEmptyOrBlank()) {
        this.visibility = visibilityIfEmpty
        this.text = ""
    } else {
        this.visibility = View.VISIBLE
        this.text = text ?: ""
    }
}


fun TextView.getOptionalText(): CharSequence? {
    return if (text.isNullOrEmptyOrBlank()) {
        null
    } else {
        text
    }
}
