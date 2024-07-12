package com.daon.fido.sdk.sample.kt.passcode

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AuthenticatePasscodeScreen(
    onNavigateUp: () -> Unit ) {
    val focusManager = LocalFocusManager.current
    val passcodeViewModel = hiltViewModel<PasscodeViewModel>()
    val context = LocalContext.current
    var textValue1 by remember { mutableStateOf(TextFieldValue("")) }

    DisposableEffect(key1 = passcodeViewModel ) {
        passcodeViewModel.onStart()
        onDispose { passcodeViewModel.onStop() }
    }

    val captureComplete = passcodeViewModel.captureComplete.collectAsState()
    if (captureComplete.value) {
        passcodeViewModel.resetCaptureComplete()
        onNavigateUp()
    }

    val captureInfo = passcodeViewModel.captureInfo.collectAsState()
    if (captureInfo.value.isNotEmpty()) {
        Toast.makeText(context, captureInfo.value, Toast.LENGTH_SHORT).show()
        passcodeViewModel.resetCaptureInfo()
    }

    val captureCompleteWithError = passcodeViewModel.captureCompleteWithError.collectAsState()
    if (captureCompleteWithError.value) {
        passcodeViewModel.resetCaptureCompleteWithError()
        onNavigateUp()

    }

    val enableRetry = passcodeViewModel.enableRetry.collectAsState()
    if (enableRetry.value) {
        //Staying in the page and resetting the text field value
        textValue1 = TextFieldValue("")
        passcodeViewModel.resetEnableRetry()
    }

    BackHandler(true) {
        //Cancel the ongoing fido operation and capture controller
        passcodeViewModel.cancelCurrentOperation()
        onNavigateUp()
    }


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text (
                "Passcode"
        )
        Text (
            "Enter passcode"
        )
        val onNext: KeyboardActionScope.() -> Unit = {
            focusManager.moveFocus(FocusDirection.Next)
        }
        val onDone: KeyboardActionScope.() -> Unit = {
            passcodeViewModel.authenticate(textValue1.text)
        }
        OutlinedTextField(
            label = { Text("Passcode")},
            value = textValue1,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            onValueChange = {
                textValue1 = it
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.NumberPassword
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.clearFocus()
                passcodeViewModel.authenticate(textValue1.text)
            })

        )
    }
}
