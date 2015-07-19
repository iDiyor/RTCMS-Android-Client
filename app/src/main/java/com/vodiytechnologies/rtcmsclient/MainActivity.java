package com.vodiytechnologies.rtcmsclient;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener , LocationListener{

    private final static int PLAY_SERVICE_RESOLUTION_REQ = 1000;
    private Location mCurrentLocation;
    private GoogleApiClient mGoogleApiClient;
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;

    private static int UPDATE_INTERVAL = 1000 * 10; // 10 sec
    private static int FASTEST_INTERVAL = 1000 * 5; // 5 sec
    private static int DISPLACEMENT = 5; // 5 meters

    // socket.io
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://52.28.143.209:3000");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    // TextViews
    private TextView mLongitudeTextView;
    private TextView mLatitudeTextView;
    private TextView mMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLongitudeTextView = (TextView) findViewById(R.id.longitudeId);
        mLatitudeTextView = (TextView) findViewById(R.id.latitudeId);
        mMessageTextView = (TextView) findViewById(R.id.messageId);

        mRequestingLocationUpdates = true;

        if (checkPlayServices()) {
            buildGoogleApiClient();
            //createLocationRequest();
        }

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT, onConnectToServer);
        mSocket.on("server:message", onServerMessage);

//        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
//            @Override
//            public void call(Object... args) {
//                mSocket.emit("foo", "hi from android");
//                //mSocket.disconnect();
//            }
//        }).on("news", new Emitter.Listener() {
//            @Override
//            public void call(final Object... args) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        JSONObject data = (JSONObject) args[0];
//                        String hello;
//                        try {
//                            hello = data.getString("hello");
//                        } catch (JSONException e) {
//                            return;
//                        }
//
//                        // update UI
//                        showMessage(hello);
//                        Log.d("IO", "After Show Message");
//                    }
//                });
//            }
//        });
        mSocket.connect();

    }

    private void setCurrentLocation(Location location) {
        mCurrentLocation = location;
    }

    public Location getCurrentLocation() {
        if (mCurrentLocation != null)
            return mCurrentLocation;
        return null;
    }

    // socket io handler functions
    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Error with socket.io connection", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onConnectToServer = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSocket.emit("mobile:connection", "android");
                }
            });
        }
    };

    private Emitter.Listener onServerMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message;
                    try {
                        message = data.getString("status");
                    } catch (JSONException e) {
                        return;
                    }

                    // update UI
                    showMessage(message);
                    Log.d("IO", "After Show Message");
                }
            });
        }
    };

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
        }
    }

    /* Google Play Services */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        createLocationRequest();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RESOLUTION_REQ).show();
            } else {
                Toast.makeText(getApplicationContext(), "This Device is not supported", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    // creating location request object
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }


    // starting location updates
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    // stop location updates
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        // display location here
        //displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("message", "Connection Failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        // sets global myCurrentLocation
        setCurrentLocation(location);
        // displays current location
        displayLocation();

        try {
            JSONObject data = new JSONObject();
            data.put("longitude", location.getLongitude());
            data.put("latitude", location.getLatitude());
            mSocket.emit("mobile:location", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();

        super.onStart();
    }
}