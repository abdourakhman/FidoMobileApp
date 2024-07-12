package com.daon.fido.sdk.sample.permission;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class PermissionHelper {
    @androidx.annotation.NonNull
    private final AppCompatActivity activity;
    private final ActivityResultLauncher<String[]> requestPermissionLauncher;

    public PermissionHelper(AppCompatActivity activity, PermissionRequestListener listener) {

        this.activity = activity;
        requestPermissionLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            for (String permissionKey : permissions.keySet()) {
                listener.permissionGranted(permissionKey, permissions.get(permissionKey));
            }
        });
    }

    public boolean checkCameraPermission() {
        return checkPermissions(Manifest.permission.CAMERA);
    }

    public boolean checkVoicePermission() {
        return checkPermissions(Manifest.permission.RECORD_AUDIO);
    }


    public void checkLocationPermission() {
        checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public void checkReadPhoneStatePermission() {
        checkPermissions(Manifest.permission.READ_PHONE_STATE);
    }

    public void checkAccessWifiPermission() {
        checkPermissions(Manifest.permission.ACCESS_WIFI_STATE);
    }

    public void checkPostNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
            }
        }
    }

    private boolean checkPermissions(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{permission});
                return false;
            }
        }
        // We have permission...
        return true;
    }

    private void requestPermissions(String[] permissions) {
        requestPermissionLauncher.launch(permissions);
    }

    public interface PermissionRequestListener {
        void permissionGranted(String permission, Boolean granted);
    }
}
