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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.FragmentTransaction;

import com.daon.fido.client.sdk.AuthenticationEventListener;
import com.daon.fido.client.sdk.IXUAF;
import com.daon.fido.client.sdk.IXUAFCheckPolicyListener;
import com.daon.fido.client.sdk.IXUAFPolicyAuthListListener;
import com.daon.fido.client.sdk.core.Error;
import com.daon.fido.client.sdk.core.INotifyUafResultCallback;
import com.daon.fido.client.sdk.core.IUafCancellableClientOperation;
import com.daon.fido.client.sdk.core.SingleShotAuthenticationRequest;
import com.daon.fido.client.sdk.model.AccountInfo;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.client.sdk.model.AuthenticatorReg;
import com.daon.fido.client.sdk.model.Transaction;
import com.daon.fido.client.sdk.state.Keys;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/***
 * This activity presents the option of creating FIDO transactions to the user.
 * It should be noted that it is not required that FIDO authenticators support transactions
 * and as such these may not work on devices with some authenticators.
 *
 */
public class TransactionActivity extends LoggedInActivity {

    private static final String TAG = TransactionActivity.class.getSimpleName();
    public static final String PREF_TRANSACTION_CONFIRAMATION = "pref_transaction_confirmation";
    public static final String PREF_GENERATE_OTP = "pref_generate_otp";
    private View progressView;
    private View transactionsView;
    private CheckBox checkBoxTxnConfirmation;
    private CheckBox checkBoxOtp;
    private Button singleShotAuthButton;
    private boolean authenticationInProgress;
    private boolean generateOtpChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        Log.d(TAG, "onCreate: ");

        checkBoxTxnConfirmation = findViewById(R.id.transaction_confirmation_check_box);
        UserPreferences.instance().putBoolean(PREF_TRANSACTION_CONFIRAMATION, checkBoxTxnConfirmation.isChecked());

        checkBoxTxnConfirmation.setOnClickListener(view -> UserPreferences.instance().putBoolean(PREF_TRANSACTION_CONFIRAMATION, checkBoxTxnConfirmation.isChecked()));

        checkBoxOtp = findViewById(R.id.otp_check_box);
        UserPreferences.instance().putBoolean(PREF_GENERATE_OTP, checkBoxOtp.isChecked());

        checkBoxOtp.setOnClickListener(view -> {
            UserPreferences.instance().putBoolean(PREF_GENERATE_OTP, checkBoxOtp.isChecked());
            generateOtpChecked = checkBoxOtp.isChecked();
        });

        transactionsView = findViewById(R.id.transaction_view);
        progressView = findViewById(R.id.transaction_progress);

        Button authenticateButton = findViewById(R.id.authenticate_button);
        authenticateButton.setOnClickListener(view -> checkForRegistrations());

        Button checkPolicyButton = findViewById(R.id.check_policy_button);
        checkPolicyButton.setOnClickListener(view -> {
            // Called to test that it works. Not used in any way.
            Keys keys = ((CoreApplication) getApplication()).getFido().getKeys(((CoreApplication) getApplication()).getAppId());
            Log.d(TAG, "Keys: " + keys);
            checkPolicy();
        });

        singleShotAuthButton = findViewById(R.id.single_shot_authenticate_button);
        singleShotAuthButton.setOnClickListener(view -> showDialog(null, getString(R.string.single_shot_alert_message), (dialog, which) -> TransactionActivity.this.performSingleShotAuth()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        enableSingleShotAuth();
    }

    private void enableSingleShotAuth() {
        Log.d(TAG, "enableSingleShotAuth: ");
        Keys keys = getFido().getKeys(((CoreApplication) getApplication()).getAppId(), ((CoreApplication) getApplication()).getEmail());
        singleShotAuthButton.setEnabled(keys.size() > 0);
    }

    private void checkPolicy() {
        Log.d(TAG, "checkPolicy: ");
        showProgress(true);
        ((CoreApplication) getApplication()).getCommService().serviceRequestAuthenticationWithParams(null, result -> {
            if (result.isSuccessful()) {
                Log.d(TAG, "serviceRequestAuthenticationWithParams: successful");
                getFido().checkMessage(result.getResponse(), null, new IXUAFCheckPolicyListener() {
                    @Override
                    public void onUafCheckPolicyComplete() {
                        Log.d(TAG, "onUafCheckPolicyComplete: ");
                        authenticationInProgress = false;
                        showProgress(false);
                        showMessage(R.string.check_policy_success, false);
                    }

                    @Override
                    public void onUafCheckPolicyFailed(int errorCode, String errorMessage) {
                        Log.e(TAG, "onUafCheckPolicyFailed: errorCode:" + errorCode + " errorMessage:" + errorMessage);
                        authenticationInProgress = false;
                        endProgressWithError(errorMessage);
                    }
                });
            } else {
                Log.e(TAG, "serviceRequestAuthenticationWithParams: " + result.getErrorInfo().getMessage());
                authenticationInProgress = false;
                endProgressWithError(result.getErrorInfo().getMessage());
            }
        });
    }


    private void performSingleShotAuth() {
        Log.d(TAG, "performSingleShotAuth: ");
        //showProgress(true);
        try {
            SingleShotAuthenticationRequest ssar = SingleShotAuthenticationRequest.createUserAuthWithAllRegisteredAuthenticators(this.getApplicationContext(), ((CoreApplication) getApplication()).getAppId(), ((CoreApplication) getApplication()).getEmail());
            ssar.addExtension("com.daon.sdk.deviceInfo", "true");
            ssar.addExtension("com.daon.passcode.type", "ALPHANUMERIC");
            String mAuthenticationRequest = ssar.toString();
            RPSAService service = (RPSAService) ((CoreApplication) getApplication()).getCommService();
            service.setmState(RPSAService.State.transaction);
            service.resetAuthRequestId();
            Bundle params = new Bundle();
            params.putString(IXUAF.IXUAF_SERVICE_PARAM_USERNAME, ((CoreApplication) getApplication()).getEmail());
            getFido().authenticateWithMessage(mAuthenticationRequest, params, authenticateTransactionEventListener);
        } catch (Exception e) {
            Log.e(TAG, "Failed to generate single shot authentication request: " + e.getMessage(), e);
        }
    }

    private void checkForRegistrations() {
        Log.d(TAG, "checkForRegistrations: ");
        getFido().checkForRegistrations(((CoreApplication) getApplication()).getEmail(), new IXUAFPolicyAuthListListener() {
            @Override
            public void onPolicyAuthListAvailable(AuthenticatorReg[] authenticatorRegs) {
                Log.d(TAG, "onPolicyAuthListAvailable: authenticators list size:" + authenticatorRegs.length);
                if (registeredAuthsPresent(authenticatorRegs)) {
                    attemptAuthentication();
                } else {
                    Log.e(TAG, "onPolicyAuthListAvailable: No suitable FIDO authenticator was found");
                    endProgressWithError("No suitable FIDO authenticator was found");
                }
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.e(TAG, "onError: errorCode:" + errorCode + " errorMessage:" + errorMessage);
                endProgressWithError(errorMessage);
            }
        });
    }

    private boolean registeredAuthsPresent(AuthenticatorReg[] authenticatorRegs) {
        for (AuthenticatorReg authenticatorReg : authenticatorRegs) {
            if (authenticatorReg.isRegistered()) {
                return true;
            }
        }
        return false;
    }


    /***
     * Attempt to perform a text transaction by asking the server to create the
     * transaction.
     *
     */
    protected void attemptAuthentication() {
        Log.d(TAG, "attemptAuthentication: ");
        //showProgress(true);
        authenticationInProgress = true;

        Bundle params = new Bundle();
        getFido().authenticateTransaction(params, authenticateTransactionEventListener);
    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        //check activity stack here
        getFido().submitDisplayTransactionResult(result.getResultCode(), null);
        authenticationInProgress = false;
        showProgress(true);
    });


    AuthenticationEventListener authenticateTransactionEventListener = new AuthenticationEventListener() {
        @Override
        public void onAuthListAvailable(Authenticator[][] authenticators) {
            Log.d(TAG, "onAuthListAvailable: authenticator list length: " + authenticators.length);
            if (authenticators.length == 1) {
                getFido().submitUserSelectedAuth(authenticators[0]);
            } else {
                showAuthenticatorChooser(authenticators);
            }

        }

        @Override
        public void onAuthenticationComplete() {
            Log.d(TAG, "onAuthenticationComplete: ");
            authenticationInProgress = false;
            showProgress(false);
            RPSAService service = (RPSAService) ((CoreApplication) getApplication()).getCommService();
            service.setmState(RPSAService.State.login);
            if (!generateOtpChecked) {
                showDialog(null, getString(R.string.transaction_validation_success), (dialog, which) -> finish());
            }
        }

        @Override
        public void onAuthenticationFailed(Error error) {
            if (error.getAuthenticator() != null) {
                Log.d(TAG, "onAuthenticationFailed errorCode :" + error.getCode() + " Authenticator :" + error.getAuthenticator().getTitle());
            } else {
                Log.d(TAG, "onAuthenticationFailed errorCode :" + error.getCode());
            }
            authenticationInProgress = false;
            endProgressWithError(error.getMessage());
            //RPSAService service = (RPSAService) ((CoreApplication) getApplication()).getCommService();
            //service.setmState(RPSAService.State.login);
            /*showDialog("Error", error.getMessage(), (dialog, which) -> {
                finish();
            });*/
        }

        @Override
        public void onAuthExpiryWarning(INotifyUafResultCallback.ExpiryWarning[] expiryWarnings) {
            Log.d(TAG, "onAuthExpiryWarning: warning error length" + expiryWarnings.length);
            displayExpiryWarnings(expiryWarnings);
        }

        @Override
        public void onAuthConfirmationOTP(String confirmationOTP) {
            Log.d(TAG, "onAuthConfirmationOTP: ");
            String message = getString(R.string.confirmation_otp_dialog_title) + ": " + confirmationOTP;
            showDialog(getString(R.string.transaction_validation_success), message, (dialog, which) -> finish());
        }

        @Override
        public void onAuthDisplayTransaction(Transaction transaction) {
            Log.d(TAG, "onAuthDisplayTransaction: ");
            String transactionData = new Gson().toJson(transaction);
            Intent intent = new Intent(TransactionActivity.this, DisplayTransactionActivity.class);
            intent.putExtra(DisplayTransactionActivity.TRANSACTION_EXTRA_INTENT_KEY, transactionData);
            mStartForResult.launch(intent);
        }

        @Override
        public void onAccountListAvailable(AccountInfo[] accountInfos) {
            Log.d(TAG, "onAccountListAvailable: ");
            showAccountChooser(accountInfos);
        }

        @Override
        public void onUserLockWarning() {
            Log.d(TAG, "onUserLockWarning");
            showMessage(R.string.user_lock_warning, false);
        }

        @Override
        public void onSubmitFailedAttemptFailed(int errorCode, String errorMessage) {
            Log.d(TAG, "ErrorCode :" + errorCode);
            if (errorCode == 1600 || errorCode == 1601 || errorCode == 1602 || errorCode == 1603 || errorCode == 1604 || errorCode == 1605) {
                getFido().cancelCurrentOperation();
            }
        }
    };

    private void displayExpiryWarnings(INotifyUafResultCallback.ExpiryWarning[] expiryWarnings) {
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
        builder.append("\n\nPlease registerWithUsername again.");
        showDialog(null, builder.toString(), (dialog, which) -> {

        });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {
        Log.d(TAG, "showProgress: ");
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        transactionsView.setVisibility(show ? View.GONE : View.VISIBLE);
        transactionsView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                transactionsView.setVisibility(show ? View.GONE : View.VISIBLE);
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
        new Handler(Looper.getMainLooper()).post(() -> showProgress(false));

        showMessage(errorMsg, false);
        transactionsView.requestFocus();
    }

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
            if (selectedAccount == 999) {
                //This will cancel the authentication.
                getFido().submitUserSelectedAccount(null);
            } else {
                showProgress(true);
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
            } else {
                showProgress(true);
                getFido().submitUserSelectedAuth(authenticators[position]);
            }
        });
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(authenticatorDialogFragment, "ChooseAuth_tag");
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        // Disable back button during authentication
        if (!authenticationInProgress) {
            super.onBackPressed();
        }
    }

    private void showDialog(String title, String message, DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TransactionActivity.this);
        if (title != null) {
            alertDialogBuilder.setTitle(title);
        }
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(R.string.dialog_confirm_yes, positiveListener);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
