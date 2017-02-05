package com.example.waynetsui.strangecarrier;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ScanFragment.OnFragmentInteractionListener {

    private ArrayList<String> clientList = new ArrayList<>();

    private FloatingActionButton fab;

    private final String BASE_URL = "http://9c1a3ec5.ngrok.io";

    private String userKey;
    private String bucketKey;

    public ArrayList<String> getClientList() {
        return clientList;
    }

    public FloatingActionButton getFab() {
        return fab;
    }

    public String getBASE_URL() {
        return BASE_URL;
    }


    public String getUserKey() {
        return userKey;
    }

    public String getBucketKey() {
        return bucketKey;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment scanFrag = new ScanFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment, scanFrag);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });

        // Call Host Key and Bucket Key APIs
        requestForUserKeyAndBucketKey();

    }

//    @Override
//    public void onDestroy() {
//        Log.d("SHIT", "onDestroy: AT ONDESTROY METHOD");
//        deleteBucket(userKey, bucketKey, false);
////        clientList.clear();
////        MainActivityFragment frag = (MainActivityFragment) getFragmentManager().findFragmentById(R.id.fragment);
////        frag.updateNumOfContributors();
//        super.onDestroy();
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete_bucket) {
            deleteBucket(userKey, bucketKey, true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (fab != null) {
            fab.show();
        }
        Fragment frag = new MainActivityFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment, frag)
                .addToBackStack(null)
                .commit();
        super.onBackPressed();

    }

    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    public void requestForUserKeyAndBucketKey() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest userKeyRequest = new StringRequest(Request.Method.GET, BASE_URL + "/user/new",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("shit", "onResponse: "+ response);
                        try {
                            JSONObject o = new JSONObject(response);
                            if (o.getBoolean("success")) {
                                userKey = o.getString("user_key");
                                requestForBucketKey(userKey);
                            } else {
                                requestForUserKeyAndBucketKey();
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
        // Add the request to the RequestQueue.
        queue.add(userKeyRequest);
    }

    public void requestForBucketKey(final String userKey) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest bucketKeyRequest = new StringRequest(Request.Method.GET, BASE_URL + "/bucket/new" + "?user_key=" + userKey,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("shit", "onResponse: "+ response);
                        try {
                            JSONObject o = new JSONObject(response);
                            if (o.getBoolean("success")) {
                                bucketKey = o.getString("bucket_key");
                            } else {
                                requestForBucketKey(userKey);
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
        queue.add(bucketKeyRequest);
    }

    public void deleteBucket(String userKey, String bucketKey, final boolean byChoice) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest destroyBucketRequest = new StringRequest(Request.Method.GET, BASE_URL + "/bucket/destroy" + "?user_key=" + userKey + "&bucket_key=" + bucketKey,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("shit", "onResponse: "+ response);
                        try {
                            JSONObject o = new JSONObject(response);
                            if (o.getBoolean("success")) {
                                Log.d("shit", "onResponse: BUCKET DELETE SUCCESS");
                                if (byChoice) {
                                    Intent intent = getIntent();
                                    overridePendingTransition(0, 0);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    finish();
                                    overridePendingTransition(0, 0);
                                    startActivity(intent);
                                }
                            } else {
                                Log.d("shit", "onResponse: BUCKET DELETE FAILED");
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("shit", "onErrorResponse: DELETE BUCKET VOLLY error");
                error.printStackTrace();
            }
        });
        queue.add(destroyBucketRequest);
    }
}
