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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.FragmentTransaction;

import com.daon.fido.client.sdk.IXUAFDeregisterEventListener;
import com.daon.fido.client.sdk.IXUAFPolicyAuthListListener;
import com.daon.fido.client.sdk.IXUAFRegisterEventListener;
import com.daon.fido.client.sdk.core.Error;
import com.daon.fido.client.sdk.core.IFidoSdk;
import com.daon.fido.client.sdk.core.INotifyUafResultCallback;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.client.sdk.model.AuthenticatorReg;
import com.daon.fido.client.sdk.state.Keys;
import com.daon.fido.client.sdk.ui.UIHelper;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.exception.CommunicationsException;
import com.daon.fido.sdk.sample.exception.ServerError;
import com.daon.fido.sdk.sample.model.AuthenticatorInfo;
import com.daon.fido.sdk.sample.model.GetAuthenticatorResponse;
import com.daon.fido.sdk.sample.model.ListAuthenticatorsResponse;
import com.daon.fido.sdk.sample.permission.PermissionHelper;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A logged in screen
 */
public class AuthenticatorsActivity extends LoggedInActivity {
    private static final String TAG = AuthenticatorsActivity.class.getSimpleName();
    private static final String ARCHIVED_STATUS = "ARCHIVED";
    public static final String ADOS_FACE_AUTHENTICATOR_AAID = "D409#8201";
    public static final String ADOS_VOICE_AUTHENTICATOR_AAID = "D409#8401";
    public static final String FACE_AUTHENTICATOR_AAID = "D409#0205";
    public static final String VOICE_AUTHENTICATOR_AAID = "D409#0402";
    private enum Action {NONE, REGISTER, DEREGISTER}
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private Action currentAction = Action.NONE;
    private ListAuthenticatorsTask listAuthenticatorsTask = null;
    private GetAuthenticatorTask getAuthenticatorTask = null;
    private AuthenticatorInfo selectedAuthenticationInfo;
    private boolean registrationInProgress;
    private View progressView;
    private View authenticatorsFormView;
    private Button deregisterButton;
    public Authenticator[] selectedAuthenticators = null;
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticators);

        deregisterButton = findViewById(R.id.deregister_button);
        deregisterButton.setOnClickListener(view -> attemptDeregister());
        deregisterButton.setEnabled(false);

        Button listAuthenticatorsButton = findViewById(R.id.list_registered_authenticators_button);
        listAuthenticatorsButton.setOnClickListener(view -> listRegisteredAuthenticators());

        Button registerButton = findViewById(R.id.register_new_authenticator_button);
        registerButton.setOnClickListener(view -> attemptRegistration());

        ListView listView = findViewById(R.id.list_view_authenticators);
        listView.setSelector(R.drawable.listitem_background);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedAuthenticationInfo = (AuthenticatorInfo) view.getTag();
            // If the authenticator is archived on the server and is either not available on this device or is not registered with the user
            // then disable deregistration
            deregisterButton.setEnabled((hasAuthenticator(selectedAuthenticationInfo.getAaid()) && getFido().isRegistered(selectedAuthenticationInfo.getAaid(), ((CoreApplication) getApplication()).getEmail(), ((CoreApplication) getApplication()).getAppId())) || !selectedAuthenticationInfo.getStatus().equals(ARCHIVED_STATUS));
        });

        authenticatorsFormView = findViewById(R.id.fido_authenticators_form);
        progressView = findViewById(R.id.authenticators_progress);

        permissionHelper = new PermissionHelper(this, this::processGrantedPermission);
        this.refreshAuthenticators();
    }

    private void processGrantedPermission(String permission, Boolean granted) {
        if (permission.equals(Manifest.permission.CAMERA)) {
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

    protected void listRegisteredAuthenticators() {
        Log.d(TAG, "listRegisteredAuthenticators: ");
        showProgress(true);
        getFido().checkForRegistrations(((CoreApplication) getApplication()).getEmail(), new IXUAFPolicyAuthListListener() {
            @Override
            public void onPolicyAuthListAvailable(AuthenticatorReg[] authenticatorRegs) {
                Log.d(TAG, "onPolicyAuthListAvailable: " + Arrays.toString(authenticatorRegs));
                showProgress(false);
                filterAuthenticators(authenticatorRegs);
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.e(TAG, "onError: " + errorMessage);
                showProgress(false);
                showMessage(errorMessage, false);
            }
        });
    }

    private void filterAuthenticators(AuthenticatorReg[] authenticatorRegs) {
        Log.d(TAG, "filterAuthenticators: ");
        AuthenticatorReg[] filteredMatchingAuthenticators = filterMatchingAuthenticators(authenticatorRegs);
        if (filteredMatchingAuthenticators.length > 0) {
            Intent intent = new Intent(AuthenticatorsActivity.this, ListAuthenticatorsActivity.class);
            intent.putExtra(ListAuthenticatorsActivity.EXTRA_AUTHENTICATORS, new Gson().toJson(convertForTransfer(filteredMatchingAuthenticators)));
            mStartForResult.launch(intent);
        } else {
            showMessage(getString(R.string.no_authenticators_registered), false);
        }
    }

    protected TransferAuthenticator[] convertForTransfer(AuthenticatorReg[] authenticators) {
        Log.d(TAG, "convertForTransfer: ");
        TransferAuthenticator[] transferAuthenticators = new TransferAuthenticator[authenticators.length];
        for (int i = 0; i < transferAuthenticators.length; i++) {
            transferAuthenticators[i] = new TransferAuthenticator();
            transferAuthenticators[i].aaid = authenticators[i].getAaid();
            transferAuthenticators[i].isRegistered = authenticators[i].isRegistered();
        }
        return transferAuthenticators;
    }

    protected AuthenticatorReg[] filterMatchingAuthenticators(AuthenticatorReg[] matchingAuthenticators) {
        Log.d(TAG, "filterMatchingAuthenticators: ");
        if (!UserPreferences.instance().getBoolean(SettingsActivity.PREF_LIST_AVAILABLE_AUTHENTICATORS, false)) {
            List<AuthenticatorReg> registeredAuthenticators = new ArrayList<>();
            for (AuthenticatorReg matchingAuthenticator : matchingAuthenticators) {
                if (matchingAuthenticator.isRegistered()) {
                    registeredAuthenticators.add(matchingAuthenticator);
                }
            }
            return registeredAuthenticators.toArray(new AuthenticatorReg[registeredAuthenticators.size()]);
        }
        return matchingAuthenticators;
    }

    protected void requestDeregisterOfInactiveAuth() {
        Log.d(TAG, "requestDeregisterOfInactiveAuth: ");
        String message = getString(R.string.confirm_dereg_inactive_auth_present);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton(R.string.dialog_confirm_yes, (arg0, arg1) -> deregisterInactiveAuthPresent());

        alertDialogBuilder.setNegativeButton(R.string.dialog_confirm_cancel, (dialog, which) -> {
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    protected void requestDeregisterOfAuth(boolean authPresent) {
        Log.d(TAG, "requestDeregisterOfAuth: ");
        String message = (authPresent) ? getString(R.string.confirm_dereg_active_auth_present) : getString(R.string.confirm_dereg_active_auth_not_present);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(R.string.dialog_confirm_yes, (arg0, arg1) -> deregisterActiveAuth());

        alertDialogBuilder.setNegativeButton(R.string.dialog_confirm_cancel, (dialog, which) -> {
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    protected void attemptDeregister() {
        Log.d(TAG, "attemptDeregister: ");
        if (this.selectedAuthenticationInfo == null) {
            deregisterButton.setEnabled(false);
            return;
        }

        //Don't deregister a locked authenticator
        if (getFido().getLockStatus(this.selectedAuthenticationInfo.getAaid()) == IFidoSdk.LockStatus.Locked) {
            showMessage(R.string.uaf_error_authenticator_access_denied, false);
            return;
        }
        if (this.selectedAuthenticationInfo.getStatus().equals(ARCHIVED_STATUS)) {
            requestDeregisterOfInactiveAuth();
        } else {
            if (hasAuthenticator(this.selectedAuthenticationInfo.getAaid())) {
                if (this.selectedAuthenticationInfo.isPresentOnDevice()) {
                    deregisterActiveAuth();
                } else {
                    requestDeregisterOfAuth(true);
                }
            } else {
                requestDeregisterOfAuth(false);
            }
        }
    }

    /***
     * Attempt to deregister the inactive authenticator
     */
    protected void deregisterInactiveAuthPresent() {
        Log.d(TAG, "deregisterInactiveAuthPresent: ");
        showProgress(true);
        this.currentAction = Action.DEREGISTER;
        getAuthenticatorTask = new GetAuthenticatorTask();
        getAuthenticatorTask.execute();
    }

    protected void deregisterActiveAuth() {
        Log.d(TAG, "deregisterActiveAuth: ");
        showProgress(true);
        this.currentAction = Action.DEREGISTER;

        getFido().deregister(selectedAuthenticationInfo.getId(), new IXUAFDeregisterEventListener() {
            @Override
            public void onDeregistrationComplete() {
                removedRegisteredFidoAccount();
                showMessage(R.string.deregistration_complete, false);
                currentAction = Action.NONE;
                refreshAuthenticators();
            }

            @Override
            public void onDeregistrationFailed(int errorCode, String errorMessage) {
                showMessage(R.string.error_deregistering_authenticator, false);
                currentAction = Action.NONE;
                refreshAuthenticators();
            }
        });

    }

    protected void refreshAuthenticators() {
        Log.d(TAG, "refreshAuthenticators: ");
        showProgress(true);
        deregisterButton.setEnabled(false);
        listAuthenticatorsTask = new ListAuthenticatorsTask();
        listAuthenticatorsTask.execute();
    }

    protected void attemptRegistration() {
        Log.d(TAG, "attemptRegistration: ");
        if (this.currentAction != Action.NONE) {
            return;
        }
        this.currentAction = Action.REGISTER;
        showProgress(true);
        registrationInProgress = true;


        //Testing cancellation at registration
        //start a thread to call cancel after 20 seconds
        /*Log.d("DAON", "calling cancel after 20 seconds");
        new Handler().postDelayed(() -> {
            Log.d("DAON", "calling cancel ");
            getFido().cancelCurrentOperation();
        }, 350);
        Log.d("DAON", "after the thread to cancel");*/

        Bundle extensions = new Bundle();
        if (UserPreferences.instance().getBoolean(SettingsActivity.PREF_SILENT_SRP_PASSCODE, false)) {
            // Emulate passing the SRP passcode value for a silent SRP passcode registration
            extensions.putString("com.daon.passcode.value", "1111");
        }
        getFido().registerWithUsername(((CoreApplication) getApplication()).getEmail(), extensions, new IXUAFRegisterEventListener() {
            @Override
            public void onAuthListAvailable(Authenticator[][] authenticators) {
                if (authenticators.length == 1) {
                    getFido().submitUserSelectedAuth(authenticators[0]);
                } else {
                    showAuthenticatorChooser(authenticators);
                }
            }

            @Override
            public void onRegistrationComplete() {
                Log.d(TAG, "onRegistrationComplete: ");
                registrationInProgress = false;
                showProgress(false);
                showSuccessfulRegistration();
            }

            @Override
            public void onRegistrationFailed(Error error) {
                Log.e(TAG, "AuthenticatorsActivity onRegistrationFailed :" + error.getCode());
                registrationInProgress = false;
                showProgress(false);
                endProgressWithError(error.getMessage());
            }

            @Override
            public void onExpiryWarning(INotifyUafResultCallback.ExpiryWarning[] expiryWarnings) {

            }

            @Override
            public void onUserLockWarning() {
                Log.d(TAG, "AuthenticatorsActivity onUserLockWarning");
            }
        });
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
                    getFido().submitUserSelectedAuth(selectedAuthenticators);
                    selectedAuthenticators = null;
                }
            }
        });
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(authenticatorDialogFragment, "ChooseAuth_tag");
        ft.commitAllowingStateLoss();
    }

    protected void showAuthSelection(AuthenticatorInfo[] authenticatorInfoList) {
        Log.d(TAG, "showAuthSelection: ");
        try {
            final ListView lv = findViewById(R.id.list_view_authenticators);

            AuthenticatorInfosAdapter adapter = new AuthenticatorInfosAdapter(this, authenticatorInfoList);
            lv.setAdapter(adapter);

            showProgress(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void removedRegisteredFidoAccount() {
        Log.d(TAG, "removedRegisteredFidoAccount: ");
        Keys keys = getFido().getKeys(((CoreApplication) getApplication()).getAppId(), ((CoreApplication) getApplication()).getEmail());
        if (keys.size() == 0) {
            ((CoreApplication) getApplication()).removeRegisteredFidoAccount(((CoreApplication) getApplication()).getEmail());
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

        authenticatorsFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        authenticatorsFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                authenticatorsFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
        showProgress(false);
        showMessage(errorMsg, false);
        this.currentAction = Action.NONE;
    }

    protected void showSuccessfulRegistration() {
        Log.d(TAG, "showSuccessfulRegistration: ");
        // User is now newly registered with FIDO
        Keys keys = getFido().getKeys(((CoreApplication) getApplication()).getAppId(), ((CoreApplication) getApplication()).getEmail());
        if (keys.size() == 1) {
            ((CoreApplication) getApplication()).addRegisteredFidoAccount(((CoreApplication) getApplication()).getEmail());
        }

        showMessage(R.string.registration_complete, false);

        this.currentAction = Action.NONE;
        refreshAuthenticators();
    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> processListAuthenticatorsActivityResult(result.getResultCode(), result.getData()));


    protected void processListAuthenticatorsActivityResult(int resultCode, Intent data) {
        Log.d(TAG, "processListAuthenticatorsActivityResult: ");
        if (resultCode == Activity.RESULT_OK) {
            String chosenAuthenticatorJson = data.getStringExtra(ListAuthenticatorsActivity.EXTRA_CHOSEN_AUTHENTICATOR);
            if (chosenAuthenticatorJson != null) {
                TransferAuthenticator transferAuthenticator = new Gson().fromJson(chosenAuthenticatorJson, TransferAuthenticator.class);
                AuthenticatorReg chosenAuthenticator = UIHelper.getAuthenticatorReg(transferAuthenticator.aaid, transferAuthenticator.isRegistered);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button during action processing
        if (!registrationInProgress) {
            super.onBackPressed();
        }
    }

    /**
     * Represents an asynchronous task used to get a list of authenticators to display
     */
    public class ListAuthenticatorsTask extends TaskExecutor<ServerOperationResult<ListAuthenticatorsResponse>> {

        @Override
        protected ServerOperationResult<ListAuthenticatorsResponse> doInBackground() {
            ServerOperationResult<ListAuthenticatorsResponse> result;
            try {
                RPSAService rpsaService = (RPSAService) ((CoreApplication) getApplication()).getCommService();
                ListAuthenticatorsResponse response = rpsaService.serviceRequestAuthInfoList();
                result = new ServerOperationResult<>(response);
            } catch (ServerError e) {
                result = new ServerOperationResult<>(e.getError());
            } catch (CommunicationsException e) {
                result = new ServerOperationResult<>(e.getError());
            }

            return result;
        }

        @Override
        protected void onPostExecute(ServerOperationResult<ListAuthenticatorsResponse> result) {
            listAuthenticatorsTask = null;

            if (result.isSuccessful()) {
                if (result.getResponse() != null) {
                    AuthenticatorInfo[] authenticatorInfoList = result.getResponse().getAuthenticatorInfoList();
                    showAuthSelection(removeAuthenticatorsNotOnThisDevice(authenticatorInfoList));
                } else {
                    showProgress(false);
                }
            } else {
                showProgress(false);
                showMessage(result.getError().getMessage(), false);
            }
        }

        @Override
        protected void onCancelled() {
            listAuthenticatorsTask = null;
            showProgress(false);
        }
    }

    private AuthenticatorInfo[] removeAuthenticatorsNotOnThisDevice(AuthenticatorInfo[] authenticatorInfoList) {
        Log.d(TAG, "removeAuthenticatorsNotOnThisDevice: ");
        String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        List<AuthenticatorInfo> trimmedAuthenticatorInfoList = new ArrayList<>();
        for (AuthenticatorInfo authenticatorInfo : authenticatorInfoList) {
            if (authenticatorInfo.getDeviceCorrelationId() == null || authenticatorInfo.getDeviceCorrelationId().isEmpty()) {
                // If the device correlation ID is unknown then add to the list as we can't
                // be sure that the authenticator is not on this device.
                trimmedAuthenticatorInfoList.add(authenticatorInfo);
            } else {
                // Device correlation ID for authenticator is known
                if (deviceId == null || deviceId.isEmpty()) {
                    // Device ID of this device is unknown, so add to the list as we can't
                    // be sure that the authenticator is not on this device.
                    trimmedAuthenticatorInfoList.add(authenticatorInfo);
                } else {
                    // Only add the device if its device ID matches this device
                    if (deviceId.equals(authenticatorInfo.getDeviceCorrelationId())) {
                        authenticatorInfo.setPresentOnDevice(true);
                        trimmedAuthenticatorInfoList.add(authenticatorInfo);
                    }
                }
            }
        }
        return trimmedAuthenticatorInfoList.toArray(new AuthenticatorInfo[trimmedAuthenticatorInfoList.size()]);
    }

    public class GetAuthenticatorTask extends TaskExecutor<ServerOperationResult<GetAuthenticatorResponse>> {

        @Override
        protected ServerOperationResult<GetAuthenticatorResponse> doInBackground() {
            ServerOperationResult<GetAuthenticatorResponse> result;
            try {
                RPSAService service = (RPSAService) ((CoreApplication) getApplication()).getCommService();
                GetAuthenticatorResponse response = service.getAuthenticator(selectedAuthenticationInfo.getId());
                result = new ServerOperationResult<>(response);
            } catch (ServerError e) {
                result = new ServerOperationResult<>(e.getError());
            } catch (CommunicationsException e) {
                result = new ServerOperationResult<>(e.getError());
            }

            return result;
        }

        @Override
        protected void onPostExecute(final ServerOperationResult<GetAuthenticatorResponse> result) {
            getAuthenticatorTask = null;

            if (result.isSuccessful()) {
                AuthenticatorInfo authenticatorInfo = result.getResponse().getAuthenticatorInfo();
                getFido().deregisterWithMessage(authenticatorInfo.getFidoDeregistrationRequest(), new IXUAFDeregisterEventListener() {
                    @Override
                    public void onDeregistrationComplete() {
                        Log.d(AuthenticatorsActivity.TAG, "Deregister complete: ");
                        removedRegisteredFidoAccount();
                        showMessage(R.string.deregistration_complete, false);
                        currentAction = AuthenticatorsActivity.Action.NONE;
                        refreshAuthenticators();
                    }

                    @Override
                    public void onDeregistrationFailed(int errorCode, String errorMessage) {
                        Log.e(AuthenticatorsActivity.TAG, "Deregister failed: " + errorMessage);
                        showMessage(R.string.error_deregistering_authenticator, false);
                        currentAction = AuthenticatorsActivity.Action.NONE;
                        refreshAuthenticators();
                    }
                });
            } else {
                showProgress(false);
                showMessage(result.getError().getMessage(), false);
            }
        }

        @Override
        protected void onCancelled() {
            getAuthenticatorTask = null;
            showProgress(false);
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

