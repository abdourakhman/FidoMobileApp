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

import android.app.Application;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.daon.fido.client.sdk.IXUAF;
import com.daon.fido.client.sdk.IXUAFCommService;
import com.daon.fido.client.sdk.IXUAFListener;
import com.daon.fido.sdk.sample.tasks.UserLogoutTask;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This contains a number of statics used by multiple activities within the application.
 */
public class CoreApplication extends Application {

    public static final String TAG = CoreApplication.class.getSimpleName();

    public static String USER_IDENTIFIER = "IdentityX_UserId";
    public static String LOGOUT_BROADCAST_ACTION = "com.daon.fido.sdk.sample.logout";
    public static String EXTRA_LOGOUT_SUCCESS = "logoutSuccess";
    public static String EXTRA_LOGOUT_ERROR_CODE = "logoutErrorCode";
    public static String EXTRA_LOGOUT_ERROR_MESSAGE = "logoutErrorMessage";
    public static String EXTRA_LOGOUT_TIMEOUT = "logoutTimeout";
    private static final String KEY_LAST_LOGGED_IN_USER_ACCOUNT = "lastLoggedInUserAccount";
    private static final String KEY_REGISTERED_FIDO_ACCOUNTS = "registeredFidoAccounts";
    private static final String KEY_APP_ID = "fidoAppId";
    private static final long GPS_TIMEOUT_WAIT = 60 * 1000;
    private static final long LOGOUT_WAIT = 3 * 60 * 1000;
    private Gson gson;
    private boolean hasExternalClient;
    private String sessionId = null;
    private String email;
    private boolean uafInitialised;
    private UserLogoutTask userLogoutTask;
    private List<String> availableAuthenticators;
    private com.daon.fido.client.sdk.model.Authenticator[][] authenticators;
    private IXUAF fido;
    private IXUAFCommService commService;
    private IXUAFListener fidoListener;

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                Log.d(TAG, "onStop: ");
                doLogout(false);
                DefaultLifecycleObserver.super.onStop(owner);
            }
        });
    }

    public IXUAFListener getFidoListener() {
        return fidoListener;
    }

    public void setFidoListener(IXUAFListener listener) {
        fidoListener = listener;
    }

    public void setAuthenticators(com.daon.fido.client.sdk.model.Authenticator[][] auths) {
        authenticators = auths;
    }

    public com.daon.fido.client.sdk.model.Authenticator[][] getAuthenticators() {
        return authenticators;
    }

    public void startGps() {
        if (getFido() != null) {
            getFido().startLocator(null);
            gpsTimeoutCountdown.cancel();
            gpsTimeoutCountdown.start();
        }
    }

    public boolean isUafInitialised() {
        return uafInitialised;
    }

    public void setUafInitialised(boolean uafInitialised) {
        this.uafInitialised = uafInitialised;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String theSessionId) {
        sessionId = theSessionId;
    }

    public void addRegisteredFidoAccount(String email) {
        // Read accounts
        String[] registeredFidoAccounts = getRegisteredFidoAccounts();

        // Add new account
        List<String> fidoAccountsList = new ArrayList<>();
        if (registeredFidoAccounts != null) {
            fidoAccountsList.addAll(Arrays.asList(registeredFidoAccounts));
        }
        fidoAccountsList.add(email);

        // Store accounts
        storeRegisteredFidoAccounts(fidoAccountsList);
    }

    public void removeRegisteredFidoAccount(String email) {
        // Read accounts
        String[] registeredFidoAccounts = getRegisteredFidoAccounts();

        // Remove account
        if (registeredFidoAccounts == null) {
            return;
        }
        List<String> fidoAccountsList = new ArrayList<>(Arrays.asList(registeredFidoAccounts));
        fidoAccountsList.remove(email);

        // Store accounts
        storeRegisteredFidoAccounts(fidoAccountsList);

    }

    public void clearRegisteredFidoAccounts() {
        UserPreferences.instance().remove(KEY_REGISTERED_FIDO_ACCOUNTS);
    }

    public String[] getRegisteredFidoAccounts() {
        String fidoAccountsJson = UserPreferences.instance().getString(KEY_REGISTERED_FIDO_ACCOUNTS, null);

        if (fidoAccountsJson == null) {
            return null;
        }

        return getGson().fromJson(fidoAccountsJson, String[].class);
    }

    private void storeRegisteredFidoAccounts(List<String> registeredFidoAccounts) {
        String fidoAccountsJson = getGson().toJson(registeredFidoAccounts.toArray(new String[0]));

        UserPreferences.instance().putString(KEY_REGISTERED_FIDO_ACCOUNTS, fidoAccountsJson);
    }

    public String getEmail() {
        return email;
    }

    public String getLastLoggedInUserEmail() {
        return UserPreferences.instance().getString(KEY_LAST_LOGGED_IN_USER_ACCOUNT, null);
    }

    public void setEmail(String theEmail) {
        email = theEmail;

        UserPreferences.instance().putString(KEY_LAST_LOGGED_IN_USER_ACCOUNT, email);
    }

    public void setHasExternalClient(boolean hasExternalClient) {
        this.hasExternalClient = hasExternalClient;
    }

    public boolean hasExternalClient() {
        return hasExternalClient;
    }

    public String getAppId() {
        return UserPreferences.instance().getString(KEY_APP_ID, null);
    }

    private Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    public List<String> getAvailableAuthenticators() {
        return availableAuthenticators;
    }

    public void setAvailableAuthenticators(List<String> availableAuthenticators) {
        this.availableAuthenticators = availableAuthenticators;
    }

    public void doLogout(boolean timeout) {
        Log.d(TAG, "doLogout with timeout: " + timeout);
        if (userLogoutTask == null) {
            userLogoutTask = new UserLogoutTask(getApplicationContext(), timeout, sessionId, getCommService(), () -> {
                setSessionId(null);
                userLogoutTask = null;
            });
            userLogoutTask.execute();
        }
    }

    public IXUAFCommService getCommService() {
        return commService;
    }

    public void setCommService(IXUAFCommService service) {
        this.commService = service;
    }

    public IXUAF getFido() {
        return fido;
    }

    public void setFido(IXUAF service) {
        fido = service;
    }

    public CountDownTimer gpsTimeoutCountdown = new CountDownTimer(GPS_TIMEOUT_WAIT, GPS_TIMEOUT_WAIT) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            ((CoreApplication) getApplicationContext()).getFido().stopLocator();
        }
    };

    public CountDownTimer logoutCountdown = new CountDownTimer(LOGOUT_WAIT, LOGOUT_WAIT) {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            doLogout(true);
        }
    };
}
