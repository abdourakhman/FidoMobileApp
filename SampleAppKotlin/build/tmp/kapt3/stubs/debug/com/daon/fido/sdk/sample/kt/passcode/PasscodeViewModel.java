package com.daon.fido.sdk.sample.kt.passcode;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.lifecycle.ViewModel;
import com.daon.fido.client.sdk.IXUAF;
import com.daon.fido.sdk.sample.kt.BaseViewModel;
import com.daon.fido.sdk.sample.kt.R;
import com.daon.sdk.authenticator.Authenticator;
import com.daon.sdk.authenticator.controller.CaptureCompleteListener;
import com.daon.sdk.authenticator.controller.CaptureCompleteResult;
import com.daon.sdk.authenticator.controller.ControllerConfiguration;
import com.daon.sdk.authenticator.controller.PasscodeControllerProtocol;
import com.daon.sdk.authenticator.exception.ControllerInitializationException;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.Dispatchers;
import java.lang.Exception;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u000e\b\u0007\u0018\u00002\u00020\u0001:\u0001=B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000e\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020\u000eJ\u0006\u0010-\u001a\u00020+J\u0010\u0010.\u001a\u00020\u000e2\u0006\u0010/\u001a\u000200H\u0002J\u0006\u00101\u001a\u00020+J\u0006\u00102\u001a\u00020+J\u0006\u00103\u001a\u00020+J\u000e\u00104\u001a\u00020+2\u0006\u0010,\u001a\u00020\u000eJ\u0006\u00105\u001a\u00020+J\u0006\u00106\u001a\u00020+J\u0006\u00107\u001a\u00020+J\u0006\u00108\u001a\u00020+J\u0006\u00109\u001a\u00020+J\u0016\u0010:\u001a\u00020+2\u0006\u0010;\u001a\u00020\u000e2\u0006\u0010<\u001a\u00020\u000eR\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0017\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0017R\u0017\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0017R\u001a\u0010\u001c\u001a\u00020\u001dX\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001e\u0010\u001f\"\u0004\b \u0010!R\u0017\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u0017R\u0017\u0010$\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u0017R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010&\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u0017R\u0017\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u0017R\u0017\u0010(\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u0017R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006>"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/passcode/PasscodeViewModel;", "Lcom/daon/fido/sdk/sample/kt/BaseViewModel;", "application", "Landroid/app/Application;", "fido", "Lcom/daon/fido/client/sdk/IXUAF;", "prefs", "Landroid/content/SharedPreferences;", "(Landroid/app/Application;Lcom/daon/fido/client/sdk/IXUAF;Landroid/content/SharedPreferences;)V", "_captureComplete", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_captureCompleteWithError", "_captureInfo", "", "_controllerInstantiated", "_enableRetry", "_isEnrol", "_isVerifyAndEnroll", "_onControllerInstantiateError", "captureComplete", "Lkotlinx/coroutines/flow/StateFlow;", "getCaptureComplete", "()Lkotlinx/coroutines/flow/StateFlow;", "captureCompleteWithError", "getCaptureCompleteWithError", "captureInfo", "getCaptureInfo", "controller", "Lcom/daon/sdk/authenticator/controller/PasscodeControllerProtocol;", "getController", "()Lcom/daon/sdk/authenticator/controller/PasscodeControllerProtocol;", "setController", "(Lcom/daon/sdk/authenticator/controller/PasscodeControllerProtocol;)V", "controllerInstantiated", "getControllerInstantiated", "enableRetry", "getEnableRetry", "isEnrol", "isVerifyAndEnroll", "onControllerInstantiateError", "getOnControllerInstantiateError", "authenticate", "", "value", "cancelCurrentOperation", "getRetriesRemainingMessage", "result", "Lcom/daon/sdk/authenticator/controller/CaptureCompleteResult;", "intializeController", "onStart", "onStop", "register", "resetCaptureComplete", "resetCaptureCompleteWithError", "resetCaptureInfo", "resetControllerInstantiatedError", "resetEnableRetry", "verifyAndReenrol", "currentPasscode", "newPasscode", "DefaultCaptureCompleteListener", "SampleAppKotlin_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class PasscodeViewModel extends com.daon.fido.sdk.sample.kt.BaseViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.daon.fido.client.sdk.IXUAF fido = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    public com.daon.sdk.authenticator.controller.PasscodeControllerProtocol controller;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _captureComplete = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> captureComplete = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _captureCompleteWithError = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> captureCompleteWithError = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _captureInfo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> captureInfo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _enableRetry = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> enableRetry = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isEnrol = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isEnrol = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isVerifyAndEnroll = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isVerifyAndEnroll = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _controllerInstantiated = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> controllerInstantiated = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _onControllerInstantiateError = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> onControllerInstantiateError = null;
    
    @javax.inject.Inject()
    public PasscodeViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application, @org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.IXUAF fido, @org.jetbrains.annotations.NotNull()
    android.content.SharedPreferences prefs) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.daon.sdk.authenticator.controller.PasscodeControllerProtocol getController() {
        return null;
    }
    
    public final void setController(@org.jetbrains.annotations.NotNull()
    com.daon.sdk.authenticator.controller.PasscodeControllerProtocol p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getCaptureComplete() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getCaptureCompleteWithError() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getCaptureInfo() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getEnableRetry() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isEnrol() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isVerifyAndEnroll() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getControllerInstantiated() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getOnControllerInstantiateError() {
        return null;
    }
    
    public final void intializeController() {
    }
    
    public final void onStart() {
    }
    
    public final void cancelCurrentOperation() {
    }
    
    public final void onStop() {
    }
    
    public final void register(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final void authenticate(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final void verifyAndReenrol(@org.jetbrains.annotations.NotNull()
    java.lang.String currentPasscode, @org.jetbrains.annotations.NotNull()
    java.lang.String newPasscode) {
    }
    
    public final void resetCaptureComplete() {
    }
    
    public final void resetCaptureCompleteWithError() {
    }
    
    public final void resetCaptureInfo() {
    }
    
    public final void resetEnableRetry() {
    }
    
    public final void resetControllerInstantiatedError() {
    }
    
    private final java.lang.String getRetriesRemainingMessage(com.daon.sdk.authenticator.controller.CaptureCompleteResult result) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0016\u00a8\u0006\u0007"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/passcode/PasscodeViewModel$DefaultCaptureCompleteListener;", "Lcom/daon/sdk/authenticator/controller/CaptureCompleteListener;", "(Lcom/daon/fido/sdk/sample/kt/passcode/PasscodeViewModel;)V", "onCaptureComplete", "", "result", "Lcom/daon/sdk/authenticator/controller/CaptureCompleteResult;", "SampleAppKotlin_debug"})
    public final class DefaultCaptureCompleteListener implements com.daon.sdk.authenticator.controller.CaptureCompleteListener {
        
        public DefaultCaptureCompleteListener() {
            super();
        }
        
        @java.lang.Override()
        public void onCaptureComplete(@org.jetbrains.annotations.NotNull()
        com.daon.sdk.authenticator.controller.CaptureCompleteResult result) {
        }
    }
}