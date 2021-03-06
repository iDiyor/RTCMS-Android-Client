package com.vodiytechnologies.rtcmsclient;

import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Diyor on 8/10/2015.
 */
public class UserProfileFragment extends Fragment {

    private String mUserName;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.user_profile_fragment, container, false);


        ImageView profileImage = (ImageView) fragmentView.findViewById(R.id.profileImageViewId);
        TextView userName = (TextView) fragmentView.findViewById(R.id.userNameTextViewId);

        profileImage.setImageResource(R.mipmap.profile_image);
        userName.setText(getArguments().getString("userName"));

        return fragmentView;
    }
}
