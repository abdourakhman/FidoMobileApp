package com.daon.fido.sdk.sample.ados;

import android.content.Context;

public interface ICertificateAccessor {
    String getCertificateBase64String(Context context);
    void storeCertificate(String base64Certificate, Context context);
}
