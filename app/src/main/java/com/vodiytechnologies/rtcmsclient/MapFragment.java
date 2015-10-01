package com.vodiytechnologies.rtcmsclient;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by Diyor on 8/10/2015.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, RoutingListener {

    private static final String TAG = "MAP_FRAGMENT:LOG";

    private LatLng mLatlng;
    private LatLng mSourceLocation;
    private LatLng mDestinationLocation;
    private Location mLocation;

    private SupportMapFragment fragment;

    private MarkerOptions mMarkerMe;
    private GoogleMap mMap;

    private boolean mIsRoute = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.map_fragment, container, false);


        Location location = getArguments().getParcelable("myLocation");
        if (location != null) {
            setLocation(location);
        }

        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        try {
            LatLng sourceLocation = getArguments().getParcelable("sourceLocation");
            LatLng destinationLocation = getArguments().getParcelable("destinationLocation");
            if (sourceLocation != null && destinationLocation != null) {
                mIsRoute = getArguments().getBoolean("isRoute");
                mSourceLocation = sourceLocation;
                mDestinationLocation = destinationLocation;
            }
        } catch (Exception e) {
            Log.d(TAG, "Problem consuming args in map fragment");
        }


        Button openGoogleMapsButton = (Button) fragmentView.findViewById(R.id.openMapApp);
        openGoogleMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Open Maps App", Toast.LENGTH_SHORT).show();
            }
        });

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;
        //map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(getLatLng(), 13));
        // Add a marker
        map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.my_location))
                .position(getLatLng()).title("My Location"));

        if (mIsRoute) {
            loadRoutes(mSourceLocation, mDestinationLocation);
        }
    }

    private void loadRoutes(LatLng source, LatLng destination) {
        //LatLng src = new LatLng(51.749387, -0.237282);
        //LatLng dest = new LatLng(51.717326, -0.283204);

        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(source, destination)
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure() {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(PolylineOptions polylineOptions, Route route) {
        PolylineOptions polyoptions = new PolylineOptions();
        polyoptions.color(Color.BLUE);
        polyoptions.width(10);
        polyoptions.addAll(polylineOptions.getPoints());
        mMap.addPolyline(polyoptions);

        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.pickup))
                .position(mSourceLocation).title("Pick-up location"));
        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.dropoff))
                .position(mDestinationLocation).title("Drop-off location"));
    }

    @Override
    public void onRoutingCancelled() {

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
        LatLng latlng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        setLatLng(latlng);
    }

    public Location getLocation() {
        if (mLocation != null) {
            return mLocation;
        }
        return null;
    }
}
