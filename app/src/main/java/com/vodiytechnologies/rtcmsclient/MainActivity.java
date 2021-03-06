package com.vodiytechnologies.rtcmsclient;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.location.Location;
import android.media.MediaRouter;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends FragmentActivity implements JobFragment.OnJobListSelectedListener, JobDetailsFragment.OnJobDetailsMapClickedListener{

    private static final String TAG = "MainActivity:Message";


    private String mUserName;
    private String mUserProfileId;
    private String mDriverId;
    private String mClientCurrentStatus;

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

    private static final String SHOW_MESSAGE_FRAGMENT_ACTION = "SHOW_MESSAGE_FRAGMENT_ACTION";
    private static final String SHOW_JOB_FRAGMENT_ACTION = "SHOW_JOB_FRAGMENT_ACTION";

    Fragment mFragment;

    private Location mLocation;

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
        mClientCurrentStatus = text;
        if (mFragment != null && mFragment instanceof CommandCentreFragment) {
            ((CommandCentreFragment) mFragment).setStatus(text);
        }
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

    @Override
    public void onListItemSelected(int position, Bundle args) {
        JobDetailsFragment jobDetailsFragment = new JobDetailsFragment();
        jobDetailsFragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, jobDetailsFragment).addToBackStack(null).commit();

    }

    @Override
    public void onMapButtonClicked(Bundle args) {
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, mapFragment).addToBackStack(null).commit();
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    public void selectItem(int position) {
        // update the main content by replacing fragments
        mFragment = null;
        switch (position) {
            case 0: // Commands
            {
                mFragment = new CommandCentreFragment();
                Bundle args = new Bundle();
                if (mClientCurrentStatus != null) {
                    args.putString("clientStatus", mClientCurrentStatus);
                } else {
                    args.putString("clientStatus", "No Job");
                }
                mFragment.setArguments(args);
            }
            break;
            case 1: // Messages
            {
                mFragment = new MessageFragment();
                Bundle args = new Bundle();
                args.putString("userProfileId", mUserProfileId);
                mFragment.setArguments(args);
            }
            break;
            case 2: // History
                mFragment = new HistoryFragment();
                break;
            case 3: // Job
            {
                mFragment = new JobFragment();
                Bundle args = new Bundle();
                args.putString("driverId", mDriverId);
                args.putParcelable("myLocation", getLocation());
                mFragment.setArguments(args);
            }
            break;
            case 4: // Map
            {
                mFragment = new MapFragment();
                Bundle args = new Bundle();
                args.putParcelable("myLocation", getLocation());
                mFragment.setArguments(args);
            }
            break;
            case 5: // User Profile
            {
                mFragment = new UserProfileFragment();
                Bundle args = new Bundle();
                args.putString("userName", mUserName);
                mFragment.setArguments(args);
            }
            break;
            default:
                break;
        }

        if (mFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, mFragment).commit();

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

    private void showNotification(String receiverCliendId, String receiverName, String notificationTitle, String notificationMessage) {
        Intent intent = new Intent(this, MainActivity.class);
        //Intent intent = new Intent();
        intent.setAction(SHOW_MESSAGE_FRAGMENT_ACTION);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("clientId", receiverCliendId);
        intent.putExtra("client", receiverName);
        int iUniqueId = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(),iUniqueId,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("New message from the admin")
                .setContentText(notificationMessage)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true).build();

        notification.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private void showJobNotification(String notificationTitle, String notificationMessage) {
        Intent intent = new Intent(this, MainActivity.class);
        //Intent intent = new Intent();
        intent.setAction(SHOW_JOB_FRAGMENT_ACTION);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int iUniqueId = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(),iUniqueId,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true).build();

        notification.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    @Override
    protected void onNewIntent (Intent intent) {
        try {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(SHOW_MESSAGE_FRAGMENT_ACTION)) {
                    mUserProfileId = getIntent().getStringExtra("userProfileId");
                    mUserName = getIntent().getStringExtra("userName");
                    selectItem(1);
                }
                else if (action.equals(SHOW_JOB_FRAGMENT_ACTION)) {
                    selectItem(3);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Problem consuming action from notification intent");
        }
    }

    private void setLocation(Location location) {
        mLocation = location;
        Log.d(TAG, "MAIN_ACTIVITY_LOCATION_SET");
    }

    private Location getLocation() {
        if (mLocation != null) {
            return mLocation;
        }
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();


        mUserName = getIntent().getStringExtra("userName");
        mUserProfileId = getIntent().getStringExtra("userProfileId");
        mDriverId = getIntent().getStringExtra("driverId");

        // Socket service
        Intent socketIntent = new Intent(MainActivity.this, SocketService.class);
        socketIntent.putExtra("userProfileId", mUserProfileId);
        socketIntent.putExtra("userName", mUserName);
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
        IntentFilter messageFromServerFromWebIntentFilter = new IntentFilter(SocketService.SOCKET_MESSAGE_FROM_SERVER_FROM_WEB_ACTION);
        IntentFilter jobFromServerFromWebIntentFilter = new IntentFilter(SocketService.SOCKET_JOB_FROM_SERVER_FROM_WEB_ACTION);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, connectionSuccessIntentFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, messageFromServerIntentFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, connectionErrorIntentFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, messageFromServerFromWebIntentFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, jobFromServerFromWebIntentFilter);

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

            if (intent.getAction().equals(SocketService.SOCKET_MESSAGE_FROM_SERVER_FROM_WEB_ACTION)) {
                JSONObject object = null;
                String messageContent = null;
                String time = null;
                String toClientId = null;
                String clientName = null;
                try {
                    object = new JSONObject(intent.getStringExtra(SocketService.WEB_CLIENT_MESSAGE));
                    messageContent = object.getString("content");
                    time = object.getString("time");
                    toClientId = object.getString("to_id_user_profile");
                    String firstName = object.getJSONObject("toUser").getString("first_name");
                    String lastName = object.getJSONObject("toUser").getString("last_name");
                    clientName = firstName + " " + lastName;
                } catch (JSONException e) {
                    Log.d(TAG, "MAIN_ACTIVITY:MESSAGE_BODY_JSON_EXCEPTION");
                    return;
                }

                if (messageContent != null && time != null && toClientId != null && clientName != null) {
                    if (mFragment != null && mFragment instanceof MessageFragment) {
                        ((MessageFragment) mFragment).addMessageToLocalList(messageContent, time, false);
                    } else {
                        showNotification(toClientId, clientName,"", messageContent);
                    }
                }
            }

            if (intent.getAction().equals(SocketService.SOCKET_JOB_FROM_SERVER_FROM_WEB_ACTION)) {
                JSONObject jobObject = null;
                String jobDescription = null;
                try {
                    JSONObject object = new JSONObject(intent.getStringExtra(SocketService.WEB_CLIENT_JOB));
                    jobObject = object.getJSONObject("job");
                    jobDescription = jobObject.getString("description");
                } catch (JSONException e) {
                    Log.d(TAG, "MAIN_ACTIVITY:JOB_BODY_JSON_EXCEPTION");
                    return;
                }

                if (jobObject != null) {
                    if (mFragment != null && mFragment instanceof JobFragment) {
                        ((JobFragment) mFragment).addJob(jobObject);
                    } else {
                        showJobNotification("You have a new job", jobDescription);
                    }
                }
            }

            /****************************
            * LOCATION SERVICE BROADCAST
            ****************************/
            if (intent.getAction().equals(LocationService.LOCATION_UPDATE_ACTION)) {
                Location location = intent.getParcelableExtra(LocationService.LOCATION_MESSAGE);
                if (mFragment != null && mFragment instanceof MapFragment) {
                    ((MapFragment)mFragment).setLocation(location);
                    setLocation(location);
                } else {
                    setLocation(location);
                }
                //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        }
    };
}