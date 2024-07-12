package com.daon.fido.sdk.sample.fragments.voice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daon.fido.sdk.sample.R;
import com.daon.sdk.authenticator.controller.CaptureCompleteListener;
import com.daon.sdk.authenticator.controller.CaptureCompleteResult;
import com.daon.fido.sdk.sample.fragments.BaseCaptureFragment;
import com.daon.sdk.voice.DaonVoice;
import com.daon.sdk.voiceauthenticator.controller.VoiceControllerProtocol;

public class AuthenticateVoiceFragment extends BaseVoiceFragment {
    private final CaptureCompleteListener captureCompleteListener = new VerifyCompleteListener();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.daon_authenticate_voice, container, false);
        getVoiceUI().init(root);
        return root;
    }

    @Override
    public void onAudioProcessingComplete(VoiceControllerProtocol.AudioProcessingCompleteResult result) {
        if(result.getErrorCode() != DaonVoice.RESULT_PASS) {
            // Audio processing failed, reset UI
            showMessage(getController().getVoiceSdkErrorMessage(result.getErrorCode()), false);
            resetUI();
        } else {
            // Audio succeeded, verify sample against enrolled sample
            reportAuthenticationInProgress();
            getController().verifyAudio(result.getAudioData(), result.getAudioDataFormat(),
                    new VerifyResultListener(), captureCompleteListener);
        }
    }

    protected void reportAuthenticationInProgress() {
        showMessage(R.string.voice_verify, false);
    }

    @Override
    protected CaptureCompleteListener getSessionTimeoutListener() {
        return captureCompleteListener;
    }

    @Override
    protected CaptureCompleteListener getAttemptFailedListener() {
        return captureCompleteListener;
    }

    @Override
    protected void onRecapture() {
        resetUI();
    }

    protected class VerifyResultListener implements VoiceControllerProtocol.VerifyResultListener {
        @Override
        public void onVerifyResult(VoiceControllerProtocol.VerifyResult result) {
            if (!result.isSuccess()) {
                showMessage(R.string.voice_verify_failed, false);
                resetUI();
            }
        }
    }

    protected class VerifyCompleteListener extends BaseCaptureFragment.DefaultCaptureCompleteListener {
        @Override
        protected String getAuthenticationErrorMessage(CaptureCompleteResult result) {
            return getResources().getString(R.string.voice_verify_failed);
        }
    }

    @Override
    protected int getCaptureFailedMessageId() {
        return R.string.voice_verify_failed;
    }

    @Override
    protected int getCaptureSuccessMessageId() {
        return R.string.voice_verify_complete;
    }

    @Override
    protected int getCaptureWarningMessageId() {
        return R.string.voice_verify_warning;
    }
}
