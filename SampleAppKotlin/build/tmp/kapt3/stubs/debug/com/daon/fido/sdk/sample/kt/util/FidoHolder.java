package com.daon.fido.sdk.sample.kt.util;

import android.content.Context;
import android.os.Bundle;
import com.daon.fido.client.sdk.IXUAF;
import com.daon.fido.client.sdk.IXUAFService;
import com.daon.sdk.fido.service.rpsa.IXUAFRPSAService;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u0000 \u00122\u00020\u0001:\u0001\u0012B\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0011\u001a\u00020\fH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0005\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001a\u0010\u000b\u001a\u00020\fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010\u00a8\u0006\u0013"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/util/FidoHolder;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "fido", "Lcom/daon/fido/client/sdk/IXUAF;", "getFido", "()Lcom/daon/fido/client/sdk/IXUAF;", "setFido", "(Lcom/daon/fido/client/sdk/IXUAF;)V", "rpsaServer", "Lcom/daon/fido/client/sdk/IXUAFService;", "getRpsaServer", "()Lcom/daon/fido/client/sdk/IXUAFService;", "setRpsaServer", "(Lcom/daon/fido/client/sdk/IXUAFService;)V", "getServer", "Companion", "SampleAppKotlin_debug"})
public final class FidoHolder {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private com.daon.fido.client.sdk.IXUAF fido;
    @org.jetbrains.annotations.NotNull()
    private com.daon.fido.client.sdk.IXUAFService rpsaServer;
    @org.jetbrains.annotations.Nullable()
    private static com.daon.fido.sdk.sample.kt.util.FidoHolder instance;
    @org.jetbrains.annotations.NotNull()
    public static final com.daon.fido.sdk.sample.kt.util.FidoHolder.Companion Companion = null;
    
    private FidoHolder(android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.daon.fido.client.sdk.IXUAF getFido() {
        return null;
    }
    
    public final void setFido(@org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.IXUAF p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.daon.fido.client.sdk.IXUAFService getRpsaServer() {
        return null;
    }
    
    public final void setRpsaServer(@org.jetbrains.annotations.NotNull()
    com.daon.fido.client.sdk.IXUAFService p0) {
    }
    
    private final com.daon.fido.client.sdk.IXUAFService getServer() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0007R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/daon/fido/sdk/sample/kt/util/FidoHolder$Companion;", "", "()V", "instance", "Lcom/daon/fido/sdk/sample/kt/util/FidoHolder;", "getInstance", "context", "Landroid/content/Context;", "SampleAppKotlin_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.daon.fido.sdk.sample.kt.util.FidoHolder getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}