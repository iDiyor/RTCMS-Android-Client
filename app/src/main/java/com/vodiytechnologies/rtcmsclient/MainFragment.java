package com.vodiytechnologies.rtcmsclient;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Diyor on 8/10/2015.
 */
public class MainFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.main_fragment, container, false);
        //        mLongitudeTextView = (TextView) findViewById(R.id.longitudeId);
//        mLatitudeTextView = (TextView) findViewById(R.id.latitudeId);
//        mMessageTextView = (TextView) findViewById(R.id.messageId);

//        TextView userTextVew = (TextView) findViewById(R.id.userTextViewId);

//        userTextVew.setText("User ID: " + mClientId + ": " + mClient);

//        final Button startServiceButton = (Button) findViewById(R.id.startServiceButtonId);
//        startServiceButton.setOnClickListener(new View.OnClickListener() {
//              @Override
//              public void onClick(View v) {
//                  // Socket service
//                  Intent socketIntent = new Intent(MainActivity.this, SocketService.class);
//                  socketIntent.putExtra("clientId", mClientId);
//                  socketIntent.putExtra("client", mClient);
//
//                  startService(socketIntent);
//                  // Location service
//                  Intent locationIntent = new Intent(MainActivity.this, LocationService.class);
//                  startService(locationIntent);
//
//              }
//        });

//        Button stopServiceButton = (Button) findViewById(R.id.stopServiceButtonId);
//        stopServiceButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Location service
//                Intent locationIntent = new Intent(MainActivity.this, LocationService.class);
//                stopService(locationIntent);
//                setCurrentLocation(null);
//                // Socket service
//                Intent intent = new Intent(MainActivity.this, SocketService.class);
//                stopService(intent);
//                showMessage("Socket connection disconnected");
//
//
//            }
//        });

//        Button commandCentreButton = (Button) findViewById(R.id.commandCentreButtonId);
//        commandCentreButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, CommandCentreFragment.class);
//                startActivity(intent);
//            }
//        });

        return fragmentView;
    }

    //    private void setCurrentLocation(Location location) {
//        mCurrentLocation = location;
//        displayLocation();
//    }
//
//    public Location getCurrentLocation() {
//        if (mCurrentLocation != null)
//            return mCurrentLocation;
//        return null;
//    }
//
//    // message from server
//    public void showMessage(String message) {
//        mMessageTextView.setText(message);
//    }
//
//    public void displayLocation() {
//        if (getCurrentLocation() != null) {
//            double latitude = getCurrentLocation().getLatitude();
//            double longitude = getCurrentLocation().getLongitude();
//            mLongitudeTextView.setText(String.valueOf(longitude));
//            mLatitudeTextView.setText(String.valueOf(latitude));
//        } else {
//            mLongitudeTextView.setText("Location Service is not available");
//            mLatitudeTextView.setText("Location Service is not available");
//        }
//    }

}
