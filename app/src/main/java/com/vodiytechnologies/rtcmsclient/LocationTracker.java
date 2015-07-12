package com.vodiytechnologies.rtcmsclient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Created by Diyor on 7/12/2015.
 */
public class LocationTracker extends Service implements LocationListener {

    private final Context mContext;

    // GPS
    boolean mIsGPSEnabled = false;

    // Network
    boolean mIsNetworkEnabled = false;

    boolean mCanGetLocation = false;

    // min distance to change updates in meters
    private static final long MIN_DISTANCE_TO_UPDATE = 10;

    // min time between updates in millisec
    private static final long MIN_TIME_BETWEEN_UPDATES = 1000 * 5 * 1; // 5 second

    // location manager
    protected LocationManager mLocationManager;

    public LocationTracker(Context context) {
        this.mContext = context;
        getLocation();
    }


    public Location getLocation() {
        try {
            mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            mIsGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            mIsNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
