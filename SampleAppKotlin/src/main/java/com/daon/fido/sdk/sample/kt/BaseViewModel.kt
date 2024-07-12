package com.daon.fido.sdk.sample.kt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import javax.inject.Inject

open class BaseViewModel @Inject constructor(application: Application): AndroidViewModel(application){
    protected val context
        get() = getApplication<Application>()

    fun getResourceString(id : Int) = context.resources.getString(id)

}