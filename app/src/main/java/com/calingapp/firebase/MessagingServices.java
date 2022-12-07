package com.calingapp.firebase;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.calingapp.activities.IncomingInvitationActivity;
import com.calingapp.helps.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingServices extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(Constants.TAG_MESSAGING, "onNewToken: "+token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null){
            Log.d("FCM", "on Message Received: "+remoteMessage.getNotification().getBody());
        }
        String type = remoteMessage.getData().get(Constants.TYPE);
//        Log.d(Constants.FIREBASE_SERVICE, "onMessageReceived: "+type);
        if (type != null) {
            if (type.equals(Constants.REMOTE_MSG_INVITATION)){
                Intent intent = new Intent(getApplicationContext(), IncomingInvitationActivity.class);
                intent.putExtra(Constants.REMOTE_MSG_MEETING_TYPE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_TYPE));

                intent.putExtra(Constants.FULL_NAME,
                        remoteMessage.getData().get(Constants.FULL_NAME));

                intent.putExtra(Constants.EMAIL_ADDRESS,
                        remoteMessage.getData().get(Constants.EMAIL_ADDRESS));

                intent.putExtra(Constants.USER_NAME,
                        remoteMessage.getData().get(Constants.USER_NAME));

                intent.putExtra(Constants.USER_ID,
                        remoteMessage.getData().get(Constants.USER_ID));

                intent.putExtra(Constants.phoneNumber,
                        remoteMessage.getData().get(Constants.phoneNumber));

                intent.putExtra(Constants.IMAGE_URL,
                        remoteMessage.getData().get(Constants.IMAGE_URL));

                intent.putExtra(Constants.REMOTE_MSG_INVITER_TOKEN,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITER_TOKEN));

                intent.putExtra(Constants.REMOTE_MSG_MEETING_ROOM,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_ROOM));

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){
                Intent intent  = new Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE);
                intent.putExtra(
                        Constants.REMOTE_MSG_INVITATION_RESPONSE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITATION_RESPONSE));
                Log.d(Constants.FIREBASE_SERVICE, "onMessageReceived: "+remoteMessage.getData().get(Constants.REMOTE_MSG_INVITATION_RESPONSE));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

            }
        }
    }
}
