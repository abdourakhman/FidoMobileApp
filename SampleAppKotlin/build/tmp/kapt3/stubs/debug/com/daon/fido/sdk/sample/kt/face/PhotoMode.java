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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0005\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/face/PhotoMode;", "", "(Ljava/lang/String;I)V", "DETECT", "TAKE", "RETAKE", "SampleAppKotlin_debug"})
public enum PhotoMode {
    /*public static final*/ DETECT /* = new DETECT() */,
    /*public static final*/ TAKE /* = new TAKE() */,
    /*public static final*/ RETAKE /* = new RETAKE() */;
    
    PhotoMode() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.daon.fido.sdk.sample.kt.face.PhotoMode> getEntries() {
        return null;
    }
}