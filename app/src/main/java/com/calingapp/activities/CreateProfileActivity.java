package com.calingapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.calingapp.R;
import com.calingapp.helps.Constants;
import com.calingapp.models.UsersModel;
import com.calingapp.preferences.UserPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class CreateProfileActivity extends AppCompatActivity {

    private final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private static final int REQUEST_PERMISSION_CODE = 10;
    private EditText eFullName, eEmailAddress, eUsername;
    private ImageView imageUser;
    private Uri imageUri = null;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference imageRef;
    private UserPreferences preferences;
    private String phoneNumber;
    private HashMap<String,String> userData;
    private String url = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        init();

        findViewById(R.id.image_user_profile)
                .setOnClickListener(v -> {
                    if (!getPermissions()){
                        CropImage.activity()
                                .setAspectRatio(1,1).start(CreateProfileActivity.this);
                    }
                });

        if (userData.get(Constants.phoneNumber) != null){
            phoneNumber = userData.get(Constants.phoneNumber);
        }else {
//            onBackPressed();
        }

        findViewById(R.id.button_save)
                .setOnClickListener(v -> {
                    try {
                        if (eFullName.getText().toString().isEmpty()){
                            eFullName.setError(Constants.empty);
                        }else if (eEmailAddress.getText().toString().isEmpty()){
                            eEmailAddress.setError(Constants.empty);
                        }else if (eUsername.getText().toString().isEmpty()){
                            eUsername.setError(Constants.empty);
                        }else if (eUsername.getText().toString().length() < 8){
                            eUsername.setError(Constants.length);
                        }else if (!(eEmailAddress.getText().toString().matches(emailPattern))){
                            eEmailAddress.setError(Constants.validEmail);
                        }else if (url == null && url.equals("")){
                            Toast.makeText(this, Constants.image_selection, Toast.LENGTH_SHORT).show();
                        }else {
                        saveData(eUsername.getText().toString(), eFullName.getText().toString(),
                                eEmailAddress.getText().toString(),phoneNumber,
                                FirebaseAuth.getInstance().getUid(), url);
                        }
                    }catch (Exception e){
                        Toast.makeText(this, Constants.image_selection, Toast.LENGTH_SHORT).show();
                        Log.d(Constants.TAG_CREATE_PROFILE, "onCreate: "+e.getMessage());
                    }
                });
    }

    private void init(){
        eEmailAddress = findViewById(R.id.eEmailAddress);
        eFullName = findViewById(R.id.eFullName);
        eUsername = findViewById(R.id.eUsername);
        imageUser = findViewById(R.id.image_user_profile);
        preferences = new UserPreferences(this);
        userData = preferences.getUserData();
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        imageRef = storage.getReference().child("imageCalling").child(auth.getUid());
    }

    private void saveData(String username, String name, String email, String phoneNumber, String uid, String url){
        UsersModel model = new UsersModel(username,phoneNumber,uid,email, new Date(), url, name);

        firestore.collection(Constants.usersCalling)
                .document(Objects.requireNonNull(auth.getUid()))
                .set(model)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        preferences.setUserData(name,email,username,url);
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> Constants.setMessage(CreateProfileActivity.this, e.getMessage()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (userData.get(Constants.FULL_NAME) != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            if (data != null){
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                imageUri = result.getUri();
                imageRef.child(Constants.IMAGE_FILE_NAME)
                        .putFile(imageUri)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                task
                                        .getResult()
                                        .getStorage()
                                        .getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            Glide
                                                    .with(this)
                                                    .load(uri)
                                                    .centerCrop()
                                                    .into(imageUser);
                                            url = uri.toString();
                                        })
                                        .addOnFailureListener(e -> {
                                            Constants.setMessage(CreateProfileActivity.this, e.getMessage());
                                            Log.d(Constants.TAG_CREATE_PROFILE, "onFailure: "+e.getMessage());
                                        });
                            }else {
                                Constants.setMessage(CreateProfileActivity.this, Constants.FAILED);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Constants.setMessage(CreateProfileActivity.this, e.getMessage());
                        });
            }
        }
    }

    //    TODO Permissions
    private boolean getPermissions() {
        if (ContextCompat.checkSelfPermission(
                CreateProfileActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(
                    CreateProfileActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CODE
            );

            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.setMessage(CreateProfileActivity.this, Constants.PERMISSION_GRANTED);
            }else {
                Constants.setMessage(CreateProfileActivity.this, Constants.PERMISSION_FAILED);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}