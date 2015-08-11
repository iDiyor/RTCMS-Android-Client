package com.vodiytechnologies.rtcmsclient;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
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
import com.android.volley.toolbox.JsonObjectRequest;

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

    private static final String messageAPIUrl = "http://52.28.143.209:3000/api/messages";

    private String mClientId;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.message_fragment, container, false);

        mClientId = getArguments().getString("clientId");


        mMessageContainer = (ListView) fragmentView.findViewById(R.id.messagesContainer);
        mMessageEditText = (EditText) fragmentView.findViewById(R.id.messageInputEditText);
        mSendButton = (Button) fragmentView.findViewById(R.id.messageSendButton);

        TextView self = (TextView) fragmentView.findViewById(R.id.selfTextView);
        TextView other = (TextView) fragmentView.findViewById(R.id.otherTextView);
        RelativeLayout viewContainer = (RelativeLayout) fragmentView.findViewById(R.id.container);

        other.setText("Admin");
        loadMessagesHistory();

        final RequestQueue queue = VolleySingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();


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
                    params.put("to_id_user_profile", 2);
                    params.put("from_id_user_profile", mClientId);
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

                            broadcastMessage(MESSAGE_BODY, responseBody, MESSAGE_SEND_ACTION);


                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                    queue.add(jsonReq);
                } else {
                    Toast.makeText(getActivity(), "Null params", Toast.LENGTH_SHORT).show();
                }

            }
        });

        return fragmentView;
    }


    private void addMessageToLocalList(String content, String time, boolean isSelf) {
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

    }

    // on receive message receiver - from server - socket
}
