package com.vodiytechnologies.rtcmsclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Timer;

/**
 * Created by Diyor on 8/10/2015.
 */
public class MessageFragment extends Fragment {

    private EditText mMessageEditText;
    private ListView mMessageContainer;
    private Button mSendButton;
    private MessageAdapter mAdapter;
    private ArrayList<Message> mMessageHistory;

    public static final String MESSAGE_SEND_ACTION = "MESSAGE_SEND_ACTION";
    public static final String MESSAGE_BODY = "MESSAGE_BODY";

    private static final String messageAPIUrl = "http://52.29.78.245:3000/api/messages";

    // admin id == user profile id. Used for sending messages and others...
    private final int mAdminId = 3;


    private String mUserProfileId;



    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.message_fragment, container, false);

        mUserProfileId = getArguments().getString("userProfileId");


        mMessageContainer = (ListView) fragmentView.findViewById(R.id.messagesContainer);
        mMessageEditText = (EditText) fragmentView.findViewById(R.id.messageInputEditText);
        mSendButton = (Button) fragmentView.findViewById(R.id.messageSendButton);

        TextView self = (TextView) fragmentView.findViewById(R.id.selfTextView);
        TextView other = (TextView) fragmentView.findViewById(R.id.otherTextView);
        RelativeLayout viewContainer = (RelativeLayout) fragmentView.findViewById(R.id.container);

        other.setText("Admin");
        self.setText("Me");
        loadMessagesHistory();


        final RequestQueue mQueue = VolleySingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        mSendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String newMessageContent = mMessageEditText.getText().toString();
                if (TextUtils.isEmpty(newMessageContent)) {
                    return;
                }

                JSONObject params = null;
                try {
                    params = new JSONObject();
                    params.put("to_id_user_profile", mAdminId);
                    params.put("from_id_user_profile", mUserProfileId);
                    params.put("content", newMessageContent);
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), "Could not send the message", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (params != null) {
                    JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, messageAPIUrl, params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {

//                            "responseTitle": "Inserting a new message into the database",
//                                    "responseStatus": "success",
//                                    "responseBody": {
//                                "to_id_user_profile": "2",
//                                        "from_id_user_profile": "3",
//                                        "content": "I want a Tesla X",
//                                        "time": "2015-08-11T00:50:59.855Z",
//                                        "id_message": 31,
//                                        "fromUser": {
//                                    "id_user_profile": 3,
//                                            "first_name": "Elon",
//                                            "last_name": "Musk"
//                                },
//                                "toUser": {
//                                    "id_user_profile": 2,
//                                            "first_name": "Diyorbek",
//                                            "last_name": "Islomov"
//                                }
                            String responseStatus;
                            String messageContent;
                            String time;
                            JSONObject responseBody;
                            try {
                                responseStatus = jsonObject.getString("responseStatus");
                                responseBody = jsonObject.getJSONObject("responseBody");
                                messageContent = jsonObject.getJSONObject("responseBody").getString("content");
                                time = jsonObject.getJSONObject("responseBody").getString("time");
                            } catch (JSONException e) {
                                Log.d("JSONException", "JSONException while JsonResponse");
                                return;
                            }

                            if (responseStatus.equals("success")) {
                                broadcastMessage(MESSAGE_BODY, responseBody, MESSAGE_SEND_ACTION);
                                addMessageToLocalList(messageContent, time, true);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                    mQueue.add(jsonReq);
                } else {
                    Toast.makeText(getActivity(), "Null params", Toast.LENGTH_SHORT).show();
                }

            }
        });

        return fragmentView;
    }


    public void addMessageToLocalList(String content, String time, boolean isSelf) {
        Message message = new Message();
        message.setId(1);
        message.setContent(content);
        message.setTime(time);
        message.setSelf(isSelf);

        mMessageEditText.setText("");

        mAdapter.add(message);
        mAdapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        mMessageContainer.setSelection(mMessageContainer.getCount() - 1);
    }

    private void broadcastMessage(String name,JSONObject object, String action) {
        Intent i = new Intent(action);
        i.putExtra(name, object.toString());
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(i);
    }

    private void loadMessagesHistory() {
        mMessageHistory  = new ArrayList<Message>();

        mAdapter = new MessageAdapter(getActivity(), new ArrayList<Message>());
        mMessageContainer.setAdapter(mAdapter);


        final RequestQueue mQueue = VolleySingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        // it will also response with admin messages
        String getClietsMessagesAPIUrl = "http://52.29.78.245:3000/api/messages/" + mUserProfileId + "/" + mAdminId;

        // get to_client messages
        JsonArrayRequest jsonReq1 = new JsonArrayRequest(getClietsMessagesAPIUrl,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonResponseArray) {

                    try {
                        final JSONArray sortedArray = selectionSort(jsonResponseArray);
                        for (int i = 0; i < sortedArray.length(); i++) {
                            JSONObject objectX = (JSONObject)jsonResponseArray.get(i);
                            String messageContent = objectX.getString("content");
                            String time = objectX.getString("time");
                            String fromClient = objectX.getString("from_id_user_profile");

                            if (fromClient.equals(mUserProfileId)) {
                                addMessageToLocalList(messageContent, time, true);
                            } else {
                                addMessageToLocalList(messageContent, time, false);
                            }
                        }
                    } catch (JSONException e) {
                        Log.d("JSONException", "JSONException while JsonResponse");
                        return;
                    }
                }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonReq1);
    }

    private JSONArray selectionSort(JSONArray array) throws JSONException{
        //JSONArray sortedArray = new JSONArray();
        for (int i = 0; i < array.length(); i++) {
            JSONObject objectA = (JSONObject)array.get(i);
            int index = i;
            for (int j = i + 1; j < array.length(); j++) {

                JSONObject objectB = (JSONObject)array.get(j);

                int messageIdA = Integer.valueOf(objectA.getString("id_message"));
                int messageIdB = Integer.valueOf(objectB.getString("id_message"));

                if (messageIdB < messageIdA) {
                    index = j;
                }
            }
            JSONObject objectWithSmallestId = (JSONObject)array.get(index);
            //sortedArray.put(objectWithSmallestId);
            array.put(index, objectA);
            array.put(i, objectWithSmallestId);
        }
        return array;
    }
    // on receive message receiver - from server - socket
}
