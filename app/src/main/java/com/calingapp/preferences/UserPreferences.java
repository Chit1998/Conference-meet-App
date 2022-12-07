package com.calingapp.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.calingapp.helps.Constants;

import java.util.HashMap;

public class UserPreferences {
    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public UserPreferences(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(Constants.User, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void setUserData(String name, String email, String username, String url){
        editor.putString(Constants.FULL_NAME,name);
        editor.putString(Constants.EMAIL_ADDRESS,email);
        editor.putString(Constants.USER_NAME,username);
        editor.putString(Constants.IMAGE_URL,url);
        editor.commit();
    }

    public void setPhoneNumber(String phone, String uid){
        editor.putString(Constants.phoneNumber, phone);
        editor.putString(Constants.USER_ID, uid);
        editor.commit();
    }

    public void setToken(String token){
        editor.putString(Constants.FCM_TOKEN, token);
        editor.commit();
    }

    public HashMap<String, String> getUserData(){
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.USER_NAME, preferences.getString(Constants.USER_NAME, null));
        map.put(Constants.FULL_NAME, preferences.getString(Constants.FULL_NAME, null));
        map.put(Constants.EMAIL_ADDRESS, preferences.getString(Constants.EMAIL_ADDRESS, null));
        map.put(Constants.USER_ID, preferences.getString(Constants.USER_ID, null));
        map.put(Constants.phoneNumber, preferences.getString(Constants.phoneNumber, null));
        map.put(Constants.FCM_TOKEN, preferences.getString(Constants.FCM_TOKEN, null));
        map.put(Constants.IMAGE_URL, preferences.getString(Constants.IMAGE_URL, null));
        return map;
    }

    public void clearData(){
        editor.clear();
        editor.commit();
    }
}
