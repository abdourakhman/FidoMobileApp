package com.daon.fido.sdk.sample.kt

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.daon.fido.sdk.sample.kt.model.*
import com.daon.fido.sdk.sample.kt.ui.theme.ButtonColor
import com.daon.fido.sdk.sample.kt.util.CircularIndeterminateProgressBar

@Composable
fun HomeScreen(
    user: String,
    onNavigateToRegistration: () -> Unit,
    backToIntro: () -> Unit,
    onNavigateToChooseAuth: (() -> Unit, ViewModel) -> Unit,
    onNavigateToPasscode: () -> Unit,
    onNavigateToFace: () -> Unit,
    onNavigateToFingerprint: () -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateToTransactionConfirmation: (() -> Unit, ViewModel) -> Unit
) {
    val viewModel = hiltViewModel<HomeViewModel>()
    val context = LocalContext.current

    var inProgress = viewModel.inProgress.collectAsState()

    LaunchedEffect(key1 = viewModel.transactionState) {
        viewModel.transactionState.collect { transactionState ->
            if (transactionState.authArrayAvailable) {
                onNavigateToChooseAuth(onNavigateUp, viewModel)
            }

            if (transactionState.authSelected) {
                viewModel.deselectAuth()
                when (transactionState.selectedAuth?.aaid) {
                    SRP_PASSCODE_AUTH_AAID, PASSCODE_AUTH_AAID -> {
                        onNavigateToPasscode()
                    }
                    FINGERPRINT_AUTH_AAID -> {
                        onNavigateToFingerprint()
                    }
                    FACE_AUTH_AAID, ADOS_FACE_AUTH_AAID -> {
                        onNavigateToFace()
                    }
                    SILENT_AUTH_AAID -> {
                        viewModel.authenticateSilent()
                    }
                }
            }

            if (transactionState.authenticationCompleted) {
                Toast.makeText(context, transactionState.message, Toast.LENGTH_SHORT).show()
                viewModel.resetAuthenticationCompleted()
            }

            if (transactionState.resetComplete) {
                viewModel.resetResetComplete()
                backToIntro()
            }

            if (transactionState.confirmTransaction) {
                onNavigateToTransactionConfirmation(onNavigateUp, viewModel)
            }

            if (transactionState.transactionConfirmationResult != 99) {
                viewModel.submitDisplayTransactionResult(transactionState.transactionConfirmationResult)
            }

            if (transactionState.confirmationOTPReceived) {
                Toast.makeText(context, "Your One-Time Password - ${transactionState.confirmationOTP}", Toast.LENGTH_SHORT).show()
                viewModel.resetConfirmationOTP()
            }

        }
    }

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose {
            viewModel.onStop()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Text(
            text = "Welcome $user",
            fontSize = 20.sp,
            color = ButtonColor
        )

        Spacer(modifier = Modifier.height(height = 50.dp))
        CircularIndeterminateProgressBar(
            isDisplayed = inProgress.value, modifier = Modifier.align(Alignment.CenterHorizontally))
        Button(
            onClick = {
                viewModel.authenticate()
            },
            shape = CutCornerShape(10),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ButtonColor,
                contentColor = Color.White
            )
        ) {
            Text(text = stringResource(R.string.action_step_up_auth))
        }
        Spacer(modifier = Modifier.height(height = 10.dp))
        Button(
            onClick = {
                onNavigateToRegistration()
            },
            shape = CutCornerShape(10),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ButtonColor,
                contentColor = Color.White
            )
        ) {
            Text(text = stringResource(R.string.action_manage_authenticators))
        }
        Spacer(modifier = Modifier.height(height = 10.dp))
        Button(
            onClick = {
                viewModel.resetFido()
            },
            shape = CutCornerShape(10),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ButtonColor,
                contentColor = Color.White
            )
        ) {
            Text(text = stringResource(R.string.action_reset))
        }
        Spacer(modifier = Modifier.height(height = 10.dp))
    }

    BackHandler(
        enabled = true,
        onBack = {
            viewModel.doLogout()
            Log.d("DAON", "Back to Intro")
            backToIntro()
        }
    )
}