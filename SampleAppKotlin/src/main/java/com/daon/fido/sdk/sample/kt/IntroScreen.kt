package com.daon.fido.sdk.sample.kt

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import com.daon.fido.sdk.sample.kt.model.*
import com.daon.fido.sdk.sample.kt.ui.theme.ButtonColor
import com.daon.fido.sdk.sample.kt.util.CircularIndeterminateProgressBar
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun IntroScreen(
    onNavigateToHome: (username: String) -> Unit,
    onNavigateToChooseAuth: (() -> Unit, ViewModel) -> Unit,
    onNavigateToPasscode: () -> Unit,
    onNavigateToFace: () -> Unit,
    onNavigateToFingerprint: () -> Unit,
    onNavigateUp: () -> Unit
) {
    //https://developer.android.com/jetpack/compose/libraries#hilte
    val viewModel = hiltViewModel<IntroViewModel>()
    var inProgress by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionStates = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.ACCESS_WIFI_STATE
        )
    )


    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_START) {
                viewModel.initFido()
                if (!permissionStates.allPermissionsGranted) {
                    permissionStates.launchMultiplePermissionRequest()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })

    LaunchedEffect(key1 = permissionStates.allPermissionsGranted) {
        if (permissionStates.allPermissionsGranted) {
            viewModel.startGps()
        }
    }


    LaunchedEffect(key1 = viewModel.uiState) {
        viewModel.uiState.collect { uiState ->

            uiState.accountCreationResult?.let { accountCreationResult ->
                if (accountCreationResult.success) {
                    Log.d("DAON", "accountCreationResult success")
                    Toast.makeText(
                        context, "Account created successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    onNavigateToHome(uiState.username.toString())
                } else {
                    Toast.makeText(
                        context, "Account creation failed ${accountCreationResult.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                viewModel.resetAccountCreationResult()

            }

            if (uiState.authArrayAvailable) {
                onNavigateToChooseAuth(onNavigateUp, viewModel)
            }

            if (uiState.authSelected) {
                viewModel.deselectAuth()
                val aaid = uiState.selectedAuth?.aaid
                when (uiState.selectedAuth?.aaid) {

                    SRP_PASSCODE_AUTH_AAID, PASSCODE_AUTH_AAID -> {
                        if (aaid != null) {
                            onNavigateToPasscode()
                        }
                    }
                    FINGERPRINT_AUTH_AAID -> {
                        onNavigateToFingerprint()
                    }
                    FACE_AUTH_AAID, ADOS_FACE_AUTH_AAID -> {
                        if (aaid != null) {
                            onNavigateToFace()
                        }
                    }
                    SILENT_AUTH_AAID -> {
                        viewModel.authenticateSilent()
                    }
                }
            }

            uiState.loginResult?.let { loginResult ->
                if (loginResult.success) {
                    Toast.makeText(
                        context, "Authentication success",
                        Toast.LENGTH_SHORT
                    ).show()
                    onNavigateToHome(uiState.username.toString())
                } else {
                    Toast.makeText(
                        context,
                        "Authentication failed ${loginResult.code} :  ${loginResult.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                viewModel.resetLoginResult()
            }

            inProgress = uiState.inProgress
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetUiState()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "IdentityX FIDO Sample",
            fontSize = 20.sp,
            color = ButtonColor
        )
        Spacer(modifier = Modifier.height(height = 50.dp))
        LoginButton(isDisplayed = inProgress, viewModel = viewModel)
        CircularIndeterminateProgressBar(isDisplayed = inProgress)
        CreateAccountButton(isDisplayed = inProgress, viewModel = viewModel)
    }
}



@Composable
fun LoginButton(isDisplayed: Boolean, viewModel: IntroViewModel) {
    if (!isDisplayed) {
        Button(
            onClick = {
                viewModel.resetUiState()
                viewModel.authenticate()
            },
            shape = CutCornerShape(10),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ButtonColor,
                contentColor = Color.White
            )
        ) {
            Text(text = "Log in with Fido")
        }
    }
}

@Composable
fun CreateAccountButton(isDisplayed: Boolean, viewModel: IntroViewModel) {
    if (!isDisplayed) {
        Button(
            onClick = {
                Log.d("DAON", "CreateAccount onClick")
                viewModel.createAccount()
            },
            shape = CutCornerShape(10),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ButtonColor,
                contentColor = Color.White
            )
        ) {
            Text(text = "Create Account")
        }
    }
}
