package com.daon.fido.sdk.sample.kt;

import android.content.Context;
import com.daon.fido.client.sdk.IXUAF;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class AppModule_ProvideFidoFactory implements Factory<IXUAF> {
  private final AppModule module;

  private final Provider<Context> appContextProvider;

  public AppModule_ProvideFidoFactory(AppModule module, Provider<Context> appContextProvider) {
    this.module = module;
    this.appContextProvider = appContextProvider;
  }

  @Override
  public IXUAF get() {
    return provideFido(module, appContextProvider.get());
  }

  public static AppModule_ProvideFidoFactory create(AppModule module,
      Provider<Context> appContextProvider) {
    return new AppModule_ProvideFidoFactory(module, appContextProvider);
  }

  public static IXUAF provideFido(AppModule instance, Context appContext) {
    return Preconditions.checkNotNullFromProvides(instance.provideFido(appContext));
  }
}
