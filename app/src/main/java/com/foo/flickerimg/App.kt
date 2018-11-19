package com.foo.flickerimg

import android.app.Application
import android.content.Context
import android.os.Handler

class App : Application() {

    companion object {
        private lateinit var singleton: App
        val instance: App
            get() {
                return singleton
            }
    }

    override fun onCreate() {
        super.onCreate()
        singleton = this
        AppDelegate.setup()
    }
}


object AppDelegate {
    // global handler (for whoever needs it)
    val handler: Handler

    init {
        handler = Handler()
    }

    fun setup() {
        // maybe add some subscribers here?
    }
}