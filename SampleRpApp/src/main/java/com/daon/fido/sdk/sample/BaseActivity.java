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


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.daon.fido.client.sdk.Fido;
import com.daon.fido.client.sdk.IXUAF;
import com.google.android.material.snackbar.Snackbar;

/**
 * This abstract base class is the super class of all the activities in the project.
 * It provides the ability to handle the processing of FIDO intents and is used to
 * determine if there is a FIDO client on the device and the authenticators available.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    /**
     * Initialise global interfaces which are made available to all activities which derive from this class
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Launched activity: " + this.getClass().getSimpleName());
        CoreApplication application = (CoreApplication) this.getApplication();

        if (application.getFido() == null) {
            if (application.getCommService() == null) {
                application.setCommService(RPSAService.getInstance(this.getApplicationContext()));
            }
            application.setFido(Fido.getInstance(this.getApplicationContext()));
            application.getFido().setFidoListener(DefaultFidoListener.getInstance());
            application.setFidoListener(DefaultFidoListener.getInstance());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((CoreApplication) this.getApplication()).startGps();
    }

    @Override
    public void onUserInteraction() {
        ((CoreApplication) this.getApplication()).startGps();
    }

    /**
     * Enable settings menu
     *
     * @param menu The options menu in which you place your items
     * @return true for menu to be displayed, otherwise false
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * React to user selecting Settings
     *
     * @param item The menu item that was selected.
     * @return Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        if (item.getItemId() == R.id.action_settings) {
            openSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Display settings menu activity.
     */
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    protected boolean hasFIDOClient() {
        CoreApplication application = (CoreApplication) this.getApplication();
        return (application.getAvailableAuthenticators() != null && !application.getAvailableAuthenticators().isEmpty());
    }

    protected boolean hasAuthenticator(String aaid) {
        CoreApplication application = (CoreApplication) this.getApplication();
        return (application.getAvailableAuthenticators() != null && application.getAvailableAuthenticators().contains(aaid));
    }

    protected IXUAF getFido() {
        CoreApplication application = (CoreApplication) this.getApplication();
        return application.getFido();
    }

    public boolean isUafInitialised() {
        CoreApplication application = (CoreApplication) this.getApplication();
        return application.isUafInitialised();
    }

    public void setIsUafInitialised(boolean isUafInitialised) {
        CoreApplication application = (CoreApplication) this.getApplication();
        application.setUafInitialised(isUafInitialised);
    }

    /**
     * Show a message in a {@link Snackbar}
     *
     * @param id     message resource ID
     * @param always true to display message permanently, false to display temporarily
     */
    protected void showMessage(int id, boolean always) {
        if (id != 0) {
            showMessage(getString(id), always);
        }
    }

    /**
     * Show a message in a {@link Snackbar}
     *
     * @param message message string
     * @param always  true to display message permanently, false to display temporarily
     */
    protected void showMessage(final String message, final boolean always) {
        Log.d(CoreApplication.TAG, "show message: " + message);
        if (message != null && !message.isEmpty()) {
            Snackbar sb = Snackbar.make(findViewById(android.R.id.content), message, always ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG);
            View snackbarView = sb.getView();
            TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setMaxLines(5);
            sb.show();
        }
    }
}
