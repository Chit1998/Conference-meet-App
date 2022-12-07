package com.calingapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.calingapp.R;
import com.calingapp.helps.Constants;
import com.calingapp.preferences.UserPreferences;
import com.chaos.view.PinView;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class VerifyActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private PinView pinView;
    private String sentOtp,code,phoneNumber;
    private UserPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        auth = FirebaseAuth.getInstance();
        phoneNumber = getIntent().getStringExtra(Constants.phoneNumber);
        pinView = findViewById(R.id.firstPinView);
        preferences = new UserPreferences(this);

        verifyPhoneNumber(phoneNumber);

        findViewById(R.id.button_verify)
                .setOnClickListener(v -> {
                    code = Objects.requireNonNull(pinView.getText()).toString().trim();
                    if (!code.isEmpty() | code.length() == 6){
                        pinView.setText(code);
                        verifyCode(code);
                    }else {
                        Constants.setMessage(VerifyActivity.this,Constants.empty);
                    }
                });
    }

    private void verifyPhoneNumber(String phoneNumber) {
        PhoneAuthOptions option = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(VerifyActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

                    @Override
                    public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                        super.onCodeAutoRetrievalTimeOut(s);
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        sentOtp = s;
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        String sms = phoneAuthCredential.getSmsCode();
                        if (sms != null) {
                            pinView.setText(sms);
                            verifyCode(sms);
                        }
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Constants.setMessage(VerifyActivity.this,e.getMessage());
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(option);

    }


    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(sentOtp,code);
        authenticateUser(credential);
    }

    public void authenticateUser(PhoneAuthCredential credential){
        auth.signInWithCredential(credential).addOnSuccessListener(authResult -> {
            preferences.setPhoneNumber(phoneNumber,auth.getUid());
            startActivity(new Intent(getApplicationContext(),CreateProfileActivity.class));
            finish();
        }).addOnFailureListener(e -> Constants.setMessage(VerifyActivity.this,e.getMessage()));
    }
}