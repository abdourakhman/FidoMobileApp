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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0017R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/face/ImageAnalyzer;", "Landroidx/camera/core/ImageAnalysis$Analyzer;", "viewModel", "Lcom/daon/fido/sdk/sample/kt/face/FaceViewModel;", "(Lcom/daon/fido/sdk/sample/kt/face/FaceViewModel;)V", "analyze", "", "image", "Landroidx/camera/core/ImageProxy;", "SampleAppKotlin_debug"})
public final class ImageAnalyzer implements androidx.camera.core.ImageAnalysis.Analyzer {
    @org.jetbrains.annotations.NotNull()
    private final com.daon.fido.sdk.sample.kt.face.FaceViewModel viewModel = null;
    
    public ImageAnalyzer(@org.jetbrains.annotations.NotNull()
    com.daon.fido.sdk.sample.kt.face.FaceViewModel viewModel) {
        super();
    }
    
    @java.lang.Override()
    @androidx.camera.core.ExperimentalGetImage()
    public void analyze(@org.jetbrains.annotations.NotNull()
    androidx.camera.core.ImageProxy image) {
    }
}