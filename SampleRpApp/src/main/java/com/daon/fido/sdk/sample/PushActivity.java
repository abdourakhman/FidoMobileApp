package com.daon.fido.sdk.sample;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.FragmentTransaction;

import com.daon.fido.client.sdk.AuthenticationEventListener;
import com.daon.fido.client.sdk.IXUAFInitialiseListener;
import com.daon.fido.client.sdk.core.Error;
import com.daon.fido.client.sdk.core.INotifyUafResultCallback;
import com.daon.fido.client.sdk.model.AccountInfo;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.client.sdk.model.Transaction;
import com.daon.fido.sdk.sample.permission.PermissionHelper;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

public class PushActivity extends BaseActivity {

    private static final String TAG = PushActivity.class.getSimpleName();
    public static final String ADOS_FACE_AUTHENTICATOR_AAID = "D409#8201";
    public static final String ADOS_VOICE_AUTHENTICATOR_AAID = "D409#8401";
    public static final String FACE_AUTHENTICATOR_AAID = "D409#0205";
    public static final String VOICE_AUTHENTICATOR_AAID = "D409#0402";
    private boolean isInitialised = true;
    private String transactionId;
    private RPSAService.State currentState = RPSAService.State.none;
    private boolean generateOtpOccurred = false;
    private PermissionHelper permissionHelper;
    public Authenticator[] selectedAuthenticators = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push);

        Intent intent = getIntent();
        transactionId = intent.getStringExtra("transaction_id");
        Log.d(CoreApplication.TAG, "In PushActivity tr_id:" + transactionId);
        RPSAService service = (RPSAService) ((CoreApplication) getApplication()).getCommService();
        currentState = service.getmState();
        permissionHelper = new PermissionHelper(this, this::processGrantedPermission);
        if (!isUafInitialised()) {
            isInitialised = false;
            new InitialiseSdkTask(this, new IXUAFInitialiseListener() {
                @Override
                public void onInitialiseComplete() {
                    setIsUafInitialised(true);
                    authenticateWithNotification();
                }

                @Override
                public void onInitialiseWarnings(List<Error> warnings) {
                    //Handle warnings if necessary
                    setIsUafInitialised(true);
                    authenticateWithNotification();
                }

                @Override
                public void onInitialiseFailed(int code, String message) {
                    finish();
                }
            }).execute();
        } else {
            authenticateWithNotification();
        }
    }

    private void authenticateWithNotification() {
        if (UserPreferences.instance().getBoolean(SettingsActivity.PREF_SILENT_SRP_PASSCODE, false)) {
            Bundle params = new Bundle();
            params.putString("com.daon.passcode.value", "1111");
            getFido().authenticateWithNotification(transactionId, params, authenticationEventListener);
        } else {
            getFido().authenticateWithNotification(transactionId, null, authenticationEventListener);
        }
    }

    AuthenticationEventListener authenticationEventListener = new AuthenticationEventListener() {
        @Override
        public void onAuthListAvailable(Authenticator[][] authenticators) {
            if (authenticators.length == 1) {
                getFido().submitUserSelectedAuth(authenticators[0]);
            } else {
                showAuthenticatorChooser(authenticators);
            }
        }

        @Override
        public void onAccountListAvailable(AccountInfo[] accountInfos) {
            showAccountChooser(accountInfos);
        }

        @Override
        public void onAuthenticationComplete() {
            Log.d(TAG, "onAuthenticationComplete: ");
            if (!generateOtpOccurred) {
                showDialog(null, getString(R.string.transaction_validation_success), (dialog, which) -> {
                    resetState();
                    if (!isInitialised) {
                        startIntroActivity();
                    }
                    finish();
                });
            }
        }

        @Override
        public void onAuthConfirmationOTP(String confirmationOTP) {
            generateOtpOccurred = true;
            Log.d(TAG, "onAuthConfirmationOTP: ");
            String message = getString(R.string.confirmation_otp_dialog_title) + ": " + confirmationOTP;
            showDialog(getString(R.string.transaction_validation_success), message, (dialog, which) -> {
                resetState();
                if (!isInitialised) {
                    startIntroActivity();
                }
                finish();
            });
        }

        @Override
        public void onAuthenticationFailed(Error error) {
            Log.d(CoreApplication.TAG, "In PushActivity onUafAuthenticationFailed");
            showDialog("Error", error.getMessage(), (dialog, which) -> {
                resetState();
                if (!isInitialised) {
                    isInitialised = true;
                    startIntroActivity();
                }
                finish();
            });

        }

        @Override
        public void onAuthExpiryWarning(INotifyUafResultCallback.ExpiryWarning[] expiryWarnings) {
            StringBuilder builder = new StringBuilder();
            boolean firstTime = true;
            for (INotifyUafResultCallback.ExpiryWarning expiryWarning : expiryWarnings) {
                if (!firstTime) {
                    builder.append("\n\n");
                }
                builder.append("The registration with ");
                builder.append(expiryWarning.getAuthenticator().getTitle());
                builder.append(" will expire on ");
                Locale currentLocale = getResources().getConfiguration().locale;
                DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, currentLocale);
                builder.append(dateFormatter.format(expiryWarning.getExpiryDate()));
                builder.append(".");
                firstTime = false;
            }
            builder.append("\n\nPlease register again.");
            showDialog(null, builder.toString(), (dialog, which) -> {
            });
        }


        @Override
        public void onAuthDisplayTransaction(Transaction transaction) {
            Intent intent = new Intent(PushActivity.this, DisplayTransactionActivity.class);
            String transactionData = new Gson().toJson(transaction);
            intent.putExtra(DisplayTransactionActivity.TRANSACTION_EXTRA_INTENT_KEY, transactionData);

            mStartForResult.launch(intent);
        }


        public void resetState() {
            RPSAService service = (RPSAService) ((CoreApplication) getApplication()).getCommService();
            if (currentState != RPSAService.State.login) {
                service.resetState();
                service.resetSessionId();
            }
        }

        public void startIntroActivity() {
            Intent intent = new Intent(PushActivity.this, IntroActivity.class);
            startActivity(intent);
        }
    };

    private void showAccountChooser(AccountInfo[] accountInfos) {
        //Using account chooser DialogFragment
        String[] accounts = new String[accountInfos.length];
        for (int i = 0; i < accountInfos.length; i++) {
            accounts[i] = accountInfos[i].getUserName();
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable("accounts", accounts);

        ChooseAccountDialogFragment chooseAccountDialogFragment = new ChooseAccountDialogFragment(selectedAccount -> {
            if (selectedAccount == 999) {
                getFido().submitUserSelectedAccount(null);
            } else {
                getFido().submitUserSelectedAccount(accountInfos[selectedAccount]);
            }
        });

        chooseAccountDialogFragment.setArguments(bundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(chooseAccountDialogFragment, "ChooseAccount_tag");
        ft.commitAllowingStateLoss();
    }

    private void showAuthenticatorChooser(Authenticator[][] authenticators) {
        ((CoreApplication) getApplication()).setAuthenticators(authenticators);
        ChooseAuthenticatorDialogFragment authenticatorDialogFragment = new ChooseAuthenticatorDialogFragment(position -> {
            if (position == -1) {
                getFido().submitUserSelectedAuth(null);
                selectedAuthenticators = null;
            } else {
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

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        //check activity stack here
        getFido().submitDisplayTransactionResult(result.getResultCode(), null);
    });

    private void showDialog(String title, String message, DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PushActivity.this);
        if (title != null) {
            alertDialogBuilder.setTitle(title);
        }
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(R.string.dialog_confirm_yes, positiveListener);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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

    private void processGrantedPermission(String permission, Boolean granted) {
        if (permission.equals(android.Manifest.permission.CAMERA)) {
            if (!granted) {
                showMessage(R.string.permission_msg_camera, false);
                getFido().submitUserSelectedAuth(null);
            } else {
                getFido().submitUserSelectedAuth(selectedAuthenticators);
                selectedAuthenticators = null;
            }
        }
        if (permission.equals(Manifest.permission.RECORD_AUDIO)) {
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

