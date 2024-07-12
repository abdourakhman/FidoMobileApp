package com.daon.fido.sdk.sample.fragments;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.daon.fido.sdk.sample.R;
import com.daon.sdk.authenticator.ErrorCodes;
import com.daon.sdk.authenticator.Extensions;
import com.daon.sdk.authenticator.controller.AuthenticatorError;
import com.daon.sdk.authenticator.controller.OOTPControllerProtocol;
import com.daon.sdk.device.IXAErrorCodes;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.lang.reflect.Field;

public class OOTPFragment extends BaseCaptureFragment {

    private static final String TAG = OOTPFragment.class.getSimpleName();
    protected SurfaceView cameraView;
    protected BarcodeDetector barcode;
    protected CameraSource cameraSource;
    protected boolean flashEnabled;
    protected ImageButton flashButton;
    private boolean cameraStarted;
    private boolean surfaceCreated;

    public OOTPControllerProtocol getController() {
        return (OOTPControllerProtocol) super.getController();
    }

    public void setController(OOTPControllerProtocol controller) {
        super.setController(controller);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (isRegistration() || !getBooleanExtension(Extensions.OTP_TRANSACTION_UI, false)) {
            // No transaction UI required.
            return null;

        } else {
            // Transaction UI required, create it here. RP can override this to create their own.
            return onCreateTransactionView(inflater, container, savedInstanceState);
        }
    }

    protected View onCreateTransactionView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.daon_ootp_qr_scan, container, false);

        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            LinearLayout flashBar = view.findViewById(R.id.flash_bar);
            flashBar.setVisibility(View.GONE);
        }

        cameraView = view.findViewById(R.id.camera_view);
        cameraView.setZOrderMediaOverlay(true);
        barcode = new BarcodeDetector.Builder(this.getContext()).setBarcodeFormats(Barcode.QR_CODE).build();
        if (!barcode.isOperational()) {
            showMessage("Sorry, the detector could not be set up!", false);
            return null;
        }
        cameraSource = new CameraSource.Builder(this.getContext(), barcode).setFacing(CameraSource.CAMERA_FACING_BACK).setRequestedFps(24).setAutoFocusEnabled(true).setRequestedPreviewSize(1920, 1024).build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                surfaceCreated = true;
                startCameraSource();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int widt, int height) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                cameraSource.stop();
                cameraStarted = false;
                surfaceCreated = false;
            }

        });

        barcode.setProcessor(new Detector.Processor<>() {

            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() > 0) {
                    Barcode scannedBarcode = barcodes.valueAt(0);
                    if (scannedBarcode.displayValue.isEmpty()) {
                        completeCaptureWithError(new AuthenticatorError(ErrorCodes.ERROR_VERIFY_FAILED, getString(R.string.qr_code_empty)));
                    } else {
                        barcode.release();
                        if (getController() != null) {
                            getController().completeCapture(scannedBarcode.displayValue, new DefaultCaptureCompleteListener());
                        }
                    }
                }
            }
        });

        flashButton = view.findViewById(R.id.flash_button);
        flashButton.setOnClickListener(v -> {
            if (flashEnabled) {
                flashLightOff();
            } else {
                flashButton.setImageResource(R.drawable.flashlight_on);
                flashLightOn();
            }
        });

        return view;
    }

    protected void flashLightOn() {

        try {
            Camera cam = getCamera(cameraSource);
            Camera.Parameters p = cam.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            cam.setParameters(p);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        flashButton.setImageResource(R.drawable.flashlight_on);
        flashEnabled = true;
    }

    protected void flashLightOff() {
        try {
            Camera cam = getCamera(cameraSource);
            Camera.Parameters p = cam.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            cam.setParameters(p);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        flashButton.setImageResource(R.drawable.flashlight_off);
        flashEnabled = false;
    }

    protected static Camera getCamera(CameraSource cameraSource) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    Camera camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        return camera;
                    }
                    return null;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return null;
    }

    protected void startCameraSource() {
        if (!cameraStarted && surfaceCreated) {
            try {
                cameraSource.start(cameraView.getHolder());
                cameraStarted = true;
            } catch (IOException | SecurityException e) {
                Log.e(TAG, getString(R.string.qr_camera_start_failure), e);
                if (getController() != null) {
                    getController().completeCaptureWithError(new AuthenticatorError(IXAErrorCodes.ERROR_HW_UNAVAILABLE, getString(R.string.qr_camera_start_failure)));
                }
            }
        }
    }

    @Override
    public void start() {
        super.start();
        if (isRegistration() || !getBooleanExtension(Extensions.OTP_TRANSACTION_UI, false)) {
            // Transaction UI not required so complete authentication immediately
            if (getController() != null) {
                getController().completeCapture();
            }
        } else {
            startCameraSource();
        }
    }

    @Override
    protected void stop() {
        super.stop();
        if (flashEnabled) {
            flashLightOff();
        }
    }

    @Override
    protected int getCaptureFailedMessageId() {
        return R.string.ootp_verify_failed;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }
}
