package com.calingapp.helps;

import android.content.Context;
import android.widget.Toast;

import java.util.HashMap;

public class Constants {
    public static final String TAG_MAIN = "MainActivity";
    public static final String TAG_SIGN_UP = "SignUpActivity";
    public static final String TAG_VERIFY = "VerifyActivity";
    public static final String TAG_MESSAGING = "MessagingActivity";
    public static final String TAG_CREATE_PROFILE = "CreateProfileActivity";
    public static final String phoneNumber = "phoneNumber";
    public static final String empty = "Empty";
    public static final String PERMISSION_GRANTED = "Permission Granted";
    public static final String PERMISSION_FAILED = "Permission Failed";
    public static final String FAILED = "Failed";
    public static final String IMAGE_FILE_NAME = "image.jpg";
    public static final String NOT_VIDEO_MEET = "User issue... Try again";
    public static final String User = "user";
    public static final String USERS = "users";
    public static final String FULL_NAME = "full_name";
    public static final String EMAIL_ADDRESS = "emailAddress";
    public static final String USER_NAME = "username";
    public static final String USER_ID = "uid";
    public static final String usersCalling = "usersCalling";
    public static final String length = "Minimum length 8 is required";
    public static final String validEmail = "Enter a valid email address";
    public static final String image_selection = "Please select image";
    public static final String FCM_TOKEN = "fcm_token";
    public static final String IMAGE_URL = "image_url";
    public static final String TIMESTAMP = "timestamp";
    public static final String TYPE = "type";
    public static final String TAG_OUTGOING = "outgoingActivity";
    public static final String TAG_INCOMING = "incomingActivity";

    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";

    public static final String REMOTE_MSG_TYPE = "type";
    public static final String REMOTE_MSG_INVITATION = "invitation";
    public static final String REMOTE_MSG_MEETING_TYPE = "meetingType";
    public static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static final String FIREBASE_SERVICE = "service";

    public static final String REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse";

    public static final String REMOTE_MSG_INVITATION_ACCEPTED = "accept";
    public static final String REMOTE_MSG_INVITATION_REJECTED = "rejected";
    public static final String REMOTE_MSG_INVITATION_CANCELLED = "cancelled";

    public static final String REMOTE_MSG_MEETING_ROOM = "meetingRoom";
    public static final String TAG_PROFILE = "profileActivity";

    public static HashMap<String, String> getRemoteMessageHeader(){
        HashMap<String, String> headers = new HashMap<>();
        headers.put(Constants.REMOTE_MSG_AUTHORIZATION,
                "key=AAAAkuwdz50:APA91bHWbn9f8f1_cKV1VNj9qRBUdu2PURJwEQglS0xzmPsNuFdIMFKrgLBdJjDisOmnqOW-9QkCfbzaG9ruh-nCZpBJyiwLB6BFg6nkRBrDjTQOPqOku99PrJ7QeLJe1-d5Wt5tayrL"
        );
        headers.put(Constants.REMOTE_MSG_CONTENT_TYPE,"application/json");

        return headers;
    }

    public static void setMessage(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
