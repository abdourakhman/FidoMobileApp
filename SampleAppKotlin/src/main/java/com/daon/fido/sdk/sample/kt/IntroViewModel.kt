package com.daon.fido.sdk.sample.kt

import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daon.fido.client.sdk.Failure
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.client.sdk.Success
import com.daon.fido.client.sdk.core.ErrorFactory
import com.daon.fido.client.sdk.model.Authenticator
import com.daon.fido.sdk.sample.kt.model.SILENT_AUTH_AAID
import com.daon.sdk.authenticator.controller.CaptureControllerProtocol
import com.daon.sdk.crypto.log.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern
import javax.inject.Inject

//https://medium.com/androiddevelopers/viewmodel-one-off-event-antipatterns-16a1da869b95
data class FidoUiState(
    val inProgress: Boolean = false,
    val initializationResult: FidoInitializationResult? = null,
    val accountCreationResult: AccountCreationResult? = null,
    val authArrayAvailable: Boolean = false,
    val authArray: Array<Authenticator>,
    val authSelected: Boolean,
    val selectedAuth: Authenticator?,
    val loginResult: LoginResult? = null,
    val username: String? = null
)

data class AccountCreationResult(
    val success: Boolean,
    val message: String
)

data class LoginResult(
    val success: Boolean,
    val message: String,
    val code: Int
)

data class FidoInitializationResult(
    val success: Boolean,
    val message: String
)


@HiltViewModel
class IntroViewModel @Inject constructor(application: Application, private val fido: IXUAF, private val prefs: SharedPreferences) : BaseViewModel(application) {

    private val _uiState = MutableStateFlow(FidoUiState(inProgress = false, authArrayAvailable = false, authArray = emptyArray<Authenticator>(),
        authSelected = false, selectedAuth = null, username = null))
    val uiState: StateFlow<FidoUiState> = _uiState

    fun initFido() {
        fido.setLogging(true, LogUtils.LogLevel.VERBOSE, true)
        rotateLogs()
        if (fido.isInitialised()) {
            Log.d("DAON", "initFido already initialized")
        } else {
            _uiState.update { currentUiState ->
                currentUiState.copy(inProgress = true)
            }
            val parameters = Bundle()
            //parameters.putString("com.daon.sdk.log", "true")
            parameters.putString("com.daon.sdk.ignoreNativeClients", "true")
            parameters.putString("com.daon.sdk.ados.enabled", "true")
            //Enabling ADoS SRP Passcode
            parameters.putString("com.daon.sdk.passcode.ados.version", "2")
            fido.initialize(parameters)
                .addCompleteListener { code, warnings ->
                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            inProgress = false)
                    }
                    if (code == ErrorFactory.NO_ERROR_CODE) {
                        if (warnings.isNotEmpty()) {
                            warnings.forEach {
                                Log.d("DAON", "warning - ${it.code} : ${it.message}")
                            }
                        }
                        _uiState.update { currentUiState ->
                            currentUiState.copy(
                                initializationResult = FidoInitializationResult(
                                    success = true,
                                    message = "Fido init done"
                                )
                            )
                        }
                    } else {
                        _uiState.update { currentUiState ->
                            currentUiState.copy(
                                initializationResult = FidoInitializationResult(
                                    success = true,
                                    message = "Fido init failes with error code $code"
                                ))
                        }
                    }
                }
        }

        fido.addChooseAuthenticatorListener {
            _uiState.update { currentUiState ->
                currentUiState.copy(authArrayAvailable = true, authArray = it)
            }
        }
    }

    private fun isFileSizeGreaterThan5KB(): Boolean {
        val file = File(context.getExternalFilesDir(null), "daon-ixa.log")
        return if (file.exists()) {
            val fileSizeInBytes = file.length()
            val fileSizeInKB = fileSizeInBytes / 1024
            fileSizeInKB > 5
        } else {
            false
        }
    }

    private fun rotateLogs() {
        //rotate logs if file size is greater than 5KB
        if (isFileSizeGreaterThan5KB()) {
            val sdf = SimpleDateFormat("HH:mm:ss.SSS-dd-MM-yyyy", Locale.getDefault())
            val currentDate = sdf.format(Date())
            fido.rotate(context, "$currentDate-fido-ixa.log")
            deleteLogs()
        }
    }

    private fun deleteLogs() {
        //delete the oldest logs if more than 5 files are present
        val directory = context.getExternalFilesDir(null)
        val logFileNamePattern = Pattern.compile("\\d{2}:\\d{2}:\\d{2}\\.\\d{3}-\\d{2}-\\d{2}-\\d{4}-fido-ixa\\.log")
        val files = directory?.listFiles()
        val logFiles = files?.filter { logFileNamePattern.matcher(it.name).matches() }?.sortedBy { it.lastModified() }
        if (logFiles != null && logFiles.size > 5) {
            for (i in 0 until logFiles.size - 5) {
                fido.delete(context, logFiles[i].name)
            }
        }

    }

    fun createAccount() {
       //resetting fido before creating a new account
        resetFido()
    }

    private fun resetFido() {
        viewModelScope.launch {
            when (val response = fido.reset()) {
                is Success -> {
                    prefs.edit().putString("currentUser", null)
                    reinitializeFido()
                }

                is Failure -> {
                    prefs.edit().putString("currentUser", null)
                    reinitializeFido()
                }
            }
        }

    }

    private fun reinitializeFido() {
        viewModelScope.launch {
            val parameters = Bundle()
            parameters.putString("com.daon.sdk.log", "true")
            parameters.putString("com.daon.sdk.ignoreNativeClients", "true")
            parameters.putString("com.daon.sdk.ados.enabled", "true")
            //Enabling ADoS SRP Passcode
            parameters.putString("com.daon.sdk.passcode.ados.version", "2")
            fido.initialize(parameters)
                .addCompleteListener { code, warnings ->
                    if (code == ErrorFactory.NO_ERROR_CODE) {
                        createNewAccount()
                    } else {
                        _uiState.update { currentUiState ->
                            currentUiState.copy(
                                initializationResult = FidoInitializationResult(
                                    success = true,
                                    message = "Fido init failes with error code $code"
                                ))
                        }
                    }
                }
        }
    }

    fun startGps() {
        fido.startLocator(null)
        gpsTimeoutCountdown.cancel()
        gpsTimeoutCountdown.start()
    }

    val gpsTimeoutCountdown: CountDownTimer = object : CountDownTimer(60000, 60000) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {
            fido.stopLocator()
        }
    }

    private fun createNewAccount() {
        val usr = generateEmail()
        Log.d("DAON", "createNewAccount usr - $usr")
        _uiState.update { currentUiState ->
            currentUiState.copy(
                inProgress = true)
        }
        viewModelScope.launch {
            val params = Bundle()
            params.putString(IXUAF.FIRSTNAME, "first name")
            params.putString(IXUAF.LASTNAME, "last name")
            params.putString(IXUAF.PASSWORD, "pp")
            when (val response = fido.requestServiceAccess(usr, params)) {
                is Success -> {
                    val editor = prefs.edit()
                    editor.putString("currentUser", usr)
                    editor.apply()
                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            username = usr,
                            inProgress = false,
                            accountCreationResult = AccountCreationResult(
                                success = true,
                                message = "Account created successfully",
                            ))
                    }
                }

                is Failure -> {
                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            inProgress = false,
                            accountCreationResult = AccountCreationResult(
                                success = false,
                                message = "Account creation failed with " +
                                        "error code ${response.params.getInt(IXUAF.ERROR_CODE)} and " +
                                        "message ${response.params.getString(IXUAF.ERROR_MESSAGE)}",
                            ))
                    }
                }
            }
        }
    }

    private fun generateEmail(): String {
        val randomString = UUID.randomUUID().toString().substring(0, 15)
        Log.d("DAON", "generateRandomString :$randomString")
        return "$randomString@daon.com"
    }

    fun authenticate() {
        _uiState.update { currentUiState ->
            currentUiState.copy(inProgress = true)
        }
        viewModelScope.launch(Dispatchers.Default) {
            val bundle = Bundle()
            val username = prefs.getString("currentUser", null)
            if (username != null) {
                bundle.putString(IXUAF.USERNAME, username)
            }
            when (val response = fido.authenticate(bundle)) {
                is Success -> {
                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            inProgress = false,
                            username = response.params.getString(IXUAF.EMAIL) ?: prefs.getString("currentUser", null),
                            loginResult = LoginResult(
                                success = true,
                                message = "Authentication success",
                                code = 0,
                            ))
                    }
                }

                is Failure -> {
                    Log.d("DAON", "IntroScreen authenticate failure " +
                            "${response.params.getInt(IXUAF.ERROR_CODE)} " +
                            "${response.params.getString(IXUAF.ERROR_MESSAGE)} + bundle -${response.params.toString()}")
                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            inProgress = false,
                            loginResult = LoginResult(
                                success = false,
                                message = response.params.getString(IXUAF.ERROR_MESSAGE).toString(),
                                code = response.params.getInt(IXUAF.ERROR_CODE),
                            )
                        )
                    }
                }
            }
        }
    }


    fun resetUiState() {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                inProgress = false,
                accountCreationResult = null,
                loginResult = null)
        }
    }

    fun resetAccountCreationResult() {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                accountCreationResult = null)
        }
    }

    fun resetLoginResult() {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                loginResult = null)
        }
    }

    fun updateSelectedAuth(auth: Authenticator) {
        _uiState.update { currentAuthState ->
            currentAuthState.copy(selectedAuth = auth, authSelected = true, authArrayAvailable = false)
        }
        prefs.edit {
            this.putString("selectedAaid", auth.aaid)
        }
    }

    fun resetAuthArrayAvailable() {
        _uiState.update { currentAuthState ->
            currentAuthState.copy(authArrayAvailable = false)
        }
    }

    fun deselectAuth() {
        _uiState.update { currentUiState ->
            currentUiState.copy(authSelected = false)
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

}