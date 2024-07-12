package com.daon.fido.sdk.sample;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.daon.fido.client.sdk.IXUAFInitialiseListener;
import com.daon.fido.client.sdk.core.Error;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.client.sdk.model.AuthenticatorReg;
import com.daon.fido.client.sdk.model.DiscoveryData;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.ados.CertificateAccessor;
import com.daon.fido.sdk.sample.ados.ICertificateAccessor;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class InitialiseSdkTask extends TaskExecutor<Void> {
    private static final String DAON_AUTHENTICATOR_VENDOR_CODE = "D409";

    private SoftReference<IXUAFInitialiseListener> callback;
    private Context context;
    private Bundle parameters = new Bundle();
    private CoreApplication application;

    public InitialiseSdkTask(Context context, IXUAFInitialiseListener callback) {
        this.context = context;
        this.callback = new SoftReference<>(callback);
        this.application = (CoreApplication) context.getApplicationContext();
    }

    @Override
    protected Void doInBackground() {

        parameters.clear();

        parameters.putString("com.daon.sdk.log", "true");

        boolean adosRootCertSupplied = UserPreferences.instance().getBoolean(SettingsActivity.PREF_ADOS_ROOT_CERT_SUPPLIED, false);
        if (adosRootCertSupplied) {
            parameters.putString("com.daon.sdk.ados.rootCert", getAdosRootCert());
        }
        String adosDecSan = UserPreferences.instance().getString(SettingsActivity.PREF_ADOS_DEC_SAN, null);
        if (adosDecSan != null) {
            parameters.putString("com.daon.sdk.ados.decId", adosDecSan);
        }

        boolean screenCaptureEnabled = UserPreferences.instance().getBoolean(SettingsActivity.PREF_SCREEN_CAPTURE, false);
        if (screenCaptureEnabled) {
            parameters.putString("com.daon.sdk.screencapture.enabled", "true");
        }
        boolean silentFingerprintRegistration = UserPreferences.instance().getBoolean(SettingsActivity.PREF_SILENT_FINGERPRINT_REGISTRATION, false);
        if (silentFingerprintRegistration) {
            parameters.putString("com.daon.finger.silent", "true");
        }
        boolean silentSrpPasscode = UserPreferences.instance().getBoolean(SettingsActivity.PREF_SILENT_SRP_PASSCODE, false);
        if (silentSrpPasscode) {
            parameters.putString("com.daon.passcode.silent", "true");
        }
        boolean invalidateFingerOnNewEnrolment = UserPreferences.instance().getBoolean(SettingsActivity.PREF_INVALIDATE_FINGER_ON_NEW_ENROLMENT, true);
        if (!invalidateFingerOnNewEnrolment) {
            parameters.putString("com.daon.finger.enrollment.invalidate", "false");
        }
        boolean sdkManagedFingerLocking = UserPreferences.instance().getBoolean(SettingsActivity.PREF_SDK_MANAGED_FINGER_LOCKING, true);
        if (sdkManagedFingerLocking) {
            parameters.putString("com.daon.finger.sdk.locking", "true");
        }
        boolean initParamsToServer = UserPreferences.instance().getBoolean(SettingsActivity.PREF_INIT_PARAMS_TO_SERVER, false);
        if (initParamsToServer) {
            parameters.putString("com.daon.sdk.initParamsToServer", "true");
        }

        boolean ignoreNativeClients = UserPreferences.instance().getBoolean(SettingsActivity.PREF_IGNORE_NATIVE_CLIENTS, true);
        if (ignoreNativeClients) {
            parameters.putString("com.daon.sdk.ignoreNativeClients", "true");
        }

        boolean alwaysAllowAuthenticatorChoice = UserPreferences.instance().getBoolean(SettingsActivity.PREF_ALWAYS_ALLOW_AUTHENTICATOR_CHOICE, true);
        if (alwaysAllowAuthenticatorChoice) {
            parameters.putString("com.daon.sdk.alwaysAllowAuthenticatorChoice", "true");
        }

        // Key cleanup
        parameters.putString("com.daon.sdk.repair.pending", "true");
        
        // Enable performance score
        parameters.putString("com.daon.sdk.devicePerformance", "true");
        parameters.putString("com.daon.sdk.ados.enabled", "true");
        //Enabling ADoS SRP Passcode
        parameters.putString("com.daon.sdk.passcode.ados.version", "2");


        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        application.getFido().initWithService(parameters, new CustomCaptureFragmentFactory(), application.getCommService(), new IXUAFInitialiseListener() {
            @Override
            public void onInitialiseComplete() {
                retrieveAvailableAuthenticatorAaids();

                if (callback != null && callback.get() != null) {
                    callback.get().onInitialiseComplete();
                    callback.clear();
                }
            }

            @Override
            public void onInitialiseWarnings(List<Error> warnings) {
                retrieveAvailableAuthenticatorAaids();

                if (callback != null && callback.get() != null) {
                    callback.get().onInitialiseWarnings(warnings);
                }
            }

            @Override
            public void onInitialiseFailed(int errorCode, String message) {
                if (callback != null && callback.get() != null) {
                    callback.get().onInitialiseFailed(errorCode, message);
                    callback.clear();
                }
            }
        });
    }

    private String getAdosRootCert() {
        if (UserPreferences.instance().getBoolean(SettingsActivity.PREF_ALT_ADOS_ROOT_CERT_PROVIDED, false)) {
            ICertificateAccessor certificateAccessor = new CertificateAccessor();
            return certificateAccessor.getCertificateBase64String(context);
        }
        return context.getString(R.string.default_ados_root_cert);
    }

    private void retrieveAvailableAuthenticatorAaids() {
        CoreApplication application = (CoreApplication) context.getApplicationContext();
        DiscoveryData discoveryData = application.getFido().discover();

        List<String> availableAaids = new ArrayList<>(discoveryData.getAvailableAuthenticators().length);
        for (Authenticator authenticator : discoveryData.getAvailableAuthenticators()) {
            if (!authenticator.getAaid().startsWith(DAON_AUTHENTICATOR_VENDOR_CODE)) {
                application.setHasExternalClient(true);
            }
            availableAaids.add(authenticator.getAaid());
            Log.d("SampleRpApp", "Discovered authenticator: " + authenticator.getAaid() + ". Is it registered to a user? " + ((AuthenticatorReg) authenticator).isRegistered());
        }
        application.setAvailableAuthenticators(availableAaids);
    }
}
