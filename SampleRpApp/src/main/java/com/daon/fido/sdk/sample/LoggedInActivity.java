package com.daon.fido.sdk.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;

import com.daon.fido.client.sdk.core.IUafCancellableClientOperation;

public abstract class LoggedInActivity extends BaseActivity {
    private boolean visible;
    private volatile Intent pendingLogoutIntent;

    private void logMe(String msg) {
        Log.d("SampleRpApp", "***" + this.getClass().getName() + "***: " + msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLogoutBroadcastReceiver, new IntentFilter(CoreApplication.LOGOUT_BROADCAST_ACTION));
//        logMe("LoggedInActivity.onCreate: restart timer and register broadcast receiver");
        ((CoreApplication) this.getApplication()).logoutCountdown.cancel();
        ((CoreApplication) this.getApplication()).logoutCountdown.start();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        logMe("LoggedInActivity.onUserInteraction: restart timer");
        ((CoreApplication) this.getApplication()).logoutCountdown.cancel();
        ((CoreApplication) this.getApplication()).logoutCountdown.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        logMe("LoggedInActivity.onResume: visible");
        visible = true;
        if (pendingLogoutIntent != null) {
            performLogout(pendingLogoutIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        logMe("LoggedInActivity.onPause: not visible");
        visible = false;
    }

    @Override
    protected void onDestroy() {
        logMe("LoggedInActivity.onDestroy: unregister broadcast receiver");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLogoutBroadcastReceiver);
        super.onDestroy();
    }

    private final BroadcastReceiver mLogoutBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            logMe("Receive logout intent broadcast");
            getFido().cancelCurrentOperation();
            if (visible) {
                logMe("Visible activity, perform logout");
                performLogout(intent);
            } else {
                logMe("Invisible activity, store pending logout intent");
                pendingLogoutIntent = intent;
            }
        }
    };

    protected void performLogout(Intent logoutIntent) {
        logMe("LoggedInActivity.performLogout");
        showProgress(false);

        pendingLogoutIntent = null;
        Bundle extras = logoutIntent.getExtras();
        boolean success = extras.getBoolean(CoreApplication.EXTRA_LOGOUT_SUCCESS);
        boolean timeout = extras.getBoolean(CoreApplication.EXTRA_LOGOUT_TIMEOUT);
        int errorCode = extras.getInt(CoreApplication.EXTRA_LOGOUT_ERROR_CODE);
        String errorMessage = extras.getString(CoreApplication.EXTRA_LOGOUT_ERROR_MESSAGE);

        logMe("LoggedInActivity.performLogout: success = " + success + ", timeout: " + timeout);

        ((CoreApplication) this.getApplication()).logoutCountdown.cancel();

        if (success) {
            returnToIntro(timeout);
        } else {
            logMe("LoggedInActivity.performLogout: errorCode: " + errorCode + ", errorMessage: " + errorMessage);
            String error = getResources().getString(R.string.logout_failed) + "\n" + errorMessage;
            showMessage(error, false);
        }
    }

    protected void returnToIntro(boolean timeout) {
        logMe("LoggedInActivity.returnToIntro with timeout: " + timeout);

        ((CoreApplication) this.getApplication()).setSessionId(null);
        Intent newIntent = new Intent(this, IntroActivity.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(newIntent);
        finish();
        if (timeout) {
            showMessage(R.string.logout_due_to_inactivity, false);
        }
    }

    public void showProgress(final boolean show) {

    }
}
