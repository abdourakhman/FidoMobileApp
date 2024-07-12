package com.daon.fido.sdk.sample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.daon.fido.client.sdk.model.Transaction;
import com.daon.fido.client.sdk.transaction.TransactionContent;
import com.daon.fido.client.sdk.transaction.TransactionUtils;
import com.google.gson.Gson;

public class DisplayTransactionActivity extends LoggedInActivity {

    private static final String TAG = DisplayTransactionActivity.class.getSimpleName();
    public static final String TRANSACTION_EXTRA_INTENT_KEY = "transaction_extras_intent_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_transaction);
        setFinishOnTouchOutside(false);

        String transactionData = getIntent().getStringExtra(TRANSACTION_EXTRA_INTENT_KEY);

        if (transactionData == null) {
            Log.e(TAG, "Transaction read error");
            cancelPressed();
            return;
        }

        Transaction transaction = new Gson().fromJson(transactionData, Transaction.class);

        TextView transactionTextContentView = findViewById(com.daon.fidosdklib.R.id.textView_transaction);
        ImageView transactionImageContentView = findViewById(com.daon.fidosdklib.R.id.imageView_transaction);

        TransactionContent transactionContent = TransactionUtils.getTransactionContent(this, transaction);

        if (transactionContent.isValid()) {
            switch (transactionContent.getType()) {
                case Text -> {
                    transactionImageContentView.setVisibility(View.GONE);
                    transactionTextContentView.setVisibility(View.VISIBLE);
                    transactionTextContentView.setText(transactionContent.getText());
                }
                case PngImage -> {
                    transactionTextContentView.setVisibility(View.GONE);
                    transactionImageContentView.setVisibility(View.VISIBLE);
                    transactionImageContentView.setImageBitmap(transactionContent.getImage());
                }
            }
        } else {
            Log.e(TAG, "Transaction content error: " + transactionContent.getErrorMessage());
            cancelPressed();
            return;
        }

        Button cancelButton = findViewById(com.daon.fidosdklib.R.id.transaction_cancel);

        cancelButton.setOnClickListener(v -> cancelPressed());

        Button okButton = findViewById(com.daon.fidosdklib.R.id.transaction_ok);

        okButton.setOnClickListener(v -> okPressed());

    }

    private void cancelPressed() {
        Intent resultIntent = new Intent();
        setResult(RESULT_CANCELED, resultIntent);
        finish();
    }

    private void okPressed() {
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
