package com.daon.fido.sdk.sample.kt;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.daon.fido.client.sdk.Failure;
import com.daon.fido.client.sdk.IXUAF;
import com.daon.fido.client.sdk.Success;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.client.sdk.util.SDKPreferences;
import com.daon.sdk.authenticator.controller.CaptureControllerProtocol;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010\u0011\n\u0002\b\u000b\n\u0002\u0010\b\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0006\u0010\u0017\u001a\u00020\u0018J\u000e\u0010\u0019\u001a\u00020\u00182\u0006\u0010\u001a\u001a\u00020\u000eJ\u0006\u0010\u001b\u001a\u00020\u0018J\b\u0010\u001c\u001a\u00020\u0018H\u0002J\u001b\u0010\u001d\u001a\u00020\u00182\f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u000e0\u001fH\u0002\u00a2\u0006\u0002\u0010 J\u0006\u0010!\u001a\u00020\u0018J\u0006\u0010\"\u001a\u00020\u0018J\u0006\u0010#\u001a\u00020\u0018J\u0006\u0010$\u001a\u00020\u0018J\u000e\u0010%\u001a\u00020\u00182\u0006\u0010\u001a\u001a\u00020\u000eJ\u0006\u0010&\u001a\u00020\u0018J\u0006\u0010\'\u001a\u00020\u0018J\u0006\u0010(\u001a\u00020\u0018J\u0018\u0010)\u001a\u00020\u00182\b\u0010\u001a\u001a\u0004\u0018\u00010\u000e2\u0006\u0010*\u001a\u00020+J\u000e\u0010,\u001a\u00020\u00182\u0006\u0010\u001a\u001a\u00020\u000eR\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0017\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006-"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/AuthenticatorsViewModel;", "Lcom/daon/fido/sdk/sample/kt/BaseViewModel;", "application", "Landroid/app/Application;", "fido", "Lcom/daon/fido/client/sdk/IXUAF;", "prefs", "Landroid/content/SharedPreferences;", "(Landroid/app/Application;Lcom/daon/fido/client/sdk/IXUAF;Landroid/content/SharedPreferences;)V", "_authState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/daon/fido/sdk/sample/kt/AuthenticatorState;", "_discoverList", "Landroidx/compose/runtime/snapshots/SnapshotStateList;", "Lcom/daon/fido/client/sdk/model/Authenticator;", "authState", "Lkotlinx/coroutines/flow/StateFlow;", "getAuthState", "()Lkotlinx/coroutines/flow/StateFlow;", "discoverList", "", "getDiscoverList", "()Ljava/util/List;", "cancelCurrentOperation", "", "deregister", "auth", "deselectAuth", "discover", "filterAuthArray", "authArray", "", "([Lcom/daon/fido/client/sdk/model/Authenticator;)V", "onStart", "onStop", "register", "registerSilent", "remove", "resetAuthListAvailable", "resetDeregistrationResult", "resetRegistrationResult", "updateAuthToDeregister", "index", "", "updateSelectedAuth", "SampleAppKotlin_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class AuthenticatorsViewModel extends com.daon.fido.sdk.sample.kt.BaseViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.daon.fido.client.sdk.IXUAF fido = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.daon.fido.sdk.sample.kt.AuthenticatorState> _authState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.daon.fido.sdk.sample.kt.AuthenticatorState> authState = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.snapshots.SnapshotStateList<com.daon.fido.client.sdk.model.Authenticator> _discoverList = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.daon.fido.client.sdk.model.Authenticator> discoverList = null;
    
    @javax.inject.Inject()
    public AuthenticatorsViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application, @org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.IXUAF fido, @org.jetbrains.annotations.NotNull()
    android.content.SharedPreferences prefs) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.daon.fido.sdk.sample.kt.AuthenticatorState> getAuthState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.daon.fido.client.sdk.model.Authenticator> getDiscoverList() {
        return null;
    }
    
    public final void onStart() {
    }
    
    private final void filterAuthArray(com.daon.fido.client.sdk.model.Authenticator[] authArray) {
    }
    
    public final void onStop() {
    }
    
    private final void discover() {
    }
    
    public final void register() {
    }
    
    public final void cancelCurrentOperation() {
    }
    
    public final void remove(@org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.model.Authenticator auth) {
    }
    
    public final void deregister(@org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.model.Authenticator auth) {
    }
    
    public final void updateSelectedAuth(@org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.model.Authenticator auth) {
    }
    
    public final void resetAuthListAvailable() {
    }
    
    public final void resetRegistrationResult() {
    }
    
    public final void resetDeregistrationResult() {
    }
    
    public final void updateAuthToDeregister(@org.jetbrains.annotations.Nullable()
    com.daon.fido.client.sdk.model.Authenticator auth, int index) {
    }
    
    public final void deselectAuth() {
    }
    
    public final void registerSilent() {
    }
}