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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.FragmentTransaction;

import com.daon.fido.client.sdk.IXUAFRegisterEventListener;
import com.daon.fido.client.sdk.ServerCommResult;
import com.daon.fido.client.sdk.core.Error;
import com.daon.fido.client.sdk.core.INotifyUafResultCallback;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.model.AuthenticationMethod;
import com.daon.fido.sdk.sample.model.CreateAccount;
import com.daon.fido.sdk.sample.permission.PermissionHelper;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A signup screen that offers signup via email/password.
 */
public class CreateAccountActivity extends BaseActivity {

    /**
     * Keep track of the tasks to ensure we can cancel them if requested.
     */
    private static final String TAG = CreateAccountActivity.class.getSimpleName();
    private UserSignupTask signupTask = null;
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    public static final String ADOS_FACE_AUTHENTICATOR_AAID = "D409#8201";
    public static final String ADOS_VOICE_AUTHENTICATOR_AAID = "D409#8401";
    public static final String FACE_AUTHENTICATOR_AAID = "D409#0205";
    public static final String VOICE_AUTHENTICATOR_AAID = "D409#0402";
    private final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    private EditText firstNameView;
    private EditText lastNameView;
    private EditText emailView;
    private EditText passwordView;
    private EditText confirmPasswordView;
    private View progressView;
    private View signupFormView;
    private boolean registrationInProgress;
    private PermissionHelper permissionHelper;
    public Authenticator[] selectedAuthenticators = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Set up the signup form.
        firstNameView = findViewById(R.id.first_name);
        lastNameView = findViewById(R.id.last_name);
        emailView = findViewById(R.id.email);

        passwordView = findViewById(R.id.password);
        confirmPasswordView = findViewById(R.id.confirm_password);
        confirmPasswordView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptSignup();
                return true;
            }
            return false;
        });

        Button createButton = findViewById(R.id.create_button);
        createButton.setOnClickListener(view -> attemptSignup());

        signupFormView = findViewById(R.id.signup_form);
        progressView = findViewById(R.id.signup_progress);

        permissionHelper = new PermissionHelper(this, this::processGrantedPermission);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptSignup() {
        if (signupTask != null) {
            return;
        }

        // Reset errors.
        firstNameView.setError(null);
        lastNameView.setError(null);
        emailView.setError(null);
        passwordView.setError(null);
        confirmPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String firstName = firstNameView.getText().toString();
        String lastName = lastNameView.getText().toString();
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();
        String confirmPassword = confirmPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid confirm password, if the user entered one.
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordView.setError(getString(R.string.error_required_confirm_password));
            focusView = confirmPasswordView;
            cancel = true;
        } else if (!password.equals(confirmPassword)) {
            // Check for a valid confirm password, if the user entered one.
            confirmPasswordView.setError(getString(R.string.error_inconsistent_password));
            focusView = confirmPasswordView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_required_password));
            focusView = passwordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            // Check for a valid password, if the user entered one.
            passwordView.setError(getString(R.string.error_invalid_password_signup));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_required_email));
            focusView = emailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email_signup));
            focusView = emailView;
            cancel = true;
        }

        // Check for a last, if the user entered one.
        if (TextUtils.isEmpty(lastName)) {
            lastNameView.setError(getString(R.string.error_required_last_name));
            focusView = lastNameView;
            cancel = true;
        }

        // Check for a first name, if the user entered one.
        if (TextUtils.isEmpty(firstName)) {
            firstNameView.setError(getString(R.string.error_required_first_name));
            focusView = firstNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(confirmPasswordView.getWindowToken(), 0);
            signupTask = new UserSignupTask(this, firstName, lastName, email, password);
            registrationInProgress = true;
            signupTask.execute();
        }
    }

    private boolean isEmailValid(String email) {
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 1;
    }

    protected void showLoggedIn() {
        try {
            Intent newIntent = new Intent(this, HomeActivity.class);
            newIntent.putExtra("LOGGED_IN_WITH", AuthenticationMethod.FIDO_AUTHENTICATION.toString());
            newIntent.putExtra("LAST_LOGGED_IN", getString(R.string.message_first_login));
            startActivity(newIntent);
            finish();
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

        signupFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        signupFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                signupFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
        passwordView.requestFocus();
    }

    @Override
    public void onBackPressed() {
        // Disable back button during registration
        if (!registrationInProgress) {
            super.onBackPressed();
        }
    }

    /**
     * Represents an asynchronous task used to create a user account.
     */
    public class UserSignupTask extends TaskExecutor<ServerCommResult> {

        private final CreateAccount createAccount;
        private final Context context;

        UserSignupTask(Context context, String firstName, String lastName, String email, String password) {
            this.context = context;
            createAccount = new CreateAccount();
            createAccount.setFirstName(firstName);
            createAccount.setLastName(lastName);
            createAccount.setEmail(email);
            createAccount.setPassword(password);
            createAccount.setLanguage(Locale.getDefault().toString());
        }

        @Override
        protected ServerCommResult doInBackground() {
            RPSAService service = (RPSAService) ((CoreApplication) context.getApplicationContext()).getCommService();
            service.resetState();
            return service.serviceCreateAccount(createAccount);
        }

        @Override
        protected void onPostExecute(ServerCommResult result) {
            signupTask = null;

            if (result.isSuccessful()) {
                ((CoreApplication) context.getApplicationContext()).setEmail(createAccount.getEmail());
                ((CoreApplication) context.getApplicationContext()).setSessionId(result.getResponse());

                if (UserPreferences.instance().getBoolean(SettingsActivity.PREF_SILENT_SRP_PASSCODE, false)) {
                    Bundle extensions = new Bundle();
                    extensions.putString("com.daon.passcode.value", "1111");
                    getFido().registerWithUsername(((CoreApplication) context.getApplicationContext()).getEmail(), extensions, new RegisterEventListener(context));
                } else {
                    getFido().registerWithUsername(((CoreApplication) context.getApplicationContext()).getEmail(), new RegisterEventListener(context));
                }

            } else {
                registrationInProgress = false;
                endProgressWithError(result.getErrorInfo().getMessage());
            }
        }

        @Override
        protected void onCancelled() {
            signupTask = null;
            showProgress(false);
        }

    }

    private class RegisterEventListener implements IXUAFRegisterEventListener {
        private final Context context;

        public RegisterEventListener(Context context) {
            this.context = context;
        }

        @Override
        public void onAuthListAvailable(Authenticator[][] authenticators) {
            if (authenticators.length == 1) {
                getFido().submitUserSelectedAuth(authenticators[0]);
            } else {
                showAuthenticatorChooser(authenticators);
            }
        }

        @Override
        public void onRegistrationFailed(Error error) {
            registrationInProgress = false;
            endProgressWithError(error.getMessage());
        }

        @Override
        public void onRegistrationComplete() {
            ((CoreApplication) context.getApplicationContext()).addRegisteredFidoAccount(((CoreApplication) context.getApplicationContext()).getEmail());
            showProgress(false);
            showLoggedIn();
        }


        @Override
        public void onExpiryWarning(INotifyUafResultCallback.ExpiryWarning[] expiryWarnings) {

        }

        @Override
        public void onUserLockWarning() {
            Log.d("DAON", "CreateAccountActivity onUserLockWarning");
        }
    }

    private void showAuthenticatorChooser(Authenticator[][] authenticators) {
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

