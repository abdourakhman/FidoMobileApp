package com.daon.fido.sdk.sample.kt;

import android.app.Application;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
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
public final class BaseViewModel_Factory implements Factory<BaseViewModel> {
  private final Provider<Application> applicationProvider;

  public BaseViewModel_Factory(Provider<Application> applicationProvider) {
    this.applicationProvider = applicationProvider;
  }

  @Override
  public BaseViewModel get() {
    return newInstance(applicationProvider.get());
  }

  public static BaseViewModel_Factory create(Provider<Application> applicationProvider) {
    return new BaseViewModel_Factory(applicationProvider);
  }

  public static BaseViewModel newInstance(Application application) {
    return new BaseViewModel(application);
  }
}
