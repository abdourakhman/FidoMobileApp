package com.daon.fido.sdk.sample;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.daon.fido.client.sdk.ServerCommResult;
import com.daon.fido.client.sdk.util.TaskExecutor;


public class SubmitOfflineOTPTask extends TaskExecutor<ServerCommResult> {
    interface SubmitOfflineOTPCallback {
        void onSubmitOfflineOTPComplete(String response);
        void onSubmitOfflineOTPFailed(int errorCode, String errorMessage);
    }

    private final String email;
    private final String otp;
    private final String transactionData;
    private final Context context;
    private final SubmitOfflineOTPTask.SubmitOfflineOTPCallback callback;

    SubmitOfflineOTPTask(Context context,  @NonNull String emailAddress,
                         @NonNull String otp,
                         @Nullable String transactionData,
                         @NonNull SubmitOfflineOTPTask.SubmitOfflineOTPCallback callback) {
        this.context = context;
        this.callback = callback;
        this.email = emailAddress;
        this.otp = otp;
        this.transactionData = transactionData;
    }

    @Override
    protected ServerCommResult doInBackground() {
        RPSAService service = (RPSAService)((CoreApplication)context).getCommService();
        return service.submitOfflineOTP(email, otp, transactionData);
    }

    @Override
    protected void onPostExecute(ServerCommResult result) {
        if (result.isSuccessful()) {
            callback.onSubmitOfflineOTPComplete(result.getResponse());
        } else {
            callback.onSubmitOfflineOTPFailed(result.getErrorInfo().getCode(), result.getErrorInfo().getMessage());
        }
    }
}
