package com.calingapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.calingapp.R;
import com.calingapp.helps.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

public class SignUpActivity extends AppCompatActivity {

    EditText ePhoneNumber;
    CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ePhoneNumber = findViewById(R.id.ePhoneNumber);
        ccp = findViewById(R.id.ccp);

        findViewById(R.id.button_login)
                .setOnClickListener(v -> {
                    if (ePhoneNumber.getText().toString().isEmpty()){
                        ePhoneNumber.setError("Empty");
                    }else {
                        sendPhoneNumberToVerify(ePhoneNumber.getText().toString(), ccp.getDefaultCountryCode());
                    }
                });

    }

    private void sendPhoneNumberToVerify(String phoneNumber, String defaultCountryCode) {
        startActivity(new Intent(getApplicationContext(), VerifyActivity.class)
                .putExtra(Constants.phoneNumber, "+"+defaultCountryCode+phoneNumber));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }
}