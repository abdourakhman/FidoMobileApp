package com.daon.fido.sdk.sample.kt

import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.daon.fido.client.sdk.Failure
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.client.sdk.IXUAF.Companion.KEY_APP_ID
import com.daon.fido.client.sdk.Success
import com.daon.fido.client.sdk.model.Authenticator
import com.daon.fido.client.sdk.util.SDKPreferences
import com.daon.fido.sdk.sample.kt.model.ADOS_VOICE_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.OOTP_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.PATTERN_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.SILENT_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.VOICE_AUTH_AAID
import com.daon.sdk.authenticator.controller.CaptureControllerProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class AuthenticatorState(
    val authArray: Array<Authenticator>,
    val authListAvailable: Boolean,
    val authSelected: Boolean,
    val selectedAuth: Authenticator?,
    val authToDeregister: Authenticator?,
    val selectedIndex: Int,
    val registrationResult: RegistrationResult?,
    val deregistrationResult: DeregistrationResult?
)

data class RegistrationResult(
    val success: Boolean,
    val message: String
)

data class DeregistrationResult(
    val success: Boolean,
    val message: String
)
@HiltViewModel
class AuthenticatorsViewModel @Inject constructor(
    application: Application, private val fido: IXUAF,
    private val prefs: SharedPreferences
) : BaseViewModel(application) {

    //addChooseAuthenticatorListener auth list
    private val _authState = MutableStateFlow(
        AuthenticatorState(
            authArray = emptyArray<Authenticator>(),
            authListAvailable = false, authSelected = false, selectedAuth = null,
            authToDeregister= null,
            selectedIndex = -1,
            registrationResult = null,
            deregistrationResult = null
        )
    )
    val authState: StateFlow<AuthenticatorState> = _authState

    //discover auth list
    private val _discoverList = mutableStateListOf<Authenticator>()
    val discoverList: List<Authenticator> = _discoverList

    fun onStart() {

        fido.addChooseAuthenticatorListener {
           filterAuthArray(it)
        }
        discover()

    }

    private fun filterAuthArray(authArray: Array<Authenticator>) {
        val resultArray = mutableStateListOf<Authenticator>()
        for (auth in authArray) {
            if (auth.aaid != VOICE_AUTH_AAID
                && auth.aaid != ADOS_VOICE_AUTH_AAID
                && auth.aaid != OOTP_AUTH_AAID
                && auth.aaid != PATTERN_AUTH_AAID) {
                resultArray.add(auth)
            }
        }
        Log.d("DAON", "filterAuthArray resultArray size : ${resultArray.size}")

        if (resultArray.size == 0) {
            _authState.update { currentAuthState ->
                currentAuthState.copy(
                    registrationResult = RegistrationResult(
                        success = false,
                        message = "No more authenticators available to register"
                    )
                )
            }
            cancelCurrentOperation()
        } else {
            _authState.update { currentAuthState ->
                currentAuthState.copy(
                    authArray = resultArray.toTypedArray(),
                    authListAvailable = true
                )
            }
        }
    }

    fun onStop() {

    }

    private fun discover(){
        val username = prefs.getString("currentUser", null).toString()
        val appId = SDKPreferences.instance().getString(context, IXUAF.KEY_APP_ID)
        val result = fido.discover()
        result.onSuccess { discover ->
            val resultArray = mutableStateListOf<Authenticator>()
            discover.availableAuthenticators.forEach { auth ->
                val res = fido.isRegistered(auth.aaid, username, appId)
                if (res.isSuccess && res.getOrNull() == true) {
                    resultArray.add(auth)
                }
            }
            _discoverList.clear()
            _discoverList.addAll(resultArray)

        }.onFailure {
            //Handle failure here
        }
    }
    fun register() {
        viewModelScope.launch(Dispatchers.Default) {
            val bundle = Bundle()
            val username = prefs.getString("currentUser", null)
            if (username != null) {
                bundle.putString(IXUAF.USERNAME, username)
            }

            when (val response = fido.register(bundle)) {
                is Success -> {
                    prefs.edit {
                        this.remove("selectedAaid")
                    }
                    //discover and update the list
                    Log.d("DAON", "fido register success")
                    _authState.update {
                        currentAuthState ->
                        currentAuthState.copy(
                            registrationResult = RegistrationResult(
                                success = true,
                                message = "Registration success"
                            )
                        )
                    }
                    discover()

                }

                is Failure -> {
                    prefs.edit {
                        this.remove("selectedAaid")
                    }
                    _authState.update { currentAuthState ->
                        currentAuthState.copy(
                            registrationResult = RegistrationResult(
                                success = false,
                                message = "Registration failed with error code " +
                                        "${response.params.getInt(IXUAF.ERROR_CODE)} and message ${response.params.getString(IXUAF.ERROR_MESSAGE)}"
                            )
                        )
                    }
                }
            }

        }
    }

    fun cancelCurrentOperation() {
        viewModelScope.launch(Dispatchers.Default) {
            fido.cancelCurrentOperation()
        }
    }

    fun remove(auth: Authenticator) {
        viewModelScope.launch(Dispatchers.Default) {
            val username = prefs.getString("currentUser", null).toString()

            when (val response = fido.remove(auth.aaid, username)) {
                is Success -> {
                    updateAuthToDeregister(null, -1)
                    Log.d("DAON", "fido remove success")
                    _authState.update {
                            currentAuthState ->
                        currentAuthState.copy(
                            deregistrationResult = DeregistrationResult(
                                success = true,
                                message = "Remove authenticator ${auth.aaid} success"
                            )
                        )
                    }
                    discover()
                }

                is Failure -> {
                    Log.d("DAON", "fido remove failure")
                    updateAuthToDeregister(null, -1)
                    _authState.update { currentAuthState ->
                        currentAuthState.copy(
                            deregistrationResult = DeregistrationResult(
                                success = false,
                                message = "Remove authenticator failed with error code " +
                                        "${response.params.getInt(IXUAF.ERROR_CODE)} and " +
                                        "message ${response.params.getString(IXUAF.ERROR_MESSAGE)}"
                            )
                        )
                    }
                }
            }
        }
    }

    fun deregister(auth: Authenticator) {
        viewModelScope.launch(Dispatchers.Default) {
            val username = prefs.getString("currentUser", null).toString()

            when (val response = fido.deregister(auth.aaid, username)) {
                is Success -> {
                    Log.d("DAON", "fido deregister success")
                    updateAuthToDeregister(null, -1)
                    _authState.update {
                        currentAuthState ->
                        currentAuthState.copy(
                            deregistrationResult = DeregistrationResult(
                                success = true,
                                message = "Deregistration success"
                            )
                        )
                    }
                    discover()
                }

                is Failure -> {
                    updateAuthToDeregister(null, -1)
                    Log.d("DAON", "fido deregister failure")
                    _authState.update { currentAuthState ->
                        currentAuthState.copy(
                            deregistrationResult = DeregistrationResult(
                                success = false,
                                message = "Deregistration failed with error code " +
                                        "${response.params.getInt(IXUAF.ERROR_CODE)} and " +
                                        "message ${response.params.getString(IXUAF.ERROR_MESSAGE)}"
                            )
                        )
                    }
                }
            }
        }
    }

    fun updateSelectedAuth(auth: Authenticator) {
        _authState.update { currentAuthState ->
            currentAuthState.copy(selectedAuth = auth, authSelected = true, authListAvailable = false)
        }
        prefs.edit {
            this.putString("selectedAaid", auth.aaid)
        }
    }

    fun resetAuthListAvailable() {
        _authState.update { currentAuthState ->
            currentAuthState.copy(authListAvailable = false)
        }
    }

    fun resetRegistrationResult() {
        _authState.update { currentAuthState ->
            currentAuthState.copy(registrationResult = null)
        }
    }

    fun resetDeregistrationResult() {
        _authState.update { currentAuthState ->
            currentAuthState.copy(deregistrationResult = null)
        }
    }

    fun updateAuthToDeregister(auth: Authenticator?, index: Int) {
        _authState.update { currentAuthState ->
            currentAuthState.copy(authToDeregister = auth, selectedIndex = index)
        }
    }

    fun deselectAuth() {
        _authState.update { currentAuthState ->
            currentAuthState.copy(authSelected = false)
        }
    }

    fun registerSilent() {
        val silentController = fido.getController(getApplication(), SILENT_AUTH_AAID) as CaptureControllerProtocol
        silentController.startCapture()
        silentController.completeCapture()
    }
}