package com.vodiytechnologies.rtcmsclient;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Diyor on 8/16/2015.
 */
public class JobDetailsFragment extends Fragment {

    OnJobDetailsMapClickedListener mCallback;

    // Container Activity must implement this interface
    public interface OnJobDetailsMapClickedListener {
        public void onMapButtonClicked(Bundle args);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.job_details_fragment, container, false);

        TextView clientNameTextView = (TextView) fragmentView.findViewById(R.id.clientNameTextView);
        TextView fromAddressTextView = (TextView) fragmentView.findViewById(R.id.fromAddressTextView);
        TextView toAddressTextView = (TextView) fragmentView.findViewById(R.id.toAddressTextView);
        TextView phoneNumberTextView = (TextView) fragmentView.findViewById(R.id.phoneNumberTextView);
        TextView timeTextView = (TextView) fragmentView.findViewById(R.id.timeTextView);

        final Location myLocation = getArguments().getParcelable("myLocation");
        final LatLng sourceLocation = getArguments().getParcelable("sourceLocation");
        final LatLng destinationLocation = getArguments().getParcelable("destinationLocation");

        clientNameTextView.setText("Client Name: " + getArguments().getString("clientName"));
        fromAddressTextView.setText("From Address: " + getArguments().getString("fromAddress"));
        toAddressTextView.setText("To Address: " + getArguments().getString("toAddress"));
        timeTextView.setText("Pickup Time: " + getArguments().getString("time"));
        phoneNumberTextView.setText("Phone Number: " + getArguments().getString("phoneNumber"));



        final Button mapButton = (Button) fragmentView.findViewById(R.id.mapButton);
        Button ringBackButton = (Button) fragmentView.findViewById(R.id.ringbackButton);


        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MapFragment mapFragment = new MapFragment();
                Bundle args = new Bundle();
                args.putParcelable("myLocation", myLocation);
                args.putParcelable("sourceLocation", sourceLocation);
                args.putParcelable("destinationLocation", destinationLocation);
                args.putBoolean("isRoute", true);
                mCallback.onMapButtonClicked(args);
                //mapFragment.setArguments(args);

                //FragmentManager fm = getChildFragmentManager();
                //fm.beginTransaction().add(R.id.mapContainer, mapFragment).addToBackStack(null).commit();
            }
        });

        return fragmentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnJobDetailsMapClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnJobDetailsMapClickedListener");
        }
    }
}
