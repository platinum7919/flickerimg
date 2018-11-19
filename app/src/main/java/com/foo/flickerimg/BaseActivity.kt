package com.foo.flickerimg

import android.os.Message
import android.support.annotation.MainThread
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.reactivex.disposables.Disposable

/**
 * parent activity for all [Activity] defined in Manifest
 */
open class BaseActivity : AppCompatActivity() {
    val TAG = this::class.simpleName

    val MSG_HIDE_TOAST = 10
    val disposableManager: DisposableManager by lazy {
        DisposableManager()
    }
    var toast: Toast? = null
    val handler = android.os.Handler {
        onHandlerMessage(it)
    }

    protected fun onHandlerMessage(it: Message?): Boolean {
        return when (it?.what) {
            MSG_HIDE_TOAST -> {
                cancelToast()
                true
            }
            else -> false
        }
    }

    @MainThread
    fun cancelToast() {
        toast?.cancel()
        toast = null
    }

    @MainThread
    fun showToast(text: CharSequence?, durationMs: Long = 2500) {
        text ?: return
        if (!isMainThread()) {
            runOnUiThread {
                showToast(text, durationMs)
            }
            return
        }

        handler.removeMessages(MSG_HIDE_TOAST)
        cancelToast()

        Toast.makeText(this, text, Toast.LENGTH_LONG).apply {
            toast = this
        }.show()

        handler.sendMessageDelayed(Message().apply {
            what = MSG_HIDE_TOAST
        }, durationMs)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposableManager.dispose()
    }

    protected fun finalize() {
        disposableManager.dispose()
    }

    fun manage(id: String, disposable: Disposable) {
        disposableManager.manage(id, disposable)
    }


}