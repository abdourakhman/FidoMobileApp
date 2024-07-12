package com.daon.fido.sdk.sample.kt

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.daon.fido.client.sdk.model.Authenticator
import com.daon.fido.sdk.sample.kt.model.*
import com.daon.fido.sdk.sample.kt.ui.theme.ButtonColor

@Composable
fun AuthenticatorsScreen(
    onNavigateToChooseAuth: (() -> Unit, ViewModel) -> Unit,
    onNavigateToPasscode: () -> Unit,
    onNavigateToFace: () -> Unit,
    onNavigateToFingerprint: () -> Unit,
    onNavigateUp: () -> Unit
) {

    val viewModel = hiltViewModel<AuthenticatorsViewModel>()
    var selectedIndex by remember { mutableStateOf(-1) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val state = viewModel.authState.collectAsState()
    val authToDeregister = state.value.authToDeregister
    LaunchedEffect(key1 = viewModel.authState) {
        viewModel.authState.collect { authState ->
            if (authState.authListAvailable) {
                onNavigateToChooseAuth(onNavigateUp, viewModel)
            }

            selectedIndex = authState.selectedIndex

            if (authState.authSelected) {
                var aaid = authState.selectedAuth?.aaid
                when (aaid) {
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
                        viewModel.registerSilent()
                    }

                }
                viewModel.deselectAuth()

            }

            authState.registrationResult?.let { registrationResult ->
                Toast.makeText(
                    context, registrationResult.message, Toast.LENGTH_SHORT
                ).show()
                viewModel.resetRegistrationResult()
            }

            authState.deregistrationResult?.let { deregistrationResult ->
                Toast.makeText(
                    context, deregistrationResult.message, Toast.LENGTH_SHORT
                ).show()
                viewModel.resetDeregistrationResult()
            }
        }
    }

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose {
            viewModel.onStop()
        }
    }

    Scaffold(topBar = {
        TopAppBar(backgroundColor = MaterialTheme.colors.background,
            title = { Text("Registered authenticators") })
    }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween
            ) {
                AuthenticatorList(viewModel, selectedIndex)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.register()
                        }, colors = ButtonDefaults.buttonColors(
                            backgroundColor = ButtonColor, contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Register", style = TextStyle(fontSize = 15.sp)
                        )

                    }
                    Button(
                        onClick = {
                            if (authToDeregister != null) {
                                viewModel.remove(viewModel.authState.value.authToDeregister!!)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please select one authenticator from the list.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ButtonColor,
                            contentColor = Color.White
                        ) ,
                        enabled = authToDeregister != null
                    ) {
                        Text(
                            text = "Remove",
                            style = TextStyle(fontSize = 15.sp)
                        )

                    }

                    Button(
                        onClick = {
                            if (authToDeregister != null) {
                                viewModel.deregister(viewModel.authState.value.authToDeregister!!)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please select one authenticator from the list.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }, colors = ButtonDefaults.buttonColors(
                            backgroundColor = ButtonColor, contentColor = Color.White
                        ), enabled = authToDeregister != null

                    ) {
                        Text(
                            text = "Deregister", style = TextStyle(fontSize = 15.sp)
                        )
                    }
                }
            }
        }

    }


    BackHandler(true) {
        onNavigateUp()
    }
}


@Composable
fun AuthenticatorList(viewModel: AuthenticatorsViewModel, selectedIndex: Int) {
    Log.d("DAON", "AuthenticatorList ${viewModel.discoverList.size}")

    val onItemClick = { index: Int ->
        Log.d("DAON", "onItemClick $index")
        viewModel.updateAuthToDeregister(viewModel.discoverList[index], index)
    }
    LazyColumn(modifier = Modifier.padding(10.dp)) {
        itemsIndexed(viewModel.discoverList) { index, item: Authenticator ->
            AuthenticatorInfoCard(
                authenticator = item,
                viewModel = viewModel,
                index = index,
                selected = selectedIndex == index,
                onItemClick = onItemClick
            )
        }
    }
}

@Composable
fun AuthenticatorInfoCard(
    authenticator: Authenticator,
    viewModel: AuthenticatorsViewModel,
    index: Int,
    selected: Boolean,
    onItemClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .background(if (selected) Color.LightGray else MaterialTheme.colors.background)
            .clickable {
                onItemClick(index)
            },
        backgroundColor = (if (selected) Color.LightGray else MaterialTheme.colors.background),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            getBitmap(authenticator.icon)?.let {
                Image(
                    bitmap = it,
                    contentDescription = " ",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(6.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Column(Modifier.padding(8.dp)) {
                Text(
                    text = authenticator.title,
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = authenticator.description, style = MaterialTheme.typography.body2
                )
            }

        }
    }
}


fun <T> SnapshotStateList<T>.swapList(newList: List<T>) {
    clear()
    addAll(newList)
}


fun getBitmap(icon: String): ImageBitmap? {
    if (icon != null) {
        val options = BitmapFactory.Options()
        options.inMutable = true
        val commaIndex = icon.indexOf(',')
        val imageBase64 = icon.substring(commaIndex + 1)
        val imgBytes = Base64.decode(imageBase64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.size, options).asImageBitmap()
    }
    return null
}