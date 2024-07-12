package com.daon.fido.sdk.sample.kt.fingerprint;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.daon.fido.client.sdk.IXUAF;
import com.daon.fido.sdk.sample.kt.BaseViewModel;
import com.daon.fido.sdk.sample.kt.R;
import com.daon.sdk.authenticator.controller.CaptureCompleteResult;
import com.daon.sdk.authenticator.controller.FingerprintCaptureControllerProtocol;
import com.daon.sdk.authenticator.exception.ControllerInitializationException;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000e\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020\u001bJ\u0006\u0010#\u001a\u00020!J\u0006\u0010$\u001a\u00020!J\u0006\u0010%\u001a\u00020!J\u000e\u0010&\u001a\u0004\u0018\u00010\'*\u00020\u001bH\u0002R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000b0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\r0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0011R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0014\u001a\u00020\u0015X\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u0017\"\u0004\b\u0018\u0010\u0019R\u001a\u0010\u001a\u001a\u00020\u001bX\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001c\u0010\u001d\"\u0004\b\u001e\u0010\u001fR\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006("}, d2 = {"Lcom/daon/fido/sdk/sample/kt/fingerprint/FingerprintViewModel;", "Lcom/daon/fido/sdk/sample/kt/BaseViewModel;", "application", "Landroid/app/Application;", "fido", "Lcom/daon/fido/client/sdk/IXUAF;", "prefs", "Landroid/content/SharedPreferences;", "(Landroid/app/Application;Lcom/daon/fido/client/sdk/IXUAF;Landroid/content/SharedPreferences;)V", "_captureComplete", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_captureInfo", "", "captureComplete", "Lkotlinx/coroutines/flow/StateFlow;", "getCaptureComplete", "()Lkotlinx/coroutines/flow/StateFlow;", "captureInfo", "getCaptureInfo", "fingerController", "Lcom/daon/sdk/authenticator/controller/FingerprintCaptureControllerProtocol;", "getFingerController", "()Lcom/daon/sdk/authenticator/controller/FingerprintCaptureControllerProtocol;", "setFingerController", "(Lcom/daon/sdk/authenticator/controller/FingerprintCaptureControllerProtocol;)V", "localContext", "Landroid/content/Context;", "getLocalContext", "()Landroid/content/Context;", "setLocalContext", "(Landroid/content/Context;)V", "onStart", "", "context", "onStop", "resetCaptureComplete", "resetCaptureInfo", "findActivity", "Landroidx/appcompat/app/AppCompatActivity;", "SampleAppKotlin_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class FingerprintViewModel extends com.daon.fido.sdk.sample.kt.BaseViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.daon.fido.client.sdk.IXUAF fido = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    public com.daon.sdk.authenticator.controller.FingerprintCaptureControllerProtocol fingerController;
    public android.content.Context localContext;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _captureComplete = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> captureComplete = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _captureInfo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> captureInfo = null;
    
    @javax.inject.Inject()
    public FingerprintViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application, @org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.IXUAF fido, @org.jetbrains.annotations.NotNull()
    android.content.SharedPreferences prefs) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.daon.sdk.authenticator.controller.FingerprintCaptureControllerProtocol getFingerController() {
        return null;
    }
    
    public final void setFingerController(@org.jetbrains.annotations.NotNull()
    com.daon.sdk.authenticator.controller.FingerprintCaptureControllerProtocol p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.content.Context getLocalContext() {
        return null;
    }
    
    public final void setLocalContext(@org.jetbrains.annotations.NotNull()
    android.content.Context p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getCaptureComplete() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getCaptureInfo() {
        return null;
    }
    
    public final void onStart(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void onStop() {
    }
    
    private final androidx.appcompat.app.AppCompatActivity findActivity(android.content.Context $this$findActivity) {
        return null;
    }
    
    public final void resetCaptureComplete() {
    }
    
    public final void resetCaptureInfo() {
    }
}