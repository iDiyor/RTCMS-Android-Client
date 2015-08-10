package com.vodiytechnologies.rtcmsclient;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    // COMMAND CENTRE
    public static final String CURRENT_STATUS_UPDATE_ACTION = "CURRENT_STATUS_UPDATE_ACTION";
    // current status as a key should be same in receiver
    public static final String CURRENT_STATUS = "CURRENT_STATUS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_container_activity);


        mTitle = mDrawerTitle = getTitle();
        mNavigationTitles = getResources().getStringArray(R.array.navigation_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mNavigationTitles));

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.mipmap.drawer_shadow, GravityCompat.START);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.mipmap.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }

    }
    public void onButtonClick(View v) {
        Button button = (Button)v;
        String text = button.getText().toString();
        // Should update Command Centre Fragment

        broadcast(CURRENT_STATUS, text, CURRENT_STATUS_UPDATE_ACTION);
    }

    private void broadcast(String name,String status, String action) {
        Intent i = new Intent(action);
        i.putExtra(name, status);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
            case R.id.action_websearch:
                // create intent to perform web search for this planet
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
                // catch event that there's no activity to handle intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new CommandCentreFragment();
            break;
            case 1:
                fragment = new MessageFragment();
            break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            setTitle(mNavigationTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
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