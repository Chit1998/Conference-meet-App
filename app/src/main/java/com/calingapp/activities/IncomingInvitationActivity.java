package com.calingapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.calingapp.R;
import com.calingapp.helps.Constants;
import com.calingapp.network.ApiClient;
import com.calingapp.network.ApiServices;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetOngoingConferenceService;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;
import org.jitsi.meet.sdk.JitsiMeetView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.logging.ConsoleHandler;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingInvitationActivity extends AppCompatActivity {

    private TextView text_meeting_invitation_user;
    private ImageView image_user_invitation,image_call_start,image_call_end,image_meetingType;
    private String meetingType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_invitation);
        init();
        meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);

        if (meetingType != null){
            if (meetingType.equals("video")){
                image_meetingType.setImageResource(R.drawable.ic_round_videocam);
            }else{
                image_meetingType.setImageResource(R.drawable.ic_round_call);
            }
            text_meeting_invitation_user.setText(getIntent().getStringExtra(Constants.FULL_NAME));
            Glide.with(this)
                    .load(getIntent().getStringExtra(Constants.IMAGE_URL))
                    .centerCrop()
                    .into(image_user_invitation);

        }

        image_call_start.setOnClickListener(v -> {
            sendInvitationResponse(Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                    getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN));
        });

        image_call_end.setOnClickListener(v -> {
            sendInvitationResponse(Constants.REMOTE_MSG_INVITATION_REJECTED,
                    getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN));
        });

    }

    private void init(){
        text_meeting_invitation_user = findViewById(R.id.text_meeting_invitation_user);
        image_user_invitation = findViewById(R.id.image_user_invitation);
        image_call_start = findViewById(R.id.image_call_start);
        image_call_end = findViewById(R.id.image_call_end);
        image_meetingType = findViewById(R.id.image_meetingType);
    }

    private void sendInvitationResponse(String type, String receiverToken){
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), type);

        }catch (Exception e){
            e.printStackTrace();
            Constants.setMessage(this,e.getMessage());
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String types){
        ApiClient.getClient().create(ApiServices.class)
                .sendRemoteMessage(Constants.getRemoteMessageHeader(), remoteMessageBody)
                .enqueue(new Callback<String>() {
                    @SuppressLint("LogNotTimber")
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        Log.d(Constants.TAG_OUTGOING, response.body());
                        if (response.isSuccessful()){
                            if (types.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                                try {
                                    URL serverUrl = new URL("https://meet.jit.si");
                                    JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                                    builder.setServerURL(serverUrl);
                                    builder.setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM));

                                    if (meetingType.equals("audio")){
                                        builder.setVideoMuted(true);
                                    }
                                    JitsiMeetActivity.launch(IncomingInvitationActivity.this,builder.build());
                                    JitsiMeetUserInfo info = new JitsiMeetUserInfo();
                                    info.setAvatar(new URL(getIntent().getStringExtra(Constants.IMAGE_URL)));
                                    info.setDisplayName(getIntent().getStringExtra(Constants.FULL_NAME));
                                    builder.setUserInfo(info);
                                    finish();
                                }catch (Exception e){
                                    e.printStackTrace();
                                    Log.d(Constants.TAG_INCOMING, "onResponse: "+e.getMessage());
                                }
                            }else {
                                Constants.setMessage(IncomingInvitationActivity.this, "rejected");
                                finish();
                            }
                        }else {
                            Constants.setMessage(IncomingInvitationActivity.this,response.message()+" "+response.code());
                            finish();
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Log.d(Constants.TAG_OUTGOING, "onFailure: "+t.getMessage());
                        Constants.setMessage(IncomingInvitationActivity.this,t.getMessage());
                        finish();
                    }
                });
    }

    private final BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @SuppressLint("LogNotTimber")
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null){
                if (type.equals(Constants.REMOTE_MSG_INVITATION_CANCELLED)){
                    Log.d(Constants.TAG_OUTGOING, "onReceive: cancelled");
                    Toast.makeText(context, "broadcast cancelled", Toast.LENGTH_SHORT).show();
                    finish();
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