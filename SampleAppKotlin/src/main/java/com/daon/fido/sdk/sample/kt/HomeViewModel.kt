package com.daon.fido.sdk.sample.kt

import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.daon.fido.client.sdk.ConfirmationOTPListener
import com.daon.fido.client.sdk.Failure
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.client.sdk.Success
import com.daon.fido.client.sdk.model.Authenticator
import com.daon.fido.client.sdk.transaction.TransactionContent
import com.daon.fido.client.sdk.transaction.TransactionUtils
import com.daon.fido.sdk.sample.kt.model.SILENT_AUTH_AAID
import com.daon.sdk.authenticator.controller.CaptureControllerProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionState(
    val authArrayAvailable: Boolean = false,
    val authArray: Array<Authenticator>,
    val authSelected: Boolean,
    val selectedAuth: Authenticator?,
    val authenticationCompleted: Boolean,
    val resetComplete: Boolean,
    val message: String?,
    val confirmTransaction: Boolean = false,
    val transactionContent: TransactionContent? = null,
    val transactionConfirmationResult:Int = 99,
    val confirmationOTPReceived: Boolean = false,
    val confirmationOTP: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(application: Application, private val fido: IXUAF, private val prefs: SharedPreferences): BaseViewModel(application) {

    private val _transactionState = MutableStateFlow(
        TransactionState(
            authArrayAvailable = false,
            authArray = emptyArray<Authenticator>(),
            authSelected = false,
            selectedAuth = null,
            authenticationCompleted = false,
            resetComplete = false,
            message = null,
            confirmTransaction = false,
            transactionContent = null,
            transactionConfirmationResult = 99,
            confirmationOTPReceived = false,
            confirmationOTP = null
        )
    )
    val transactionState: StateFlow<TransactionState> = _transactionState

        protected var _inProgress = MutableStateFlow(false)
        val inProgress = _inProgress.asStateFlow()

    fun onStart() {
        fido.addChooseAuthenticatorListener {
            // Show the authList to user
            _transactionState.update { currentTransactionState ->
                currentTransactionState.copy(authArrayAvailable = true, authArray = it)

            }
        }

        fido.addDisplayTransactionListener {
            _transactionState.update { currentTransactionState ->
                val txnContent = TransactionUtils.getTransactionContent(context, it)
                currentTransactionState.copy(confirmTransaction = true, transactionContent = txnContent)
            }
        }

        fido.addConfirmationOTPListener { confirmationOTP ->
            Log.d("DAON", "Confirmation OTP: $confirmationOTP")
            _transactionState.update { currentTransactionState ->
                currentTransactionState.copy(
                    confirmationOTPReceived = true,
                    confirmationOTP = confirmationOTP
                )
            }
        }
    }

    fun onStop() {

    }


    fun authenticate() {
        _inProgress.value = true
        viewModelScope.launch(Dispatchers.Default) {
            val bundle = Bundle()
            val username = prefs.getString("currentUser", null)
            if (username != null) {
                bundle.putString(IXUAF.USERNAME, username)
            }
            //Image content
            //bundle.putString(IXUAF.TRANSACTION_CONTENT_TYPE, "image/png")
            //bundle.putString(IXUAF.TRANSACTION_CONTENT_DATA, getResourceString(R.string.transaction_image_content))
            //Text content
            bundle.putString(IXUAF.TRANSACTION_CONTENT_TYPE, "text/plain")
            bundle.putString(IXUAF.TRANSACTION_CONTENT_DATA, getResourceString(R.string.transaction_text_content))
            bundle.putBoolean(IXUAF.CONFIRMATION_OTP, true)
            when (val response = fido.authenticate(bundle)) {
                is Success -> {
                    Log.d("DAON", "Successfully authenticated !!!")
                    _inProgress.value = false
                    _transactionState.update { currentTransactionState ->
                        currentTransactionState.copy(authenticationCompleted = true, message = getResourceString(R.string.transaction_validation_success))
                    }
                }

                is Failure -> {
                    Log.d("DAON", "Authentication Failure !!!")
                    _inProgress.value = false
                    _transactionState.update { currentTransactionState ->
                        currentTransactionState.copy(authenticationCompleted = true, message = response.params.getString(IXUAF.ERROR_MESSAGE))
                    }
                }
            }
        }
    }

    fun resetFido() {
        deleteUser()
    }

    private fun reset() {
        viewModelScope.launch {
            when (fido.reset()) {
                is Success -> {
                    Log.d("DAON", "Reset Fido Success")
                    _transactionState.update { currentTransactionState ->
                        currentTransactionState.copy(resetComplete = true)
                    }
                }

                is Failure -> {

                }
            }
        }
    }

    private fun deleteUser() {
        val bundle = Bundle()
        val username = prefs.getString("currentUser", null)
        viewModelScope.launch {
            when (fido.deleteUser(username, Bundle())) {
                is Success -> {
                    Log.d("DAON", "Delete User Success")
                    reset()
                }

                is Failure -> {
                    Log.d("DAON", "Delete User Failure")
                }
            }
        }
    }

    fun doLogout() {
        viewModelScope.launch {
            when (fido.revokeServiceAccess(Bundle())) {
                is Success -> {
                    Log.d("DAON", "Logout Success")
                }

                is Failure -> {
                    Log.d("DAON", "Logout Failure")
                }
            }
        }
    }

    fun resetResetComplete() {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(resetComplete = false)
        }
    }

    fun resetAuthenticationCompleted() {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(authenticationCompleted = false)
        }
    }

    fun deselectAuth() {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(authSelected = false)
        }
    }

    fun updateSelectedAuth(auth: Authenticator) {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(selectedAuth = auth, authSelected = true, authArrayAvailable = false)
        }
        prefs.edit {
            this.putString("selectedAaid", auth.aaid)
        }
    }

    fun resetAuthArrayAvailable() {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(authArrayAvailable = false)
        }
    }

    fun authenticateSilent() {
        val silentController = fido.getController(getApplication(), SILENT_AUTH_AAID) as CaptureControllerProtocol
        silentController.startCapture()
        silentController.completeCapture()
    }

    fun cancelCurrentOperation() {
        viewModelScope.launch(Dispatchers.Default) {
            fido.cancelCurrentOperation()
        }
    }

    fun updateTransactionConfirmationResult(result: Int) {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(transactionConfirmationResult = result)
        }
    }

    fun submitDisplayTransactionResult(result: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            resetTransactionConfirmationResult()
            fido.submitDisplayTransactionResult(result, " ")
        }
    }

    fun resetTransactionData() {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(confirmTransaction = false, transactionContent = null)
        }
    }

    fun resetTransactionConfirmationResult() {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(transactionConfirmationResult = 99)
        }
    }

    fun resetConfirmationOTP() {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(confirmationOTPReceived = false, confirmationOTP = null)
        }
    }

}