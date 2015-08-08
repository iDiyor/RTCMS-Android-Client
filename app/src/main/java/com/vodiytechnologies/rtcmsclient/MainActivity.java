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

//    private final static int PLAY_SERVICE_RESOLUTION_REQ = 1000;
    private Location mCurrentLocation;
//    private GoogleApiClient mGoogleApiClient;
//    private boolean mRequestingLocationUpdates = false;
//    private LocationRequest mLocationRequest;
//
//    private static int UPDATE_INTERVAL = 1000 * 10; // 10 sec
//    private static int FASTEST_INTERVAL = 1000 * 5; // 5 sec
//    private static int DISPLACEMENT = 5; // 5 meters

    // TextViews
    private TextView mLongitudeTextView;
    private TextView mLatitudeTextView;
    private TextView mMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_container_activity);

        mLongitudeTextView = (TextView) findViewById(R.id.longitudeId);
        mLatitudeTextView = (TextView) findViewById(R.id.latitudeId);
        mMessageTextView = (TextView) findViewById(R.id.messageId);

        TextView userTextVew = (TextView) findViewById(R.id.userTextViewId);

        final String user = getIntent().getStringExtra("client");
        userTextVew.setText("User: " + user);


        //mRequestingLocationUpdates = true;

//        if (checkPlayServices()) {
//            buildGoogleApiClient();
//
//        }

        final Button startServiceButton = (Button) findViewById(R.id.startServiceButtonId);
        startServiceButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent socketIntent = new Intent(MainActivity.this, SocketService.class);
                  socketIntent.putExtra("client", user);
                  startService(socketIntent);

                  Intent locationIntent = new Intent(MainActivity.this, LocationService.class);
                  startService(locationIntent);

              }
        });

        Button stopServiceButton = (Button) findViewById(R.id.stopServiceButtonId);
        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SocketService.class);
                stopService(intent);
                showMessage("Socket connection disconnected");

                Intent locationIntent = new Intent(MainActivity.this, LocationService.class);
                stopService(locationIntent);
                setCurrentLocation(null);
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

//    /* Google Play Services */
//    protected synchronized void buildGoogleApiClient() {
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API).build();
//        createLocationRequest();
//    }
//
//    private boolean checkPlayServices() {
//        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//
//        if (resultCode != ConnectionResult.SUCCESS) {
//            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
//                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RESOLUTION_REQ).show();
//            } else {
//                Toast.makeText(getApplicationContext(), "This Device is not supported", Toast.LENGTH_LONG).show();
//                finish();
//            }
//            return false;
//        }
//        return true;
//    }
//
//    // creating location request object
//    protected void createLocationRequest() {
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(UPDATE_INTERVAL);
//        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
//    }
//
//
//    // starting location updates
//    protected void startLocationUpdates() {
//        LocationServices.FusedLocationApi.requestLocationUpdates(
//                mGoogleApiClient, mLocationRequest, this);
//    }
//
//    // stop location updates
//    protected void stopLocationUpdates() {
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//    }
//
//    @Override
//    public void onConnected(Bundle bundle) {
//        // display location here
//        //displayLocation();
//
//        if (mRequestingLocationUpdates) {
//            startLocationUpdates();
//        }
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        mGoogleApiClient.connect();
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//        Log.d("message", "Connection Failed");
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        // sets global myCurrentLocation
//        setCurrentLocation(location);
//        // displays current location
//        displayLocation();
//
//    }

    @Override
    protected void onStart() {
        super.onStart();
//        if (mGoogleApiClient != null) {
//            mGoogleApiClient.connect();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
//            startLocationUpdates();
//        }
//        // Starting socket service
//        Intent socketIntent = new Intent(MainActivity.this, SocketService.class);
//        startService(socketIntent);
//        // Starting location service
//        Intent locationIntent = new Intent(MainActivity.this, LocationService.class);
//        startService(locationIntent);

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
//        stopLocationUpdates();

//        // Destroying socket service
//        Intent socketIntent = new Intent(MainActivity.this, SocketService.class);
//        stopService(socketIntent);
//        // Destroying location service
//        Intent locationIntent = new Intent(MainActivity.this, LocationService.class);
//        stopService(locationIntent);

        // unregister receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
//        mGoogleApiClient.disconnect();
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