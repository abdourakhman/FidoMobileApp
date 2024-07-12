package com.daon.fido.sdk.sample.fragments.passcode;

import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;

import com.daon.sdk.authenticator.Extensions;
import com.daon.sdk.authenticator.controller.PasscodeControllerProtocol;
import com.daon.fido.sdk.sample.fragments.BaseCaptureFragment;
import com.daon.sdk.authenticator.util.Keypad;

public abstract class BasePasscodeFragment extends BaseCaptureFragment {
    public PasscodeControllerProtocol getController() {
        return (PasscodeControllerProtocol) super.getController();
    }

    public void setController(PasscodeControllerProtocol controller) {
        super.setController(controller);
    }

    protected void setPasscodeEditTextRestrictions(EditText editText) {
        editText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        if(getController() != null) {
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(getController().getMaxLength())});

            if (Extensions.TYPE_ALPHANUMERIC.equals(getController().getKeyboardType())) {
                editText.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            }
        }
    }

    @Override
    protected void start() {
        super.start();

        // Show keypad
        final EditText passcodeEditText = getPrimaryPasscodeEditText();
        if(passcodeEditText != null) {
            passcodeEditText.requestFocus();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Keypad.show(getActivity(), passcodeEditText);
                }
            }, 100);
        }
    }

    @Override
    protected void stop() {
        super.stop();

        // Hide keypad
        EditText passcodeEditText = getPrimaryPasscodeEditText();
        if(passcodeEditText != null) {
            Keypad.hide(getActivity(), passcodeEditText);
        }
    }

    protected abstract EditText getPrimaryPasscodeEditText();
    protected abstract void reset();

    @Override
    protected void onRecapture() {
        reset();
    }
}
