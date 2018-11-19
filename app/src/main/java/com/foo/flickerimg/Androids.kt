package com.foo.flickerimg

import android.content.Context
import android.os.Looper
import android.support.annotation.StringRes
import android.util.Log

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
