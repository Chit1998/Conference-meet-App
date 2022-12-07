package com.calingapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.calingapp.R;
import com.calingapp.helps.Constants;
import com.calingapp.interfaces.UsersListener;
import com.calingapp.models.UsersModel;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserVH> {
    private List<UsersModel> model;
    private UsersListener listener;
    private List<UsersModel> selectedUsers;

    public UserAdapter(List<UsersModel> model, UsersListener listener) {
        this.model = model;
        this.listener = listener;
        selectedUsers = new ArrayList<>();
    }

    public List<UsersModel> getSelectedUsers(){
        return selectedUsers;
    }

    @NonNull
    @Override
    public UserAdapter.UserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserVH(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_item_container_layout, parent,false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserVH holder, int position) {
        holder.setValue(model.get(position));
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class UserVH extends RecyclerView.ViewHolder {
        TextView fullName, username, firstChar;
        ImageView videoMeet, audioMeet, imageSelected;
        ConstraintLayout layout;

        public UserVH(@NonNull View itemView) {
            super(itemView);

            fullName = itemView.findViewById(R.id.text_fullName);
            username = itemView.findViewById(R.id.text_username);
            firstChar = itemView.findViewById(R.id.textFirstChar);
            videoMeet = itemView.findViewById(R.id.image_videoMeeting);
            audioMeet = itemView.findViewById(R.id.image_CallMeeting);
            imageSelected = itemView.findViewById(R.id.imageSelected);
            layout = itemView.findViewById(R.id.userContainer);
        }

        void setValue(UsersModel models){
            fullName.setText(models.getFull_name());
            username.setText(models.getUsername());
            firstChar.setText(models.getFull_name().substring(0,1));
            videoMeet.setOnClickListener(v -> listener.onClickVideoMeetListener(models));
            audioMeet.setOnClickListener(v -> listener.onClickAudioMeetListener(models));

            layout.setOnLongClickListener(v -> {
                if (imageSelected.getVisibility() == View.GONE){
                    selectedUsers.add(models);
                    imageSelected.setVisibility(View.VISIBLE);
                    videoMeet.setVisibility(View.GONE);
                    audioMeet.setVisibility(View.GONE);
                    listener.onClickMultipleUserActionListener(true);
                }
                return true;
            });

            layout.setOnClickListener(v -> {
                if (imageSelected.getVisibility() == View.VISIBLE){
                    selectedUsers.remove(models);
                    imageSelected.setVisibility(View.GONE);
                    videoMeet.setVisibility(View.VISIBLE);
                    audioMeet.setVisibility(View.VISIBLE);
                    if (selectedUsers.size() == 0){
                        listener.onClickMultipleUserActionListener(false);
                    }
                }else {
                    if (selectedUsers.size() > 0){
                        selectedUsers.add(models);
                        imageSelected.setVisibility(View.VISIBLE);
                        videoMeet.setVisibility(View.GONE);
                        audioMeet.setVisibility(View.GONE);
                    }
                }
            });

        }
    }
}
