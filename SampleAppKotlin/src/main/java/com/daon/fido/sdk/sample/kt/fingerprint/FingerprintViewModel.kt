package com.daon.fido.sdk.sample.kt.fingerprint

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.sdk.sample.kt.BaseViewModel
import com.daon.fido.sdk.sample.kt.R
import com.daon.fido.sdk.sample.kt.model.FINGERPRINT_AUTH_AAID
import com.daon.sdk.authenticator.controller.CaptureCompleteResult
import com.daon.sdk.authenticator.controller.FingerprintCaptureControllerProtocol
import com.daon.sdk.authenticator.exception.ControllerInitializationException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FingerprintViewModel @Inject constructor(
    application: Application,
    private val fido: IXUAF,
    private val prefs: SharedPreferences
): BaseViewModel(application){

    lateinit var fingerController: FingerprintCaptureControllerProtocol
    lateinit var localContext: Context

    private val _captureComplete = MutableStateFlow( false)
    val captureComplete  = _captureComplete.asStateFlow()

    private val _captureInfo = MutableStateFlow("")
    val captureInfo = _captureInfo.asStateFlow()

    fun onStart(context: Context) {
        try {
            fingerController = fido.getController(
                getApplication(),
                FINGERPRINT_AUTH_AAID
            ) as FingerprintCaptureControllerProtocol
            localContext = context
            val activity = localContext.findActivity()
            fingerController.startCapture(context.findActivity()) { result ->
                if (result != null) {
                    when (result.type) {
                        CaptureCompleteResult.Type.TERMINATE_SUCCESS -> {
                            Log.d("DAON", "finger capture complete")
                            _captureComplete.value = true
                        }

                        CaptureCompleteResult.Type.TERMINATE_FAILURE -> {
                            Log.d("DAON", "finger capture TERMINATE_FAILURE")
                            fingerController.cancelCapture()
                            _captureComplete.value = true
                        }

                        CaptureCompleteResult.Type.CLIENT_ERROR -> {
                            Log.d("DAON", "finger capture CLIENT_ERROR")
                            _captureComplete.value = true
                        }

                        CaptureCompleteResult.Type.CLIENT_VALIDATION_ERROR -> {
                            Log.d("DAON", "finger capture CLIENT_VALIDATION_ERROR")
                        }
                    }
                }
            }

            fido.addUserLockWarningListener {
                _captureInfo.value = getResourceString(R.string.user_lock_warning)
            }
        } catch (e: ControllerInitializationException) {
            Log.e("DAON", "Error starting fingerprint capture: ${e.message}")
            _captureInfo.value = e.message ?: ""
            _captureComplete.value = true
            fingerController.cancelCapture()
        }

    }

    fun onStop() {
        fingerController.stopCapture()
    }

    private fun Context.findActivity(): AppCompatActivity? = when (this) {
        is AppCompatActivity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    fun resetCaptureComplete() {
        _captureComplete.value = false
    }

    fun resetCaptureInfo() {
        _captureInfo.value = ""
    }
}