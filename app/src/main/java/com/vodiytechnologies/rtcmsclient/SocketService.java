package com.vodiytechnologies.rtcmsclient;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.HandlerThread;
import android.os.IBinder;

import android.os.Looper;
import android.os.Message;
import android.os.Handler;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * Created by Diyor on 7/24/2015.
 */
public class SocketService extends Service {

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private static final String TAG = "SocketService:Message";

    // BROADCAST ACTIONS
    public static final String SOCKET_CONNECTION_ERROR_ACTION = "SOCKET_CONNECTION_ERROR";
    public static final String SOCKET_CONNECTION_SUCCESS_ACTION = "SOCKET_CONNECTION_SUCCESS";
    public static final String SOCKET_MESSAGE_FROM_SERVER_ACTION = "SOCKET_MESSAGE_FROM_SERVER";
    public static final String SOCKET_MESSAGE_FROM_SERVER_FROM_WEB_ACTION = "SOCKET_MESSAGE_FROM_SERVER_FROM_WEB_ACTION";
    public static final String SOCKET_JOB_FROM_SERVER_FROM_WEB_ACTION = "SOCKET_JOB_FROM_SERVER_FROM_WEB_ACTION";

    // SOCKET EVENTS
    private static final String MOBILE_LOCATION_EMIT = "mobile:location";
    private static final String MOBILE_CLIENT_STATUS_EMIT = "mobile:client:status";
    private static final String MOBILE_CLIENT_CONNECTION_EMIT = "client:connection";
    private static final String MOBILE_CLIENT_DISCONNECTION_EMIT = "client:disconnect";
    private static final String MOBILE_ON_MESSAGE_FROM_SERVER = "server:message";
    private static final String MOBILE_CLIENT_MESSAGE_SEND = "mobile:client:message:send";

    private static String MOBILE_ON_MESSAGE_FROM_SERVER_FROM_WEB = "server:web:client:message:send:";
    private static String MOBILE_ON_JOB_FROM_SERVER_FROM_WEB = "server:web:client:job:dispatch:";


    // MESSAGE NAME
    public static final String CONNECTION_STATUS = "CONNECTION_STATUS";
    public static final String SERVER_SAID = "SERVER_SAID";
    public static final String WEB_CLIENT_MESSAGE = "WEB_CLIENT_MESSAGE";
    public static final String WEB_CLIENT_JOB = "WEB_CLIENT_JOB";

    private boolean IS_SERVICE_RUNNING = false;

    private String mClient; // client name == user name inside user profile table
    private String mClientId; // client id == user id from user profile table

    private Socket mSocket;

    private JSONObject mObjectMissed = null;
    private String mEventMissed;
    private boolean mIsObjectMissed = false;

    private Location mLastKnownLocation;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // do work here
            if (!IS_SERVICE_RUNNING) {
                mSocket.on(Socket.EVENT_CONNECT, onConnectToServer);
                mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
                mSocket.on(MOBILE_ON_MESSAGE_FROM_SERVER, onMessageFromServer);
                mSocket.on(MOBILE_ON_MESSAGE_FROM_SERVER_FROM_WEB, onMessageFromServerFromWeb);
                mSocket.on(MOBILE_ON_JOB_FROM_SERVER_FROM_WEB, onJobFromServerFromWeb);
                mSocket.connect();
                IS_SERVICE_RUNNING = true;
            }
            //stopSelf(msg.arg1);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            IO.Options options = new IO.Options();
            //options.forceNew = true;
            options.reconnection = true;

            mSocket = IO.socket("http://52.28.143.209:3000", options);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        //mSocket.connect();

        /**
         * LOCATION SERVICE BROADCAST REGISTER
         */
        IntentFilter locationUpdateIntentFilter = new IntentFilter(LocationService.LOCATION_UPDATE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(socketBroadcastReceiver, locationUpdateIntentFilter);
        /**
         * CONTROL CENTRE BROADCAST REGISTER
         */
        IntentFilter statusUpdateIntentFilter = new IntentFilter(CommandCentreFragment.CURRENT_STATUS_UPDATE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(socketBroadcastReceiver,statusUpdateIntentFilter);
        /**
         * MESSAGE BROADCAST REGISTER
         */
        IntentFilter messageSendIntentFilter = new IntentFilter(MessageFragment.MESSAGE_SEND_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(socketBroadcastReceiver, messageSendIntentFilter);
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        mClient = intent.getStringExtra("client");
        mClientId = intent.getStringExtra("clientId");

        MOBILE_ON_MESSAGE_FROM_SERVER_FROM_WEB = "server:web:client:message:send:"  + mClientId;
        MOBILE_ON_JOB_FROM_SERVER_FROM_WEB = "server:web:client:job:dispatch:" + mClientId;
        Log.d(TAG, MOBILE_ON_MESSAGE_FROM_SERVER_FROM_WEB);
        Log.d(TAG, MOBILE_ON_JOB_FROM_SERVER_FROM_WEB);
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        return START_STICKY;
    }

    // SOCKET HANDLER FUNCTIONS
    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            broadcastIntentWithMessageWithAction(CONNECTION_STATUS,"Socket.io connection error!!!", SOCKET_CONNECTION_ERROR_ACTION);
            Log.d("socket.io", "connection error");
        }
    };

    private Emitter.Listener onConnectToServer = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data;
            try {
                data = new JSONObject();
                data.put("type", "mobile");
                data.put("clientId", mClientId);
                data.put("client", mClient);
                data.put("clientStatus", "No Job");
                data.put("lastKnowLocation", getLocationJSONObject(mLastKnownLocation));

            } catch (JSONException e) {
                return;
            }
            emit(MOBILE_CLIENT_CONNECTION_EMIT, data);

            Log.d("SOCKET", "CONNECTION EMIT");

            Log.d(TAG, "Connection emit");

            broadcastIntentWithMessageWithAction(CONNECTION_STATUS, "Socket.io connection success!!!", SOCKET_CONNECTION_SUCCESS_ACTION);
        }
    };

    private Emitter.Listener onMessageFromServer = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data;
            String message;

            try {
                data = (JSONObject) args[0];
                message = data.getString("status");
            } catch (JSONException e) {
                return;
            }
            broadcastIntentWithMessageWithAction(SERVER_SAID, message, SOCKET_MESSAGE_FROM_SERVER_ACTION);
        }
    };

    private Emitter.Listener onMessageFromServerFromWeb = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data;
            String message;

            data = (JSONObject) args[0];
            message = data.toString();

            broadcastIntentWithMessageWithAction(WEB_CLIENT_MESSAGE, message, SOCKET_MESSAGE_FROM_SERVER_FROM_WEB_ACTION);
            Log.d(TAG, "MESSAGE_TO_WEB");
        }
    };

    private Emitter.Listener onJobFromServerFromWeb = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data;
            String message;

            data = (JSONObject) args[0];
            message = data.toString();

            broadcastIntentWithMessageWithAction(WEB_CLIENT_JOB, message, SOCKET_JOB_FROM_SERVER_FROM_WEB_ACTION);
            Log.d(TAG, "MESSAGE_TO_WEB");
        }
    };

    private void broadcastIntentWithMessageWithAction(String name, String content, String action) {
        Intent i = new Intent(action);
        i.putExtra(name, content);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    private void emit(String event, JSONObject object) {
        if (mSocket.connected()) {
            mSocket.emit(event, object);

            /** Fix for location emit
             * (Because sometimes location emits first before socket connection)
             */
            if (mIsObjectMissed) {
                mSocket.emit(mEventMissed, mObjectMissed);

                mIsObjectMissed = false;
                mEventMissed = null;
                mObjectMissed = null;
            }
        } else {
            Log.d(TAG, "Socket is not connected");
            Log.d(TAG, "An object miss");
            mIsObjectMissed = true;
            mEventMissed = event;
            mObjectMissed = object;
        }
    }

    private JSONObject getJSONObject(String key, String value) {
        JSONObject data;
        try {
            data = new JSONObject();
            data.put(key, value);
        } catch (JSONException e) {
            return null;
        }
        return data;
    }
    private JSONObject getLocationJSONObject(Location location) {
        if (location != null) {
            JSONObject data;
            try {
                data = new JSONObject();
                data.put("clientId", mClientId);
                data.put("client", mClient);
                data.put("longitude", location.getLongitude());
                data.put("latitude", location.getLatitude());
                data.put("accuracy", location.getAccuracy());
                data.put("bearing", location.getBearing());
                data.put("speed", location.getSpeed());
                data.put("time", location.getTime());
            } catch (JSONException e) {
                return null;
            }
            return data;
        } else {
            JSONObject data;
            try {
                data = new JSONObject();
                data.put("clientId", mClientId);
                data.put("client", mClient);
                data.put("longitude", 0);
                data.put("latitude", 0);
                data.put("accuracy", 0);
                data.put("bearing", 0);
                data.put("speed", 0);
                data.put("time", 0);
            } catch (JSONException e) {
                return null;
            }
            return data;
        }
    }

    private JSONObject createClientStatusJSONObject(String status) {
        JSONObject data;
        try {
            data = new JSONObject();
            data.put("clientId", mClientId);
            data.put("client", mClient);
            data.put("clientStatus", status);
        } catch (JSONException e) {
            return null;
        }
        return data;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(socketBroadcastReceiver);

        JSONObject data;
        try {
            data = new JSONObject();
            data.put("type", "mobile");
            data.put("clientId", mClientId);
            data.put("client", mClient);
            //data.put("last_known_position", getLocationJSONObject(mLastKnownLocation));
        } catch (JSONException e) {
            return;
        }
        emit(MOBILE_CLIENT_DISCONNECTION_EMIT, data);

        mSocket.off(Socket.EVENT_CONNECT, onConnectToServer);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(MOBILE_ON_MESSAGE_FROM_SERVER, onMessageFromServer);
        mSocket.off(MOBILE_ON_MESSAGE_FROM_SERVER_FROM_WEB, onMessageFromServerFromWeb);
        mSocket.off(MOBILE_ON_JOB_FROM_SERVER_FROM_WEB, onJobFromServerFromWeb);
        mSocket.disconnect();
        mSocket = null;
        mLastKnownLocation = null;

        IS_SERVICE_RUNNING = false;
    }

    private BroadcastReceiver socketBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            /****************************
             * LOCATION SERVICE BROADCAST
             ****************************/
            if (intent.getAction().equals(LocationService.LOCATION_UPDATE_ACTION)) {
                Location location = intent.getParcelableExtra(LocationService.LOCATION_MESSAGE);
                mLastKnownLocation = location;
                JSONObject locationJSONData = getLocationJSONObject(location);
                emit(MOBILE_LOCATION_EMIT, locationJSONData);
                Log.d("SOCKET", "LOCATION EMIT");
            }

            /****************************
             * COMMAND CENTRE BROADCAST
             ****************************/
            if (intent.getAction().equals(CommandCentreFragment.CURRENT_STATUS_UPDATE_ACTION)) {
                String status = intent.getStringExtra(CommandCentreFragment.CURRENT_STATUS);

                JSONObject statusJSONData = createClientStatusJSONObject(status);
                emit(MOBILE_CLIENT_STATUS_EMIT, statusJSONData);
                Log.d("SOCKET", "STATUS EMIT");
            }

            /****************************
             * MESSAGES BROADCAST
             ****************************/
            if (intent.getAction().equals(MessageFragment.MESSAGE_SEND_ACTION)) {
                JSONObject messageBody = null;
                try {
                    messageBody = new JSONObject(intent.getStringExtra(MessageFragment.MESSAGE_BODY));
                } catch (JSONException e) {
                    Log.d(TAG, "MESSAGE_BODY_JSON_EXCEPTION");
                    return;
                }

                if (messageBody != null) {
                    emit(MOBILE_CLIENT_MESSAGE_SEND, messageBody);
                    Log.d(TAG, "MESSAGE_EMTI");
                }
            }
        }
    };
}
