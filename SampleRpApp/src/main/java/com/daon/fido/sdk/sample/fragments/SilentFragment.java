package com.daon.fido.sdk.sample.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daon.fido.sdk.sample.R;
import com.daon.sdk.authenticator.Extensions;


public class SilentFragment extends BaseCaptureFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getBooleanExtension(Extensions.SILENT_UI, false)) {
            return inflater.inflate(R.layout.daon_silent, container, false);
        }

        return null;
    }

    @Override
    public void start() {
        super.start();
        completeAuthentication();
    }

    protected void completeAuthentication() {
        // Create keys if necessary
        if(getController() != null) {
            if (getBooleanExtension(Extensions.SILENT_UI, false)) {
                getController().completeCapture(new DefaultCaptureCompleteListener());
            } else {
                getController().completeCapture();
            }
        }
    }
}
