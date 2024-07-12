package com.daon.fido.sdk.sample.kt.face

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.sdk.sample.kt.R
import com.daon.sdk.authenticator.Authenticator
import com.daon.sdk.authenticator.ErrorCodes
import com.daon.sdk.authenticator.controller.AuthenticatorError
import com.daon.sdk.authenticator.controller.LockResult
import com.daon.sdk.faceauthenticator.FaceErrorCodes
import com.daon.sdk.faceauthenticator.YUVTools

import com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AuthenticateFaceViewModel @Inject constructor(
    application: Application,
    private val fido: IXUAF,
    private val prefs: SharedPreferences
) : FaceViewModel(application, fido, prefs) {
    override fun onLivenessEvent(info: FaceControllerProtocol.LivenessEventInfo?) {
        Log.d("DAON", "onLivenessEvent ${info?.livenessEvent}")
        if (faceController.expectedLivenessEvents.isNotEmpty()) {
            if (info != null) {
                if (info.allLivenessEventsDetected()) {
                    stopFaceCapture()
                    _instructions.value = ""
                    _status.value = "Authenticating ..."
                    _processing.value = true
                    if (info.image != null) {
                        _inProgress.value = true
                        faceController.authenticateImage(
                            info.image,
                            YUVTools.mirroredAngle(_rotation.value)
                        ) { errorCode, result, yuv ->
                            _inProgress.value = false
                            if (errorCode == ErrorCodes.NO_ERROR) {
                                _faceCaptureState.update { state ->
                                    state.copy(
                                        message = getResourceString(
                                            R.string.face_verify_complete
                                        )
                                    )
                                }
                            } else {
                                _faceCaptureState.update { state ->
                                    state.copy(
                                        message = getResourceString(
                                            R.string.face_verify_failed
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        //Handle no liveness image error
                    }
                } else {
                    when (info.livenessEvent) {
                        FaceControllerProtocol.LivenessEvent.INITIALIZING -> {
                            _status.value = "Initializing"
                        }

                        FaceControllerProtocol.LivenessEvent.STARTED -> {
                            _status.value = "Determining liveness"
                        }

                        FaceControllerProtocol.LivenessEvent.TRACKING -> {
                            _status.value = "Look alive!"
                        }

                        FaceControllerProtocol.LivenessEvent.ANALYZING -> {
                            _status.value = "Analyzing"
                        }

                        FaceControllerProtocol.LivenessEvent.COMPLETED -> {
                            _status.value = "Done"
                        }

                        FaceControllerProtocol.LivenessEvent.RESET -> {
                            _status.value = "Looking for a face"
                        }

                        else -> {

                        }
                    }
                }
            }
        }
    }

    override fun onFailure(error: AuthenticatorError?, result: LockResult?) {
        Log.d("DAON", "onFailure error - ${error?.code} result - ${result?.lockInfo?.state}")
        if (error != null) {
            when (error.code) {
                FaceErrorCodes.FACE_LIVENESS_AT_AUTH_TIMEOUT,
                FaceErrorCodes.FACE_REC_TIMEOUT,
                FaceErrorCodes.FACE_VERIFY_TIMEOUT_NO_FACE_FOUND-> {
                    _faceCaptureState.update { state ->
                        state.copy(message = getResourceString(R.string.face_verify_timeout))
                    }
                    if (result != null) {
                        if (result.lockInfo.state == Authenticator.Lock.UNLOCKED) {
                            onRecapture()
                        } else {
                            _captureComplete.value = true
                        }
                    } else {
                        _captureComplete.value = true
                    }
                }

                FaceErrorCodes.FACE_LOST_FACE_CONTINUITY -> {
                    _faceCaptureState.update { state ->
                        state.copy(message = getResourceString(R.string.face_tracking_lost))
                    }
                    _captureComplete.value = true
                }

                ErrorCodes.ERROR_MAX_ATTEMPTS -> {
                    _faceCaptureState.update { state ->
                        state.copy(message = error.message)
                    }
                    _captureComplete.value = true

                }
            }

        }

    }

    override fun onRecapture() {
        stopFaceCapture()
        _instructions.value = ""
        faceController.resumeAuthenticationProcessing()
        _enablePreview.value = false
        _photoMode.value = PhotoMode.DETECT
        _takePhotoEnabled.value = false
        _inProgress.value = false
        _processing.value = false
        _status.value = ""
    }
}