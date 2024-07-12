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

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.daon.fido.client.sdk.IXUAFInitialiseListener;
import com.daon.fido.client.sdk.core.Error;
import com.daon.fido.client.sdk.core.ErrorFactory;
import com.daon.fido.sdk.sample.ados.CertificateAccessor;
import com.daon.fido.sdk.sample.ados.ICertificateAccessor;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

/**
 * The first screen displayed which determines if there are FIDO clients on the
 * device and if there are clients, what authenticators are available.
 *
 * This process is a little difficult as it requires that we get a list of all the
 * FIDO Clients and then call each client asking for its authenticators.
 *
 * While it is not specified in the FIDO Specifications there will be issues if the
 * FIDO clients do not all return the same authenticators.  If a FIDO Client only
 * works with an subset of the authenticator on the device and another FIDO Client
 * works with all the authenticators then issues will arise if the wrong client is
 * called.
 *
 */
public class SplashActivity extends BaseActivity  {
    private boolean hasExtras = false;
    private Bundle extras = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(CoreApplication.TAG, "Key: " + key + " Value: " + value);
            }
            if(getIntent().getExtras().containsKey("id")) {
                //It contains the Push Activity Keys
                Log.d(CoreApplication.TAG,"extras contains id");
                hasExtras = true;
                extras = getIntent().getExtras();
            }
        }
        setContentView(R.layout.activity_splash);

        UserPreferences.initialize(this);


        //DataStore Migration test code
       /* DataStoreHelper dataStoreHelper = new DataStoreHelper(this);
        dataStoreHelper.updateSettings(R.color.white);

        DataStore<Preferences> dataStore = dataStoreHelper.getDataStore();
        getFido().setDataStore(dataStore);*/

        // Initialise SDK then list authenticators
        if(!isUafInitialised()) {
            new InitialiseSdkTask(this, new IXUAFInitialiseListener() {
                @Override
                public void onInitialiseComplete() {
                    setIsUafInitialised(true);
                    getFirebaseToken();
                }

                @Override
                public void onInitialiseWarnings(List<Error> warnings) {
                    for(int i=0; i<warnings.size(); i++) {
                        Log.d("DAON", "Init warning :" + warnings.get(i).getMessage());
                    }
                    showMessage(warnings.get(0).getMessage(), false);
                }

                @Override
                public void onInitialiseFailed(int code, String message) {
                    if(code != ErrorFactory.SDK_INITIALISING_CODE) {
                        setIsUafInitialised(false);
                        showMessage(R.string.error_initialising_uaf, false);
                    }
                }
            }).execute();
        } else {
            startNextActivity();
        }
    }

    private String getAdosRootCert() {
        if (UserPreferences.instance().getBoolean(SettingsActivity.PREF_ALT_ADOS_ROOT_CERT_PROVIDED, false)) {
            ICertificateAccessor certificateAccessor = new CertificateAccessor();
            return certificateAccessor.getCertificateBase64String(this);
        }
        return getString(R.string.default_ados_root_cert);
    }

    private void startNextActivity() {
        if(hasExtras) {
            startPushActivity();
        }else {
            try {
                Intent newIntent = new Intent(SplashActivity.this, IntroActivity.class);
                startActivity(newIntent);
                finish();
            } catch (Throwable ex) {
                showMessage(ex.getMessage(), false);
            }
        }
    }

    private void startPushActivity() {
        Intent intent = new Intent(SplashActivity.this, PushActivity.class);
        String id = extras.get("id").toString();
        intent.putExtra("transaction_id", id);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void getFirebaseToken() {
        if(firebaseIncluded()) {
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String token) {
                    if(token != null)
                        getFido().setPushNotificationServiceToken(token);
                    startNextActivity();
                }
            });
        }else {
            startNextActivity();
        }
    }

    private boolean firebaseIncluded() {
        try {
            Class.forName("com.google.firebase.FirebaseApp");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    private void showWarningsDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", (arg0, arg1) -> startNextActivity());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}

