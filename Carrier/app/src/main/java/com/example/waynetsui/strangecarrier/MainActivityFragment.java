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
            clientList.add(scannedClientKey);
            for (int i=0; i < clientList.size(); i++) {
                Log.d("shit", "onCreate: " + clientList.get(i));
            }

            // TODO: Add new connection based on scannedClientKey
            String userKey = ((MainActivity) getActivity()).getUserKey();
            String bucketKey = ((MainActivity) getActivity()).getBucketKey();
            addConnection(userKey, bucketKey);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        final ListView listview = (ListView) view.findViewById(R.id.listview);
        TextView tv = (TextView) view.findViewById(R.id.numberOfContributors);
        Toast.makeText(getActivity().getApplicationContext(), String.valueOf(clientList.size()), Toast.LENGTH_SHORT);
        int clientSize = clientList.size();
        Log.d("shit", "onCreateView: " + clientSize);
        if (clientSize > 0) {
            String header = "Number of Contributors: " + String.valueOf(clientList.size());
            tv.setText(header);
        }

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
                                clientList.remove(position);
                                Log.d("shit", "run: " + position + " " + item);
                                updateNumOfContributors();

                                // TODO: Destroy connection for item
                                String userKey = ((MainActivity) getActivity()).getUserKey();
                                String bucketKey = ((MainActivity) getActivity()).getBucketKey();
                                removeConnection(userKey, bucketKey);
                            }
                        });
            }

        });

        FloatingActionButton floatingActionButton = ((MainActivity) getActivity()).getFab();
        if (floatingActionButton != null) {
            floatingActionButton.show();
        }

        return view;
    }

    public void updateNumOfContributors() {
        View view = getView();
        TextView tv = (TextView) view.findViewById(R.id.numberOfContributors);
        int clientSize = clientList.size();
        Log.d("shit", "onCreateView: " + clientSize);
        if (clientSize > 0) {
            String header = "Number of Contributors: " + String.valueOf(clientList.size());
            tv.setText(header);
        }
    }

    public void addConnection(final String userKey, final String bucketKey) {
        String BASE_URL = ((MainActivity) getActivity()).getBASE_URL();
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest addConnectionRequest = new StringRequest(Request.Method.GET, BASE_URL + "/connection/new" + "?user_key=" + userKey + "&bucket_key=" + bucketKey,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("shit", "onResponse: "+ response);
                        try {
                            JSONObject o = new JSONObject(response);
                            if (o.getBoolean("success")) {
                                Log.d("shit", "onResponse: CONNECTION ADDING SUCCESS");
                            } else {
                                Log.d("shit", "onResponse: CONNECTION ADDING FAILED");
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

    public void removeConnection(final String userKey, final String bucketKey) {
        String BASE_URL = ((MainActivity) getActivity()).getBASE_URL();
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest removeConnectionRequest = new StringRequest(Request.Method.GET, BASE_URL + "/connection/destroy" + "?user_key=" + userKey + "&bucket_key=" + bucketKey,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("shit", "onResponse: "+ response);
                        try {
                            JSONObject o = new JSONObject(response);
                            if (o.getBoolean("success")) {
                                Log.d("shit", "onResponse: CONNECTION REMOVING SUCCESS");
                            } else {
                                Log.d("shit", "onResponse: CONNECTION REMOVING FAILED");
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
