package com.daon.fido.sdk.sample.push;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.daon.fido.sdk.sample.CoreApplication;
import com.daon.fido.sdk.sample.PushActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FidoFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = FidoFirebaseMessagingService.class.getSimpleName();
    private static final String EXTRA_TRANSACTION_ID = "transaction_id";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "FireBaseService onMessageReceived:" + remoteMessage.getFrom());
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData());
            handleData(remoteMessage);
        }
    }

    private void handleData(RemoteMessage msg) {

        String description = msg.getData().get("description");
        String provider = msg.getData().get("provider");
        String pushType = msg.getData().get("push.type");
        String id = msg.getData().get("id");
        String time = msg.getData().get("time_to_live");
        Log.d(CoreApplication.TAG, "Push Data Desc:" + description + " provider:" + provider + " push type:" + pushType + " id:" + id + " time:" + time);

        Intent intent = new Intent(this, PushActivity.class);
        intent.putExtra(EXTRA_TRANSACTION_ID, id);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
        notificationUtils.showNotificationMessage(provider, description, intent);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        if (((CoreApplication) getApplication()).getFido() != null)
            ((CoreApplication) getApplication()).getFido().setPushNotificationServiceToken(token);
    }
}
