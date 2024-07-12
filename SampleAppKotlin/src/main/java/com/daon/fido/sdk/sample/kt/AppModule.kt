package com.daon.fido.sdk.sample.kt

import android.content.Context
import android.content.SharedPreferences
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.sdk.sample.kt.util.FidoHolder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideFido(@ApplicationContext appContext: Context): IXUAF =
        FidoHolder.getInstance(appContext).fido

    @Provides
    @Singleton
    fun provideSharedPreference(@ApplicationContext appContext: Context): SharedPreferences =
        appContext.getSharedPreferences("pref_sampleapp_kotlin", Context.MODE_PRIVATE)
}