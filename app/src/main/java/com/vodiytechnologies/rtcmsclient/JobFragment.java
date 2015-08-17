package com.vodiytechnologies.rtcmsclient;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Diyor on 8/10/2015.
 */
public class JobFragment extends Fragment {
    OnJobListSelectedListener mCallback;


    private ListView mListView;
    private ArrayAdapter<String> mAdapter;

    private String mDriverId;
    private Location mMyLocation;

    private List<String> mArrayList;
    private JSONArray mJsonArray;

    // Container Activity must implement this interface
    public interface OnJobListSelectedListener {
        public void onListItemSelected(int position, Bundle args);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.job_fragment, container, false);

        mDriverId = getArguments().getString("driverId");
        mMyLocation = getArguments().getParcelable("myLocation");

        mListView = (ListView) fragmentView.findViewById(R.id.jobList);

        setupAdapter();
        loadJobs();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // array of jobs from the server
                // on click load from the array
                FragmentManager fm = getFragmentManager();
                JobDetailsFragment detailsFragment = new JobDetailsFragment();
                Bundle args = new Bundle();
                // set args
                // Name
                // From
                // To
                // Phone number
                // Map button
                // Ring back
                JSONObject object = null;
                try {

                    object = (JSONObject) mJsonArray.get(position);
                    String clientName = object.getString("client_name");
                    String fromAddress = object.getString("from_address");
                    String toAddress = object.getString("to_address");
                    String time = object.getString("time");
                    String phoneNumber = object.getString("phone_number");

                    String sourceLongitude = ((JSONObject)(object.getJSONArray("direction").get(0))).getJSONObject("source").getString("longitude");
                    String sourceLatitude = ((JSONObject)(object.getJSONArray("direction").get(0))).getJSONObject("source").getString("latitude");
                    LatLng sourceLocation = new LatLng(Double.valueOf(sourceLatitude), Double.valueOf(sourceLongitude));

                    String destinationLongitude = ((JSONObject)(object.getJSONArray("direction").get(0))).getJSONObject("destination").getString("longitude");
                    String destinationLatitude = ((JSONObject)(object.getJSONArray("direction").get(0))).getJSONObject("destination").getString("latitude");
                    LatLng destinationLocation = new LatLng(Double.valueOf(destinationLatitude), Double.valueOf(destinationLongitude));

                    Location myLocation = mMyLocation;

                    args.putString("clientName", clientName);
                    args.putString("fromAddress", fromAddress);
                    args.putString("toAddress", toAddress);
                    args.putString("time", time);
                    args.putString("phoneNumber", phoneNumber);
                    args.putParcelable("myLocation",myLocation);
                    args.putParcelable("sourceLocation", sourceLocation);
                    args.putParcelable("destinationLocation", destinationLocation);
                    mCallback.onListItemSelected(position, args);
                    //detailsFragment.setArguments(args);

                } catch (JSONException e) {
                    Log.d("JSONException", "JSONException while Json parse");
                    return;
                }

                //fm.beginTransaction().add(R.id.detailsContainer, detailsFragment).addToBackStack(null).commit();
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
            mCallback = (OnJobListSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnJobListSelectedListener");
        }
    }

    private void loadJobs() {

        final RequestQueue mQueue = VolleySingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        String getJobsUrl = "http://52.28.143.209:3000/api/jobs/driver/" + mDriverId;

        JsonArrayRequest jsonReq1 = new JsonArrayRequest(getJobsUrl,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonResponseArray) {

                try {
                    for (int i = 0; i < jsonResponseArray.length(); i++) {
                        JSONObject object = (JSONObject) jsonResponseArray.get(i);
                        addJob(object);
                    }
                } catch (JSONException e) {
                    Log.d("JSONException", "JSONException while JsonResponse");
                    return;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonReq1);
    }

    public void addJob(JSONObject jobObject) {
        mJsonArray.put(jobObject);
        try {
            String clientName = jobObject.getString("client_name");
            String fromAddress = jobObject.getString("from_address");
            String toAddress = jobObject.getString("to_address");
            String time = jobObject.getString("time");
            String itemTitle = clientName + " | From: " + fromAddress + " | To: " + toAddress + " | Pickup Time: " + time;
            addItem(itemTitle);
        } catch (JSONException e) {
            Log.d("JSONException", "JSONException while JsonResponse");
            return;
        }
    }

    private void addItem(String itemTitle) {
        if (mArrayList != null) {
            mArrayList.add(itemTitle);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void setupAdapter() {
        mJsonArray = new JSONArray();
        mArrayList = new ArrayList<>();
        mAdapter = new ArrayAdapter<String>(getActivity(), R.layout.job_list_item, android.R.id.text1, mArrayList);
        mListView.setAdapter(mAdapter);
    }
}
