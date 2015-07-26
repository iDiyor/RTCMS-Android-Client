package com.vodiytechnologies.rtcmsclient;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;

import android.os.Looper;
import android.os.Message;
import android.os.Handler;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Diyor on 7/25/2015.
 */
public class LocationService extends Service implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private final static int PLAY_SERVICE_RESOLUTION_REQ = 1000;
    private Location mCurrentLocation;
    private GoogleApiClient mGoogleApiClient;
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;

    private static int UPDATE_INTERVAL = 1000 * 10; // 10 sec
    private static int FASTEST_INTERVAL = 1000 * 5; // 5 sec
    private static int DISPLACEMENT = 5; // 5 meters

    private static final String TAG = "LocationService:Message";

    public static final String LOCATION_UPDATE_ACTION = "LOCATION_UPDATE_ACTION";

    public static final String LOCATION_MESSAGE = "LOCATION";

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // IMPORTANT VARIABLE
        mRequestingLocationUpdates = true;

        if (checkPlayServices()) {
            buildGoogleApiClient();
        }

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        HandlerThread thread = new HandlerThread("LocationServiceStartArgs", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);


        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopLocationUpdates();
        mGoogleApiClient.disconnect();
    }

    private void broadcastLocation(String name, String action) {
        Intent i = new Intent(action);
        i.putExtra(name, getCurrentLocation());
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    /*****************************
     * LOCATION LISTENER METHODS
     ****************************/
    @Override
    public void onConnected(Bundle bundle) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        setCurrentLocation(location);

        broadcastLocation(LOCATION_MESSAGE, LOCATION_UPDATE_ACTION);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection Failed");
        stopLocationUpdates();
        stopSelf();
    }


    private void setCurrentLocation(Location location) {
        mCurrentLocation = location;
    }

    private Location getCurrentLocation() {
        if (mCurrentLocation != null)
            return mCurrentLocation;
        return null;
    }

    /***************************************
     * GOOGLE PLAY LOCATION SERVICE METHODS
     ***************************************/
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
                //GooglePlayServicesUtil.getErrorDialog(resultCode, getApplicationContext(), PLAY_SERVICE_RESOLUTION_REQ).show();
                Log.d(TAG, "Google Play Location Service Error");
            } else {
                //Toast.makeText(getApplicationContext(), "This Device is not supported", Toast.LENGTH_LONG).show();
                //finish();
                Log.d(TAG, "This device is not supported (Google Play Location Service API");
                stopSelf();
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

}
