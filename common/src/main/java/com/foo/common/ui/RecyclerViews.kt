package com.foo.common.ui

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

fun <A : RecyclerView.Adapter<*>> RecyclerView.getAdapterOfType(): A? {
    (adapter as? A)?.let {
        return it
    }
    return null
}


/**
 * RecyclerView.VERTICAL or  RecyclerView.HORIZONTAL
 */
fun RecyclerView.setup(orientation: Int = RecyclerView.VERTICAL, reverseLayout: Boolean = false): RecyclerView.LayoutManager {
    return when (orientation) {
        RecyclerView.VERTICAL -> {
            LinearLayoutManager(context, RecyclerView.VERTICAL, reverseLayout)
        }

        RecyclerView.HORIZONTAL -> {
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, reverseLayout)
        }

        else -> {
            throw IllegalArgumentException("Unsupported orientation ${orientation}")
        }
    }.apply {
        this@setup.layoutManager = this
    }
}

