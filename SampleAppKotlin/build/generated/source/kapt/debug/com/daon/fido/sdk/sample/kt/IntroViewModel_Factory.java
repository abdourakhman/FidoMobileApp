package com.daon.fido.sdk.sample.kt;

import android.app.Application;
import android.content.SharedPreferences;
import com.daon.fido.client.sdk.IXUAF;
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
public final class IntroViewModel_Factory implements Factory<IntroViewModel> {
  private final Provider<Application> applicationProvider;

  private final Provider<IXUAF> fidoProvider;

  private final Provider<SharedPreferences> prefsProvider;

  public IntroViewModel_Factory(Provider<Application> applicationProvider,
      Provider<IXUAF> fidoProvider, Provider<SharedPreferences> prefsProvider) {
    this.applicationProvider = applicationProvider;
    this.fidoProvider = fidoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public IntroViewModel get() {
    return newInstance(applicationProvider.get(), fidoProvider.get(), prefsProvider.get());
  }

  public static IntroViewModel_Factory create(Provider<Application> applicationProvider,
      Provider<IXUAF> fidoProvider, Provider<SharedPreferences> prefsProvider) {
    return new IntroViewModel_Factory(applicationProvider, fidoProvider, prefsProvider);
  }

  public static IntroViewModel newInstance(Application application, IXUAF fido,
      SharedPreferences prefs) {
    return new IntroViewModel(application, fido, prefs);
  }
}
