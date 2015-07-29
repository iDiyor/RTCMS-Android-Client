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

    // BROADCAST ACTIONS
    public static final String SOCKET_CONNECTION_ERROR_ACTION = "SOCKET_CONNECTION_ERROR";
    public static final String SOCKET_CONNECTION_SUCCESS_ACTION = "SOCKET_CONNECTION_SUCCESS";
    public static final String SOCKET_MESSAGE_FROM_SERVER_ACTION = "SOCKET_MESSAGE_FROM_SERVER";

    // SOCKET EVENTS
    private static final String MOBILE_LOCATION_EMIT = "mobile:location";
    private static final String MOBILE_CLIENT_CONNECTION_EMIT = "client:connection";
    private static final String MOBILE_CLIENT_DISCONNECTION_EMIT = "client:disconnection";
    private static final String MOBILE_ON_MESSAGE_FROM_SERVER = "server:message";

    // MESSAGE NAME
    public static final String CONNECTION_STATUS = "CONNECTION_STATUS";
    public static final String SERVER_SAID = "SERVER_SAID";

    private boolean IS_SERVICE_RUNNING = false;

    private Socket mSocket;
    private String mUser;

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
            options.forceNew = true;
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
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        mUser = intent.getStringExtra("user");

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
                data.put("user", mUser);
            } catch (JSONException e) {
                return;
            }
            emit(MOBILE_CLIENT_CONNECTION_EMIT, data);
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

    private void broadcastIntentWithMessageWithAction(String name, String content, String action) {
        Intent i = new Intent(action);
        i.putExtra(name, content);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    private void emit(String event, JSONObject object) {
        if (mSocket.connected()) {
            mSocket.emit(event, object);
        }
    }

    private JSONObject getLocationJSONObject(Location location) {
        JSONObject data;
        try {
            data = new JSONObject();
            data.put("user", mUser);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(socketBroadcastReceiver);

        JSONObject data;
        try {
            data = new JSONObject();
            data.put("type", "mobile");
            data.put("user", mUser);
        } catch (JSONException e) {
            return;
        }
        emit(MOBILE_CLIENT_DISCONNECTION_EMIT, data);

        mSocket.off(Socket.EVENT_CONNECT, onConnectToServer);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(MOBILE_ON_MESSAGE_FROM_SERVER, onMessageFromServer);
        mSocket.disconnect();
        mSocket = null;

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

                JSONObject locationJSONData = getLocationJSONObject(location);
                emit(MOBILE_LOCATION_EMIT, locationJSONData);
            }
        }
    };
}
