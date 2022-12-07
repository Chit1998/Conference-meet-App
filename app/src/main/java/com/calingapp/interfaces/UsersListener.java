package com.calingapp.interfaces;

import com.calingapp.models.UsersModel;

public interface UsersListener {

    void onClickVideoMeetListener(UsersModel user);
    void onClickAudioMeetListener(UsersModel user);
    void onClickMultipleUserActionListener(Boolean isMultipleSelected);

}
