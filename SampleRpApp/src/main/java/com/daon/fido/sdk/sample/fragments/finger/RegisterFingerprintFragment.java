package com.daon.fido.sdk.sample.fragments.finger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daon.fido.sdk.sample.fragments.BaseCaptureFragment;
import com.daon.sdk.authenticator.controller.FingerprintCaptureControllerProtocol;
import com.daon.fido.sdk.sample.R;

public class RegisterFingerprintFragment extends BaseCaptureFragment {
    private static final int TERMINATION_DELAY_MILLIS = 1000;

    public FingerprintCaptureControllerProtocol getController() {
        return (FingerprintCaptureControllerProtocol) super.getController();
    }

//    public void setController(FingerprintCaptureControllerProtocol controller) {
//        super.setController(controller);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.finger_auth, container, false);
    }

    @Override
    protected void start() {
        if (getController() != null) {
            // Allow time for messages to be read...
            setParentActivityTerminationDelay(TERMINATION_DELAY_MILLIS);

            getController().startCapture(this, new DefaultCaptureCompleteListener());
        }
    }

    @Override
    protected void stop() {
        if (getController() != null)
            getController().stopCapture();
    }
}
