package com.daon.fido.sdk.sample.kt;

import android.content.Context;
import android.content.SharedPreferences;
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
public final class AppModule_ProvideSharedPreferenceFactory implements Factory<SharedPreferences> {
  private final AppModule module;

  private final Provider<Context> appContextProvider;

  public AppModule_ProvideSharedPreferenceFactory(AppModule module,
      Provider<Context> appContextProvider) {
    this.module = module;
    this.appContextProvider = appContextProvider;
  }

  @Override
  public SharedPreferences get() {
    return provideSharedPreference(module, appContextProvider.get());
  }

  public static AppModule_ProvideSharedPreferenceFactory create(AppModule module,
      Provider<Context> appContextProvider) {
    return new AppModule_ProvideSharedPreferenceFactory(module, appContextProvider);
  }

  public static SharedPreferences provideSharedPreference(AppModule instance, Context appContext) {
    return Preconditions.checkNotNullFromProvides(instance.provideSharedPreference(appContext));
  }
}
