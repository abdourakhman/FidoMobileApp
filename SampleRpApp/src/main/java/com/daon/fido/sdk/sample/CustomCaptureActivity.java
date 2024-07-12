package com.daon.fido.sdk.sample;

import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;

import com.daon.fido.client.sdk.model.AccountInfo;
import com.daon.sdk.authenticator.capture.CaptureActivity;

public class CustomCaptureActivity extends CaptureActivity {

    private static CustomCaptureActivity instance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public static CustomCaptureActivity getInstance() {
        return instance;
    }

    public void showAccountChooser(AccountInfo[] accountInfos) {
        //Using account chooser DialogFragment
        String[] accounts = new String[accountInfos.length];
        for (int i = 0; i < accountInfos.length; i++) {
            accounts[i] = accountInfos[i].getUserName();
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable("accounts", accounts);

        ChooseAccountDialogFragment chooseAccountDialogFragment = new ChooseAccountDialogFragment(selectedAccount -> {
            if (selectedAccount == -1) {
                //This will cancel the authentication.
                ((CoreApplication) getApplication()).getFido().submitUserSelectedAccount(null);
            } else {
                ((CoreApplication) getApplication()).getFido().submitUserSelectedAccount(accountInfos[selectedAccount]);
            }
        });

        chooseAccountDialogFragment.setArguments(bundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(chooseAccountDialogFragment, "ChooseAccount_tag");
        ft.commitAllowingStateLoss();
    }
}
