package com.daon.fido.sdk.sample.kt;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import androidx.lifecycle.ViewModel;
import com.daon.fido.client.sdk.Failure;
import com.daon.fido.client.sdk.IXUAF;
import com.daon.fido.client.sdk.Success;
import com.daon.fido.client.sdk.core.ErrorFactory;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.sdk.authenticator.controller.CaptureControllerProtocol;
import com.daon.sdk.crypto.log.LogUtils;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\n\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0006\u0010\u0014\u001a\u00020\u0015J\u0006\u0010\u0016\u001a\u00020\u0015J\u0006\u0010\u0017\u001a\u00020\u0015J\u0006\u0010\u0018\u001a\u00020\u0015J\b\u0010\u0019\u001a\u00020\u0015H\u0002J\b\u0010\u001a\u001a\u00020\u0015H\u0002J\u0006\u0010\u001b\u001a\u00020\u0015J\b\u0010\u001c\u001a\u00020\u001dH\u0002J\u0006\u0010\u001e\u001a\u00020\u0015J\b\u0010\u001f\u001a\u00020 H\u0002J\b\u0010!\u001a\u00020\u0015H\u0002J\u0006\u0010\"\u001a\u00020\u0015J\u0006\u0010#\u001a\u00020\u0015J\b\u0010$\u001a\u00020\u0015H\u0002J\u0006\u0010%\u001a\u00020\u0015J\u0006\u0010&\u001a\u00020\u0015J\b\u0010\'\u001a\u00020\u0015H\u0002J\u0006\u0010(\u001a\u00020\u0015J\u000e\u0010)\u001a\u00020\u00152\u0006\u0010*\u001a\u00020+R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013\u00a8\u0006,"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/IntroViewModel;", "Lcom/daon/fido/sdk/sample/kt/BaseViewModel;", "application", "Landroid/app/Application;", "fido", "Lcom/daon/fido/client/sdk/IXUAF;", "prefs", "Landroid/content/SharedPreferences;", "(Landroid/app/Application;Lcom/daon/fido/client/sdk/IXUAF;Landroid/content/SharedPreferences;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/daon/fido/sdk/sample/kt/FidoUiState;", "gpsTimeoutCountdown", "Landroid/os/CountDownTimer;", "getGpsTimeoutCountdown", "()Landroid/os/CountDownTimer;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "authenticate", "", "authenticateSilent", "cancelCurrentOperation", "createAccount", "createNewAccount", "deleteLogs", "deselectAuth", "generateEmail", "", "initFido", "isFileSizeGreaterThan5KB", "", "reinitializeFido", "resetAccountCreationResult", "resetAuthArrayAvailable", "resetFido", "resetLoginResult", "resetUiState", "rotateLogs", "startGps", "updateSelectedAuth", "auth", "Lcom/daon/fido/client/sdk/model/Authenticator;", "SampleAppKotlin_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class IntroViewModel extends com.daon.fido.sdk.sample.kt.BaseViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.daon.fido.client.sdk.IXUAF fido = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.daon.fido.sdk.sample.kt.FidoUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.daon.fido.sdk.sample.kt.FidoUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final android.os.CountDownTimer gpsTimeoutCountdown = null;
    
    @javax.inject.Inject()
    public IntroViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application, @org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.IXUAF fido, @org.jetbrains.annotations.NotNull()
    android.content.SharedPreferences prefs) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.daon.fido.sdk.sample.kt.FidoUiState> getUiState() {
        return null;
    }
    
    public final void initFido() {
    }
    
    private final boolean isFileSizeGreaterThan5KB() {
        return false;
    }
    
    private final void rotateLogs() {
    }
    
    private final void deleteLogs() {
    }
    
    public final void createAccount() {
    }
    
    private final void resetFido() {
    }
    
    private final void reinitializeFido() {
    }
    
    public final void startGps() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.os.CountDownTimer getGpsTimeoutCountdown() {
        return null;
    }
    
    private final void createNewAccount() {
    }
    
    private final java.lang.String generateEmail() {
        return null;
    }
    
    public final void authenticate() {
    }
    
    public final void resetUiState() {
    }
    
    public final void resetAccountCreationResult() {
    }
    
    public final void resetLoginResult() {
    }
    
    public final void updateSelectedAuth(@org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.model.Authenticator auth) {
    }
    
    public final void resetAuthArrayAvailable() {
    }
    
    public final void deselectAuth() {
    }
    
    public final void authenticateSilent() {
    }
    
    public final void cancelCurrentOperation() {
    }
}