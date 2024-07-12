package com.daon.fido.sdk.sample

import android.util.Log
import androidx.lifecycle.*
import androidx.multidex.MultiDexApplication

class SampleApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        val appLifeCycleObserver = AppLifeCycleObserver()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifeCycleObserver)
    }
}

class AppLifeCycleObserver: DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        Log.d("DAON", "App Foreground")
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d("DAON", "App Background")
        //Logout when app going to background .
//        CoreApplication.doLogout(false)
    }
}