package com.daon.fido.sdk.sample.kt;

import android.util.Log;
import android.widget.Toast;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.ViewModel;
import com.daon.fido.sdk.sample.kt.model.*;
import com.google.accompanist.permissions.ExperimentalPermissionsApi;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\u001a\u0018\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u0007\u001a\u0083\u0001\u0010\u0006\u001a\u00020\u00012!\u0010\u0007\u001a\u001d\u0012\u0013\u0012\u00110\t\u00a2\u0006\f\b\n\u0012\b\b\u000b\u0012\u0004\b\b(\f\u0012\u0004\u0012\u00020\u00010\b2\u001e\u0010\r\u001a\u001a\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00010\u000f\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00010\u000e2\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00010\u000f2\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\u000f2\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u000f2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\u000fH\u0007\u001a\u0018\u0010\u0015\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u0007\u00a8\u0006\u0016"}, d2 = {"CreateAccountButton", "", "isDisplayed", "", "viewModel", "Lcom/daon/fido/sdk/sample/kt/IntroViewModel;", "IntroScreen", "onNavigateToHome", "Lkotlin/Function1;", "", "Lkotlin/ParameterName;", "name", "username", "onNavigateToChooseAuth", "Lkotlin/Function2;", "Lkotlin/Function0;", "Landroidx/lifecycle/ViewModel;", "onNavigateToPasscode", "onNavigateToFace", "onNavigateToFingerprint", "onNavigateUp", "LoginButton", "SampleAppKotlin_debug"})
public final class IntroScreenKt {
    
    @kotlin.OptIn(markerClass = {com.google.accompanist.permissions.ExperimentalPermissionsApi.class})
    @androidx.compose.runtime.Composable()
    public static final void IntroScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToHome, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super kotlin.jvm.functions.Function0<kotlin.Unit>, ? super androidx.lifecycle.ViewModel, kotlin.Unit> onNavigateToChooseAuth, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToPasscode, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToFace, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToFingerprint, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateUp) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void LoginButton(boolean isDisplayed, @org.jetbrains.annotations.NotNull()
    com.daon.fido.sdk.sample.kt.IntroViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void CreateAccountButton(boolean isDisplayed, @org.jetbrains.annotations.NotNull()
    com.daon.fido.sdk.sample.kt.IntroViewModel viewModel) {
    }
}