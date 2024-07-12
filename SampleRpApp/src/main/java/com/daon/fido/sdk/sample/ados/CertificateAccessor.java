package com.daon.fido.sdk.sample.ados;

import android.content.Context;

import com.daon.fido.sdk.sample.UserPreferences;

public class CertificateAccessor implements ICertificateAccessor {
    private static final String KEY_ADOS_ROOT_CERT = "AdosRootCert";

    @Override
    public String getCertificateBase64String(Context context) {
        return UserPreferences.instance().getString(KEY_ADOS_ROOT_CERT, null);
    }

    @Override
    public void storeCertificate(String base64Certificate, Context context) {
        UserPreferences.instance().putString(KEY_ADOS_ROOT_CERT, base64Certificate);
    }
}
