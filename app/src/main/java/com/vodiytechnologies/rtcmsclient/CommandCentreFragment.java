package com.vodiytechnologies.rtcmsclient;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Diyor on 7/27/2015.
 */
public class CommandCentreFragment extends Fragment{

    private TextView mCurrentStatusTextView;

    public static final String CURRENT_STATUS_UPDATE_ACTION = "CURRENT_STATUS_UPDATE_ACTION";
    // current status as a key should be same in receiver
    public static final String CURRENT_STATUS = "CURRENT_STATUS";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.command_centre_fragment, container, false);

        mCurrentStatusTextView = (TextView) fragmentView.findViewById(R.id.currentStatusTextViewId);



        return fragmentView;
    }

    public void setStatus(String status) {
        mCurrentStatusTextView.setText(status);
    }
}
