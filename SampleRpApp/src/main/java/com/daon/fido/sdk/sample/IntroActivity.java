/*
 * Copyright Daon.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.daon.fido.sdk.sample;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.daon.fido.client.sdk.AuthenticationEventListener;
import com.daon.fido.client.sdk.IXUAF;
import com.daon.fido.client.sdk.OOTPGenerationEventListener;
import com.daon.fido.client.sdk.core.Error;
import com.daon.fido.client.sdk.core.IFidoSdk;
import com.daon.fido.client.sdk.core.IUafCancellableClientOperation;
import com.daon.fido.client.sdk.model.AccountInfo;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.sdk.sample.model.CreateSessionResponse;
import com.daon.fido.sdk.sample.permission.PermissionHelper;

import java.text.DateFormat;
import java.util.Date;

/**
 * The first screen displayed which offers the user the option to
 * 1. Login with username & password
 * 2. Login with FIDO
 * 3. Add a new account
 */
public class IntroActivity extends BaseActivity {
    private static final String TAG = IntroActivity.class.getSimpleName();
    public static final String AUTH_COMPLETE_BROADCAST_ACTION = "com.daon.fido.sdk.sample.AUTH_COMPLETE_BROADCAST";
    public static final String ADOS_FACE_AUTHENTICATOR_AAID = "D409#8201";
    public static final String ADOS_VOICE_AUTHENTICATOR_AAID = "D409#8401";
    public static final String FACE_AUTHENTICATOR_AAID = "D409#0205";
    public static final String VOICE_AUTHENTICATOR_AAID = "D409#0402";
    // UI Components
    private View progressView;
    private View introView;
    private Button fidoLoginButton;
    private Button generateOtpButton;
    private boolean authenticationInProgress;
    public String chosenAccountUsername;
    private boolean paused;
    private boolean loggedInWhilePaused;
    private CreateSessionResponse createSessionResponse;
    public Authenticator[] selectedAuthenticators = null;
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        permissionHelper = new PermissionHelper(this, this::processGrantedPermission);
        checkAllPermissions();

        setContentView(R.layout.activity_intro);

        introView = findViewById(R.id.intro_form);
        progressView = findViewById(R.id.intro_progress);

        Button newAccountButton = findViewById(R.id.new_account_button);
        newAccountButton.setOnClickListener(view -> {
            Log.d(TAG, "startCreateAccountActivity: ");
            Intent newIntent = new Intent(this, CreateAccountActivity.class);
            startActivity(newIntent);
        });

        Button passwordLoginButton = findViewById(R.id.login_password_button);
        passwordLoginButton.setOnClickListener(view -> {
            RPSAService service = (RPSAService) ((CoreApplication) getApplication()).getCommService();
            service.resetState();
            service.resetSessionId();

            Intent newIntent = new Intent(this, LoginActivity.class);
            startActivity(newIntent);
        });

        fidoLoginButton = findViewById(R.id.login_fido_button);
        fidoLoginButton.setOnClickListener(view -> {
            RPSAService service = (RPSAService) ((CoreApplication) getApplication()).getCommService();
            service.resetState();
            service.resetSessionId();
            attemptFIDOLogin();
        });

        generateOtpButton = findViewById(R.id.generate_ootp_button);
        generateOtpButton.setOnClickListener(v -> {
            Log.d(CoreApplication.TAG, "OOTP time to live: " + getFido().getOOTPTimeToLive());
            generateOOTP();
        });

        fidoLoginButton.setEnabled(this.hasFIDOClient() && !((CoreApplication) getApplication()).getAvailableAuthenticators().isEmpty());
        generateOtpButton.setEnabled(this.hasFIDOClient() && !((CoreApplication) getApplication()).getAvailableAuthenticators().isEmpty() && ((CoreApplication) getApplication()).getLastLoggedInUserEmail() != null);
        introView.setVisibility(View.VISIBLE);

    }

    private void processGrantedPermission(String permission, Boolean granted) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION -> {
                if (!granted) {
                    showMessage(R.string.permission_msg_location, false);
                }
                checkReadPhoneState();
            }
            case Manifest.permission.READ_PHONE_STATE -> {
                if (!granted) {
                    showMessage(R.string.permission_msg_read_phone_state, false);
                }
                checkAccessWifi();
            }
            case Manifest.permission.ACCESS_WIFI_STATE -> {
                if (!granted) {
                    showMessage(R.string.permission_msg_wifi, false);
                }
                checkPostNotifications();
            }
            case Manifest.permission.POST_NOTIFICATIONS -> {
                if (!granted) {
                    showMessage(R.string.permission_msg_wifi, false);
                }
            }
            case Manifest.permission.CAMERA -> {
                if (!granted) {
                    showMessage(R.string.permission_msg_camera, false);
                    getFido().submitUserSelectedAuth(null);
                } else {
                    getFido().submitUserSelectedAuth(selectedAuthenticators);
                    selectedAuthenticators = null;
                }
            }
            case Manifest.permission.RECORD_AUDIO -> {
                if (!granted) {
                    showMessage(R.string.permission_msg_voice, false);
                    getFido().submitUserSelectedAuth(null);
                } else {
                    getFido().submitUserSelectedAuth(selectedAuthenticators);
                    selectedAuthenticators = null;
                }
            }
        }
    }

    private void checkAllPermissions() {
        permissionHelper.checkLocationPermission();
        permissionHelper.checkReadPhoneStatePermission();
        permissionHelper.checkAccessWifiPermission();
        permissionHelper.checkPostNotificationsPermission();
    }

    @Override
    protected void onPause() {
        paused = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        fidoLoginButton.setEnabled(this.hasFIDOClient() && !((CoreApplication) getApplication()).getAvailableAuthenticators().isEmpty());
        generateOtpButton.setEnabled(this.hasFIDOClient() && !((CoreApplication) getApplication()).getAvailableAuthenticators().isEmpty() && ((CoreApplication) getApplication()).getLastLoggedInUserEmail() != null);
        if (loggedInWhilePaused) {
            loggedInWhilePaused = false;
            showLoggedIn(createSessionResponse);
        }
    }

    public void generateOOTP() {
        Log.d(TAG, "generateOOTP: ");
        IFidoSdk.OOTPGenerationMode ootpGenerationMode = UserPreferences.instance().getBoolean(SettingsActivity.PREF_SIGN_OOTP, false) ? IFidoSdk.OOTPGenerationMode.SignWithOTP : IFidoSdk.OOTPGenerationMode.IdentifyWithOTP;
        Bundle inParams = new Bundle();
        if (UserPreferences.instance().getBoolean(SettingsActivity.PREF_NATIVE_LOGON_ALWAYS, false)) {
            inParams.putString(IXUAF.IXUAF_PARAM_AUTH_FILTER, IXUAF.IXUAF_AUTH_FILTER_UNREGISTERED_EMBEDDED);
        }

        getFido().generateOOTP(((CoreApplication) getApplication()).getAppId(), inParams, ootpGenerationMode, ootpGenerationEventListener);
    }

    OOTPGenerationEventListener ootpGenerationEventListener = new OOTPGenerationEventListener() {
        @Override
        public void onOOTPGenerated(final String otp, final String transactionData, Authenticator[] authenticators, Bundle extraParams) {
            Log.d(TAG, "onOOTPGenerated: ");
            authenticationInProgress = false;
            broadcastAuthComplete();

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(IntroActivity.this);
            alertDialogBuilder.setTitle(R.string.offline_otp_dialog_title);
            alertDialogBuilder.setMessage(otp);

            alertDialogBuilder.setPositiveButton("Accept", (arg0, arg1) -> submitOtp(otp, transactionData, chosenAccountUsername));

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        @Override
        public void onAccountListAvailable(AccountInfo[] accountInfos) {
            Log.d(TAG, "onAccountListAvailable: ");
            showAccountChooser(accountInfos);
        }

        @Override
        public void onOOTPGenerationFailed(Error error) {
            Log.e(TAG, "onOOTPGenerationFailed: " + error);
            authenticationInProgress = false;
            broadcastAuthComplete();
            showProgress(false);
            endProgressWithError(error.getMessage());
        }

        @Override
        public void onAuthListAvailable(Authenticator[][] authenticators) {
            Log.d(TAG, "onAuthListAvailable: ");
            if (authenticators.length == 1) {
                getFido().submitUserSelectedAuth(authenticators[0]);
            } else {
                showAuthenticatorChooser(authenticators);
            }
        }
    };

    public void attemptFIDOLogin() {
        Log.d(TAG, "attemptFIDOLogin: ");
        showProgress(true);
        authenticationInProgress = true;
        Bundle inParams = new Bundle();
        if (UserPreferences.instance().getBoolean(SettingsActivity.PREF_NATIVE_LOGON_ALWAYS, false)) {
            inParams.putString(IXUAF.IXUAF_PARAM_AUTH_FILTER, IXUAF.IXUAF_AUTH_FILTER_UNREGISTERED_EMBEDDED);
        }
        String[] registeredFidoAccounts = ((CoreApplication) getApplication()).getRegisteredFidoAccounts();
        if (registeredFidoAccounts != null && registeredFidoAccounts.length == 1) {
            String description = ((CoreApplication) getApplication()).getEmail() + " login " + new Date();
            if (UserPreferences.instance().getBoolean(SettingsActivity.PREF_SILENT_SRP_PASSCODE, false)) {
                // Emulate passing the SRP passcode value for a silent SRP passcode registration
                inParams.putString("com.daon.passcode.value", "1111");
            }
            getFido().authenticateWithUserNameAndDescription(registeredFidoAccounts[0], description, inParams, authenticateEventListener);
        } else {
            if (UserPreferences.instance().getBoolean(SettingsActivity.PREF_SILENT_SRP_PASSCODE, false)) {
                // Emulate passing the SRP passcode value for a silent SRP passcode registration
                inParams.putString("com.daon.passcode.value", "1111");
            }
            getFido().authenticate(inParams, authenticateEventListener);
        }
    }

    AuthenticationEventListener authenticateEventListener = new AuthenticationEventListener() {
        @Override
        public void onAuthListAvailable(Authenticator[][] authenticators) {
            Log.d(TAG, "onAuthListAvailable: ");
            if (authenticators.length == 1) {
                getFido().submitUserSelectedAuth(authenticators[0]);
            } else {
                showAuthenticatorChooser(authenticators);
            }
        }

        @Override
        public void onAccountListAvailable(AccountInfo[] accountInfos) {
            Log.d(TAG, "onAccountListAvailable: ");
            CustomCaptureActivity customCaptureActivity = CustomCaptureActivity.getInstance();
            if (customCaptureActivity != null) {
                customCaptureActivity.showAccountChooser(accountInfos);
            } else {
                showAccountChooser(accountInfos);
            }
        }

        @Override
        public void onAuthenticationComplete() {
            Log.d(TAG, "onAuthenticationComplete: ");
            authenticationInProgress = false;
            showProgress(false);
            RPSAService service = (RPSAService) ((CoreApplication) getApplication()).getCommService();
            if (paused) {
                loggedInWhilePaused = true;
                createSessionResponse = service.getCreateSessionResponse();
            } else {
                showLoggedIn(service.getCreateSessionResponse());
            }
            service.setmState(RPSAService.State.login);
        }

        @Override
        public void onAuthenticationFailed(Error error) {
            Log.e(TAG, "onAuthenticationFailed: " + error);
            authenticationInProgress = false;
            showProgress(false);
            endProgressWithError(error.getCode() + ": " + error.getMessage());
            RPSAService service = (RPSAService) ((CoreApplication) getApplication()).getCommService();
            service.resetState();
            service.resetSessionId();
        }

        @Override
        public void onUserLockWarning() {
            Log.d(TAG, "onUserLockWarning");
            showMessage(R.string.user_lock_warning, false);
        }

        @Override
        public void onSubmitFailedAttemptFailed(int errorCode, String errorMessage) {
            Log.e(TAG, "onSubmitFailedAttemptFailed: " + errorCode);
            if (errorCode == 1600 || errorCode == 1601 || errorCode == 1602 || errorCode == 1603 || errorCode == 1604 || errorCode == 1605) {
                getFido().cancelCurrentOperation();
            }
        }
    };

    private void showAccountChooser(AccountInfo[] accountInfos) {
        Log.d(TAG, "showAccountChooser: ");
        //Using account chooser DialogFragment
        String[] accounts = new String[accountInfos.length];
        for (int i = 0; i < accountInfos.length; i++) {
            accounts[i] = accountInfos[i].getUserName();
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable("accounts", accounts);
        showProgress(false);

        ChooseAccountDialogFragment chooseAccountDialogFragment = new ChooseAccountDialogFragment(selectedAccount -> {
            if (selectedAccount == -1) {
                //This will cancel the authentication.
                getFido().submitUserSelectedAccount(null);
            } else {
                showProgress(true);
                chosenAccountUsername = accountInfos[selectedAccount].getUserName();
                getFido().submitUserSelectedAccount(accountInfos[selectedAccount]);
            }
        });

        chooseAccountDialogFragment.setArguments(bundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(chooseAccountDialogFragment, "ChooseAccount_tag");
        ft.commitAllowingStateLoss();
    }

    private void showAuthenticatorChooser(Authenticator[][] authenticators) {
        Log.d(TAG, "showAuthenticatorChooser: ");
        ((CoreApplication) getApplication()).setAuthenticators(authenticators);
        showProgress(false);
        ChooseAuthenticatorDialogFragment authenticatorDialogFragment = new ChooseAuthenticatorDialogFragment(position -> {
            if (position == -1) {
                //This will cancel the authentication.
                getFido().submitUserSelectedAuth(null);
                selectedAuthenticators = null;
            } else {
                showProgress(true);
                selectedAuthenticators = authenticators[position];
                if (checkPermissionForAuthenticator(selectedAuthenticators)) {
                    getFido().submitUserSelectedAuth(authenticators[position]);
                    selectedAuthenticators = null;
                }

            }
        });
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(authenticatorDialogFragment, "ChooseAuth_tag");
        ft.commitAllowingStateLoss();
    }

    private void broadcastAuthComplete() {
        Log.d(TAG, "broadcastAuthComplete: ");
        Intent localIntent = new Intent(AUTH_COMPLETE_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /***
     * The login has been successful and the server has returned a session Id and some additional
     * details to present to the user.
     *
     * @param response the create session response
     */
    protected void showLoggedIn(CreateSessionResponse response) {
        Log.d(TAG, "showLoggedIn: ");
        try {
            ((CoreApplication) getApplication()).setSessionId(response.getSessionId());
            ((CoreApplication) getApplication()).setEmail(response.getEmail());

            Intent newIntent = new Intent(this, HomeActivity.class);
            newIntent.putExtra("LOGGED_IN_WITH", response.getLoggedInWith().toString());
            if (response.getLastLoggedIn() == null) {
                newIntent.putExtra("LAST_LOGGED_IN", getString(R.string.message_first_login));
            } else {
                String dateString = DateFormat.getDateTimeInstance().format(response.getLastLoggedIn());
                newIntent.putExtra("LAST_LOGGED_IN", dateString);
            }

            startActivity(newIntent);
        } catch (Throwable e) {
            showMessage(e.getMessage(), false);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        introView.setVisibility(show ? View.GONE : View.VISIBLE);
        introView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                introView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    protected void endProgressWithError(String errorMsg) {
        Log.d(TAG, "endProgressWithError: " + errorMsg);
        showProgress(false);
        showMessage(errorMsg, false);
        introView.requestFocus();
    }


    protected void submitOtp(String otp, String transactionData, String emailAddress) {
        Log.d(TAG, "submitOtp: ");
        String[] registeredFidoAccounts = ((CoreApplication) getApplication()).getRegisteredFidoAccounts();
        if (registeredFidoAccounts != null) {
            if (registeredFidoAccounts.length == 1) {
                emailAddress = registeredFidoAccounts[0];
            }
            // Send data to server
            new SubmitOfflineOTPTask(this.getApplicationContext(), emailAddress, otp, transactionData, new SubmitOfflineOTPTask.SubmitOfflineOTPCallback() {
                @Override
                public void onSubmitOfflineOTPComplete(String response) {
                    Log.d(TAG, "Successfully submitted OOTP to server");
                }

                @Override
                public void onSubmitOfflineOTPFailed(int code, String message) {
                    Log.e(TAG, "Failed to submit OOTP to server. Code: " + code + ", Message: " + message);
                }
            }).execute();
        }
    }

    private void checkReadPhoneState() {
        permissionHelper.checkReadPhoneStatePermission();
        checkAccessWifi();
    }

    private void checkAccessWifi() {
        permissionHelper.checkAccessWifiPermission();
        checkPostNotifications();
    }

    private void checkPostNotifications() {
        permissionHelper.checkPostNotificationsPermission();
    }

    @Override
    public void onBackPressed() {
        // Disable back button during authentication
        if (!authenticationInProgress) {
            super.onBackPressed();
        }
    }

    private boolean checkPermissionForAuthenticator(Authenticator[] authenticators) {
        for (Authenticator authenticator : authenticators) {
            Log.d(TAG, "checkPermissionForAuthenticator: " + authenticator.getAaid());
            switch (authenticator.getAaid()) {
                case ADOS_FACE_AUTHENTICATOR_AAID, FACE_AUTHENTICATOR_AAID -> {
                    return permissionHelper.checkCameraPermission();
                }
                case VOICE_AUTHENTICATOR_AAID, ADOS_VOICE_AUTHENTICATOR_AAID -> {
                    return permissionHelper.checkVoicePermission();
                }
            }
        }
        return true;
    }
}

