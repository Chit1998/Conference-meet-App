package com.calingapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.calingapp.R;
import com.calingapp.helps.Constants;
import com.calingapp.preferences.UserPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

import timber.log.Timber;

public class ProfileViewActivity extends AppCompatActivity {

    private ImageView image_user_profile_picture;
    private TextView text_name_main,text_email_main,text_username_main,text_phoneNumber_main,text_logOut_main;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private UserPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        init();
        preferences = new UserPreferences(this);
        try {
            db.collection(Constants.usersCalling)
                    .document(Objects.requireNonNull(auth.getUid()))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            String url = task.getResult().getString("image_url");
                            String name = task.getResult().getString("full_name");
                            String username = task.getResult().getString("username");
                            String email = task.getResult().getString("emailAddress");
                            String phone = task.getResult().getString("phoneNumber");

                            Glide.with(ProfileViewActivity.this)
                                    .load(url)
                                    .centerCrop()
                                    .into(image_user_profile_picture);

                            text_name_main.setText(name);
                            text_username_main.setText(username);
                            text_email_main.setText(email);
                            text_phoneNumber_main.setText(phone);

                        }
                    })
                    .addOnFailureListener(e -> {
                        Timber.tag(Constants.TAG_PROFILE).d("onCreate: %s", e.getMessage());
                        Constants.setMessage(ProfileViewActivity.this, e.getMessage());
                    });
        }catch (Exception e){
            e.printStackTrace();
            Timber.tag(Constants.TAG_PROFILE).d("onCreate: %s", e.getMessage());
        }

        text_logOut_main.setOnClickListener(v -> signOut());
    }

    private void init(){
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        image_user_profile_picture = findViewById(R.id.image_user_profile_picture);
        text_name_main = findViewById(R.id.text_name_main);
        text_email_main = findViewById(R.id.text_email_main);
        text_username_main = findViewById(R.id.text_username_main);
        text_phoneNumber_main = findViewById(R.id.text_phoneNumber_main);
        text_logOut_main = findViewById(R.id.text_logOut_main);
    }

    private void signOut(){
        DocumentReference reference = db.collection(Constants.usersCalling)
                .document(Objects.requireNonNull(auth.getUid()));
        HashMap<String, Object> token = new HashMap<>();
        token.put(Constants.FCM_TOKEN, FieldValue.delete());
        reference.update(token)
                .addOnSuccessListener(unused -> {
                    preferences.clearData();
                    auth.signOut();
                    startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Constants.setMessage(ProfileViewActivity.this,e.getMessage()));
    }
}