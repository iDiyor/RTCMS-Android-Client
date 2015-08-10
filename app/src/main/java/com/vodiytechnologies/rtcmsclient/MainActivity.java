package com.vodiytechnologies.rtcmsclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends FragmentActivity {


    private String mClient;
    private String mClientId;

    private String[] mNavigationTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_container_activity);

        mNavigationTitles = getResources().getStringArray(R.array.navigation_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mNavigationTitles));

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //mClient = getIntent().getStringExtra("client");
        //mClientId = getIntent().getStringExtra("clientId");
        mClient = "Steve Jobs";
        mClientId = "1";
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
        //setCurrentLocation(null);
        // Socket service
        Intent intent = new Intent(MainActivity.this, SocketService.class);
        stopService(intent);
        //showMessage("Socket connection disconnected");

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
                //showMessage(message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

            if (intent.getAction().equals(SocketService.SOCKET_MESSAGE_FROM_SERVER_ACTION)) {
                String message = intent.getStringExtra(SocketService.SERVER_SAID);
                //showMessage(message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

            if (intent.getAction().equals(SocketService.SOCKET_CONNECTION_ERROR_ACTION)) {
                String message = intent.getStringExtra(SocketService.CONNECTION_STATUS);
                //showMessage(message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

            /****************************
            * LOCATION SERVICE BROADCAST
            ****************************/
            if (intent.getAction().equals(LocationService.LOCATION_UPDATE_ACTION)) {
                Location location = intent.getParcelableExtra(LocationService.LOCATION_MESSAGE);
                //setCurrentLocation(location);
                //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

        }
    };
}