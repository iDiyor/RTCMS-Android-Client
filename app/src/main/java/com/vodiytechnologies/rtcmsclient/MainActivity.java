package com.vodiytechnologies.rtcmsclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {//implements ConnectionCallbacks, OnConnectionFailedListener , LocationListener{ //SocketResultReceiver.Receiver {

    private Location mCurrentLocation;

    // TextViews
    private TextView mLongitudeTextView;
    private TextView mLatitudeTextView;
    private TextView mMessageTextView;

    private String mClient;
    private String mClientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_container_activity);

        mLongitudeTextView = (TextView) findViewById(R.id.longitudeId);
        mLatitudeTextView = (TextView) findViewById(R.id.latitudeId);
        mMessageTextView = (TextView) findViewById(R.id.messageId);

        TextView userTextVew = (TextView) findViewById(R.id.userTextViewId);

        userTextVew.setText("User ID: " + mClientId + ": " + mClient);

        final Button startServiceButton = (Button) findViewById(R.id.startServiceButtonId);
        startServiceButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  // Socket service
                  Intent socketIntent = new Intent(MainActivity.this, SocketService.class);
                  socketIntent.putExtra("clientId", mClientId);
                  socketIntent.putExtra("client", mClient);

                  startService(socketIntent);
                  // Location service
                  Intent locationIntent = new Intent(MainActivity.this, LocationService.class);
                  startService(locationIntent);

              }
        });

        Button stopServiceButton = (Button) findViewById(R.id.stopServiceButtonId);
        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Location service
                Intent locationIntent = new Intent(MainActivity.this, LocationService.class);
                stopService(locationIntent);
                setCurrentLocation(null);
                // Socket service
                Intent intent = new Intent(MainActivity.this, SocketService.class);
                stopService(intent);
                showMessage("Socket connection disconnected");


            }
        });

        Button commandCentreButton = (Button) findViewById(R.id.commandCentreButtonId);
        commandCentreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CommandCentreActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setCurrentLocation(Location location) {
        mCurrentLocation = location;
        displayLocation();
    }

    public Location getCurrentLocation() {
        if (mCurrentLocation != null)
            return mCurrentLocation;
        return null;
    }

    // message from server
    public void showMessage(String message) {
        mMessageTextView.setText(message);
    }

    public void displayLocation() {
        if (getCurrentLocation() != null) {
            double latitude = getCurrentLocation().getLatitude();
            double longitude = getCurrentLocation().getLongitude();
            mLongitudeTextView.setText(String.valueOf(longitude));
            mLatitudeTextView.setText(String.valueOf(latitude));
        } else {
            mLongitudeTextView.setText("Location Service is not available");
            mLatitudeTextView.setText("Location Service is not available");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mClient = getIntent().getStringExtra("client");
        mClientId = getIntent().getStringExtra("clientId");

        // Socket service
        Intent socketIntent = new Intent(MainActivity.this, SocketService.class);
        socketIntent.putExtra("clientId", mClientId);
        socketIntent.putExtra("client", mClient);

        startService(socketIntent);
        // Location service
        Intent locationIntent = new Intent(MainActivity.this, LocationService.class);
        startService(locationIntent);


        /**
         * SOCKET SERVICE BROADCAST REGISTER
         */
        IntentFilter connectionSuccessIntentFilter = new IntentFilter(SocketService.SOCKET_CONNECTION_SUCCESS_ACTION);
        IntentFilter messageFromServerIntentFilter = new IntentFilter(SocketService.SOCKET_MESSAGE_FROM_SERVER_ACTION);
        IntentFilter connectionErrorIntentFilter = new IntentFilter(SocketService.SOCKET_CONNECTION_ERROR_ACTION);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, connectionSuccessIntentFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, messageFromServerIntentFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, connectionErrorIntentFilter);

        /**
         * LOCATION SERVICE BROADCAST REGISTER
         */
        IntentFilter locationUpdateIntentFilter = new IntentFilter(LocationService.LOCATION_UPDATE_ACTION);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, locationUpdateIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Location service
        Intent locationIntent = new Intent(MainActivity.this, LocationService.class);
        stopService(locationIntent);
        setCurrentLocation(null);
        // Socket service
        Intent intent = new Intent(MainActivity.this, SocketService.class);
        stopService(intent);
        showMessage("Socket connection disconnected");

        // unregister receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /**************************
            * SOCKET SERVICE BROADCAST
            ***************************/
            if (intent.getAction().equals(SocketService.SOCKET_CONNECTION_SUCCESS_ACTION)) {
                String message = intent.getStringExtra(SocketService.CONNECTION_STATUS);
                showMessage(message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

            if (intent.getAction().equals(SocketService.SOCKET_MESSAGE_FROM_SERVER_ACTION)) {
                String message = intent.getStringExtra(SocketService.SERVER_SAID);
                //showMessage(message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

            if (intent.getAction().equals(SocketService.SOCKET_CONNECTION_ERROR_ACTION)) {
                String message = intent.getStringExtra(SocketService.CONNECTION_STATUS);
                showMessage(message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

            /****************************
            * LOCATION SERVICE BROADCAST
            ****************************/
            if (intent.getAction().equals(LocationService.LOCATION_UPDATE_ACTION)) {
                Location location = intent.getParcelableExtra(LocationService.LOCATION_MESSAGE);
                setCurrentLocation(location);
                //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

        }
    };
}