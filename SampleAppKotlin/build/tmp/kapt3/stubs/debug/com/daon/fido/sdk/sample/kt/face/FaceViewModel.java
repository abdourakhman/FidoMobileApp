package com.daon.fido.sdk.sample.kt.face;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android.util.Size;
import androidx.camera.core.ImageProxy;
import com.daon.fido.client.sdk.IXUAF;
import com.daon.fido.sdk.sample.kt.BaseViewModel;
import com.daon.fido.sdk.sample.kt.R;
import com.daon.sdk.authenticator.Authenticator;
import com.daon.sdk.authenticator.ErrorCodes;
import com.daon.sdk.authenticator.controller.AuthenticatorError;
import com.daon.sdk.authenticator.controller.CaptureCompleteResult;
import com.daon.sdk.authenticator.controller.LockResult;
import com.daon.sdk.authenticator.exception.ControllerInitializationException;
import com.daon.sdk.face.LivenessResult;
import com.daon.sdk.face.Result;
import com.daon.sdk.face.YUV;
import com.daon.sdk.faceauthenticator.FaceErrorCodes;
import com.daon.sdk.faceauthenticator.YUVTools;
import com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00ae\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u001b\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0017\u0018\u00002\u00020\u00012\u00020\u0002B\u001f\b\u0007\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b\u00a2\u0006\u0002\u0010\tJ\u0010\u0010V\u001a\u00020W2\u0006\u0010X\u001a\u00020YH\u0007J\u0006\u0010Z\u001a\u00020WJ\u0010\u0010[\u001a\u00020\f2\u0006\u0010\\\u001a\u00020]H\u0002J\u0006\u0010^\u001a\u00020WJ\u000e\u0010_\u001a\u00020\'2\u0006\u0010`\u001a\u00020\'J\u0006\u0010a\u001a\u00020WJ\b\u0010b\u001a\u0004\u0018\u00010cJ\u0010\u0010d\u001a\u00020\u001a2\u0006\u0010\\\u001a\u00020eH\u0002J\u0010\u0010f\u001a\u00020W2\u0006\u0010`\u001a\u00020\'H\u0016J\u001c\u0010g\u001a\u00020W2\b\u0010h\u001a\u0004\u0018\u00010i2\b\u0010\\\u001a\u0004\u0018\u00010jH\u0016J$\u0010k\u001a\u00020W2\b\u0010l\u001a\u0004\u0018\u0001052\b\u0010\\\u001a\u0004\u0018\u00010]2\u0006\u0010m\u001a\u00020\fH\u0016J\u0012\u0010n\u001a\u00020W2\b\u0010o\u001a\u0004\u0018\u00010pH\u0016J\b\u0010q\u001a\u00020WH\u0016J\b\u0010r\u001a\u00020WH\u0016J\u0006\u0010s\u001a\u00020WJ\u0006\u0010t\u001a\u00020WJ\u0006\u0010u\u001a\u00020WJ\u0006\u0010v\u001a\u00020WJ\u0012\u0010w\u001a\u0004\u0018\u00010x2\u0006\u0010y\u001a\u00020xH\u0002J\u0006\u0010z\u001a\u00020WJ\u0006\u0010{\u001a\u00020WJ\u0006\u0010|\u001a\u00020WJ\u0010\u0010}\u001a\u00020W2\u0006\u0010\\\u001a\u00020]H\u0002R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\r\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u001a\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000fR\u001a\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\u000bX\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u000fR \u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0084\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u000f\"\u0004\b\u0017\u0010\u0018R\u001a\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u001a0\u000bX\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u000fR\u0014\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u001a0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020 0\u000bX\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u000fR\u0014\u0010\"\u001a\b\u0012\u0004\u0012\u00020#0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010$\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u000fR\u001a\u0010&\u001a\b\u0012\u0004\u0012\u00020\'0\u000bX\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\u000fR\u001a\u0010)\u001a\b\u0012\u0004\u0012\u00020\u001a0\u000bX\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\u000fR\u001a\u0010+\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010\u000fR\u0014\u0010-\u001a\b\u0012\u0004\u0012\u00020\u001a0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010.\u001a\b\u0012\u0004\u0012\u00020\f0/\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u00101R\u0017\u00102\u001a\b\u0012\u0004\u0012\u00020\f0/\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u00101R\u0010\u00104\u001a\u0004\u0018\u000105X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u00106\u001a\b\u0012\u0004\u0012\u00020\f0/\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u00101R\u0017\u00108\u001a\b\u0012\u0004\u0012\u00020\u00130/\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u00101R\u001a\u0010:\u001a\u00020;X\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b<\u0010=\"\u0004\b>\u0010?R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010@\u001a\b\u0012\u0004\u0012\u00020\f0/\u00a2\u0006\b\n\u0000\u001a\u0004\bA\u00101R\u0017\u0010B\u001a\b\u0012\u0004\u0012\u00020\u001a0/\u00a2\u0006\b\n\u0000\u001a\u0004\bC\u00101R\u0017\u0010D\u001a\b\u0012\u0004\u0012\u00020\f0/\u00a2\u0006\b\n\u0000\u001a\u0004\bD\u00101R\u0017\u0010E\u001a\b\u0012\u0004\u0012\u00020\f0/\u00a2\u0006\b\n\u0000\u001a\u0004\bE\u00101R\u0017\u0010F\u001a\b\u0012\u0004\u0012\u00020\u001a0/\u00a2\u0006\b\n\u0000\u001a\u0004\bG\u00101R\u0017\u0010H\u001a\b\u0012\u0004\u0012\u00020 0/\u00a2\u0006\b\n\u0000\u001a\u0004\bI\u00101R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010J\u001a\b\u0012\u0004\u0012\u00020#0/\u00a2\u0006\b\n\u0000\u001a\u0004\bK\u00101R\u0017\u0010L\u001a\b\u0012\u0004\u0012\u00020\f0/\u00a2\u0006\b\n\u0000\u001a\u0004\bM\u00101R\u0017\u0010N\u001a\b\u0012\u0004\u0012\u00020\'0/\u00a2\u0006\b\n\u0000\u001a\u0004\bO\u00101R\u0017\u0010P\u001a\b\u0012\u0004\u0012\u00020\u001a0/\u00a2\u0006\b\n\u0000\u001a\u0004\bQ\u00101R\u0017\u0010R\u001a\b\u0012\u0004\u0012\u00020\f0/\u00a2\u0006\b\n\u0000\u001a\u0004\bS\u00101R\u0017\u0010T\u001a\b\u0012\u0004\u0012\u00020\u001a0/\u00a2\u0006\b\n\u0000\u001a\u0004\bU\u00101\u00a8\u0006~"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/face/FaceViewModel;", "Lcom/daon/fido/sdk/sample/kt/BaseViewModel;", "Lcom/daon/sdk/faceauthenticator/controller/FaceControllerProtocol$FaceAnalysisListener;", "application", "Landroid/app/Application;", "fido", "Lcom/daon/fido/client/sdk/IXUAF;", "prefs", "Landroid/content/SharedPreferences;", "(Landroid/app/Application;Lcom/daon/fido/client/sdk/IXUAF;Landroid/content/SharedPreferences;)V", "_authStateProcessed", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_captureComplete", "get_captureComplete", "()Lkotlinx/coroutines/flow/MutableStateFlow;", "_enablePreview", "get_enablePreview", "_faceCaptureState", "Lcom/daon/fido/sdk/sample/kt/face/FaceCaptureState;", "get_faceCaptureState", "_inProgress", "get_inProgress", "set_inProgress", "(Lkotlinx/coroutines/flow/MutableStateFlow;)V", "_instructions", "", "get_instructions", "_isEnrol", "_isQuality", "_maskWarnings", "_photoMode", "Lcom/daon/fido/sdk/sample/kt/face/PhotoMode;", "get_photoMode", "_previewSize", "Landroid/util/Size;", "_processing", "get_processing", "_rotation", "", "get_rotation", "_status", "get_status", "_takePhotoEnabled", "get_takePhotoEnabled", "_warnings", "authStateProcessed", "Lkotlinx/coroutines/flow/StateFlow;", "getAuthStateProcessed", "()Lkotlinx/coroutines/flow/StateFlow;", "captureComplete", "getCaptureComplete", "capturedImage", "Lcom/daon/sdk/face/YUV;", "enablePreview", "getEnablePreview", "faceCaptureState", "getFaceCaptureState", "faceController", "Lcom/daon/sdk/faceauthenticator/controller/FaceControllerProtocol;", "getFaceController", "()Lcom/daon/sdk/faceauthenticator/controller/FaceControllerProtocol;", "setFaceController", "(Lcom/daon/sdk/faceauthenticator/controller/FaceControllerProtocol;)V", "inProgress", "getInProgress", "instructions", "getInstructions", "isEnrol", "isQuality", "maskWarnings", "getMaskWarnings", "photoMode", "getPhotoMode", "previewSize", "getPreviewSize", "processing", "getProcessing", "rotation", "getRotation", "status", "getStatus", "takePhotoEnabled", "getTakePhotoEnabled", "warnings", "getWarnings", "analyzeImage", "", "image", "Landroidx/camera/core/ImageProxy;", "cancelCurrentOperation", "checkAndUpdateQualityInfo", "result", "Lcom/daon/sdk/face/Result;", "enroll", "getAlertMessage", "alert", "getAuthenticationMode", "getCapturedImage", "Landroidx/compose/ui/graphics/ImageBitmap;", "getRetriesRemainingMessage", "Lcom/daon/sdk/authenticator/controller/CaptureCompleteResult;", "onAlert", "onFailure", "error", "Lcom/daon/sdk/authenticator/controller/AuthenticatorError;", "Lcom/daon/sdk/authenticator/controller/LockResult;", "onImageAnalyzed", "yuv", "isQualityImage", "onLivenessEvent", "info", "Lcom/daon/sdk/faceauthenticator/controller/FaceControllerProtocol$LivenessEventInfo;", "onRecapture", "onStart", "onStop", "resetCaptureComplete", "resetFaceCaptureState", "retakePhoto", "rotate", "Landroid/graphics/Bitmap;", "bmp", "startFaceCapture", "stopFaceCapture", "takePhoto", "updateInfo", "SampleAppKotlin_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public class FaceViewModel extends com.daon.fido.sdk.sample.kt.BaseViewModel implements com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol.FaceAnalysisListener {
    @org.jetbrains.annotations.NotNull()
    private final com.daon.fido.client.sdk.IXUAF fido = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    public com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol faceController;
    @org.jetbrains.annotations.Nullable()
    private com.daon.sdk.face.YUV capturedImage;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _captureComplete = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> captureComplete = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.daon.fido.sdk.sample.kt.face.FaceCaptureState> _faceCaptureState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.daon.fido.sdk.sample.kt.face.FaceCaptureState> faceCaptureState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isQuality = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isQuality = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _instructions = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> instructions = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _status = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> status = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _warnings = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> warnings = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _maskWarnings = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> maskWarnings = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _rotation = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> rotation = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.daon.fido.sdk.sample.kt.face.PhotoMode> _photoMode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.daon.fido.sdk.sample.kt.face.PhotoMode> photoMode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _enablePreview = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> enablePreview = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _takePhotoEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> takePhotoEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _inProgress;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> inProgress = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _processing = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> processing = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isEnrol = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isEnrol = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<android.util.Size> _previewSize = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<android.util.Size> previewSize = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _authStateProcessed = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> authStateProcessed = null;
    
    @javax.inject.Inject()
    public FaceViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application, @org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.IXUAF fido, @org.jetbrains.annotations.NotNull()
    android.content.SharedPreferences prefs) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol getFaceController() {
        return null;
    }
    
    public final void setFaceController(@org.jetbrains.annotations.NotNull()
    com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> get_captureComplete() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getCaptureComplete() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final kotlinx.coroutines.flow.MutableStateFlow<com.daon.fido.sdk.sample.kt.face.FaceCaptureState> get_faceCaptureState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.daon.fido.sdk.sample.kt.face.FaceCaptureState> getFaceCaptureState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isQuality() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> get_instructions() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getInstructions() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> get_status() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getWarnings() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getMaskWarnings() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> get_rotation() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getRotation() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final kotlinx.coroutines.flow.MutableStateFlow<com.daon.fido.sdk.sample.kt.face.PhotoMode> get_photoMode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.daon.fido.sdk.sample.kt.face.PhotoMode> getPhotoMode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> get_enablePreview() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getEnablePreview() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> get_takePhotoEnabled() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getTakePhotoEnabled() {
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
    
    @org.jetbrains.annotations.NotNull()
    protected final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> get_processing() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getProcessing() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isEnrol() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<android.util.Size> getPreviewSize() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getAuthStateProcessed() {
        return null;
    }
    
    public final void getAuthenticationMode() {
    }
    
    public void onStart() {
    }
    
    public final void onStop() {
    }
    
    public final void cancelCurrentOperation() {
    }
    
    @androidx.camera.core.ExperimentalGetImage()
    public final void analyzeImage(@org.jetbrains.annotations.NotNull()
    androidx.camera.core.ImageProxy image) {
    }
    
    public final void startFaceCapture() {
    }
    
    private final java.lang.String getRetriesRemainingMessage(com.daon.sdk.authenticator.controller.CaptureCompleteResult result) {
        return null;
    }
    
    public final void resetCaptureComplete() {
    }
    
    public final void resetFaceCaptureState() {
    }
    
    public final void stopFaceCapture() {
    }
    
    @java.lang.Override()
    public void onImageAnalyzed(@org.jetbrains.annotations.Nullable()
    com.daon.sdk.face.YUV yuv, @org.jetbrains.annotations.Nullable()
    com.daon.sdk.face.Result result, boolean isQualityImage) {
    }
    
    private final boolean checkAndUpdateQualityInfo(com.daon.sdk.face.Result result) {
        return false;
    }
    
    private final void updateInfo(com.daon.sdk.face.Result result) {
    }
    
    public final int getAlertMessage(int alert) {
        return 0;
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
    
    public void onRecapture() {
    }
    
    @java.lang.Override()
    public void onAlert(int alert) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final androidx.compose.ui.graphics.ImageBitmap getCapturedImage() {
        return null;
    }
    
    private final android.graphics.Bitmap rotate(android.graphics.Bitmap bmp) {
        return null;
    }
    
    public final void takePhoto() {
    }
    
    public final void retakePhoto() {
    }
    
    public final void enroll() {
    }
}