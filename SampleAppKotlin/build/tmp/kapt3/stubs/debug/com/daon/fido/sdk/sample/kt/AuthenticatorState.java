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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u001e\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001BS\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0006\u0012\b\u0010\b\u001a\u0004\u0018\u00010\u0004\u0012\b\u0010\t\u001a\u0004\u0018\u00010\u0004\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\b\u0010\f\u001a\u0004\u0018\u00010\r\u0012\b\u0010\u000e\u001a\u0004\u0018\u00010\u000f\u00a2\u0006\u0002\u0010\u0010J\u0014\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0012J\t\u0010!\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\"\u001a\u00020\u0006H\u00c6\u0003J\u000b\u0010#\u001a\u0004\u0018\u00010\u0004H\u00c6\u0003J\u000b\u0010$\u001a\u0004\u0018\u00010\u0004H\u00c6\u0003J\t\u0010%\u001a\u00020\u000bH\u00c6\u0003J\u000b\u0010&\u001a\u0004\u0018\u00010\rH\u00c6\u0003J\u000b\u0010\'\u001a\u0004\u0018\u00010\u000fH\u00c6\u0003Jl\u0010(\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00042\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00042\b\b\u0002\u0010\n\u001a\u00020\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\r2\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000fH\u00c6\u0001\u00a2\u0006\u0002\u0010)J\u0013\u0010*\u001a\u00020\u00062\b\u0010+\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010,\u001a\u00020\u000bH\u00d6\u0001J\t\u0010-\u001a\u00020.H\u00d6\u0001R\u0019\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\n\n\u0002\u0010\u0013\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0015R\u0013\u0010\t\u001a\u0004\u0018\u00010\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0013\u0010\f\u001a\u0004\u0018\u00010\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0013\u0010\b\u001a\u0004\u0018\u00010\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0018R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001f\u00a8\u0006/"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/AuthenticatorState;", "", "authArray", "", "Lcom/daon/fido/client/sdk/model/Authenticator;", "authListAvailable", "", "authSelected", "selectedAuth", "authToDeregister", "selectedIndex", "", "registrationResult", "Lcom/daon/fido/sdk/sample/kt/RegistrationResult;", "deregistrationResult", "Lcom/daon/fido/sdk/sample/kt/DeregistrationResult;", "([Lcom/daon/fido/client/sdk/model/Authenticator;ZZLcom/daon/fido/client/sdk/model/Authenticator;Lcom/daon/fido/client/sdk/model/Authenticator;ILcom/daon/fido/sdk/sample/kt/RegistrationResult;Lcom/daon/fido/sdk/sample/kt/DeregistrationResult;)V", "getAuthArray", "()[Lcom/daon/fido/client/sdk/model/Authenticator;", "[Lcom/daon/fido/client/sdk/model/Authenticator;", "getAuthListAvailable", "()Z", "getAuthSelected", "getAuthToDeregister", "()Lcom/daon/fido/client/sdk/model/Authenticator;", "getDeregistrationResult", "()Lcom/daon/fido/sdk/sample/kt/DeregistrationResult;", "getRegistrationResult", "()Lcom/daon/fido/sdk/sample/kt/RegistrationResult;", "getSelectedAuth", "getSelectedIndex", "()I", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "([Lcom/daon/fido/client/sdk/model/Authenticator;ZZLcom/daon/fido/client/sdk/model/Authenticator;Lcom/daon/fido/client/sdk/model/Authenticator;ILcom/daon/fido/sdk/sample/kt/RegistrationResult;Lcom/daon/fido/sdk/sample/kt/DeregistrationResult;)Lcom/daon/fido/sdk/sample/kt/AuthenticatorState;", "equals", "other", "hashCode", "toString", "", "SampleAppKotlin_debug"})
public final class AuthenticatorState {
    @org.jetbrains.annotations.NotNull()
    private final com.daon.fido.client.sdk.model.Authenticator[] authArray = null;
    private final boolean authListAvailable = false;
    private final boolean authSelected = false;
    @org.jetbrains.annotations.Nullable()
    private final com.daon.fido.client.sdk.model.Authenticator selectedAuth = null;
    @org.jetbrains.annotations.Nullable()
    private final com.daon.fido.client.sdk.model.Authenticator authToDeregister = null;
    private final int selectedIndex = 0;
    @org.jetbrains.annotations.Nullable()
    private final com.daon.fido.sdk.sample.kt.RegistrationResult registrationResult = null;
    @org.jetbrains.annotations.Nullable()
    private final com.daon.fido.sdk.sample.kt.DeregistrationResult deregistrationResult = null;
    
    public AuthenticatorState(@org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.model.Authenticator[] authArray, boolean authListAvailable, boolean authSelected, @org.jetbrains.annotations.Nullable()
    com.daon.fido.client.sdk.model.Authenticator selectedAuth, @org.jetbrains.annotations.Nullable()
    com.daon.fido.client.sdk.model.Authenticator authToDeregister, int selectedIndex, @org.jetbrains.annotations.Nullable()
    com.daon.fido.sdk.sample.kt.RegistrationResult registrationResult, @org.jetbrains.annotations.Nullable()
    com.daon.fido.sdk.sample.kt.DeregistrationResult deregistrationResult) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.daon.fido.client.sdk.model.Authenticator[] getAuthArray() {
        return null;
    }
    
    public final boolean getAuthListAvailable() {
        return false;
    }
    
    public final boolean getAuthSelected() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.client.sdk.model.Authenticator getSelectedAuth() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.client.sdk.model.Authenticator getAuthToDeregister() {
        return null;
    }
    
    public final int getSelectedIndex() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.sdk.sample.kt.RegistrationResult getRegistrationResult() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.sdk.sample.kt.DeregistrationResult getDeregistrationResult() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.daon.fido.client.sdk.model.Authenticator[] component1() {
        return null;
    }
    
    public final boolean component2() {
        return false;
    }
    
    public final boolean component3() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.client.sdk.model.Authenticator component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.client.sdk.model.Authenticator component5() {
        return null;
    }
    
    public final int component6() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.sdk.sample.kt.RegistrationResult component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.sdk.sample.kt.DeregistrationResult component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.daon.fido.sdk.sample.kt.AuthenticatorState copy(@org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.model.Authenticator[] authArray, boolean authListAvailable, boolean authSelected, @org.jetbrains.annotations.Nullable()
    com.daon.fido.client.sdk.model.Authenticator selectedAuth, @org.jetbrains.annotations.Nullable()
    com.daon.fido.client.sdk.model.Authenticator authToDeregister, int selectedIndex, @org.jetbrains.annotations.Nullable()
    com.daon.fido.sdk.sample.kt.RegistrationResult registrationResult, @org.jetbrains.annotations.Nullable()
    com.daon.fido.sdk.sample.kt.DeregistrationResult deregistrationResult) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}