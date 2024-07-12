package com.daon.fido.sdk.sample.kt;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.daon.fido.client.sdk.ConfirmationOTPListener;
import com.daon.fido.client.sdk.Failure;
import com.daon.fido.client.sdk.IXUAF;
import com.daon.fido.client.sdk.Success;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.client.sdk.transaction.TransactionContent;
import com.daon.fido.client.sdk.transaction.TransactionUtils;
import com.daon.sdk.authenticator.controller.CaptureControllerProtocol;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0011\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0006\u0010\u0018\u001a\u00020\u0019J\u0006\u0010\u001a\u001a\u00020\u0019J\u0006\u0010\u001b\u001a\u00020\u0019J\b\u0010\u001c\u001a\u00020\u0019H\u0002J\u0006\u0010\u001d\u001a\u00020\u0019J\u0006\u0010\u001e\u001a\u00020\u0019J\u0006\u0010\u001f\u001a\u00020\u0019J\u0006\u0010 \u001a\u00020\u0019J\b\u0010!\u001a\u00020\u0019H\u0002J\u0006\u0010\"\u001a\u00020\u0019J\u0006\u0010#\u001a\u00020\u0019J\u0006\u0010$\u001a\u00020\u0019J\u0006\u0010%\u001a\u00020\u0019J\u0006\u0010&\u001a\u00020\u0019J\u0006\u0010\'\u001a\u00020\u0019J\u0006\u0010(\u001a\u00020\u0019J\u000e\u0010)\u001a\u00020\u00192\u0006\u0010*\u001a\u00020+J\u000e\u0010,\u001a\u00020\u00192\u0006\u0010-\u001a\u00020.J\u000e\u0010/\u001a\u00020\u00192\u0006\u0010*\u001a\u00020+R \u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0084\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fR\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00110\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0015\u00a8\u00060"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/HomeViewModel;", "Lcom/daon/fido/sdk/sample/kt/BaseViewModel;", "application", "Landroid/app/Application;", "fido", "Lcom/daon/fido/client/sdk/IXUAF;", "prefs", "Landroid/content/SharedPreferences;", "(Landroid/app/Application;Lcom/daon/fido/client/sdk/IXUAF;Landroid/content/SharedPreferences;)V", "_inProgress", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "get_inProgress", "()Lkotlinx/coroutines/flow/MutableStateFlow;", "set_inProgress", "(Lkotlinx/coroutines/flow/MutableStateFlow;)V", "_transactionState", "Lcom/daon/fido/sdk/sample/kt/TransactionState;", "inProgress", "Lkotlinx/coroutines/flow/StateFlow;", "getInProgress", "()Lkotlinx/coroutines/flow/StateFlow;", "transactionState", "getTransactionState", "authenticate", "", "authenticateSilent", "cancelCurrentOperation", "deleteUser", "deselectAuth", "doLogout", "onStart", "onStop", "reset", "resetAuthArrayAvailable", "resetAuthenticationCompleted", "resetConfirmationOTP", "resetFido", "resetResetComplete", "resetTransactionConfirmationResult", "resetTransactionData", "submitDisplayTransactionResult", "result", "", "updateSelectedAuth", "auth", "Lcom/daon/fido/client/sdk/model/Authenticator;", "updateTransactionConfirmationResult", "SampleAppKotlin_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class HomeViewModel extends com.daon.fido.sdk.sample.kt.BaseViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.daon.fido.client.sdk.IXUAF fido = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.daon.fido.sdk.sample.kt.TransactionState> _transactionState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.daon.fido.sdk.sample.kt.TransactionState> transactionState = null;
    @org.jetbrains.annotations.NotNull()
    private kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _inProgress;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> inProgress = null;
    
    @javax.inject.Inject()
    public HomeViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application, @org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.IXUAF fido, @org.jetbrains.annotations.NotNull()
    android.content.SharedPreferences prefs) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.daon.fido.sdk.sample.kt.TransactionState> getTransactionState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> get_inProgress() {
        return null;
    }
    
    protected final void set_inProgress(@org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getInProgress() {
        return null;
    }
    
    public final void onStart() {
    }
    
    public final void onStop() {
    }
    
    public final void authenticate() {
    }
    
    public final void resetFido() {
    }
    
    private final void reset() {
    }
    
    private final void deleteUser() {
    }
    
    public final void doLogout() {
    }
    
    public final void resetResetComplete() {
    }
    
    public final void resetAuthenticationCompleted() {
    }
    
    public final void deselectAuth() {
    }
    
    public final void updateSelectedAuth(@org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.model.Authenticator auth) {
    }
    
    public final void resetAuthArrayAvailable() {
    }
    
    public final void authenticateSilent() {
    }
    
    public final void cancelCurrentOperation() {
    }
    
    public final void updateTransactionConfirmationResult(int result) {
    }
    
    public final void submitDisplayTransactionResult(int result) {
    }
    
    public final void resetTransactionData() {
    }
    
    public final void resetTransactionConfirmationResult() {
    }
    
    public final void resetConfirmationOTP() {
    }
}