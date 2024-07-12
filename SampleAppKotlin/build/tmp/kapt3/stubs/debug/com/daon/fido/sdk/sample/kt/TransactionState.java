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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b)\b\u0086\b\u0018\u00002\u00020\u0001B\u007f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\b\u0010\b\u001a\u0004\u0018\u00010\u0006\u0012\u0006\u0010\t\u001a\u00020\u0003\u0012\u0006\u0010\n\u001a\u00020\u0003\u0012\b\u0010\u000b\u001a\u0004\u0018\u00010\f\u0012\b\b\u0002\u0010\r\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000f\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0011\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\f\u00a2\u0006\u0002\u0010\u0014J\t\u0010(\u001a\u00020\u0003H\u00c6\u0003J\t\u0010)\u001a\u00020\u0011H\u00c6\u0003J\t\u0010*\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010+\u001a\u0004\u0018\u00010\fH\u00c6\u0003J\u0014\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0016J\t\u0010-\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010.\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\t\u0010/\u001a\u00020\u0003H\u00c6\u0003J\t\u00100\u001a\u00020\u0003H\u00c6\u0003J\u000b\u00101\u001a\u0004\u0018\u00010\fH\u00c6\u0003J\t\u00102\u001a\u00020\u0003H\u00c6\u0003J\u000b\u00103\u001a\u0004\u0018\u00010\u000fH\u00c6\u0003J\u0094\u0001\u00104\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00032\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00062\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u00032\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f2\b\b\u0002\u0010\r\u001a\u00020\u00032\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0012\u001a\u00020\u00032\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\fH\u00c6\u0001\u00a2\u0006\u0002\u00105J\u0013\u00106\u001a\u00020\u00032\b\u00107\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00108\u001a\u00020\u0011H\u00d6\u0001J\t\u00109\u001a\u00020\fH\u00d6\u0001R\u0019\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\n\n\u0002\u0010\u0017\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0019R\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0019R\u0011\u0010\r\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0019R\u0013\u0010\u0013\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001eR\u0011\u0010\u0012\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0019R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001eR\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0019R\u0013\u0010\b\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0011\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010%R\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\'\u00a8\u0006:"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/TransactionState;", "", "authArrayAvailable", "", "authArray", "", "Lcom/daon/fido/client/sdk/model/Authenticator;", "authSelected", "selectedAuth", "authenticationCompleted", "resetComplete", "message", "", "confirmTransaction", "transactionContent", "Lcom/daon/fido/client/sdk/transaction/TransactionContent;", "transactionConfirmationResult", "", "confirmationOTPReceived", "confirmationOTP", "(Z[Lcom/daon/fido/client/sdk/model/Authenticator;ZLcom/daon/fido/client/sdk/model/Authenticator;ZZLjava/lang/String;ZLcom/daon/fido/client/sdk/transaction/TransactionContent;IZLjava/lang/String;)V", "getAuthArray", "()[Lcom/daon/fido/client/sdk/model/Authenticator;", "[Lcom/daon/fido/client/sdk/model/Authenticator;", "getAuthArrayAvailable", "()Z", "getAuthSelected", "getAuthenticationCompleted", "getConfirmTransaction", "getConfirmationOTP", "()Ljava/lang/String;", "getConfirmationOTPReceived", "getMessage", "getResetComplete", "getSelectedAuth", "()Lcom/daon/fido/client/sdk/model/Authenticator;", "getTransactionConfirmationResult", "()I", "getTransactionContent", "()Lcom/daon/fido/client/sdk/transaction/TransactionContent;", "component1", "component10", "component11", "component12", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Z[Lcom/daon/fido/client/sdk/model/Authenticator;ZLcom/daon/fido/client/sdk/model/Authenticator;ZZLjava/lang/String;ZLcom/daon/fido/client/sdk/transaction/TransactionContent;IZLjava/lang/String;)Lcom/daon/fido/sdk/sample/kt/TransactionState;", "equals", "other", "hashCode", "toString", "SampleAppKotlin_debug"})
public final class TransactionState {
    private final boolean authArrayAvailable = false;
    @org.jetbrains.annotations.NotNull()
    private final com.daon.fido.client.sdk.model.Authenticator[] authArray = null;
    private final boolean authSelected = false;
    @org.jetbrains.annotations.Nullable()
    private final com.daon.fido.client.sdk.model.Authenticator selectedAuth = null;
    private final boolean authenticationCompleted = false;
    private final boolean resetComplete = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String message = null;
    private final boolean confirmTransaction = false;
    @org.jetbrains.annotations.Nullable()
    private final com.daon.fido.client.sdk.transaction.TransactionContent transactionContent = null;
    private final int transactionConfirmationResult = 0;
    private final boolean confirmationOTPReceived = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String confirmationOTP = null;
    
    public TransactionState(boolean authArrayAvailable, @org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.model.Authenticator[] authArray, boolean authSelected, @org.jetbrains.annotations.Nullable()
    com.daon.fido.client.sdk.model.Authenticator selectedAuth, boolean authenticationCompleted, boolean resetComplete, @org.jetbrains.annotations.Nullable()
    java.lang.String message, boolean confirmTransaction, @org.jetbrains.annotations.Nullable()
    com.daon.fido.client.sdk.transaction.TransactionContent transactionContent, int transactionConfirmationResult, boolean confirmationOTPReceived, @org.jetbrains.annotations.Nullable()
    java.lang.String confirmationOTP) {
        super();
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
    
    public final boolean getAuthenticationCompleted() {
        return false;
    }
    
    public final boolean getResetComplete() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getMessage() {
        return null;
    }
    
    public final boolean getConfirmTransaction() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.client.sdk.transaction.TransactionContent getTransactionContent() {
        return null;
    }
    
    public final int getTransactionConfirmationResult() {
        return 0;
    }
    
    public final boolean getConfirmationOTPReceived() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getConfirmationOTP() {
        return null;
    }
    
    public final boolean component1() {
        return false;
    }
    
    public final int component10() {
        return 0;
    }
    
    public final boolean component11() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component12() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.daon.fido.client.sdk.model.Authenticator[] component2() {
        return null;
    }
    
    public final boolean component3() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.client.sdk.model.Authenticator component4() {
        return null;
    }
    
    public final boolean component5() {
        return false;
    }
    
    public final boolean component6() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component7() {
        return null;
    }
    
    public final boolean component8() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.daon.fido.client.sdk.transaction.TransactionContent component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.daon.fido.sdk.sample.kt.TransactionState copy(boolean authArrayAvailable, @org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.model.Authenticator[] authArray, boolean authSelected, @org.jetbrains.annotations.Nullable()
    com.daon.fido.client.sdk.model.Authenticator selectedAuth, boolean authenticationCompleted, boolean resetComplete, @org.jetbrains.annotations.Nullable()
    java.lang.String message, boolean confirmTransaction, @org.jetbrains.annotations.Nullable()
    com.daon.fido.client.sdk.transaction.TransactionContent transactionContent, int transactionConfirmationResult, boolean confirmationOTPReceived, @org.jetbrains.annotations.Nullable()
    java.lang.String confirmationOTP) {
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