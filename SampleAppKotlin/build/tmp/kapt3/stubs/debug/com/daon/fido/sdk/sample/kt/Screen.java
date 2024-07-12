package com.daon.fido.sdk.sample.kt;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.material.SnackbarDuration;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Modifier;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;
import androidx.navigation.NavGraphBuilder;
import dagger.hilt.android.AndroidEntryPoint;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u000b\u0007\b\t\n\u000b\f\r\u000e\u000f\u0010\u0011B\u000f\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u0082\u0001\u000b\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u00a8\u0006\u001d"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/Screen;", "", "route", "", "(Ljava/lang/String;)V", "getRoute", "()Ljava/lang/String;", "AuthenticationAuths", "Authenticators", "Face", "Fingerprint", "Home", "Intro", "Passcode", "Registration", "RegistrationAuths", "TransactionAuths", "TransactionConfirmation", "Lcom/daon/fido/sdk/sample/kt/Screen$AuthenticationAuths;", "Lcom/daon/fido/sdk/sample/kt/Screen$Authenticators;", "Lcom/daon/fido/sdk/sample/kt/Screen$Face;", "Lcom/daon/fido/sdk/sample/kt/Screen$Fingerprint;", "Lcom/daon/fido/sdk/sample/kt/Screen$Home;", "Lcom/daon/fido/sdk/sample/kt/Screen$Intro;", "Lcom/daon/fido/sdk/sample/kt/Screen$Passcode;", "Lcom/daon/fido/sdk/sample/kt/Screen$Registration;", "Lcom/daon/fido/sdk/sample/kt/Screen$RegistrationAuths;", "Lcom/daon/fido/sdk/sample/kt/Screen$TransactionAuths;", "Lcom/daon/fido/sdk/sample/kt/Screen$TransactionConfirmation;", "SampleAppKotlin_debug"})
public abstract class Screen {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String route = null;
    
    private Screen(java.lang.String route) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRoute() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\u0003\u001a\u00020\u00042\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\b\u001a\u00020\t\u00a8\u0006\n"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/Screen$AuthenticationAuths;", "Lcom/daon/fido/sdk/sample/kt/Screen;", "()V", "createRoute", "", "navigateUp", "Lkotlin/Function0;", "", "viewModel", "Landroidx/lifecycle/ViewModel;", "SampleAppKotlin_debug"})
    public static final class AuthenticationAuths extends com.daon.fido.sdk.sample.kt.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.daon.fido.sdk.sample.kt.Screen.AuthenticationAuths INSTANCE = null;
        
        private AuthenticationAuths() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String createRoute(@org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function0<kotlin.Unit> navigateUp, @org.jetbrains.annotations.NotNull()
        androidx.lifecycle.ViewModel viewModel) {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/Screen$Authenticators;", "Lcom/daon/fido/sdk/sample/kt/Screen;", "()V", "SampleAppKotlin_debug"})
    public static final class Authenticators extends com.daon.fido.sdk.sample.kt.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.daon.fido.sdk.sample.kt.Screen.Authenticators INSTANCE = null;
        
        private Authenticators() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/Screen$Face;", "Lcom/daon/fido/sdk/sample/kt/Screen;", "()V", "SampleAppKotlin_debug"})
    public static final class Face extends com.daon.fido.sdk.sample.kt.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.daon.fido.sdk.sample.kt.Screen.Face INSTANCE = null;
        
        private Face() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/Screen$Fingerprint;", "Lcom/daon/fido/sdk/sample/kt/Screen;", "()V", "SampleAppKotlin_debug"})
    public static final class Fingerprint extends com.daon.fido.sdk.sample.kt.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.daon.fido.sdk.sample.kt.Screen.Fingerprint INSTANCE = null;
        
        private Fingerprint() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0004\u00a8\u0006\u0006"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/Screen$Home;", "Lcom/daon/fido/sdk/sample/kt/Screen;", "()V", "createRoute", "", "user", "SampleAppKotlin_debug"})
    public static final class Home extends com.daon.fido.sdk.sample.kt.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.daon.fido.sdk.sample.kt.Screen.Home INSTANCE = null;
        
        private Home() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String createRoute(@org.jetbrains.annotations.NotNull()
        java.lang.String user) {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/Screen$Intro;", "Lcom/daon/fido/sdk/sample/kt/Screen;", "()V", "SampleAppKotlin_debug"})
    public static final class Intro extends com.daon.fido.sdk.sample.kt.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.daon.fido.sdk.sample.kt.Screen.Intro INSTANCE = null;
        
        private Intro() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/Screen$Passcode;", "Lcom/daon/fido/sdk/sample/kt/Screen;", "()V", "SampleAppKotlin_debug"})
    public static final class Passcode extends com.daon.fido.sdk.sample.kt.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.daon.fido.sdk.sample.kt.Screen.Passcode INSTANCE = null;
        
        private Passcode() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/Screen$Registration;", "Lcom/daon/fido/sdk/sample/kt/Screen;", "()V", "SampleAppKotlin_debug"})
    public static final class Registration extends com.daon.fido.sdk.sample.kt.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.daon.fido.sdk.sample.kt.Screen.Registration INSTANCE = null;
        
        private Registration() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\u0003\u001a\u00020\u00042\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\b\u001a\u00020\t\u00a8\u0006\n"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/Screen$RegistrationAuths;", "Lcom/daon/fido/sdk/sample/kt/Screen;", "()V", "createRoute", "", "navigateUp", "Lkotlin/Function0;", "", "viewModel", "Landroidx/lifecycle/ViewModel;", "SampleAppKotlin_debug"})
    public static final class RegistrationAuths extends com.daon.fido.sdk.sample.kt.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.daon.fido.sdk.sample.kt.Screen.RegistrationAuths INSTANCE = null;
        
        private RegistrationAuths() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String createRoute(@org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function0<kotlin.Unit> navigateUp, @org.jetbrains.annotations.NotNull()
        androidx.lifecycle.ViewModel viewModel) {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\u0003\u001a\u00020\u00042\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\b\u001a\u00020\t\u00a8\u0006\n"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/Screen$TransactionAuths;", "Lcom/daon/fido/sdk/sample/kt/Screen;", "()V", "createRoute", "", "navigateUp", "Lkotlin/Function0;", "", "viewModel", "Landroidx/lifecycle/ViewModel;", "SampleAppKotlin_debug"})
    public static final class TransactionAuths extends com.daon.fido.sdk.sample.kt.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.daon.fido.sdk.sample.kt.Screen.TransactionAuths INSTANCE = null;
        
        private TransactionAuths() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String createRoute(@org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function0<kotlin.Unit> navigateUp, @org.jetbrains.annotations.NotNull()
        androidx.lifecycle.ViewModel viewModel) {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\u0003\u001a\u00020\u00042\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\b\u001a\u00020\t\u00a8\u0006\n"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/Screen$TransactionConfirmation;", "Lcom/daon/fido/sdk/sample/kt/Screen;", "()V", "createRoute", "", "navigateUp", "Lkotlin/Function0;", "", "viewModel", "Landroidx/lifecycle/ViewModel;", "SampleAppKotlin_debug"})
    public static final class TransactionConfirmation extends com.daon.fido.sdk.sample.kt.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.daon.fido.sdk.sample.kt.Screen.TransactionConfirmation INSTANCE = null;
        
        private TransactionConfirmation() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String createRoute(@org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function0<kotlin.Unit> navigateUp, @org.jetbrains.annotations.NotNull()
        androidx.lifecycle.ViewModel viewModel) {
            return null;
        }
    }
}