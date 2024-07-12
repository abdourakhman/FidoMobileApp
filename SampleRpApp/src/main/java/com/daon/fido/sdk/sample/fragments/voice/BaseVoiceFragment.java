package com.daon.fido.sdk.sample.fragments.voice;

import android.content.Context;

import androidx.annotation.NonNull;

import com.daon.fido.sdk.sample.fragments.BaseCaptureFragment;
import com.daon.sdk.authenticator.ErrorCodes;
import com.daon.sdk.authenticator.controller.AuthenticatorError;
import com.daon.sdk.authenticator.controller.CaptureCompleteListener;
import com.daon.sdk.voiceauthenticator.R;
import com.daon.sdk.voiceauthenticator.controller.VoiceControllerProtocol;

public abstract class BaseVoiceFragment extends BaseCaptureFragment implements VoiceUI.VoiceUIListener, VoiceControllerProtocol.VoiceInitializedListener, VoiceControllerProtocol.AudioProcessingCompleteListener, VoiceControllerProtocol.RecordingTimeoutListener {
    protected final int DEFAULT_RECORD_TIMEOUT = 10000;
    private VoiceUI voiceUI;

    public VoiceControllerProtocol getController() {
        return (VoiceControllerProtocol) super.getController();
    }

    public void setController(VoiceControllerProtocol controller) {
        super.setController(controller);
    }

    protected VoiceUI getVoiceUI() {
        return voiceUI;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        voiceUI = new VoiceUI(context, this);
    }

    @Override
    protected void start() {
        super.start();
        getController().startVoiceCapture(this, getSessionTimeoutListener());
    }

    @Override
    protected void stop() {
        super.stop();
        if (getController() != null) {
            getController().cancelRecording();
            if (voiceUI != null) {
                voiceUI.resetMicButton();
            }
        }
    }

    @Override
    public void onMicButtonClick() {
        startStopRecording();
    }

    @Override
    public void onVoiceInitialized(Exception e) {
        if (getController() != null) {
            if (e != null) {
                getController().completeCaptureWithError(new AuthenticatorError(ErrorCodes.ERROR_HW_UNAVAILABLE, getString(R.string.voice_not_initialized)));
            } else {
                if (voiceUI != null) {
                    voiceUI.enablePhrase(getController().getPhraseToSpeak());
                }
            }
        }
    }

    @Override
    public void onRecordingTimeout() {
        if (getController() != null) {
            enableRecordingUI(false);
            getController().stopRecording(this, getAttemptFailedListener());
        }
    }

    protected void startStopRecording() {
        if (getController() != null) {
            if (getController().isRecording()) {
                enableRecordingUI(false);
                getController().stopRecording(this, getAttemptFailedListener());
            } else {
                enableRecordingUI(true);
                getController().startRecording(DEFAULT_RECORD_TIMEOUT, this);
            }
        }
    }

    protected void enableRecordingUI(boolean enable) {
        if (voiceUI == null) return;
        if (enable) {
            voiceUI.activateMicButton();
            voiceUI.startRecordAnimation(new VoiceUI.VoiceRecorder() {
                @Override
                public boolean isRecording() {
                    if (getController() != null) {
                        return getController().isRecording();
                    } else {
                        return false;
                    }
                }

                @Override
                public int getRecordingAmplitude() {
                    if (getController() != null) {
                        return getController().getRmsPower();
                    } else {
                        return 0;
                    }
                }
            });
        } else {
            voiceUI.resetMicButton();
            voiceUI.disablePhrase();
        }
    }

    protected void resetUI() {
        getVoiceUI().enablePhrase(getController().getPhraseToSpeak());
    }

    protected abstract CaptureCompleteListener getSessionTimeoutListener();

    protected abstract CaptureCompleteListener getAttemptFailedListener();
}
