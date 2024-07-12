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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b \n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001Bi\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\u0003\u0012\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n\u0012\u0006\u0010\f\u001a\u00020\u0003\u0012\b\u0010\r\u001a\u0004\u0018\u00010\u000b\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000f\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0011\u00a2\u0006\u0002\u0010\u0012J\t\u0010$\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010%\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010&\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\t\u0010\'\u001a\u00020\u0003H\u00c6\u0003J\u0014\u0010(\u001a\b\u0012\u0004\u0012\u00020\u000b0\nH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0016J\t\u0010)\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010*\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003J\u000b\u0010+\u001a\u0004\u0018\u00010\u000fH\u00c6\u0003J\u000b\u0010,\u001a\u0004\u0018\u00010\u0011H\u00c6\u0003Jx\u0010-\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00072\b\b\u0002\u0010\b\u001a\u00020\u00032\u000e\b\u0002\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\b\b\u0002\u0010\f\u001a\u00020\u00032\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000b2\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000f2\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u00c6\u0001\u00a2\u0006\u0002\u0010.J\u0013\u0010/\u001a\u00020\u00032\b\u00100\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00101\u001a\u000202H\u00d6\u0001J\t\u00103\u001a\u00020\u0011H\u00d6\u0001R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0019\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n\u00a2\u0006\n\n\u0002\u0010\u0017\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0019R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0019R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0013\u0010\r\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0013\u0010\u0010\u001a\u0004\u0018\u00010\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#\u00a8\u00064"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/FidoUiState;", "", "inProgress", "", "initializationResult", "Lcom/daon/fido/sdk/sample/kt/FidoInitializationResult;", "accountCreationResult", "Lcom/daon/fido/sdk/sample/kt/AccountCreationResult;", "authArrayAvailable", "authArray", "", "Lcom/daon/fido/client/sdk/model/Authenticator;", "authSelected", "selectedAuth", "loginResult", "Lcom/daon/fido/sdk/sample/kt/LoginResult;", "username", "", "(ZLcom/daon/fido/sdk/sample/kt/FidoInitializationResult;Lcom/daon/fido/sdk/sample/kt/AccountCreationResult;Z[Lcom/daon/fido/client/sdk/model/Authenticator;ZLcom/daon/fido/client/sdk/model/Authenticator;Lcom/daon/fido/sdk/sample/kt/LoginResult;Ljava/lang/String;)V", "getAccountCreationResult", "()Lcom/daon/fido/sdk/sample/kt/AccountCreationResult;", "getAuthArray", "()[Lcom/daon/fido/client/sdk/model/Authenticator;", "[Lcom/daon/fido/client/sdk/model/Authenticator;", "getAuthArrayAvailable", "()Z", "getAuthSelected", "getInProgress", "getInitializationResult", "()Lcom/daon/fido/sdk/sample/kt/FidoInitializationResult;", "getLoginResult", "()Lcom/daon/fido/sdk/sample/kt/LoginResult;", "getSelectedAuth", "()Lcom/daon/fido/client/sdk/model/Authenticator;", "getUsername", "()Ljava/lang/String;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(ZLcom/daon/fido/sdk/sample/kt/FidoInitializationResult;Lcom/daon/fido/sdk/sample/kt/AccountCreationResult;Z[Lcom/daon/fido/client/sdk/model/Authenticator;ZLcom/daon/fido/client/sdk/model/Authenticator;Lcom/daon/fido/sdk/sample/kt/LoginResult;Ljava/lang/String;)Lcom/daon/fido/sdk/sample/kt/FidoUiState;", "equals", "other", "hashCode", "", "toString", "SampleAppKotlin_debug"})
public final class FidoUiState {
    private final boolean inProgress = false;
    @org.jetbrains.annotations.Nullable()
    private final com.daon.fido.sdk.sample.kt.FidoInitializationResult initializationResult = null;
    @org.jetbrains.annotations.Nullable()
    private final com.daon.fido.sdk.sample.kt.AccountCreationResult accountCreationResult = null;
    private final boolean authArrayAvailable = false;
    @org.jetbrains.annotations.NotNull()
    private final com.daon.fido.client.sdk.model.Authenticator[] authArray = null;
    private final boolean authSelected = false;
    @org.jetbrains.annotations.Nullable()
    private final com.daon.fido.client.sdk.model.Authenticator selectedAuth = null;
    @org.jetbrains.annotations.Nullable()
    private final com.daon.fido.sdk.sample.kt.LoginResult loginResult = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String username = null;
    
    public FidoUiState(boolean inProgress, @org.jetbrains.annotations.Nullable()
    com.daon.fido.sdk.sample.kt.FidoInitializationResult initializationResult, @org.jetbrains.annotations.Nullable()
    com.daon.fido.sdk.sample.kt.AccountCreationResult accountCreationResult, boolean authArrayAvailable, @org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.model.Authenticator[] authArray, boolean authSelected, @org.jetbrains.annotations.Nullable()
    com.daon.fido.client.sdk.model.Authenticator selectedAuth, @org.jetbrains.annotations.Nullable()
    com.daon.fido.sdk.sample.kt.LoginResult loginResult, @org.jetbrains.annotations.Nullable()
    java.lang.String username) {
        super();
    }
    
    public final boolean getInProgress() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.sdk.sample.kt.FidoInitializationResult getInitializationResult() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.sdk.sample.kt.AccountCreationResult getAccountCreationResult() {
        return null;
    }
    
    public final boolean getAuthArrayAvailable() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.daon.fido.client.sdk.model.Authenticator[] getAuthArray() {
        return null;
    }
    
    public final boolean getAuthSelected() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.client.sdk.model.Authenticator getSelectedAuth() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.sdk.sample.kt.LoginResult getLoginResult() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getUsername() {
        return null;
    }
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.sdk.sample.kt.FidoInitializationResult component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.sdk.sample.kt.AccountCreationResult component3() {
        return null;
    }
    
    public final boolean component4() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.daon.fido.client.sdk.model.Authenticator[] component5() {
        return null;
    }
    
    public final boolean component6() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.client.sdk.model.Authenticator component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.sdk.sample.kt.LoginResult component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.daon.fido.sdk.sample.kt.FidoUiState copy(boolean inProgress, @org.jetbrains.annotations.Nullable()
    com.daon.fido.sdk.sample.kt.FidoInitializationResult initializationResult, @org.jetbrains.annotations.Nullable()
    com.daon.fido.sdk.sample.kt.AccountCreationResult accountCreationResult, boolean authArrayAvailable, @org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.model.Authenticator[] authArray, boolean authSelected, @org.jetbrains.annotations.Nullable()
    com.daon.fido.client.sdk.model.Authenticator selectedAuth, @org.jetbrains.annotations.Nullable()
    com.daon.fido.sdk.sample.kt.LoginResult loginResult, @org.jetbrains.annotations.Nullable()
    java.lang.String username) {
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