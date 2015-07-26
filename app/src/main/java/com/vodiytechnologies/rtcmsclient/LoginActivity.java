package com.vodiytechnologies.rtcmsclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Diyor on 7/26/2015.
 */
public class LoginActivity extends Activity {

    private EditText mUsernameTextView;
    private EditText mPasswordTextView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsernameTextView = (EditText)findViewById(R.id.usernameTextViewId);
        mPasswordTextView = (EditText) findViewById(R.id.passwordTextViewId);

        Button signInButton = (Button) findViewById(R.id.signInButtonId);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Sign In", Toast.LENGTH_SHORT).show();
            }
        });


    }

}
