package com.daon.fido.sdk.sample;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.daon.fido.client.sdk.IXUAFCallback;
import com.daon.fido.client.sdk.core.ErrorFactory;
import com.daon.fido.client.sdk.core.IUafCancellableClientOperation;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.client.sdk.model.AuthenticatorReg;
import com.daon.fido.client.sdk.model.DiscoveryData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavascriptApiDemoActivity extends LoggedInActivity {
    private static String AAID_SILENT = "D409#0602";
    private static String AAID_ADOS_PASSCODE = "D409#8301";
    private static String AAID_SRP_PASSCODE = "D409#8302";
    private static String AAID_PASSCODE = "D409#0302";
    private static String AAID_FINGERPRINT = "D409#0102";

    private RPSAService service;

    private Button register;
    private ListView registerList;
    private Button verify;
    private ListView verifyList;
    private View mProgressView;

    private List<String> unsupportedAaids = Arrays.asList(/** Daon Face **/"D409#0205", /* Ados Face */"D409#8201", "D409#0204", "D409#0902", "D409#8401", "D409#0402", "D409#0502", "D409#0801");

    private DiscoveryData discoveryData;

    private List<String> registerAaids;
    private List<String> verifyAaids;
    private List<String> registerAaidsTitle;
    private List<String> verifyAaidsTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_javascript);

        service = (RPSAService) ((CoreApplication) getApplication()).getCommService();

        mProgressView = findViewById(R.id.authenticators_progress);

        register = findViewById(R.id.register);
        registerList = findViewById(R.id.registerList);
        verify = findViewById(R.id.verify);
        verifyList = findViewById(R.id.verifyList);

        discoveryData = ((CoreApplication) getApplication()).getFido().discover();

        registerAaids = new ArrayList<>(discoveryData.getAvailableAuthenticators().length);
        verifyAaids = new ArrayList<>(discoveryData.getAvailableAuthenticators().length);
        registerAaidsTitle = new ArrayList<>(discoveryData.getAvailableAuthenticators().length);
        verifyAaidsTitle = new ArrayList<>(discoveryData.getAvailableAuthenticators().length);


        ArrayAdapter<String> regAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, registerAaidsTitle);
        registerList.setAdapter(regAdapter);

        ArrayAdapter<String> verAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, verifyAaidsTitle);
        verifyList.setAdapter(verAdapter);

        registerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                showMessage("Registering " + registerAaidsTitle.get(position), false);
                registerList.setVisibility(View.GONE);
                verifyList.setVisibility(View.GONE);
                showProgress(true);
                startRegistration(registerAaids.get(position));
            }
        });

        verifyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                showMessage("Verifying " + verifyAaidsTitle.get(position), false);
                registerList.setVisibility(View.GONE);
                verifyList.setVisibility(View.GONE);
                showProgress(true);
                startVerification(verifyAaids.get(position));
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerAaids.clear();
                registerAaidsTitle.clear();
                discoveryData = ((CoreApplication) getApplication()).getFido().discover();
                for (Authenticator authenticator : discoveryData.getAvailableAuthenticators()) {
                    if (isSupported(authenticator.getAaid()) && !((AuthenticatorReg) authenticator).isRegistered()) {
                        registerAaids.add(authenticator.getAaid());
                        registerAaidsTitle.add(authenticator.getTitle());
                    }
                }
                if (registerAaids.size() == 0) {
                    showMessage("No more authenticators available to register", false);
                } else {
                    registerList.setVisibility(View.VISIBLE);
                }
            }
        });

        verify.setOnClickListener(view -> {
            discoveryData = ((CoreApplication) this.getApplication()).getFido().discover();
            verifyAaids.clear();
            verifyAaidsTitle.clear();
            for (Authenticator authenticator : discoveryData.getAvailableAuthenticators()) {
                if (isSupported(authenticator.getAaid()) && ((AuthenticatorReg) authenticator).isRegistered()) {
                    verifyAaids.add(authenticator.getAaid());
                    verifyAaidsTitle.add(authenticator.getTitle());
                }
            }
            if (verifyAaids.size() == 0) {
                showMessage("No authenticators available to authenticate", false);
            } else {
                verifyList.setVisibility(View.VISIBLE);
            }
        });
        ((DefaultFidoListener) ((CoreApplication) this.getApplication()).getFidoListener()).setCurrentActivity(this);
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

            register.setVisibility(show ? View.GONE : View.VISIBLE);
            register.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    register.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
            verify.setVisibility(show ? View.GONE : View.VISIBLE);
            verify.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    verify.setVisibility(show ? View.GONE : View.VISIBLE);
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
            register.setVisibility(show ? View.GONE : View.VISIBLE);
            verify.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private boolean isSupported(String aaid) {
        return !unsupportedAaids.contains(aaid);
    }

    private void startRegistration(String aaid) {
        if (aaid.equals(AAID_PASSCODE) || aaid.equals(AAID_ADOS_PASSCODE) || (aaid.equals(AAID_SRP_PASSCODE))) {
            getFido().registerWithAaid(aaid, ((CoreApplication) getApplication()).getEmail(), "1234", this, new RegistrationCallback());
        } else {
            getFido().registerWithAaid(aaid, ((CoreApplication) getApplication()).getEmail(), null, this, new RegistrationCallback());
        }
    }

    private void startVerification(String aaid) {
        if (aaid.equals(AAID_PASSCODE) || aaid.equals(AAID_ADOS_PASSCODE) || (aaid.equals(AAID_SRP_PASSCODE))) {
            getFido().authenticateWithAaid(aaid, ((CoreApplication) getApplication()).getEmail(), "authentication", null, "1234", this, new VerificationCallback());
        } else {
            getFido().authenticateWithAaid(aaid, ((CoreApplication) getApplication()).getEmail(), "authentication", null, null, this, new VerificationCallback());
        }
    }

    private IXUAFCallback regCallback = new RegistrationCallback();
    private IXUAFCallback authCallback = new VerificationCallback();

    private class RegistrationCallback implements IXUAFCallback {
        @Override
        public void onAuthenticationFailed(int code, String message) {
            showProgress(false);
            showMessage("Registration failed", false);
        }

        @Override
        public void onAuthenticationComplete(Bundle bundle) {
            showProgress(false);
            showMessage("Registration success", false);
        }
    }

    private class VerificationCallback implements IXUAFCallback {
        boolean failed;

        @Override
        public void onAuthenticationComplete(Bundle bundle) {
            Log.d("JSAPI", "JavascriptApiDemoActivity onAuthenticationComplete");
            showProgress(false);
            showMessage("Verification success", false);
        }

        @Override
        public void onAuthenticationFailed(int code, String message) {
            if (!failed) {
                showProgress(false);
                showMessage("Javascript_api Verification failed :" + message, false);
            }
        }
    }
}
