package com.daon.fido.sdk.sample.fragments.pattern;

import com.daon.fido.sdk.sample.fragments.BaseCaptureFragment;
import com.daon.sdk.authenticator.controller.CaptureCompleteResult;
import com.daon.fido.sdk.sample.R;

public class AuthenticatePatternFragment extends PatternFragment {
    PatternValidListener patternValidListener;

    @Override
    protected int getLayoutResource() {
        return R.layout.daon_authenticate_pattern;
    }

    @Override
    protected PatternCollect.Mode getPatternCollectMode() {
        return PatternCollect.Mode.AUTHENTICATE;
    }

    @Override
    public void store(int[] pattern) {
        throw new RuntimeException("This pattern cannot be stored during authentication");
    }

    @Override
    public void validate(int[] pattern, PatternValidListener listener) {
        this.patternValidListener = listener;
        if(getController() != null) {
            getController().authenticatePattern(pattern, new CaptureCompleteListener());
        }
    }


    @Override
    public void onPatternCollectResult(PatternCollect.PatternCollectResult result) {

    }

    private class CaptureCompleteListener extends BaseCaptureFragment.DefaultCaptureCompleteListener {
        @Override
        protected void onTerminateSuccess(CaptureCompleteResult result) {
            if(patternValidListener != null) {
                patternValidListener.onPatternValid(true);
            }
            super.onTerminateSuccess(result);
        }

        @Override
        protected void onClientAuthenticationFailed(CaptureCompleteResult result) {
            if(patternValidListener != null) {
                patternValidListener.onPatternValid(false);
            }
            super.onClientAuthenticationFailed(result);
        }

        @Override
        protected void onClientError(CaptureCompleteResult result) {
            if(patternValidListener != null) {
                patternValidListener.onPatternValid(false);
            }
            super.onClientError(result);
        }
    }

    @Override
    protected int getCaptureSuccessMessageId() {
        return R.string.pattern_verify_complete;
    }

    @Override
    protected int getCaptureFailedMessageId() {
        return R.string.pattern_verify_failed;
    }

    @Override
    protected int getCaptureWarningMessageId() {
        return R.string.pattern_verify_warning;
    }
}
