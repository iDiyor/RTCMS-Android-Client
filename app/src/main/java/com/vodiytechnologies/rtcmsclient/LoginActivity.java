package com.vodiytechnologies.rtcmsclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Diyor on 7/26/2015.
 */
public class LoginActivity extends Activity {

    private EditText mUsernameTextView;
    private EditText mPasswordTextView;

    private final String authUrl = "http://52.28.143.209:3000/api/user/authenticate";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        mUsernameTextView = (EditText)findViewById(R.id.usernameEditTextId);
        mPasswordTextView = (EditText) findViewById(R.id.passwordEditTextId);

        final TextView responseTextView = (TextView) findViewById(R.id.responseTextViewId);



        final RequestQueue queue = VolleySingleton.getInstance(this.getApplicationContext()).getRequestQueue();

        final Button signInButton = (Button) findViewById(R.id.signInButtonId);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // request params
                JSONObject params = null;
                if (mUsernameTextView.getText().length() > 0 && mPasswordTextView.getText().length() > 0) {
                    try {
                        params = new JSONObject();
                        params.put("username", mUsernameTextView.getText());
                        params.put("password", mPasswordTextView.getText());
                        params.put("role", "driver");
                    } catch (JSONException e) {
                        return;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter valid username or password!", Toast.LENGTH_SHORT).show();
                }
                // json request
                if (params != null) {
                    JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, authUrl, params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {

                            String responseStatus;
                            String user;
                            try {
                                responseStatus = jsonObject.getString("responseStatus");
                                user = jsonObject.getJSONObject("responseBody").getJSONObject("userProfile").getString("first_name");
                            } catch (JSONException e) {
                                Log.d("JSONException","JSONException while JsonResponse");
                                return;
                            }
                            Toast.makeText(getApplicationContext(), responseStatus, Toast.LENGTH_SHORT).show();
                            responseTextView.setText(jsonObject.toString());
                            signIn(user);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                    queue.add(jsonReq);
                } else {
                    Toast.makeText(getApplicationContext(), "Null params", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signIn(String user) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("user", user);
        startActivity(intent);
        finish();
    }

}
