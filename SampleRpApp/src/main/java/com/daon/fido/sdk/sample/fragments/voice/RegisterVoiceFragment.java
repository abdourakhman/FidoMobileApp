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

import java.util.ArrayList;
import java.util.List;

public class RegisterVoiceFragment extends BaseVoiceFragment {
    private final static String TAG = "RegisterVoiceFragment";
    private int index = 1;
    private int numberOfPhrases = 3;

    private List<byte[]> enrollAudio = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.daon_register_voice, container, false);
        getVoiceUI().init(root);
        return root;
    }

    @Override
    public void onVoiceInitialized(Exception e) {
        super.onVoiceInitialized(e);
        if(getVoiceUI() != null) {
            getVoiceUI().updateSteps(index, numberOfPhrases);
        }
    }

    @Override
    public void onAudioProcessingComplete(VoiceControllerProtocol.AudioProcessingCompleteResult result) {
        if(result.getErrorCode() != DaonVoice.RESULT_PASS) {
            // Audio processing failed, reset UI
            showMessage(getController().getVoiceSdkErrorMessage(result.getErrorCode()), false);
            resetUI();
        } else {
            // Audio succeeded, update UI to record next sample or enrol if done
            enrollAudio.add(result.getAudioData());
            index++;
            boolean done = index > numberOfPhrases;
            if (done) {
                reportRegistrationInProgress();
                getController().enrollAudio(enrollAudio, result.getAudioDataFormat(),
                        new EnrolResultListener(), new EnrolCaptureCompleteListener());
            } else {
                getVoiceUI().updateSteps(index, numberOfPhrases);
                resetUI();
            }
        }
    }

    @Override
    protected CaptureCompleteListener getSessionTimeoutListener() {
        // Only required for verification
        return null;
    }

    @Override
    protected CaptureCompleteListener getAttemptFailedListener() {
        // Only required for verification
        return null;
    }

    protected void reportRegistrationInProgress() {
        showMessage(R.string.voice_enroll, false);
    }

    @Override
    protected void onRecapture() {
        // Reset UI back to recording the first sample
        index = 1;
        getVoiceUI().updateSteps(index, numberOfPhrases);
        resetUI();
    }

    protected class EnrolResultListener implements VoiceControllerProtocol.EnrolResultListener {
        @Override
        public void onEnrolResult(VoiceControllerProtocol.VoiceProcessingResult result) {
            if(result.getErrorCode() != DaonVoice.RESULT_PASS) {
                enrollAudio.clear();
                showMessage(getController().getVoiceSdkErrorMessage(result.getErrorCode()), false);
                onRecapture();
            }
        }
    }

    protected class EnrolCaptureCompleteListener extends BaseCaptureFragment.DefaultCaptureCompleteListener {
        @Override
        public void onCaptureComplete(CaptureCompleteResult result) {
            enrollAudio.clear();
            super.onCaptureComplete(result);
        }
    }

    @Override
    protected int getCaptureFailedMessageId() {
        return R.string.voice_enroll_failed;
    }

    @Override
    protected int getCaptureSuccessMessageId() {
        return R.string.voice_enroll_complete;
    }

}
