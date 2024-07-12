package com.daon.fido.sdk.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.daon.fido.client.sdk.core.IFidoSdk;
import com.daon.fido.client.sdk.model.AuthenticatorReg;
import com.daon.fido.client.sdk.ui.UIHelper;

import com.google.gson.Gson;

public class ListAuthenticatorsActivity extends LoggedInActivity {
    public static final String EXTRA_AUTHENTICATORS = "authenticators";
    public static final String EXTRA_CHOSEN_AUTHENTICATOR = "chosenAuthenticator";

    private Gson gson = new Gson();

    private AuthenticatorReg[] authenticators;
    private int selectedIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_authenticators);

        setFinishOnTouchOutside(false);

        TransferAuthenticator[] transferAuthenticators = gson.fromJson(getIntent().getExtras().getString(EXTRA_AUTHENTICATORS), TransferAuthenticator[].class);
        authenticators = convertToAuthenticatorReg(transferAuthenticators);

        ListView authenticatorsListView = (ListView) findViewById(R.id.list_view_authenticators);

        Button cancelButton = findViewById(R.id.button_cancel_list_authenticators);


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelPressed();
            }
        });

        AuthenticatorRegAdapter adapter = new AuthenticatorRegAdapter(this, authenticators);
        authenticatorsListView.setAdapter(adapter);
        authenticatorsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedIndex = position;
            }
        });
    }

    private AuthenticatorReg[] convertToAuthenticatorReg(TransferAuthenticator[] transferAuthenticators) {
        AuthenticatorReg[] authenticators = new AuthenticatorReg[transferAuthenticators.length];

        for(int i=0; i<authenticators.length; i++) {
            authenticators[i] = UIHelper.getAuthenticatorReg(transferAuthenticators[i].aaid, transferAuthenticators[i].isRegistered);
        }

        return authenticators;
    }

    private void cancelPressed() {
        finish();
    }

}

