package com.foo.flickerimg

import android.content.Context
import android.os.Looper
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.annotation.StringRes
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue

/**
 * Various extension functions to make things easier
 */
val TAG = "Global"

fun isMainThread(): Boolean {
    return Looper.getMainLooper() == Looper.myLooper()
}

fun runOnMain(delay: Long = 0, runnable: Runnable) {
    AppDelegate.handler.postDelayed(runnable, delay)
}

fun getContext(): Context {
    return App.instance
}

fun getStringRes(@StringRes stringRes: Int, vararg args: Any) {
    getContext().resources.getString(stringRes, *args)
}


/**
 * Resource id to pixel
 */
@ColorInt
fun Context.resToColor(@ColorRes colorRes: Int): Int {
    return resources.getColor(colorRes)
}


/**
 * Resource id to pixel
 */
fun Context.resToPx(@DimenRes dimenRes: Int): Int {
    val r = this.getResources()
    return r.getDimensionPixelSize(dimenRes)
}

/**
 * Pixel to dp
 */
fun Context.pxToDp(px: Int): Float {
    val r = this.resources
    val metrics = r.displayMetrics
    return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

/**
 * dp to pixel
 */
fun Context.dpToPx(dp: Int): Int {
    return dpToPx(dp.toFloat()).toInt()
}

/**
 * dp(float) to pixel
 */
fun Context.dpToPx(dp: Float): Float {
    val r = this.getResources()
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics())
}


fun Any.castToString(): String? {
    return try {
        Flickr.gson.toJson(this)
    } catch (t: Throwable) {
        Log.w(TAG, t.message, t)
        null
    }
}

inline fun <reified K> String.castToObject(): K? {
    return try {
        Flickr.gson.fromJson(this, K::class.java)
    } catch (t: Throwable) {
        Log.w(TAG, t.message, t)
        null
    }
}
