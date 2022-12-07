package com.calingapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.calingapp.R;
import com.calingapp.adapters.UserAdapter;
import com.calingapp.helps.Constants;
import com.calingapp.interfaces.UsersListener;
import com.calingapp.models.UsersModel;
import com.calingapp.preferences.UserPreferences;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements UsersListener {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private UserPreferences preferences;
    private HashMap<String, String> map;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private List<UsersModel> users;
    private UserAdapter adapter;
    private ImageView imageConference,image_user;
    private final int REQUEST_CODE_BATTERY_OPTIMIZATION = 21;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        preferences = new UserPreferences(this);
        map = preferences.getUserData();

        recyclerView = findViewById(R.id.rv_user_list);
        refreshLayout = findViewById(R.id.swipeRefreshLayout);
        imageConference = findViewById(R.id.imageConference);
        image_user = findViewById(R.id.image_user);
        refreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        refreshLayout.setOnRefreshListener(this::getUsers);

        users = new ArrayList<>();
        adapter = new UserAdapter(users, this);
        recyclerView.setAdapter(adapter);

        db.collection(Constants.usersCalling)
                .document(auth.getUid())
                .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    String url = task.getResult().getString("image_url");

                                    Glide.with(MainActivity.this)
                                            .load(url)
                                            .centerCrop()
                                            .into(image_user);

                                    image_user.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),ProfileViewActivity.class)));
                                }
                            }
                        });

        getUsers();
        checkForBatteryOptimization();

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::sendFCMTokenToDatabase);
    }

    private void getUsers(){
        refreshLayout.setRefreshing(true);
        db.collection(Constants.usersCalling)
                .get()
                .addOnCompleteListener(task -> {
                    refreshLayout.setRefreshing(false);
                    if (task.isSuccessful() && task.getResult() != null){
                        recyclerView.setVisibility(View.VISIBLE);
                        users.clear();
                        for (QueryDocumentSnapshot snapshot : task.getResult()){
                            if (Objects.equals(map.get(Constants.USER_ID), snapshot.getId())){
                                continue;
                            }
                            users.add(new UsersModel(
                                    snapshot.getString(Constants.USER_NAME),
                                    snapshot.getString(Constants.phoneNumber),
                                    snapshot.getString(Constants.USER_ID),
                                    snapshot.getString(Constants.EMAIL_ADDRESS),
                                    snapshot.getDate(Constants.TIMESTAMP),
                                    snapshot.getString(Constants.IMAGE_URL),
                                    snapshot.getString(Constants.FULL_NAME),
                                    snapshot.getString(Constants.FCM_TOKEN)
                            ));
                        }
                        if (users.size() > 0){
                            adapter.notifyDataSetChanged();
                        }else {
                            Toast.makeText(this, Constants.FAILED, Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(this, Constants.FAILED, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendFCMTokenToDatabase(String token){
        preferences.setToken(token);
        db = FirebaseFirestore.getInstance();
        DocumentReference reference = db.collection(Constants.usersCalling)
                .document(Objects.requireNonNull(auth.getUid()));
        reference.update(Constants.FCM_TOKEN,token)
                .addOnSuccessListener(unused -> Timber.tag(Constants.TAG_MAIN).d("updateToken: %s", token))
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onClickVideoMeetListener(UsersModel user) {
        Timber.tag(Constants.TAG_MAIN).d("onClickVideoMeetListener: %s", user.getToken());
        if (user.getToken() == null || user.getToken().trim().isEmpty()){
            Toast.makeText(this, Constants.NOT_VIDEO_MEET, Toast.LENGTH_SHORT).show();
        }else {
            startActivity(new Intent(getApplicationContext(), OutgoingInvitationActivity.class)
                    .putExtra(Constants.User, user)
                    .putExtra(Constants.TYPE, "video"));
        }
    }

    @Override
    public void onClickAudioMeetListener(UsersModel user) {
        if (user.getToken() == null || user.getToken().trim().isEmpty()){
            Toast.makeText(this, Constants.NOT_VIDEO_MEET, Toast.LENGTH_SHORT).show();
        }else {
            startActivity(new Intent(getApplicationContext(), OutgoingInvitationActivity.class)
                    .putExtra(Constants.User, user)
                    .putExtra(Constants.TYPE, "audio"));
        }
    }

    @Override
    public void onClickMultipleUserActionListener(Boolean isMultipleSelected) {
        if (isMultipleSelected){
            imageConference.setVisibility(View.VISIBLE);
            imageConference.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
                intent.putExtra("selectedUser", new Gson().toJson(adapter.getSelectedUsers()));
                intent.putExtra(Constants.TYPE, "video");
                intent.putExtra("isMultiple", true);
                startActivity(intent);
            });
        } else {
            imageConference.setVisibility(View.GONE);
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void checkForBatteryOptimization(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Warning");
                builder.setMessage("Battery optimization is enabled. It can interrupt running background services.");
                builder.setPositiveButton("Disable", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATION);
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                builder.create().show();

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==  REQUEST_CODE_BATTERY_OPTIMIZATION){
            checkForBatteryOptimization();
        }
    }
}