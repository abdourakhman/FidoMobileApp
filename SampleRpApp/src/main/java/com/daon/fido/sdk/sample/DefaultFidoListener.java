package com.daon.fido.sdk.sample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.daon.fido.client.sdk.AuthenticatorChoiceGroup;
import com.daon.fido.client.sdk.IXUAFListener;
import com.daon.fido.client.sdk.core.INotifyUafResultCallback;
import com.daon.fido.client.sdk.model.AccountInfo;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.client.sdk.model.Transaction;
import com.google.gson.Gson;

public class DefaultFidoListener implements IXUAFListener {
    Activity currentActivity;
    private static DefaultFidoListener instance = null;

    public static DefaultFidoListener getInstance() {
        if (instance == null) instance = new DefaultFidoListener();

        return instance;
    }

    public void setCurrentActivity(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onAccountListAvailable(AccountInfo[] accountInfos) {

    }

    @Override
    public void onAuthListAvailable(Authenticator[][] authenticators) {

    }

    @Override
    public void onDisplayTransaction(Transaction transaction) {
        Log.d("JSAPI", "DefaultFidoListener onDisplayTransaction");
        Intent intent = new Intent(currentActivity, JavascriptApiDisplayTransactionActivity.class);

        String transactionData = new Gson().toJson(transaction);
        intent.putExtra(JavascriptApiDisplayTransactionActivity.TRANSACTION_EXTRA_INTENT_KEY, transactionData);
        currentActivity.startActivity(intent);
    }

    @Override
    public void onConfirmationOTP(String confirmationOTP) {

    }

    @Override
    public void onExpiryWarning(INotifyUafResultCallback.ExpiryWarning[] expiryWarnings) {

    }

    @Override
    public void onUserLockWarning() {

    }

    @Override
    public void onReadyToAuthenticate(AuthenticatorChoiceGroup authenticatorChoiceGroup) {

    }
}
