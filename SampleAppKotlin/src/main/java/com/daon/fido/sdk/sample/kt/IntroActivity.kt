package com.daon.fido.sdk.sample.kt

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.daon.fido.sdk.sample.kt.face.AuthenticateFaceScreen
import com.daon.fido.sdk.sample.kt.face.FaceScreen
import com.daon.fido.sdk.sample.kt.face.RegisterFaceScreen
import com.daon.fido.sdk.sample.kt.fingerprint.FingerprintScreen
import com.daon.fido.sdk.sample.kt.passcode.AuthenticatePasscodeScreen
import com.daon.fido.sdk.sample.kt.passcode.PasscodeScreen
import com.daon.fido.sdk.sample.kt.passcode.RegisterPasscodeScreen
import com.daon.fido.sdk.sample.kt.passcode.VerifyAndReenrolPasscodeScreen
import com.daon.fido.sdk.sample.kt.ui.theme.IdentityxandroidsdkfidoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroActivity(): AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FidoApp()
        }
    }

    @Composable
    fun FidoApp() {
        IdentityxandroidsdkfidoTheme {
            val appState: FidoAppState = rememberFidoAppState()
            val navController = appState.navController
            Scaffold(
                scaffoldState = appState.scaffoldState
            ) { innerPadding ->

                val showSnackbar:(String, SnackbarDuration) -> Unit = {message, duration ->
                    appState.showSnackbar(message = message, duration = duration)
                }
                NavHost(
                    navController = navController,
                    startDestination = Screen.Intro.route,
                    modifier = Modifier.padding(innerPadding)) {
                        composable(route = Screen.Intro.route) {
                            IntroScreen(
                                onNavigateToHome = {user: String -> navController.navigate(Screen.Home.createRoute(user))},
                                onNavigateToChooseAuth = {
                                        navigateUp: () -> Unit, viewModel: ViewModel ->
                                    navController.navigate(
                                        Screen.AuthenticationAuths.createRoute(
                                            navigateUp, viewModel
                                        ),
                                    )
                                },
                                onNavigateToPasscode = {navController.navigate(Screen.Passcode.route)},
                                onNavigateToFace = {navController.navigate(Screen.Face.route)},
                                onNavigateToFingerprint = { navController.navigate(Screen.Fingerprint.route)},
                                onNavigateUp = { navController.popBackStack() },
                            )
                        }
                        composable(Screen.Home.route) {
                            it.arguments?.getString("user")?.let { it1 ->
                                HomeScreen(
                                    it1,
                                    onNavigateToRegistration = { navController.navigate(Screen.Registration.route) },
                                    backToIntro = { navController.navigate(Screen.Intro.route) {
                                        popUpTo(navController.graph.id) {
                                            inclusive = true
                                        }
                                    } },
                                    onNavigateToChooseAuth = { navigateUp: () -> Unit, viewModel: ViewModel ->
                                        navController.navigate(
                                            Screen.TransactionAuths.createRoute(
                                                navigateUp, viewModel
                                            ),
                                        )
                                    },
                                    onNavigateToPasscode = {navController.navigate(Screen.Passcode.route)},
                                    onNavigateToFace = {navController.navigate(Screen.Face.route)},
                                    onNavigateToFingerprint = { navController.navigate(Screen.Fingerprint.route)},
                                    onNavigateUp = { navController.popBackStack() },
                                    onNavigateToTransactionConfirmation = { navigateUp: () -> Unit, viewModel: ViewModel ->
                                        navController.navigate(
                                            Screen.TransactionConfirmation.createRoute(
                                                navigateUp, viewModel
                                            ),
                                        )
                                    },

                                )
                            }
                        }
                        composable(Screen.Passcode.route) {
                            PasscodeScreen(onNavigateUp = { navController.popBackStack()})
                        }


                            composable(Screen.Face.route) {
                                FaceScreen(onNavigateUp = { navController.popBackStack()})
                            }

                            composable(Screen.Fingerprint.route) {
                                FingerprintScreen (onNavigateUp = { navController.popBackStack()})
                            }
                            registerGraph(navController = navController)
                            composable(Screen.AuthenticationAuths.route) {
                                val parentEntry = remember(it) { navController.getBackStackEntry(Screen.Intro.route) }
                                val parentViewModel = hiltViewModel<IntroViewModel>(parentEntry)
                                AuthenticationChoicesScreen(onNavigateUp = {navController.popBackStack()}, parentViewModel)
                            }
                            composable(Screen.TransactionAuths.route) {
                                val parentEntry = remember(it) { navController.getBackStackEntry(Screen.Home.route) }
                                val parentViewModel = hiltViewModel<HomeViewModel>(parentEntry)
                                TransactionChoicesScreen(onNavigateUp = {navController.popBackStack()}, parentViewModel)
                            }
                        composable(Screen.TransactionConfirmation.route) {
                            val parentEntry = remember(it) { navController.getBackStackEntry(Screen.Home.route) }
                            val parentViewModel = hiltViewModel<HomeViewModel>(parentEntry)
                            TransactionConfirmationScreen(onNavigateUp = {navController.popBackStack()}, parentViewModel)
                        }
                }

            }
        }
    }

    private fun NavGraphBuilder.registerGraph(navController: NavController) {
        navigation(startDestination = Screen.Authenticators.route, route = Screen.Registration.route) {
            composable(Screen.Authenticators.route) {
                AuthenticatorsScreen(
                    onNavigateToChooseAuth = { navigateUp: () -> Unit, viewModel: ViewModel ->
                        navController.navigate(
                            Screen.RegistrationAuths.createRoute(
                                navigateUp, viewModel
                            ),
                        )
                    },
                    onNavigateToPasscode = { navController.navigate(Screen.Passcode.route) },
                    onNavigateToFace = { navController.navigate(Screen.Face.route)},
                    onNavigateToFingerprint = { navController.navigate(Screen.Fingerprint.route)},
                    onNavigateUp = { navController.popBackStack() },
                )
            }
            composable(Screen.RegistrationAuths.route) {
                val parentEntry = remember(it) { navController.getBackStackEntry(Screen.Authenticators.route) }
                val parentViewModel = hiltViewModel<AuthenticatorsViewModel>(parentEntry)
                RegistrationChoicesScreen(onNavigateUp = {navController.popBackStack()}, parentViewModel)
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Intro: Screen("intro")
    object Home: Screen("home/{user}") {
        fun createRoute(user: String) = "home/$user"
    }
    object Passcode: Screen("passcode")
    object Registration: Screen("registration")
    object Authenticators: Screen("authenticators")
    object RegistrationAuths: Screen("registrationAuths/{navigateUp}/{viewModel}") {
        fun createRoute(
            navigateUp: () -> Unit,
            viewModel: ViewModel
        ) = "registrationAuths/$navigateUp/$viewModel"
    }
    object AuthenticationAuths: Screen("authenticationAuths/{navigateUp}/{viewModel}") {
        fun createRoute(
            navigateUp: () -> Unit,
            viewModel: ViewModel
        ) = "authenticationAuths/$navigateUp/$viewModel"
    }
    object TransactionAuths: Screen("transactionAuths/{navigateUp}/{viewModel}") {
        fun createRoute(
            navigateUp: () -> Unit,
            viewModel: ViewModel
        ) = "transactionAuths/$navigateUp/$viewModel"
    }
    object TransactionConfirmation: Screen("transactionConfirmation/{navigateUp}/{viewModel}") {
        fun createRoute(
            navigateUp: () -> Unit,
            viewModel: ViewModel
        ) = "transactionConfirmation/$navigateUp/$viewModel"
    }
    object Face: Screen("face")
    object Fingerprint: Screen("fingerprint")
}



