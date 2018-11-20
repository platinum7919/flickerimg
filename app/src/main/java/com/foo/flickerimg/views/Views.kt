package com.foo.flickerimg.views

import android.support.annotation.IdRes
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.foo.common.utils.isNullOrEmptyOrBlank

/**
 * Remove all children from parent if there is a tag with a certain value in the child
 */
fun ViewGroup.removeChildWithTag(@IdRes tagId: Int, tagValue: Any): Int {
    var count = 0
    findChildrenWithTag(tagId, tagValue).forEach {
        this.removeView(it)
        count++
    }
    return count
}

fun ViewGroup.findChildrenWithTag(@IdRes tagId: Int, tagValue: Any): List<View> {
    return getChildren().filter { view ->
        view.getTag(tagId)?.equals(tagValue) == true
    }
}


fun ViewGroup.getChildren(): List<View> {
    return mutableListOf<View>().apply {
        for (i in 0 until this@getChildren.childCount) {
            add(this@getChildren.getChildAt(i))
        }
    }
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
