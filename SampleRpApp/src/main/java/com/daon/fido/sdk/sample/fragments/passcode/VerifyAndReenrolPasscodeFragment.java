package com.daon.fido.sdk.sample.fragments.passcode;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.daon.fido.sdk.sample.R;
import com.daon.sdk.authenticator.Authenticator;

import com.daon.sdk.authenticator.controller.AuthenticatorError;
import com.daon.sdk.authenticator.util.Strings;

public class VerifyAndReenrolPasscodeFragment extends BasePasscodeFragment {
    private EditText verifyPasscodeEditText;
    private EditText reenrolPasscodeEditText;
    private EditText confirmEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.daon_verify_and_reenrol_passcode, container, false);

        if(rootView != null) {
            verifyPasscodeEditText = rootView.findViewById(R.id.verifyPin);
            reenrolPasscodeEditText = rootView.findViewById(R.id.reenrolPin);
            confirmEditText = rootView.findViewById(R.id.confirm);
            confirmEditText.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    verifyAndReenroll();
                }

                return false;
            });
        }

        return rootView;
    }

    @Override
    protected void start() {
        super.start();

        // Set edit passcode restrictions - requires state information so must be done here
        setPasscodeEditTextRestrictions(verifyPasscodeEditText);
        setPasscodeEditTextRestrictions(reenrolPasscodeEditText);
        setPasscodeEditTextRestrictions(confirmEditText);
    }

    @Override
    protected EditText getPrimaryPasscodeEditText() {
        return verifyPasscodeEditText;
    }

    @Override
    protected void reset() {
        if(verifyPasscodeEditText != null && reenrolPasscodeEditText != null && confirmEditText != null) {
            verifyPasscodeEditText.setEnabled(true);
            verifyPasscodeEditText.setText("");

            reenrolPasscodeEditText.setEnabled(true);
            reenrolPasscodeEditText.setText("");

            confirmEditText.setEnabled(true);
            confirmEditText.setText("");

            verifyPasscodeEditText.requestFocus();
        }
    }

    protected void resetNewPasscode() {
        if(reenrolPasscodeEditText != null && confirmEditText != null) {
            reenrolPasscodeEditText.setEnabled(true);
            reenrolPasscodeEditText.setText("");

            confirmEditText.setEnabled(true);
            confirmEditText.setText("");

            reenrolPasscodeEditText.requestFocus();
        }
    }

    protected void verifyAndReenroll() {
        if(verifyPasscodeEditText != null && reenrolPasscodeEditText != null && confirmEditText != null &&
                getController() != null) {
            verifyPasscodeEditText.setEnabled(false);
            reenrolPasscodeEditText.setEnabled(false);
            confirmEditText.setEnabled(false);

            if(!Strings.equals(reenrolPasscodeEditText.getText(), confirmEditText.getText())) {
                showMessage(getString(R.string.passcode_mismatch), false);
            } else if(verifyPasscodeEditText.getText().length()==0 ||
                    reenrolPasscodeEditText.getText().length()==0) {
                showMessage(getString(R.string.passcode_empty), false);
            } else {
                AuthenticatorError error = getController().verifyAndReenrolPasscode(
                        Strings.toCharArray(verifyPasscodeEditText.getText()),
                        Strings.toCharArray(reenrolPasscodeEditText.getText()),
                        new DefaultCaptureCompleteListener());

                if(error != null) {
                    showMessage(error.getMessage(), false);
                } else {
                    if (getController().getType() == Authenticator.Type.ADOS) {
                        onAuthenticateWait(true);
                    }

                    return;
                }
            }

            resetNewPasscode();
        }
    }

    @Override
    protected int getCaptureFailedMessageId() {
        return R.string.passcode_enroll_failed;
    }

    @Override
    protected int getCaptureSuccessMessageId() {
        return R.string.passcode_enroll_complete;
    }
}
