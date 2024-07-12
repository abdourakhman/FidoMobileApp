package com.daon.fido.sdk.sample.kt.face

import android.app.Application
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.util.Size
import androidx.camera.core.ImageProxy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.viewModelScope
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.sdk.sample.kt.BaseViewModel
import com.daon.fido.sdk.sample.kt.R
import com.daon.sdk.authenticator.Authenticator
import com.daon.sdk.authenticator.ErrorCodes
import com.daon.sdk.authenticator.controller.AuthenticatorError
import com.daon.sdk.authenticator.controller.CaptureCompleteResult
import com.daon.sdk.authenticator.controller.LockResult
import com.daon.sdk.authenticator.exception.ControllerInitializationException
import com.daon.sdk.face.LivenessResult
import com.daon.sdk.face.Result
import com.daon.sdk.face.YUV
import com.daon.sdk.faceauthenticator.FaceErrorCodes
import com.daon.sdk.faceauthenticator.YUVTools
import com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FaceCaptureState(
    val message: String?
)
@HiltViewModel
open class FaceViewModel @Inject constructor(
    application: Application,
    private val fido: IXUAF,
    private val prefs: SharedPreferences
) : BaseViewModel(application), FaceControllerProtocol.FaceAnalysisListener {
    lateinit var faceController: FaceControllerProtocol

    private var capturedImage: YUV? = null

    protected val _captureComplete = MutableStateFlow(false)
    val captureComplete = _captureComplete.asStateFlow()

    protected val _faceCaptureState = MutableStateFlow(FaceCaptureState(message = null))
    val faceCaptureState: StateFlow<FaceCaptureState> = _faceCaptureState

    private val _isQuality = MutableStateFlow( false)
    val isQuality  = _isQuality.asStateFlow()

    protected val _instructions = MutableStateFlow("")
    val instructions = _instructions.asStateFlow()

    protected val _status = MutableStateFlow("")
    val status = _status.asStateFlow()

    private val _warnings = MutableStateFlow("")
    val warnings = _warnings.asStateFlow()

    private val _maskWarnings = MutableStateFlow("")
    val maskWarnings = _maskWarnings.asStateFlow()

    protected val _rotation = MutableStateFlow(0)
    val rotation = _rotation.asStateFlow()

    protected val _photoMode = MutableStateFlow( PhotoMode.DETECT )
    val photoMode = _photoMode.asStateFlow()

    protected val _enablePreview = MutableStateFlow( false)
    val enablePreview  = _enablePreview.asStateFlow()

    protected val _takePhotoEnabled = MutableStateFlow(false)
    val takePhotoEnabled = _takePhotoEnabled.asStateFlow()

    protected var _inProgress = MutableStateFlow(false)
    val inProgress = _inProgress.asStateFlow()

    protected val _processing = MutableStateFlow( false)
    val processing   = _processing.asStateFlow()

    private val _isEnrol = MutableStateFlow(false)
    val isEnrol = _isEnrol.asStateFlow()

    private val _previewSize = MutableStateFlow(Size(0, 0))
    val previewSize = _previewSize.asStateFlow()

    private val _authStateProcessed = MutableStateFlow(false)
    val authStateProcessed = _authStateProcessed.asStateFlow()

    fun getAuthenticationMode() {
        val aaidValue = prefs.getString("selectedAaid", null)
        if (aaidValue != null) {
            //Handling the ControllerInitializationException here -
            // for example. if the authenticator is locked
            try {
                faceController = fido.getController(getApplication(), aaidValue) as FaceControllerProtocol
                _authStateProcessed.value = true
                _isEnrol.value = faceController.isEnrol
                _previewSize.value =
                    if (faceController.passiveLivenessEvent == FaceControllerProtocol.LivenessEvent.SERVER &&
                        faceController.type == Authenticator.Type.ADOS
                    ) {
                        Size(1280, 720)
                    } else {
                        Size(640, 480)
                    }
            }catch (e: ControllerInitializationException) {
                Log.d("DAON", "FaceViewModel getControllerMode exception: ${e.message}")
                _faceCaptureState.update { state ->
                    state.copy(message = "Error: ${e.message}")
                }
                _captureComplete.value = true
                cancelCurrentOperation()
            }
        }
    }

    open fun onStart() {
        val aaidValue = prefs.getString("selectedAaid", null)
        if (aaidValue != null) {
            //Handling the ControllerInitializationException here -
            // for example. if the authenticator is locked
            try {
                faceController = fido.getController(getApplication(), aaidValue) as FaceControllerProtocol
                faceController.setImageQualityChecker { result, _ ->
                    result.qualityResult.hasAcceptableQuality() && result.qualityResult.isFaceCentered && result.isDeviceUpright
                }

                startFaceCapture()

            }catch (e: ControllerInitializationException) {
                Log.d("DAON", "FaceViewModel getControllerMode exception: ${e.message}")
                _faceCaptureState.update { state ->
                    state.copy(message = "Error: ${e.message}")
                }
                _captureComplete.value = true
                cancelCurrentOperation()
            }
        }

        fido.addUserLockWarningListener{
            _faceCaptureState.update { state ->
                state.copy(message = getResourceString(R.string.user_lock_warning))
            }
        }
    }

    fun onStop() {
        faceController.stopFaceCapture()
    }

    fun cancelCurrentOperation() {
        viewModelScope.launch(Dispatchers.Default) {
            fido.cancelCurrentOperation()
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    fun analyzeImage(image: ImageProxy) {
        _rotation.value = image.imageInfo.rotationDegrees
        val yuv = YUV(image.image)
        image.close()
        faceController.analyzeImage(yuv)

    }

    fun startFaceCapture() {
        faceController.startFaceCapture(previewSize.value.width, previewSize.value.height, this) { result ->
            Log.d("DAON", "onCaptureComplete result - ${result.type}")
            if (result != null) {
                when (result.type) {
                    CaptureCompleteResult.Type.TERMINATE_SUCCESS -> {
                        Log.d("DAON", "face capture complete")
                        _captureComplete.value = true
                    }

                    CaptureCompleteResult.Type.SERVER_VALIDATION_ERROR -> {
                        Log.d("DAON", "face capture server validation error")
                        val errorMessage = result.error.message
                        if (errorMessage != null) {
                            _faceCaptureState.update { state ->
                                state.copy(message = errorMessage + "\n" + getRetriesRemainingMessage(result))
                            }
                        } else {
                            _faceCaptureState.update { state ->
                                state.copy(message = getRetriesRemainingMessage(result))
                            }
                        }
                        onRecapture()
                    }

                    CaptureCompleteResult.Type.TERMINATE_FAILURE -> {
                        Log.d("DAON", "face capture TERMINATE_FAILURE")
                        faceController.cancelCapture()
                        _captureComplete.value = true
                    }

                    CaptureCompleteResult.Type.CLIENT_ERROR -> {
                        Log.d("DAON", "face capture CLIENT_ERROR")
                        if (faceController.isRegistration) {
                            _faceCaptureState.update { state ->
                                state.copy(message = "Face registration failed.")
                            }
                        } else {
                            _faceCaptureState.update { state ->
                                state.copy(message = "Face authentication failed.")
                            }
                        }
                        _captureComplete.value = true
                    }

                    CaptureCompleteResult.Type.CLIENT_VALIDATION_ERROR -> {
                        Log.d("DAON", "face capture CLIENT_VALIDATION_ERROR")
                        if (result.lockInfo.state == Authenticator.Lock.UNLOCKED) {
                            if (result.info.getBoolean(CaptureCompleteResult.InfoKey.IS_WARN_ATTEMPT, false)) {
                                _faceCaptureState.update { state ->
                                    state.copy(message = "Detecting multiple failed matches, please adjust your device or lighting condition and try again.")
                                }
                            } else {
                                _faceCaptureState.update { state ->
                                    state.copy(message = getRetriesRemainingMessage(result))
                                }
                            }
                            onRecapture()
                        } else {
                            if (result.lockInfo.state == Authenticator.Lock.TEMPORARY) {
                                _faceCaptureState.update { state ->
                                    state.copy(message = "Too many authentication attempts. The authenticator is locked for ${result.lockInfo.seconds} seconds. Please try later.")
                                }
                            } else if (result.lockInfo.state == Authenticator.Lock.PERMANENT) {
                                _faceCaptureState.update { state ->
                                    state.copy(message = "Too many authentication attempts. The authenticator is locked.")
                                }
                            }
                            _captureComplete.value = true
                        }
                    }

                }
            }

        }
    }

    private fun getRetriesRemainingMessage(result: CaptureCompleteResult): String {
        var retryMessage = ""
        val numberOfRetries = result.info.getInt(CaptureCompleteResult.InfoKey.RETRIES_REMAINING, -1)
        if (numberOfRetries > 0) {
            if (numberOfRetries ==1) {
                retryMessage = "1 retry remaining"
            } else {
                retryMessage = "$numberOfRetries retries remaining"
            }
        } else {
            retryMessage = "Please try again later"
        }
        return retryMessage
    }


    fun resetCaptureComplete() {
        _captureComplete.value = false
    }

    fun resetFaceCaptureState() {
        _faceCaptureState.update { state ->
            state.copy(message = null)
        }
    }

    fun stopFaceCapture() {
        faceController.stopFaceCapture()
    }

    override fun onImageAnalyzed(yuv: YUV?, result: Result?, isQualityImage: Boolean) {
        Log.d("DAON", "onImageAnalyzed")
        _isQuality.value = result != null && result.isDeviceUpright && isQualityImage

        Log.d("DAON", "onImageAnalyzed isQuality ${_isQuality.value}")

        //ToDo - like sample app different text views ?
        if (result?.hasMask() == true) {
            _maskWarnings.value = "Please make sure you are not wearing a face mask"
        }

        if (!faceController.isLivenessEnabled) {
            if (result != null) {
                checkAndUpdateQualityInfo(result)
            }
            _takePhotoEnabled.value = isQualityImage && result?.qualityResult?.isFaceCentered == true
            if (_takePhotoEnabled.value) {
                if (yuv != null) {
                    capturedImage = yuv
                }
                _photoMode.value = PhotoMode.TAKE
                _instructions.value = ""
                _warnings.value = ""
            }
        } else {
            if (result != null) {
                if (checkAndUpdateQualityInfo(result)) {
                    updateInfo(result)
                }
            }
        }
    }

    private fun checkAndUpdateQualityInfo(result: Result): Boolean {
        val quality = result.qualityResult
        if (getAlertMessage(result.livenessResult.alert) > 0) {
            _warnings.value = getResourceString(getAlertMessage(result.livenessResult.alert))
        } else if (quality.hasMask()) {
            _maskWarnings.value = getResourceString(R.string.face_quality_mask_detected)
        } else if (!quality.isFaceCentered) {
            _warnings.value = getResourceString(R.string.face_not_centered)
        } else if (!quality.hasAcceptableEyeDistance()) {
            _warnings.value = getResourceString(R.string.move_closer)
        } else if (!result.qualityResult.hasAcceptableQuality()) {
            val goodLighting = quality.hasAcceptableExposure()
                    && quality.hasUniformLighting()
                    && quality.hasAcceptableGrayscaleDensity()
                    && quality.hasFace()
            if(!goodLighting) {
                _warnings.value = getResourceString(R.string.face_quality_non_uniform_lighting)
            } else {
                _warnings.value = getResourceString(R.string.face_quality_unknown)
            }
        } else {
            _warnings.value = ""
            return true
        }

        return false
    }

    private fun updateInfo(result: Result) {
        if (result.isTrackingFace) {
            if (faceController.expectedLivenessEvents.contains(FaceControllerProtocol.LivenessEvent.BLINK)) {
                if (faceController.expectedLivenessEvents.contains(FaceControllerProtocol.LivenessEvent.PASSIVE)) {
                    if (faceController.detectedLivenessEvents.contains(FaceControllerProtocol.LivenessEvent.BLINK)) {
                        if (faceController.detectedLivenessEvents.contains(FaceControllerProtocol.LivenessEvent.PASSIVE)) {
                            _instructions.value = getResourceString(R.string.photo_info)
                        } else {
                            _instructions.value =
                                getResourceString(R.string.face_blink_detected_liveness_not_detected)
                        }

                    } else {
                        if (faceController.detectedLivenessEvents.contains(FaceControllerProtocol.LivenessEvent.PASSIVE)) {
                            _instructions.value =
                                getResourceString(R.string.face_blink_not_detected_liveness_detected)
                        } else {
                            _instructions.value =
                                getResourceString(R.string.face_blink_liveness_not_detected)
                        }
                    }
                } else {
                    if (faceController.detectedLivenessEvents.contains(FaceControllerProtocol.LivenessEvent.BLINK)) {
                        _instructions.value = getResourceString(R.string.photo_info)
                    } else {
                        _instructions.value = getResourceString(R.string.face_blink_not_detected)
                    }
                }
            } else {
                if (faceController.detectedLivenessEvents.contains(FaceControllerProtocol.LivenessEvent.PASSIVE)) {
                    _instructions.value = getResourceString(R.string.photo_info)
                } else {
                    _instructions.value = getResourceString(R.string.face_liveness_not_detected)
                }
            }
        }
    }

    fun getAlertMessage(alert: Int): Int {
        var result = -1

        when (alert) {
            LivenessResult.ALERT_FACE_NOT_DETECTED -> {
                result = R.string.face_liveness_hmd_face_not_detected
            }
            LivenessResult.ALERT_FACE_NOT_CENTERED -> {
                result = R.string.face_liveness_hmd_face_not_centered
            }
            LivenessResult.ALERT_MOTION_TOO_FAST -> {
                result = R.string.face_liveness_hmd_motion_too_fast
            }
            LivenessResult.ALERT_MOTION_SWING_TOO_FAST -> {
                result = R.string.face_liveness_hmd_motion_swing_too_fast
            }
            LivenessResult.ALERT_MOTION_TOO_FAR -> {
                result = R.string.face_liveness_hmd_motion_too_far
            }
            LivenessResult.ALERT_FACE_TOO_CLOSE_TO_EDGE -> {
                result = R.string.face_liveness_hmd_too_close_to_edge
            }
            LivenessResult.ALERT_FACE_TOO_NEAR -> {
                result = R.string.face_liveness_hmd_too_near
            }
            LivenessResult.ALERT_FACE_TOO_FAR -> {
                result = R.string.face_liveness_hmd_too_far
            }
            LivenessResult.ALERT_LIVENESS_SPOOF -> {
                result = R.string.face_liveness_hmd_spoof
            }
            LivenessResult.ALERT_INSUFFICIENT_FACE_DATA -> {
                result = R.string.face_liveness_hmd_insufficient_face_data
            }
            LivenessResult.ALERT_INSUFFICIENT_FRAME_DATA -> {
                result = R.string.face_liveness_hmd_insufficient_frame_data
            }
            LivenessResult.ALERT_FRAME_MISMATCH -> {
                result = R.string.face_liveness_hmd_frame_mismatch
            }
            LivenessResult.ALERT_NO_MOVEMENT_DETECTED -> {
                result = R.string.face_liveness_hmd_no_movement_detected
            }
            LivenessResult.ALERT_FACE_QUALITY -> {
                result = R.string.face_liveness_hmd_quality
            }
            LivenessResult.ALERT_TIMEOUT -> {
                result = R.string.face_liveness_timeout
            }
            LivenessResult.ALERT_PERFORMANCE -> {
                result = R.string.face_liveness_performance
            }
        }
        return result
    }

    override fun onLivenessEvent(info: FaceControllerProtocol.LivenessEventInfo?) {
        if (faceController.expectedLivenessEvents.isNotEmpty()) {
            if (info != null) {
                if (info.allLivenessEventsDetected()) {
                    _takePhotoEnabled.value = true
                    _instructions.value = ""
                    _warnings.value = ""
                    _photoMode.value = PhotoMode.TAKE
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
                FaceErrorCodes.FACE_LIVENESS_AT_REG_TIMEOUT-> {
                    _faceCaptureState.update { state ->
                        state.copy(message = getResourceString(R.string.face_verify_timeout))
                    }
                    onRecapture()
                }

                FaceErrorCodes.FACE_LOST_FACE_CONTINUITY -> {
                    _faceCaptureState.update { state ->
                        state.copy(message = getResourceString(R.string.face_tracking_lost))
                    }
                    onRecapture()
                }

            }

        }

    }

    open fun onRecapture() {
        _inProgress.value = false
        _processing.value = false
        stopFaceCapture()
        retakePhoto()
    }

    override fun onAlert(alert: Int) {
        Log.d("DAON", "onAlert ---- $alert")
    }

    fun getCapturedImage(): ImageBitmap? {
        val yuv = if (faceController.isLivenessEnabled) {
            faceController.captureImage()
        }else {
            capturedImage
        }

        if (yuv != null) {
            val bitmap = rotate(yuv.toBitmap())
            if (bitmap != null) {
                return bitmap.asImageBitmap()
            }
        }
        return null
    }

    private fun rotate(bmp: Bitmap): Bitmap? {
        val mtx = Matrix()
        val w = bmp.width
        val h = bmp.height
        //ToDo - get rotation from ViewModel
        mtx.postRotate(rotation.value.toFloat())
        mtx.postScale(-1.0f, 1.0f)
        return Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true)
    }

    fun takePhoto() {
        stopFaceCapture()
        _photoMode.value = PhotoMode.RETAKE
        _instructions.value = ""

        if (faceController.isLivenessEnabled) {
            capturedImage = faceController.captureImage()
        }

        _enablePreview.value = true
    }

    fun retakePhoto() {
        _instructions.value = ""
        faceController.resumeRegistrationProcessing()
        _enablePreview.value = false
        _photoMode.value = PhotoMode.DETECT
        _takePhotoEnabled.value = false
        capturedImage = null
    }

    fun enroll() {
        _takePhotoEnabled.value = false
        _status.value = "Enrolling ..."
        _inProgress.value = true
        _processing.value = true
        faceController.registerImage(capturedImage, YUVTools.mirroredAngle(_rotation.value)) { errorCode, _, _ ->
            _inProgress.value = false
            when (errorCode) {
                ErrorCodes.NO_ERROR -> {
                    Log.d("DAON", "enroll success")
                    _faceCaptureState.update { state ->
                        state.copy(message = "Successfully enrolled !!!")
                    }
                    stopFaceCapture()
                }

                ErrorCodes.ERROR_ENROLL_QUALITY -> {
                    Log.d("DAON", "enroll ERROR_ENROLL_QUALITY")
                    _faceCaptureState.update { state ->
                        state.copy(message = getResourceString(R.string.face_quality))
                    }
                    stopFaceCapture()
                    retakePhoto()
                }

                else -> {
                    Log.d("DAON", "enroll else retakePhoto")
                    _faceCaptureState.update { state ->
                        state.copy(message = getResourceString(R.string.face_enroll_failed))
                    }
                    stopFaceCapture()
                    retakePhoto()
                }
            }
        }
    }
 }