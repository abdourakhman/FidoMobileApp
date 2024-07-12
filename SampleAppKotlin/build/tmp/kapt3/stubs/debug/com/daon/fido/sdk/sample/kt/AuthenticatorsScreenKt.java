package com.daon.fido.sdk.sample.kt;

import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.ButtonDefaults;
import androidx.compose.runtime.Composable;
import androidx.compose.runtime.snapshots.SnapshotStateList;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.TextStyle;
import androidx.lifecycle.ViewModel;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.sdk.sample.kt.model.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000R\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0000\u001a<\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0012\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00010\u000bH\u0007\u001a\u0018\u0010\f\u001a\u00020\u00012\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u0007H\u0007\u001a`\u0010\u000e\u001a\u00020\u00012\u001e\u0010\u000f\u001a\u001a\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00010\u0011\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00010\u00102\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u00112\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\u00112\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00010\u00112\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00010\u0011H\u0007\u001a\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u00182\u0006\u0010\u0019\u001a\u00020\u001a\u001a$\u0010\u001b\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u001c*\b\u0012\u0004\u0012\u0002H\u001c0\u001d2\f\u0010\u001e\u001a\b\u0012\u0004\u0012\u0002H\u001c0\u001f\u00a8\u0006 "}, d2 = {"AuthenticatorInfoCard", "", "authenticator", "Lcom/daon/fido/client/sdk/model/Authenticator;", "viewModel", "Lcom/daon/fido/sdk/sample/kt/AuthenticatorsViewModel;", "index", "", "selected", "", "onItemClick", "Lkotlin/Function1;", "AuthenticatorList", "selectedIndex", "AuthenticatorsScreen", "onNavigateToChooseAuth", "Lkotlin/Function2;", "Lkotlin/Function0;", "Landroidx/lifecycle/ViewModel;", "onNavigateToPasscode", "onNavigateToFace", "onNavigateToFingerprint", "onNavigateUp", "getBitmap", "Landroidx/compose/ui/graphics/ImageBitmap;", "icon", "", "swapList", "T", "Landroidx/compose/runtime/snapshots/SnapshotStateList;", "newList", "", "SampleAppKotlin_debug"})
public final class AuthenticatorsScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void AuthenticatorsScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super kotlin.jvm.functions.Function0<kotlin.Unit>, ? super androidx.lifecycle.ViewModel, kotlin.Unit> onNavigateToChooseAuth, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToPasscode, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToFace, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToFingerprint, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateUp) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void AuthenticatorList(@org.jetbrains.annotations.NotNull()
    com.daon.fido.sdk.sample.kt.AuthenticatorsViewModel viewModel, int selectedIndex) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void AuthenticatorInfoCard(@org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.model.Authenticator authenticator, @org.jetbrains.annotations.NotNull()
    com.daon.fido.sdk.sample.kt.AuthenticatorsViewModel viewModel, int index, boolean selected, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onItemClick) {
    }
    
    public static final <T extends java.lang.Object>void swapList(@org.jetbrains.annotations.NotNull()
    androidx.compose.runtime.snapshots.SnapshotStateList<T> $this$swapList, @org.jetbrains.annotations.NotNull()
    java.util.List<? extends T> newList) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public static final androidx.compose.ui.graphics.ImageBitmap getBitmap(@org.jetbrains.annotations.NotNull()
    java.lang.String icon) {
        return null;
    }
}