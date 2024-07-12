package com.daon.fido.sdk.sample.kt.passcode

import android.widget.Toast
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PasscodeScreen(
    onNavigateUp: () -> Unit
) {
    val passcodeViewModel = hiltViewModel<PasscodeViewModel>()
    var controllerInstantiated = passcodeViewModel.controllerInstantiated.collectAsState()
    val isEnrol = passcodeViewModel.isEnrol.collectAsState()
    val isVerifyAndEnroll = passcodeViewModel.isVerifyAndEnroll.collectAsState()
    val captureInfo = passcodeViewModel.captureInfo.collectAsState()
    val context = LocalContext.current
    DisposableEffect(key1 = passcodeViewModel) {
        passcodeViewModel.intializeController()
        onDispose {

        }
    }

    LaunchedEffect(key1 = passcodeViewModel.onControllerInstantiateError) {
        passcodeViewModel.onControllerInstantiateError.collect { controllerInstantiatedError ->
            if (controllerInstantiatedError) {
                passcodeViewModel.resetControllerInstantiatedError()
                Toast.makeText(context, captureInfo.value, Toast.LENGTH_SHORT).show()
                onNavigateUp()
            }

        }

    }


    if (controllerInstantiated.value) {
        if (isEnrol.value) {
            RegisterPasscodeScreen {
                onNavigateUp()
            }
        } else {
            if (isVerifyAndEnroll.value) {
                VerifyAndReenrolPasscodeScreen {
                    onNavigateUp()
                }
            } else {
                AuthenticatePasscodeScreen {
                    onNavigateUp()
                }
            }
        }
    } else {
        Text("Waiting for the controller to initialize ...")
    }

}