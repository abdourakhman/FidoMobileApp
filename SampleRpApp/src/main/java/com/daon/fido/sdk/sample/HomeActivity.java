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
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.daon.fido.client.sdk.IXUAFDeregisterEventListener;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.exception.ServerError;
import com.daon.fido.sdk.sample.model.AuthenticatorInfo;
import com.daon.fido.sdk.sample.model.DeleteAccountResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the activity presented to the user after:
 * 1. he/she logs in
 * 2. after the account is created
 * <p>
 * It presents a number of options to the user:
 * 1. Logout
 * 2. Delete the account
 * 3. Start an activity to manage the FIDO Authenticators
 * 4. Start an activity to perform FIDO Transactions
 */
public class HomeActivity extends LoggedInActivity {

    private AccountDeleteTask mAccountDeleteTask = null;

    private List<AuthenticatorInfo> authsToDeactivate = new ArrayList<>();

    // UI references.
    private View mProgressView;
    private View mHomeFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Button mAuthenticatorsButton = findViewById(R.id.authenticators_button);
        mAuthenticatorsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                manageAuthenticators();
            }
        });

        Button mDeleteAccountButton = findViewById(R.id.delete_account_button);
        mDeleteAccountButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAccount();
            }
        });

        Button mLogoutButton = findViewById(R.id.logout_button);
        mLogoutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });


        Button mTransactionButton = findViewById(R.id.step_up_auth_button);
        if (this.hasFIDOClient() && !((CoreApplication) getApplication()).getAvailableAuthenticators().isEmpty()) {
            mTransactionButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    showTransactions();
                }
            });
        } else {
            mTransactionButton.setEnabled(false);
        }

        Button javascriptApi = findViewById(R.id.javascriptapi);
        javascriptApi.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showJavascriptApiDemoPage();
            }
        });

        // Set the info fields on the screen
        ((TextView) findViewById(R.id.textView_user)).setText(((CoreApplication) getApplication()).getEmail());
        String lastLoggedIn = getIntent().getExtras().getString("LAST_LOGGED_IN");
        ((TextView) findViewById(R.id.textView_last_logged_in_date)).setText(lastLoggedIn);
        String loggedInWith = getIntent().getExtras().getString("LOGGED_IN_WITH");
        ((TextView) findViewById(R.id.textView_last_logged_in_with)).setText(loggedInWith);

        mHomeFormView = findViewById(R.id.home_form);
        mProgressView = findViewById(R.id.home_progress);
    }

    private void showJavascriptApiDemoPage() {
        Intent intent = new Intent(this, JavascriptApiDemoActivity.class);
        startActivity(intent);
    }

    /***
     * Start the intent to manage authenticators
     */
    public void manageAuthenticators() {

        try {
            Intent newIntent = new Intent(this, AuthenticatorsActivity.class);
            startActivity(newIntent);
        } catch (Throwable e) {
            showMessage(e.getMessage(), false);
        }
    }

    /***
     *  Delete the account of the current user
     *  Ask the user to confirm the deletion first.
     *  All FIDO authenticators should be deactivated before the account is deleted.  This
     *  will be done by the server operation which will return a list of FIDO authenticators
     *  it just deactivated.  If there are corresponding authenticators on this device, these
     *  should be deregistered.
     */
    public void deleteAccount() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.dialog_delete_confirmation);

        alertDialogBuilder.setPositiveButton(R.string.dialog_delete_confirmation_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                showProgress(true);
                mAccountDeleteTask = new AccountDeleteTask();
                mAccountDeleteTask.execute();
            }
        });

        alertDialogBuilder.setNegativeButton(R.string.dialog_delete_confirmation_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    /**
     * Logout
     * Basically, delete the session and finish this Activity
     */
    public void logout() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.dialog_logout_confirmation);

        alertDialogBuilder.setPositiveButton(R.string.dialog_logout_confirmation_yes, (arg0, arg1) -> ((CoreApplication) getApplication()).doLogout(false));

        alertDialogBuilder.setNegativeButton(R.string.dialog_logout_confirmation_no, (dialog, which) -> {
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Start the transaction activity
     */
    protected void showTransactions() {
        try {
            Intent newIntent = new Intent(this, TransactionActivity.class);
            startActivity(newIntent);
        } catch (Throwable e) {
            showMessage(e.getMessage(), false);
        }
    }

    /***
     * Process the list of deactivated FIDO authenticators by
     *      1. filtering the list to remove authenticators not available
     *      2. starting the process of deregistering the first
     * @param authInfos - the array of authenticator information
     */
    protected void processDeactivatedFIDOAuthenticators(AuthenticatorInfo[] authInfos) {

        this.authsToDeactivate.clear();
        if (authInfos != null) {
            for (AuthenticatorInfo authInfo : authInfos) {
                if (hasAuthenticator(authInfo.getAaid())) {
                    this.authsToDeactivate.add(authInfo);
                }
            }
        }
        this.deregisterNextAuthenticator();
    }

    /***
     * Check to see if there are any authenticators left to deregister.
     * If there are not, finish this activity and alert the user that the account has been deleted
     * If there are, call the deregistraton FIDO intent
     */
    protected void deregisterNextAuthenticator() {

        if (authsToDeactivate.size() == 0) {
            ((CoreApplication) getApplication()).removeRegisteredFidoAccount(((CoreApplication) getApplication()).getEmail());
            RPSAService service = (RPSAService) ((CoreApplication) getApplication()).getCommService();
            service.resetSessionId();
            service.resetState();
            showProgress(false);
            showMessage(R.string.message_account_deleted, false);
            finish();
            return;
        }
        AuthenticatorInfo authenticatorInfo = this.authsToDeactivate.remove(0);
        /**
         * Attempt to deregister all remaining authenticators on the device, even if this dereg fails.
         * Failure may be because the FIDO Authenticator on the device is not registered.
         */
        getFido().deregisterWithMessage(authenticatorInfo.getFidoDeregistrationRequest(), new IXUAFDeregisterEventListener() {
            @Override
            public void onDeregistrationComplete() {
                deregisterNextAuthenticator();
            }

            @Override
            public void onDeregistrationFailed(int errorCode, String errorMessage) {
                deregisterNextAuthenticator();
            }
        });
    }


    /**
     * Shows the progress UI and hides the home form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mHomeFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mHomeFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mHomeFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mHomeFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Callback when there is an error received from the server
     *
     * @param errorMsg - the error message
     */
    protected void endProgressWithError(String errorMsg) {
        showProgress(false);
        showMessage(errorMsg, false);
    }


    /**
     * Represents an asynchronous task used to delete a user account
     */
    public class AccountDeleteTask extends TaskExecutor<ServerOperationResult<DeleteAccountResponse>> {
        @Override
        protected ServerOperationResult<DeleteAccountResponse> doInBackground() {
            ServerOperationResult<DeleteAccountResponse> result;
            try {
                RPSAService service = (RPSAService) ((CoreApplication) getApplication()).getCommService();
                DeleteAccountResponse response = service.deleteAccount(((CoreApplication) getApplication()).getSessionId());
                result = new ServerOperationResult<>(response);
            } catch (ServerError e) {
                result = new ServerOperationResult<>(e.getError());
            }
            return result;
        }

        @Override
        protected void onPostExecute(ServerOperationResult<DeleteAccountResponse> response) {
            if (response.isSuccessful()) {
                showProgress(false);
                processDeactivatedFIDOAuthenticators(response.getResponse().getFidoDeregistrationRequests());
            } else {
                showMessage(response.getError().getMessage(), false);
            }
        }
    }

    @Override
    public void onBackPressed() {
        logout();
    }
}

