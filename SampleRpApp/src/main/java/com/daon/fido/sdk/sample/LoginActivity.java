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
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.daon.fido.client.sdk.ServerCommResult;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.model.CreateRegRequestResponse;
import com.daon.fido.sdk.sample.model.CreateSession;
import com.daon.fido.sdk.sample.model.CreateSessionResponse;

import java.text.DateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A login screen that offers signup via email/password.
 */
public class LoginActivity extends BaseActivity {

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    /**
     * Keep track of the tasks to ensure we can cancel it if requested.
     */
    private UserLoginWithEmailTask mLoginTask = null;
    private CreateSessionResponse mCreateSessionResponse;

    private CreateRegRequestResponse mCreateRegRequestResponse;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = findViewById(R.id.email);
        String lastedLoggedInEmail = ((CoreApplication)getApplication()).getLastLoggedInUserEmail();
        if(lastedLoggedInEmail != null) {
            mEmailView.setText(lastedLoggedInEmail);
        }

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == R.id.login_form || id == EditorInfo.IME_NULL) {
                attemptUsernameAndPasswordLogin();
                return true;
            }
            return false;
        });

        Button mLoginButton = findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptUsernameAndPasswordLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptUsernameAndPasswordLogin() {
        if(mLoginTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_required_email));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email_signup));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_required_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password_signup));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            showProgress(true);
            mLoginTask = new UserLoginWithEmailTask(email, password);
            mLoginTask.execute();
        }
    }

    private boolean isEmailValid(String email) {

        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 1;
    }

    private void clearRegistration() {

        if (UserPreferences.instance().getString(CoreApplication.USER_IDENTIFIER, null) != null) {

            UserPreferences.instance().remove(CoreApplication.USER_IDENTIFIER);
        }

    }

    protected void showLoggedIn(CreateSessionResponse response) {

        try {
            Intent newIntent = new Intent(this, HomeActivity.class);
            ((CoreApplication) getApplication()).setSessionId(response.getSessionId());
            ((CoreApplication) getApplication()).setEmail(response.getEmail());
            newIntent.putExtra("LOGGED_IN_WITH", response.getLoggedInWith().toString());
            if (response.getLastLoggedIn() == null) {
                newIntent.putExtra("LAST_LOGGED_IN", getString(R.string.message_first_login));
            } else {
                String dateString = DateFormat.getDateTimeInstance().format(response.getLastLoggedIn());
                newIntent.putExtra("LAST_LOGGED_IN", dateString);
            }

            startActivity(newIntent);
            finish();
        } catch (Throwable e) {
            showMessage(e.getMessage(), false);
        }
    }
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    protected void endProgressWithError(String errorMsg) {
        showProgress(false);
        showMessage(errorMsg, false);
        mPasswordView.requestFocus();
    }



    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginWithEmailTask extends TaskExecutor<ServerCommResult> {

        private final CreateSession createSession;

        UserLoginWithEmailTask(String email, String password) {
            createSession = new CreateSession();
            getCreateSession().setEmail(email);
            getCreateSession().setPassword(password);
        }

        @Override
        protected ServerCommResult doInBackground() {
            RPSAService rpsaService = (RPSAService) ((CoreApplication) getApplication()).getCommService();
            return rpsaService.createSessionWithEmail(this.getCreateSession());
        }

        @Override
        protected void onPostExecute(ServerCommResult result) {
            mLoginTask = null;

            if (result.isSuccessful()) {
                RPSAService rpsaService = (RPSAService) ((CoreApplication) getApplication()).getCommService();
                showLoggedIn(rpsaService.getCreateSessionResponse());
            } else {
                endProgressWithError(result.getErrorInfo().getMessage());
            }
        }

        @Override
        protected void onCancelled() {
            mLoginTask = null;
            showProgress(false);
        }

        public CreateSession getCreateSession() {
            return createSession;
        }
    }


}

