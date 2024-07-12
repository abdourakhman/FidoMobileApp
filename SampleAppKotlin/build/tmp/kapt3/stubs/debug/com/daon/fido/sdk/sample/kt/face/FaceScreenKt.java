package com.daon.fido.sdk.sample.kt.face;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import com.daon.fido.sdk.sample.kt.R;
import com.google.accompanist.permissions.ExperimentalPermissionsApi;
import java.util.concurrent.Executors;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000@\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\u001a\u001e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a(\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\fH\u0007\u001a\u0016\u0010\r\u001a\u00020\u00012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a\u0018\u0010\u000e\u001a\u00020\u00012\u0006\u0010\u000f\u001a\u00020\b2\u0006\u0010\u0010\u001a\u00020\u0011H\u0007\u001a\u0018\u0010\u0012\u001a\u00020\u00012\u0006\u0010\u0013\u001a\u00020\b2\u0006\u0010\u0014\u001a\u00020\u0015H\u0007\u001a\u001e\u0010\u0016\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a \u0010\u0017\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u0018\u001a\u00020\b2\u0006\u0010\u0010\u001a\u00020\u0011H\u0007\u001a\u0010\u0010\u0019\u001a\u0004\u0018\u00010\u001a2\u0006\u0010\u0010\u001a\u00020\u0011\u00a8\u0006\u001b"}, d2 = {"AuthenticateFaceScreen", "", "previewSize", "Landroid/util/Size;", "onNavigateUp", "Lkotlin/Function0;", "CameraPreview", "analyzing", "", "photoMode", "Lcom/daon/fido/sdk/sample/kt/face/PhotoMode;", "imageAnalyzer", "Lcom/daon/fido/sdk/sample/kt/face/ImageAnalyzer;", "FaceScreen", "PreviewImage", "enablePreview", "viewModel", "Lcom/daon/fido/sdk/sample/kt/face/FaceViewModel;", "QualityIndicator", "isQuality", "modifier", "Landroidx/compose/ui/Modifier;", "RegisterFaceScreen", "TakeRetakeAndEnrollButtonGroup", "takePhotoEnabled", "getPreviewImage", "Landroidx/compose/ui/graphics/ImageBitmap;", "SampleAppKotlin_debug"})
public final class FaceScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void FaceScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateUp) {
    }
    
    @kotlin.OptIn(markerClass = {com.google.accompanist.permissions.ExperimentalPermissionsApi.class})
    @androidx.compose.runtime.Composable()
    public static final void RegisterFaceScreen(@org.jetbrains.annotations.NotNull()
    android.util.Size previewSize, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateUp) {
    }
    
    @kotlin.OptIn(markerClass = {com.google.accompanist.permissions.ExperimentalPermissionsApi.class})
    @androidx.compose.runtime.Composable()
    public static final void AuthenticateFaceScreen(@org.jetbrains.annotations.NotNull()
    android.util.Size previewSize, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateUp) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void PreviewImage(boolean enablePreview, @org.jetbrains.annotations.NotNull()
    com.daon.fido.sdk.sample.kt.face.FaceViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void QualityIndicator(boolean isQuality, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void TakeRetakeAndEnrollButtonGroup(@org.jetbrains.annotations.NotNull()
    com.daon.fido.sdk.sample.kt.face.PhotoMode photoMode, boolean takePhotoEnabled, @org.jetbrains.annotations.NotNull()
    com.daon.fido.sdk.sample.kt.face.FaceViewModel viewModel) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public static final androidx.compose.ui.graphics.ImageBitmap getPreviewImage(@org.jetbrains.annotations.NotNull()
    com.daon.fido.sdk.sample.kt.face.FaceViewModel viewModel) {
        return null;
    }
    
    @androidx.compose.runtime.Composable()
    public static final void CameraPreview(boolean analyzing, @org.jetbrains.annotations.NotNull()
    com.daon.fido.sdk.sample.kt.face.PhotoMode photoMode, @org.jetbrains.annotations.NotNull()
    android.util.Size previewSize, @org.jetbrains.annotations.NotNull()
    com.daon.fido.sdk.sample.kt.face.ImageAnalyzer imageAnalyzer) {
    }
}