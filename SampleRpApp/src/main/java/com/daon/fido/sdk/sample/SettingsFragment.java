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

import static com.daon.fido.sdk.sample.SettingsActivity.PREF_ADOS_DEC_SAN;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_ADOS_ROOT_CERT_SUPPLIED;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_ALT_ADOS_ROOT_CERT_PROVIDED;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_ALWAYS_ALLOW_AUTHENTICATOR_CHOICE;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_APP_VERSION;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_FACET_ID;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_HARD_RESET;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_IGNORE_NATIVE_CLIENTS;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_INIT_PARAMS_TO_SERVER;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_INVALIDATE_FINGER_ON_NEW_ENROLMENT;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_NATIVE_LOGON_ALWAYS;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_RESET;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_SCREEN_CAPTURE;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_SDK_MANAGED_FINGER_LOCKING;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_SERVER_PORT;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_SERVER_URL;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_SILENT_FINGERPRINT_REGISTRATION;
import static com.daon.fido.sdk.sample.SettingsActivity.PREF_SILENT_SRP_PASSCODE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.daon.fido.client.sdk.IXUAFDeregisterEventListener;
import com.daon.fido.client.sdk.IXUAFInitialiseListener;
import com.daon.fido.client.sdk.core.Error;
import com.daon.fido.client.sdk.uaf.UafMessageUtils;
import com.daon.fido.sdk.sample.ados.CertificateAccessor;
import com.daon.fido.sdk.sample.ados.CertificateParser;
import com.daon.fido.sdk.sample.ados.ICertificateAccessor;
import com.daon.fido.sdk.sample.ados.ICertificateParser;
import com.daon.sdk.authenticator.util.BusyIndicator;

import java.util.List;
import java.util.Map;

/**
 * Displays application settings based on preferences.xml
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    private static final int REQ_CODE_CHOOSE_FILE = 123;
    private static final String REQUEST_CODE = "requestCode";

    private CheckBoxPreference adosRootCertSet;
    private boolean fidoSdkInitRequired;

    /**
     * Displays application settings based on preferences.xml
     *
     * @param savedInstanceState saved instance state
     */
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        Preference serverUrl = findPreference(PREF_SERVER_URL);
        if (serverUrl != null) {
            bindPreferenceSummaryToValue(serverUrl);
            serverUrl.setOnPreferenceClickListener(new EditTextSetSelectionListener());
        }

        Preference serverPort = findPreference(PREF_SERVER_PORT);
        if (serverPort != null) {
            bindPreferenceSummaryToValue(serverPort);
            serverPort.setOnPreferenceClickListener(new EditTextSetSelectionListener());
        }

        Preference facetId = findPreference(PREF_FACET_ID);
        if (facetId != null) {
            facetId.setOnPreferenceClickListener(preference -> {
                emailFacetID();
                return true;
            });

            String id = UafMessageUtils.getFacetId(getActivity());
            if (!id.isEmpty()) facetId.setSummary(id);
        }

        Preference appVersion = findPreference(PREF_APP_VERSION);
        setAppVersionSummary(appVersion);

        Preference alwaysAllowNativeAuth = findPreference(PREF_NATIVE_LOGON_ALWAYS);
        if (alwaysAllowNativeAuth != null) {
            alwaysAllowNativeAuth.setEnabled(((CoreApplication) getActivity().getApplication()).hasExternalClient());
        }

        adosRootCertSet = findPreference(PREF_ALT_ADOS_ROOT_CERT_PROVIDED);
        if (adosRootCertSet != null) {
            adosRootCertSet.setOnPreferenceChangeListener(new AdosRootCertSetListener());
        }

        String alternativeAdosRootCert = getAlternativeAdosRootCertArg();
        if (alternativeAdosRootCert != null) {
            UserPreferences.instance().putBoolean(PREF_ALT_ADOS_ROOT_CERT_PROVIDED, true);
            storeAlternativeAdosRootCert(alternativeAdosRootCert);
        }

        Preference reset = findPreference(PREF_RESET);
        if (reset != null) {
            reset.setOnPreferenceClickListener(preference -> {
                resetApp();
                return true;
            });
        }

        Preference hardReset = findPreference(PREF_HARD_RESET);
        if (hardReset != null) {
            hardReset.setOnPreferenceClickListener(preference -> {
                hardResetApp();
                return true;
            });
        }

        setFidoSdkInitRequiredListener(findPreference(PREF_ADOS_DEC_SAN));
        setFidoSdkInitRequiredListener(findPreference(PREF_SCREEN_CAPTURE));
        setFidoSdkInitRequiredListener(findPreference(PREF_SILENT_FINGERPRINT_REGISTRATION));
        setFidoSdkInitRequiredListener(findPreference(PREF_SILENT_SRP_PASSCODE));
        setFidoSdkInitRequiredListener(findPreference(PREF_ADOS_ROOT_CERT_SUPPLIED));
        setFidoSdkInitRequiredListener(findPreference(PREF_INVALIDATE_FINGER_ON_NEW_ENROLMENT));
        setFidoSdkInitRequiredListener(findPreference(PREF_SDK_MANAGED_FINGER_LOCKING));
        setFidoSdkInitRequiredListener(findPreference(PREF_INIT_PARAMS_TO_SERVER));
        setFidoSdkInitRequiredListener(findPreference(PREF_IGNORE_NATIVE_CLIENTS));
        setFidoSdkInitRequiredListener(findPreference(PREF_ALWAYS_ALLOW_AUTHENTICATOR_CHOICE));

    }

    private void setAppVersionSummary(Preference preference) {
        if (preference != null) {
            try {
                PackageInfo packageInfo;
                if (getActivity() != null) {
                    PackageManager packageManager = getActivity().getPackageManager();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        packageInfo = packageManager.getPackageInfo(getActivity().getPackageName(), PackageManager.PackageInfoFlags.of(0));
                    } else {
                        packageInfo = packageManager.getPackageInfo(getActivity().getPackageName(), 0);
                    }
                    if (packageInfo != null) {
                        preference.setSummary(packageInfo.versionName);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(CoreApplication.TAG, "setAppVersionSummary: " + e.getMessage());
            }
        }
    }

    private void setFidoSdkInitRequiredListener(Preference preference) {
        if (preference != null) {
            preference.setOnPreferenceChangeListener(new FidoSdkInitRequiredListener());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getPreferenceScreen().getSharedPreferences() != null) {
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getPreferenceScreen().getSharedPreferences() != null) {
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }
    }

    private void reinitializeSdk() {
        Log.d(TAG, "reinitializeSdk: ");
        if (fidoSdkInitRequired) {
            Log.d(TAG, "reinitializeSdk: fidoSdkInitRequired is true");
            BusyIndicator.setBusy(getActivity(), getString(R.string.reinit_sdk_wait_message));
            new InitialiseSdkTask(getActivity(), new IXUAFInitialiseListener() {
                @Override
                public void onInitialiseComplete() {
                    Log.d(TAG, "onInitialiseComplete: ");
                    fidoSdkInitRequired = false;
                    BusyIndicator.setNotBusy(getActivity());
                }

                @Override
                public void onInitialiseWarnings(List<Error> warnings) {
                    Log.w(TAG, "onInitialiseWarnings: " + warnings);
                    // Handle warnings
                    fidoSdkInitRequired = false;
                    BusyIndicator.setNotBusy(getActivity());
                }

                @Override
                public void onInitialiseFailed(int code, String message) {
                    Log.e(TAG, "onInitialiseFailed: " + message);
                    fidoSdkInitRequired = false;
                    BusyIndicator.setNotBusy(getActivity());
                    Toast.makeText(getActivity(), R.string.error_initialising_uaf, Toast.LENGTH_LONG).show();
                }
            }).execute();
        }
    }

    private void resetApp() {
        Log.d(CoreApplication.TAG, "Pre-reset state");
        logSdkStoredData();

        String message = getString(R.string.confirm_reset);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getActivity());
        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton(R.string.dialog_confirm_yes, (arg0, arg1) -> ((CoreApplication) getActivity().getApplication()).getFido().reset(((CoreApplication) getActivity().getApplication()).getAppId(), ixuafDeregisterEventListener));

        alertDialogBuilder.setNegativeButton(R.string.dialog_confirm_cancel, (dialog, which) -> {
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void hardResetApp() {
        Log.d(CoreApplication.TAG, "Pre-reset state");
        logSdkStoredData();

        RPSAService service = (RPSAService) ((CoreApplication) getActivity().getApplication()).getCommService();
        service.resetState();
        service.resetSessionId();

        String message = getString(R.string.confirm_hard_reset);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getActivity());
        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton(R.string.dialog_confirm_yes, (arg0, arg1) -> ((CoreApplication) getActivity().getApplication()).getFido().reset(ixuafDeregisterEventListener));

        alertDialogBuilder.setNegativeButton(R.string.dialog_confirm_cancel, (dialog, which) -> {
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void logSdkStoredData() {
        Log.d(CoreApplication.TAG, "logSdkStoredData: Shared Preferences:");
        if (UserPreferences.instance() != null) {
            Map<String, ?> sharedPrefs = UserPreferences.instance().getAll();
            if (sharedPrefs != null) {
                for (Map.Entry<String, ?> sharedPrefEntry : sharedPrefs.entrySet()) {
                    if (sharedPrefEntry.getValue() == null) {
                        Log.d(CoreApplication.TAG, sharedPrefEntry.getKey() + ":null");
                    } else {
                        Log.d(CoreApplication.TAG, sharedPrefEntry.getKey() + ":" + sharedPrefEntry.getValue().toString());
                    }
                }
            }
        }

        Log.d(CoreApplication.TAG, "logSdkStoredData: Private Files:");
        if (getActivity() != null) {
            String[] fileList = getActivity().fileList();
            for (String file : fileList) {
                Log.d(CoreApplication.TAG, file);
            }
        }
    }

    private String getAlternativeAdosRootCertArg() {
        Bundle args = getArguments();
        String base64EncodedCert = null;
        if (args != null) {
            base64EncodedCert = args.getString(SettingsActivity.ARG_ADOS_ROOT_CERT, null);
        }
        return base64EncodedCert;
    }

    private void storeAlternativeAdosRootCert(String base64EncodedCert) {
        ICertificateAccessor certificateAccessor = new CertificateAccessor();
        certificateAccessor.storeCertificate(base64EncodedCert, getActivity());
        if (adosRootCertSet != null) {
            adosRootCertSet.setChecked(true);
        }
        Toast.makeText(getActivity(), R.string.ados_root_cert_set, Toast.LENGTH_LONG).show();
    }

    private void emailFacetID() {
        String id = UafMessageUtils.getFacetId(getActivity());
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("message/rfc822");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "IdentityX FIDO Facet ID");

        String body = "Facet ID:\n" + id;
        sendIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(sendIntent, "email"));
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the preference's value is
     * changed, its summary (line of text below the preference title) is updated to reflect the value.
     * The summary is also immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     */
    private void bindPreferenceSummaryToValue(Preference preference) {

        if (preference == null) return;

        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        this.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferenceChanged: ");
        reinitializeSdk();
    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object value) {
        String stringValue = value.toString();
        if (preference instanceof ListPreference) {
            // For list settings, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

        } else {
            if (stringValue.isEmpty()) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getActivity());
                alertDialogBuilder.setMessage(R.string.field_cannot_be_empty);

                alertDialogBuilder.setPositiveButton(R.string.dialog_ok, (arg0, arg1) -> {
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return false;
            }

            // For all other settings, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }

    private final IXUAFDeregisterEventListener ixuafDeregisterEventListener = new IXUAFDeregisterEventListener() {

        @Override
        public void onDeregistrationComplete() {
            Log.d(TAG, "onDeregistrationComplete: ");
            boolean isInit = ((CoreApplication) getActivity().getApplication()).getFido().isInitialised();
            Log.d(CoreApplication.TAG, "Post-reset state isInit :" + isInit);
            ((CoreApplication) getActivity().getApplication()).clearRegisteredFidoAccounts();
            Log.d(CoreApplication.TAG, "Post-reset state");
            logSdkStoredData();
            Toast.makeText(getActivity(), "Reset succeeded", Toast.LENGTH_LONG).show();
            if (!((CoreApplication) getActivity().getApplication()).getFido().isInitialised()) {
                Log.d(CoreApplication.TAG, "Post-reset state fido !init");
                fidoSdkInitRequired = true;
                reinitializeSdk();
            }
        }

        @Override
        public void onDeregistrationFailed(int errorCode, String errorMessage) {
            Log.e(TAG, "onDeregistrationFailed: " + errorMessage);
            Log.d(CoreApplication.TAG, "Post-reset state on failure with code: " + errorCode + ", message: " + errorMessage);
            logSdkStoredData();
            Toast.makeText(getActivity(), "Reset failed: " + errorMessage, Toast.LENGTH_LONG).show();
            if (!((CoreApplication) getActivity().getApplication()).getFido().isInitialised()) {
                Log.d(CoreApplication.TAG, "Post-reset state failure fido !init");
                fidoSdkInitRequired = true;
                reinitializeSdk();
            }
        }
    };

    private class AdosRootCertSetListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            if (preference instanceof CheckBoxPreference) {
                if (((CheckBoxPreference) preference).isChecked()) {
                    Toast.makeText(getActivity(), R.string.ados_root_cert_unset, Toast.LENGTH_LONG).show();
                    return true;
                } else {
                    Intent intent = Intent.createChooser(new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT), "Select a file");
                    intent.putExtra(REQUEST_CODE, REQ_CODE_CHOOSE_FILE);
                    mStartForResult.launch(intent);
                    return false;
                }
            }
            return true;
        }
    }

    private static class EditTextSetSelectionListener implements Preference.OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(@NonNull Preference preference) {
            EditTextPreference editPref = (EditTextPreference) preference;
            if (editPref.getText() != null) {
                editPref.setOnBindEditTextListener(editText -> editText.setSelection(editPref.getText().length()));
            }
            return true;
        }
    }

    private class FidoSdkInitRequiredListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            Log.d(TAG, "onPreferenceChange: ");
            fidoSdkInitRequired = true;
            return true;
        }
    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent intent = result.getData();
            if (intent != null) {
                int requestCode = Integer.parseInt(intent.getStringExtra(REQUEST_CODE));
                if (requestCode == REQ_CODE_CHOOSE_FILE) {
                    try {
                        byte[] fileContents = SettingsActivity.readFileContents(intent.getData(), getActivity());
                        ICertificateParser certificateParser = new CertificateParser();
                        String base64EncodedCert = certificateParser.parse(fileContents);
                        storeAlternativeAdosRootCert(base64EncodedCert);
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    });
}
