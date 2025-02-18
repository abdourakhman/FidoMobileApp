package com.daon.fido.sdk.sample.kt.passcode

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.sdk.sample.kt.BaseViewModel
import com.daon.fido.sdk.sample.kt.R
import com.daon.sdk.authenticator.Authenticator
import com.daon.sdk.authenticator.controller.CaptureCompleteListener
import com.daon.sdk.authenticator.controller.CaptureCompleteResult
import com.daon.sdk.authenticator.controller.ControllerConfiguration
import com.daon.sdk.authenticator.controller.PasscodeControllerProtocol
import com.daon.sdk.authenticator.exception.ControllerInitializationException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class PasscodeViewModel @Inject constructor(application: Application, private val fido: IXUAF, private val prefs: SharedPreferences): BaseViewModel(application) {
    lateinit var controller: PasscodeControllerProtocol

    private val _captureComplete = MutableStateFlow(false)
    val captureComplete = _captureComplete.asStateFlow()

    private val _captureCompleteWithError = MutableStateFlow(false)
    val captureCompleteWithError = _captureCompleteWithError.asStateFlow()

    private val _captureInfo = MutableStateFlow("")
    val captureInfo = _captureInfo.asStateFlow()

    private val _enableRetry = MutableStateFlow(false)
    val enableRetry = _enableRetry.asStateFlow()

    private val _isEnrol = MutableStateFlow(false)
    val isEnrol = _isEnrol.asStateFlow()

    private val _isVerifyAndEnroll = MutableStateFlow(false)
    val isVerifyAndEnroll = _isVerifyAndEnroll.asStateFlow()

    private val _controllerInstantiated = MutableStateFlow(false)
    val controllerInstantiated = _controllerInstantiated.asStateFlow()

    private val _onControllerInstantiateError = MutableStateFlow(false)
    val onControllerInstantiateError = _onControllerInstantiateError.asStateFlow()

    fun intializeController() {
        val aaidValue = prefs.getString("selectedAaid", null)
        if (aaidValue != null) {
            //Handling the ControllerInitializationException here - for example. if the authenticator is locked
            try {
                controller = fido.getController(getApplication(), aaidValue) as PasscodeControllerProtocol
                _controllerInstantiated.value = true
                _isEnrol.value = controller.isEnrol
                if (controller.authenticationMode == ControllerConfiguration.AuthenticationMode.VERIFY_AND_REENROL) {
                    _isVerifyAndEnroll.value = true
                }

            }catch (e: ControllerInitializationException) {
                Log.d("DAON", "PasscodeViewModel getControllerMode exception: ${e.message}")
                _captureInfo.value = "Error: ${e.message}"
                _onControllerInstantiateError.value = true
                cancelCurrentOperation()
            }
        }


        fido.addUserLockWarningListener{
            _captureInfo.value = getResourceString(R.string.user_lock_warning)
        }

    }

    // https://fvilarino.medium.com/handling-lifecycle-events-on-jetpack-compose-f4f53de41f0a
    fun onStart() {
        controller.startCapture()
    }

    fun cancelCurrentOperation() {
        viewModelScope.launch(Dispatchers.Default) {
            fido.cancelCurrentOperation()
        }
    }

    fun onStop() {

    }

    fun register(value: String) {
        try {
            var error =
                controller.registerPasscode(value.toCharArray(), DefaultCaptureCompleteListener())
            Log.d("DAON", "register error: $error")
            if (error != null) {
                _captureInfo.value = error.message
                _enableRetry.value = true
            }
        }catch (e: Exception) {
            Log.d("DAON", "register exception: ${e.message}")
            _captureInfo.value = "Error: ${e.message}"
            _captureCompleteWithError.value = true
        }
    }

    fun authenticate(value: String) {
        controller.authenticatePasscode(value.toCharArray(), DefaultCaptureCompleteListener())

    }

    fun verifyAndReenrol(currentPasscode: String, newPasscode: String) {
        controller.verifyAndReenrolPasscode(currentPasscode.toCharArray(), newPasscode.toCharArray(), DefaultCaptureCompleteListener())
    }

    fun resetCaptureComplete() {
        _captureComplete.value = false
    }

    fun resetCaptureCompleteWithError() {
        _captureCompleteWithError.value = false
    }

    fun resetCaptureInfo() {
        _captureInfo.value = ""
    }

    fun resetEnableRetry() {
        _enableRetry.value = false
    }

    fun resetControllerInstantiatedError() {
        _onControllerInstantiateError.value = false
    }

    private fun getRetriesRemainingMessage(result: CaptureCompleteResult): String {
        var retryMessage = ""
        val numberOfRetries = result.info.getInt(CaptureCompleteResult.InfoKey.RETRIES_REMAINING, -1)
        if (numberOfRetries > 0) {
            if (numberOfRetries ==1) {
                retryMessage = "1 retry remaining"
            } else {
                retryMessage = "$numberOfRetries retries remaining"
            }
        } else {
            retryMessage = "Please try again later"
        }
        return retryMessage
    }


    inner class DefaultCaptureCompleteListener: CaptureCompleteListener {
        override fun onCaptureComplete(result: CaptureCompleteResult) {
            Log.d("DAON", "PasscodeViewModel CaptureCompleteListener onCaptureComplete ${result.type}")
            if (controller != null) {
                when (result.type) {
                    CaptureCompleteResult.Type.TERMINATE_SUCCESS -> {
                        _captureComplete.value = true
                    }
                    CaptureCompleteResult.Type.TERMINATE_FAILURE -> {
                        controller.cancelCapture()
                        _captureCompleteWithError.value = true
                    }
                    CaptureCompleteResult.Type.SERVER_VALIDATION_ERROR -> {
                        _enableRetry.value = true
                        val errorMessage = result.error.message

                        if (errorMessage != null) {
                            _captureInfo.value = errorMessage + "\n" + getRetriesRemainingMessage(result)
                        } else {
                            _captureInfo.value = getRetriesRemainingMessage(result)
                        }
                    }
                    CaptureCompleteResult.Type.CLIENT_VALIDATION_ERROR -> {
                       if (result.lockInfo.state == Authenticator.Lock.UNLOCKED) {
                           _enableRetry.value = true
                           if (result.info.getBoolean(CaptureCompleteResult.InfoKey.IS_WARN_ATTEMPT, false)) {
                               _captureInfo.value = "Detecting multiple failed matches, please ensure you are entering the correct PIN and try again."
                           } else {
                               _captureInfo.value = getRetriesRemainingMessage(result)
                           }
                       } else {
                           _captureCompleteWithError.value = true
                           Log.d("DAON", "CLIENT_VALIDATION_ERROR ${captureCompleteWithError.value}")
                            if (result.lockInfo.state == Authenticator.Lock.TEMPORARY) {
                                _captureInfo.value = "Too many authentication attempts. The authenticator is locked for ${result.lockInfo.seconds} seconds. Please try later."
                            } else if (result.lockInfo.state == Authenticator.Lock.PERMANENT) {
                                _captureInfo.value = "Too many authentication attempts. The authenticator is locked."
                            }
                       }
                    }
                    CaptureCompleteResult.Type.CLIENT_ERROR -> {
                        _captureCompleteWithError.value = true
                        if (controller.isRegistration) {
                            _captureInfo.value = "Passcode registration failed. "
                        } else {
                            _captureInfo.value = "Passcode authentication failed."
                        }
                    }


                }
            }
        }
    }
}