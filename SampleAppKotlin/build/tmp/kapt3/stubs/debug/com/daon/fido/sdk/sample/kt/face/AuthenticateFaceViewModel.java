package com.daon.fido.sdk.sample.kt.face;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import com.daon.fido.client.sdk.IXUAF;
import com.daon.fido.sdk.sample.kt.R;
import com.daon.sdk.authenticator.Authenticator;
import com.daon.sdk.authenticator.ErrorCodes;
import com.daon.sdk.authenticator.controller.AuthenticatorError;
import com.daon.sdk.authenticator.controller.LockResult;
import com.daon.sdk.faceauthenticator.FaceErrorCodes;
import com.daon.sdk.faceauthenticator.YUVTools;
import com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u001c\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\b\u0010\r\u001a\u0004\u0018\u00010\u000eH\u0016J\u0012\u0010\u000f\u001a\u00020\n2\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u0016J\b\u0010\u0012\u001a\u00020\nH\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/face/AuthenticateFaceViewModel;", "Lcom/daon/fido/sdk/sample/kt/face/FaceViewModel;", "application", "Landroid/app/Application;", "fido", "Lcom/daon/fido/client/sdk/IXUAF;", "prefs", "Landroid/content/SharedPreferences;", "(Landroid/app/Application;Lcom/daon/fido/client/sdk/IXUAF;Landroid/content/SharedPreferences;)V", "onFailure", "", "error", "Lcom/daon/sdk/authenticator/controller/AuthenticatorError;", "result", "Lcom/daon/sdk/authenticator/controller/LockResult;", "onLivenessEvent", "info", "Lcom/daon/sdk/faceauthenticator/controller/FaceControllerProtocol$LivenessEventInfo;", "onRecapture", "SampleAppKotlin_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class AuthenticateFaceViewModel extends com.daon.fido.sdk.sample.kt.face.FaceViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.daon.fido.client.sdk.IXUAF fido = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    
    @javax.inject.Inject()
    public AuthenticateFaceViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application, @org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.IXUAF fido, @org.jetbrains.annotations.NotNull()
    android.content.SharedPreferences prefs) {
        super(null, null, null);
    }
    
    @java.lang.Override()
    public void onLivenessEvent(@org.jetbrains.annotations.Nullable()
    com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol.LivenessEventInfo info) {
    }
    
    @java.lang.Override()
    public void onFailure(@org.jetbrains.annotations.Nullable()
    com.daon.sdk.authenticator.controller.AuthenticatorError error, @org.jetbrains.annotations.Nullable()
    com.daon.sdk.authenticator.controller.LockResult result) {
    }
    
    @java.lang.Override()
    public void onRecapture() {
    }
}