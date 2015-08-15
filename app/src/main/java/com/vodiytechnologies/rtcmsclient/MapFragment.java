package com.vodiytechnologies.rtcmsclient;

import android.app.Fragment;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Diyor on 8/10/2015.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MAP_FRAGMENT:LOG";

    private LatLng mLatlng;
    private Location mLocation;

    private MarkerOptions mMarkerMe;
    private GoogleMap mMap;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.map_fragment, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) ((FragmentActivity)getActivity()).getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Location location = getArguments().getParcelable("myLocation");
        if (location != null) {
            setLocation(location);
        }

        return fragmentView;
    }

    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;
        // Add a marker
        map.addMarker(new MarkerOptions().position(mLatlng).title("My Location"));
        map.moveCamera(CameraUpdateFactory.newLatLng(mLatlng));
        map.moveCamera(CameraUpdateFactory.zoomTo(15));
        loadRoutes();
    }

    private void loadRoutes() {
        
    }

    private String getMapsApiDirectionsUrl() {
        LatLng src = new LatLng(51.749387, -0.237282);
        LatLng dest = new LatLng(51.717326, -0.283204);
        String url = "http://maps.googleapis.com/maps/api/directions/json?origin=" + src.latitude + "," + src.longitude + "&destination=" + dest.latitude + "," + dest.longitude + "&mode=driving&sensor=false";
        return url;
    }

    private void updateCameraToNewLocation(LatLng latlng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
    }

    private void setMap(GoogleMap map) {
        mMap = map;
    }

    private GoogleMap getMap() {
        if (mMap != null) {
            return mMap;
        }
        return null;
    }

    private void addMarker(String title) {
        mMarkerMe = new MarkerOptions();
        mMarkerMe.title(title);
        mMap.addMarker(mMarkerMe);
    }

    private MarkerOptions getMarker() {
        if (mMarkerMe != null) {
            return mMarkerMe;
        }
        return null;
    }

    private void setMarkerPosition(LatLng latlng) {
        mMarkerMe.position(latlng);
        updateCameraToNewLocation(latlng);
    }


    private void setLatLng(LatLng latlng) {
        mLatlng = latlng;
    }

    private LatLng getLatLng() {
        if (mLatlng != null) {
            return mLatlng;
        }
        return null;
    }

    public void setLocation(Location location) {
        mLocation = location;
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        setLatLng(latlng);
    }

    public Location getLocation() {
        if (mLocation != null) {
            return mLocation;
        }
        return null;
    }
}
