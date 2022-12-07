package com.calingapp.models;

import java.io.Serializable;
import java.util.Date;

public class UsersModel implements Serializable {
    public String username, phoneNumber, uid, emailAddress, image_url, full_name, token;
    public Date timestamp;

    public UsersModel() {
    }

    public UsersModel(String username, String phoneNumber, String uid, String emailAddress, Date timestamp, String image_url, String full_name) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.uid = uid;
        this.emailAddress = emailAddress;
        this.timestamp = timestamp;
        this.image_url = image_url;
        this.full_name = full_name;
    }

    public UsersModel(String username, String phoneNumber, String uid, String emailAddress, Date timestamp, String image_url, String full_name, String token) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.uid = uid;
        this.emailAddress = emailAddress;
        this.timestamp = timestamp;
        this.image_url = image_url;
        this.full_name = full_name;
        this.token = token;
    }



    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
