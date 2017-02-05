package com.example.waynetsui.strangecarrier;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String TAG = "MainActivityFragment";

    private ArrayList<String> clientList = new ArrayList<>();
    private String scannedClientKey;

    public MainActivityFragment() {
    }

    public static MainActivityFragment newInstance(String scannedClientKey) {
        MainActivityFragment fragment = new MainActivityFragment();
        Bundle args = new Bundle();
        args.putString("SCANNED_CLIENT_KEY", scannedClientKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            scannedClientKey = getArguments().getString("SCANNED_CLIENT_KEY");
        }
        clientList = ((MainActivity) getActivity()).getClientList();
        if (!clientList.contains(scannedClientKey) && scannedClientKey != null) {
            for (int i=0; i < clientList.size(); i++) {
                Log.d(TAG, "onCreate: " + clientList.get(i));
            }

            // Add new connection based on scannedClientKey
            String bucketKey = ((MainActivity) getActivity()).getBucketKey();
            addConnection(scannedClientKey, bucketKey);
        }

    }

    public void refreshList() {
        final ListView listview = (ListView) getView().findViewById(R.id.listview);
        TextView tv = (TextView) getView().findViewById(R.id.numberOfContributors);
        int clientSize = clientList.size();
        Log.d(TAG, "onCreateView: " + clientSize);
        String header = "Contributors: " + String.valueOf(clientList.size());
        tv.setText(header);

        final StableArrayAdapter adapter = new StableArrayAdapter(getActivity().getApplicationContext(),
                R.layout.list_item, clientList);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    final int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                view.animate().setDuration(1000).alpha(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                view.setAlpha(1);

                                // Destroy connection for item
                                String userKey = ((MainActivity) getActivity()).getUserKey();
                                String bucketKey = ((MainActivity) getActivity()).getBucketKey();
                                removeConnection(item, bucketKey, userKey, position, adapter);
                            }
                        });
            }

        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);


        FloatingActionButton floatingActionButton = ((MainActivity) getActivity()).getFab();
        if (floatingActionButton != null) {
            floatingActionButton.show();
        }

        return view;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        refreshList();
    }

    public void updateNumOfContributors() {
        View view = getView();
        TextView tv = (TextView) view.findViewById(R.id.numberOfContributors);
        int clientSize = clientList.size();
        String header = "Contributors: " + String.valueOf(clientSize);
        tv.setText(header);
    }

    public void addConnection(final String userKey, final String bucketKey) {
        String BASE_URL = ((MainActivity) getActivity()).getBASE_URL();
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest addConnectionRequest = new StringRequest(Request.Method.GET, BASE_URL + "/connection/new" + "?user_key=" + userKey + "&bucket_key=" + bucketKey,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: "+ response);
                        try {
                            JSONObject o = new JSONObject(response);
                            if (o.getBoolean("success")) {
                                Log.d(TAG, "onResponse: CONNECTION ADDING SUCCESS");
                                clientList.add(scannedClientKey);
                                refreshList();
                            } else {
                                Log.d(TAG, "onResponse: CONNECTION ADDING FAILED");
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(addConnectionRequest);
    }

    public void removeConnection(final String userKey, final String bucketKey, final String requesterKey, final int position, final StableArrayAdapter adapter) {
        String BASE_URL = ((MainActivity) getActivity()).getBASE_URL();
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest removeConnectionRequest = new StringRequest(Request.Method.GET, BASE_URL + "/connection/destroy" + "?user_key=" + userKey + "&bucket_key=" + bucketKey + "&requester_key=" + requesterKey,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: "+ response);
                        try {
                            JSONObject o = new JSONObject(response);
                            if (o.getBoolean("success")) {
                                Log.d(TAG, "onResponse: CONNECTION REMOVING SUCCESS");
                                clientList.remove(position);
                                updateNumOfContributors();
                                adapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "onResponse: CONNECTION REMOVING FAILED");
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(removeConnectionRequest);
    }

}
