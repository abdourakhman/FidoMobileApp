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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.daon.fido.sdk.sample.ados.CertificateParser;
import com.daon.fido.sdk.sample.ados.ICertificateParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Activity which displays application settings */
public class SettingsActivity extends FragmentActivity {
  public static final String PREF_SERVER_URL = "pref_server_url";
  public static final String PREF_SERVER_PORT = "pref_server_port";
  public static final String PREF_SERVER_SECURE = "pref_server_secure";
  public static final String PREF_LIST_AVAILABLE_AUTHENTICATORS =
      "pref_list_available_authenticators";
  public static final String PREF_NATIVE_LOGON_ALWAYS = "pref_native_logon_always";
  public static final String PREF_TEXT_TX = "pref_text_tx";
  public static final String PREF_ALT_ADOS_ROOT_CERT_PROVIDED = "pref_alt_ados_root_cert_provided";
  public static final String PREF_ADOS_DEC_SAN = "pref_ados_dec_san";
  public static final String ARG_ADOS_ROOT_CERT = "arg_ados_root_cert";
  public static final String PREF_SCREEN_CAPTURE = "pref_screen_capture";
  public static final String PREF_SILENT_FINGERPRINT_REGISTRATION = "pref_silent_finger_reg";
  public static final String PREF_SILENT_SRP_PASSCODE = "pref_silent_srp_passcode";
  public static final String PREF_ADOS_ROOT_CERT_SUPPLIED = "pref_ados_root_cert_supplied";
  public static final String PREF_SIGN_OOTP = "pref_sign_ootp";
  public static final String PREF_INVALIDATE_FINGER_ON_NEW_ENROLMENT =
      "pref_invalidate_finger_on_new_enrolment";
  public static final String PREF_SDK_MANAGED_FINGER_LOCKING = "pref_sdk_managed_finger_locking";
  public static final String PREF_INIT_PARAMS_TO_SERVER = "pref_init_params_to_server";
  public static final String PREF_IGNORE_NATIVE_CLIENTS = "pref_ignore_native_clients";
  public static final String PREF_ALWAYS_ALLOW_AUTHENTICATOR_CHOICE =
      "pref_always_allow_authenticator_choice";
  public static final String PREF_NO_DAON_EXTS = "pref_no_daon_exts";
  public static final String PREF_FACET_ID = "facet_id_preference";
  public static final String PREF_APP_VERSION = "app_version_preference";
  public static final String PREF_RESET = "pref_reset";
  public static final String PREF_HARD_RESET = "pref_hard_reset";

  /**
   * Display the {@link SettingsFragment}
   *
   * @param savedInstanceState saved instance state
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Get intent, action and MIME type
    Intent intent = getIntent();
    String action = intent.getAction();
    String type = intent.getType();

    String base64EncodedAdosRootCert = null;
    if (Intent.ACTION_SEND.equals(action) && type != null) {
      try {
        if ("text/plain".equals(type)) {
          base64EncodedAdosRootCert = handleSendText(intent); // Handle text being sent
        } else {
          base64EncodedAdosRootCert = handleSendCertFile(intent);
        }
      } catch (IllegalArgumentException e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        new Handler()
            .postDelayed(
                new Runnable() {
                  @Override
                  public void run() {
                    SettingsActivity.this.finish();
                  }
                },
                3000);
        return;
      }
    }

    // Send ADoS root cert to activity if necessary
    SettingsFragment settingsFragment = new SettingsFragment();
    if (base64EncodedAdosRootCert != null) {
      Bundle args = new Bundle();
      args.putString(ARG_ADOS_ROOT_CERT, base64EncodedAdosRootCert);
      settingsFragment.setArguments(args);
    }

    // Display the fragment as the main content.
    getSupportFragmentManager()
        .beginTransaction()
        .replace(android.R.id.content, settingsFragment)
        .commit();
  }

  private String handleSendCertFile(Intent intent) {
    Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
    byte[] cert = readFileContents(uri, this);
    ICertificateParser certificateParser = new CertificateParser();
    return certificateParser.parse(cert);
  }

  private String handleSendText(Intent intent) {
    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
    if (sharedText != null) {
      ICertificateParser certificateParser = new CertificateParser();
      return certificateParser.parse(sharedText);
    }
    return null;
  }

  public static byte[] readFileContents(Uri fileUri, Context context) {
    InputStream is = null;
    try {
      is = context.getContentResolver().openInputStream(fileUri);
      return getBytes(is);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (Exception e1) {
          e1.printStackTrace();
          return null;
        }
      }
    }
  }

  private static byte[] getBytes(InputStream inputStream) throws IOException {
    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
    int bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];

    int len = 0;
    while ((len = inputStream.read(buffer)) != -1) {
      byteBuffer.write(buffer, 0, len);
    }
    return byteBuffer.toByteArray();
  }
}
