package com.foo.flickerimg

import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


/**
 * A helper class that helps with managing multiple [Disposable] objects
 * by either adding it to [CompositeDisposable] or keeping one copy at a time (by id)
 *
 * Can [Disposable.dispose] all instances add to it by calling [dispose]
 *
 * Usually use it in an instance of an [Activity]
 */
class DisposableManager : Disposable {

    val TAG = this::class.simpleName

    private var composite = CompositeDisposable()
    private var mapped = mutableMapOf<String, Disposable>()
    private var disposed: Boolean = false


    override fun isDisposed(): Boolean {
        return disposed
    }


    /**
     * TODO we can create a map here
     */
    fun manage(id: String?, disposable: Disposable) {
        if (isDisposed) {
            Log.e(TAG, "Cannot manage disposable after disposed")
            return
        }
        id?.let {
            mapped[it] = disposable
        } ?: run {
            composite.add(disposable)
        }
    }

    override fun dispose() {
        composite.dispose()
        for ((_, disposable) in mapped) {
            if (!disposable.isDisposed) {
                disposable.dispose()
            }
        }
        mapped.clear()
        disposed = true
    }


}

