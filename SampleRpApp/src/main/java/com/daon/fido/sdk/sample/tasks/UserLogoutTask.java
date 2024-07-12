package com.daon.fido.sdk.sample.tasks;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.daon.fido.client.sdk.IXUAFCommService;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.CoreApplication;
import com.daon.fido.sdk.sample.RPSAService;
import com.daon.fido.sdk.sample.ServerOperationResult;
import com.daon.fido.sdk.sample.exception.CommunicationsException;
import com.daon.fido.sdk.sample.exception.ServerError;

public class UserLogoutTask extends TaskExecutor<ServerOperationResult<Boolean>> {
    private static final String TAG = UserLogoutTask.class.getSimpleName();
    private final boolean timeout;
    private final Context context;
    private final String sessionId;
    private final IXUAFCommService communicationService;
    private final ITaskResultCallback taskResultCallback;

    public UserLogoutTask(Context context, boolean timeout, String sessionId, IXUAFCommService communicationService, ITaskResultCallback taskResultCallback) {
        Log.d(TAG, "init UserLogoutTask with timeout: " + timeout);
        this.taskResultCallback = taskResultCallback;
        this.communicationService = communicationService;
        this.context = context;
        this.sessionId = sessionId;
        this.timeout = timeout;
    }

    @Override
    protected ServerOperationResult<Boolean> doInBackground() {
        ServerOperationResult<Boolean> result;
        try {
            Log.d(TAG, "Call deleteSession(): " + sessionId);
            RPSAService service = (RPSAService) communicationService;
            service.deleteSession(sessionId);
            result = new ServerOperationResult<>(true);
        } catch (ServerError e) {
            result = new ServerOperationResult<>(e.getError());
        } catch (CommunicationsException e) {
            result = new ServerOperationResult<>(e.getError());
        }
        return result;
    }

    @Override
    protected void onPostExecute(ServerOperationResult<Boolean> response) {
        Log.d(TAG, "deleteSession() complete with timeout: " + timeout);
        taskResultCallback.finalizeTask();

        Intent intent = new Intent(CoreApplication.LOGOUT_BROADCAST_ACTION);
        intent.putExtra(CoreApplication.EXTRA_LOGOUT_TIMEOUT, timeout);

        if (response.isSuccessful()) {
            Log.d(TAG, "deleteSession() Success");
            RPSAService service = (RPSAService) communicationService;
            service.resetSessionId();
            service.setmState(RPSAService.State.logout);
            intent.putExtra(CoreApplication.EXTRA_LOGOUT_SUCCESS, true);
        } else {
            // Expired session - just logout and ignore the error
            if (response.getError().getCode() == 202) {
                Log.d(TAG, "deleteSession() Failed with expired session so success");
                intent.putExtra(CoreApplication.EXTRA_LOGOUT_SUCCESS, true);
            } else {
                Log.d(TAG, "deleteSession() Failed. Code: " + response.getError().getCode() + ", Message: " + response.getError().getMessage());
                intent.putExtra(CoreApplication.EXTRA_LOGOUT_SUCCESS, false);
                intent.putExtra(CoreApplication.EXTRA_LOGOUT_ERROR_CODE, response.getError().getCode());
                intent.putExtra(CoreApplication.EXTRA_LOGOUT_ERROR_MESSAGE, response.getError().getMessage());
            }
        }

        Log.d(TAG, "Broadcast logout intent.");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    protected void onCancelled() {
        Log.d(TAG, "onCancelled: ");
        taskResultCallback.finalizeTask();
    }

}
