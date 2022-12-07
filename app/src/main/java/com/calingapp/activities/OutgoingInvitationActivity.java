package com.calingapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.calingapp.R;
import com.calingapp.helps.Constants;
import com.calingapp.models.UsersModel;
import com.calingapp.network.ApiClient;
import com.calingapp.network.ApiServices;
import com.calingapp.preferences.UserPreferences;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingInvitationActivity extends AppCompatActivity {

    private ImageView image_user_invitation,image_call_end,image_meetingType;
    private View view_background_1, view_background_2;
    private TextView text_meeting_invitation_user;
    private CardView cardImageProfile;
    private UserPreferences preferences;
    private HashMap<String, String> sender;
    private String invitationToken = null;
    private String meetingRoom = null;
    private String meetingType = null;
    private int rejectionCount = 0;
    private int totalReceivers = 0;
    private UsersModel model = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);
        init();

        meetingType = getIntent().getStringExtra(Constants.TYPE);
        model = (UsersModel) getIntent().getSerializableExtra(Constants.User);

        preferences = new UserPreferences(this);
        sender = preferences.getUserData();

        if (model != null){
            text_meeting_invitation_user.setText(model.getFull_name());
            Glide.with(this)
                    .load(model.getImage_url())
                    .centerCrop()
                    .into(image_user_invitation);
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(OutgoingInvitationActivity.this, task -> {
                    if (task.isSuccessful() && task.getResult() != null){
                        invitationToken = task.getResult();
                        if (meetingType != null){
                            if (getIntent().getBooleanExtra("isMultiple",false)){
                                Type type = new TypeToken<ArrayList<UsersModel>>(){}.getType();
                                ArrayList<UsersModel> receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUser"),type);
                                if (receivers != null){
                                    totalReceivers = receivers.size();
                                }
                                initiateMeeting(meetingType,null, receivers);
                            }else {
                                if (model != null){
                                    totalReceivers = 1;
                                    initiateMeeting(meetingType,model.getToken(),null);
                                }
                            }
                        }
                        Log.d(Constants.TAG_OUTGOING, "onCreate: "+invitationToken);
                    }
        });

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        image_call_end.setOnClickListener(v -> {
            if (getIntent().getBooleanExtra("isMultiple",false)){
                Type type = new TypeToken<ArrayList<UsersModel>>(){}.getType();
                ArrayList<UsersModel> receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUser"),type);
                cancelInvitation(null, receivers);
            }else {
                if (model != null){
                    cancelInvitation(model.getToken(), null);
                }
            }
        });


        if (meetingType != null){
            if (meetingType.equals("video")){
                image_meetingType.setImageResource(R.drawable.ic_round_videocam);
            }else {
                image_meetingType.setImageResource(R.drawable.ic_round_call);
            }
        }

    }

    private void init(){
        image_meetingType = findViewById(R.id.image_meetingType);
        image_user_invitation = findViewById(R.id.image_user_invitation);
        text_meeting_invitation_user = findViewById(R.id.text_meeting_invitation_user);
        image_call_end = findViewById(R.id.image_call_end);
        view_background_1 = findViewById(R.id.view_background_1);
        view_background_2 = findViewById(R.id.view_background_2);
        cardImageProfile = findViewById(R.id.cardImageProfile);

    }

    private void initiateMeeting(String meetingType, String receiverToken, ArrayList<UsersModel> receivers){
        try {
            JSONArray tokens = new JSONArray();
            if (receiverToken != null){
                tokens.put(receiverToken);
            }

            if (receivers != null && receivers.size() > 0){
                StringBuilder userName = new StringBuilder();
                for (int i = 0; i < receivers.size(); i++){
                    tokens.put(receivers.get(i).getToken());
                    userName.append(receivers.get(i).getFull_name()).append("\n");
                }
                text_meeting_invitation_user.setText(userName.toString());
                view_background_1.setVisibility(View.GONE);
                view_background_2.setVisibility(View.GONE);
                cardImageProfile.setVisibility(View.GONE);
            }

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);

            data.put(Constants.FULL_NAME, sender.get(Constants.FULL_NAME));
            data.put(Constants.USER_NAME, sender.get(Constants.USER_NAME));
            data.put(Constants.EMAIL_ADDRESS, sender.get(Constants.EMAIL_ADDRESS));
            data.put(Constants.USER_ID, sender.get(Constants.USER_ID));
            data.put(Constants.phoneNumber, sender.get(Constants.phoneNumber));
            data.put(Constants.IMAGE_URL, sender.get(Constants.IMAGE_URL));

            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, invitationToken);

            meetingRoom = sender.get(Constants.USER_ID) + "_"+ UUID.randomUUID().toString().substring(0,5);

            data.put(Constants.REMOTE_MSG_MEETING_ROOM,meetingRoom);

            body.put(Constants.REMOTE_MSG_DATA, data);

            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(),Constants.REMOTE_MSG_INVITATION);
        }catch (Exception e){
            e.printStackTrace();
            Constants.setMessage(OutgoingInvitationActivity.this,e.getMessage());
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String types){
        ApiClient.getClient().create(ApiServices.class)
                .sendRemoteMessage(Constants.getRemoteMessageHeader(), remoteMessageBody)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        Log.d(Constants.TAG_OUTGOING, response.body());
                        if (response.isSuccessful()){
                            if (types.equals(Constants.REMOTE_MSG_INVITATION)){
                                Log.d(Constants.TAG_OUTGOING, response.body());
                            }else if (types.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){
                                Log.d(Constants.TAG_OUTGOING, "onResponse: "+types);
                                Constants.setMessage(OutgoingInvitationActivity.this, "response Cancelled");
                                finish();
                            }
                        }else {
                            Constants.setMessage(OutgoingInvitationActivity.this,response.message()+" "+response.code());
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Log.d(Constants.TAG_OUTGOING, "onFailure: "+t.getMessage());
                        Constants.setMessage(OutgoingInvitationActivity.this,t.getMessage());
                        finish();
                    }
                });
    }

    private void cancelInvitation(String receiverToken, ArrayList<UsersModel> receivers){
        try {
            JSONArray tokens = new JSONArray();
            if (receiverToken != null){
                tokens.put(receiverToken);
            }

            if (receivers != null && receivers.size() > 0){
                for (UsersModel user : receivers){
                    tokens.put(user.getToken());
                }
            }
            
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
            Log.d(Constants.TAG_OUTGOING, "cancelInvitation: "+data);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE);

        }catch (Exception e){
            e.printStackTrace();
            Constants.setMessage(this,e.getMessage());
        }
    }

    private final BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null){
                if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                    try {
                        URL serverUrl = new URL("https://meet.jit.si");
                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverUrl);
                        builder.setRoom(meetingRoom);
                        if (meetingType.equals("audio")){
                            builder.setVideoMuted(true);
                        }
                        JitsiMeetUserInfo info = new JitsiMeetUserInfo();
                        info.setAvatar(new URL(model.getImage_url()));
                        info.setDisplayName(model.getFull_name());
                        builder.setUserInfo(info);
                        JitsiMeetActivity.launch(OutgoingInvitationActivity.this,builder.build());
                        finish();
                    }catch (Exception e){
                        e.printStackTrace();
                        Log.d(Constants.TAG_OUTGOING, "onReceive: "+e.getMessage());
                        finish();
                    }
                }else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)){
                    rejectionCount += 1;
                    if (rejectionCount == totalReceivers){
                        Log.d(Constants.TAG_OUTGOING, "onReceive: Rejected");
                        Toast.makeText(context, "broadcast Rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(
                        invitationResponseReceiver,
                        new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(
                        invitationResponseReceiver);
    }
}