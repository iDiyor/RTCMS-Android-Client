package com.vodiytechnologies.rtcmsclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Created by Diyor on 7/27/2015.
 */
public class CommandCentreActivity extends Activity{

    private TextView mCurrentStatusTextView;

    public static final String CURRENT_STATUS_UPDATE_ACTION = "CURRENT_STATUS_UPDATE_ACTION";
    // current status as a key should be same in receiver
    public static final String CURRENT_STATUS = "CURRENT_STATUS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.commend_centre_activity);

        mCurrentStatusTextView = (TextView) findViewById(R.id.currentStatusTextViewId);
    }

    public void onButtonClick(View v) {
        Button button = (Button)v;
        String text = button.getText().toString();
        mCurrentStatusTextView.setText(text);
        broadcast(CURRENT_STATUS, text, CURRENT_STATUS_UPDATE_ACTION);
    }

    private void broadcast(String name,String status, String action) {
        Intent i = new Intent(action);
        i.putExtra(name, status);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
}
