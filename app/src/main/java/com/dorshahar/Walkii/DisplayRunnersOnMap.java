package com.example.alonsiwek.demomap;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Dor on 27-May-17.
 * Get all the people that walk from the server
 *
 */

public class DisplayRunnersOnMap extends AsyncTask <Void,Void,String> {

    Context mContext;
    View mView;
    int viewID;
    GoogleMap gMap;
    MapView mapView;
    static HashMap<String, Marker> usersMarkers = new HashMap<>();
    final ArrayList<UserData> data = null;

    DisplayRunnersOnMap(Context context,View view, GoogleMap gMap, MapView mapView, int viewID){
        this.mContext = context;
        this.mView = view;
        this.viewID = viewID;
        this.gMap = gMap;
        this.mapView = mapView;
    }


    @Override
    protected String doInBackground(Void... params) {

        String isNull = getRunnersAtDisplayROM();
        if (isNull == null){
            Log.e(this.getClass().toString(),  "Error fetching users");
            return null;
        }

        return isNull;
    }

    /**
     * Get the people that walk from the server
     * @return json string of the people
     */
    String getRunnersAtDisplayROM(){
        String get_all_users_url = Constants.SERVER_URL + Constants.LOC_STATUS_PATH + Constants.IS_RUNNING;

        BufferedReader bufferedReader = null;
        StringBuilder result = new StringBuilder();

        try {
            // Open connection to the server
            // Open url for reading
            URL url = new URL(get_all_users_url);

            HttpURLConnection urlConnection = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                Log.d(UserAtApp.class.toString(), String.valueOf(urlConnection));
            } catch (IOException e) {
                Log.e(UserAtApp.class.toString(), String.valueOf(e));
            }


            //set the time to read from url - in miliSec
            urlConnection.setReadTimeout(10000);
            //set time to be used when opening a communications link
            // to the resource referenced by this URLConnection connect to url
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestMethod("GET");

            //set header
            urlConnection.setRequestProperty("Content-Type", "application/json");

            try {
                urlConnection.connect();
            } catch (Exception e) {
                Log.e(UserAtApp.class.toString(), "Connect exception: " + e.toString());
                e.printStackTrace();
                return null;
            }
            Log.d(UserAtApp.class.toString(), "Connecting");


            //Read the response from the server
            //////////////    FOR DEBUG - CATCH ERROR WHEN CONNECTING     ///////////////////////////////////////
            InputStream inputStream = null;

            try {
                inputStream = urlConnection.getInputStream();
                Log.d(UserAtApp.class.toString(), " urlConnection.getInputStream() : " + String.valueOf(urlConnection.getInputStream()));
            } catch (IOException ioe) {
                Log.e(this.getClass().toString(), "Error getting response from server");
                ioe.printStackTrace();
            }
            //////////////////////////////////////////////////////////////////////////////
            Log.d(UserAtApp.class.toString(), "connection OK");

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                result.append(line).append("\n");

            }
            // disconnect
            urlConnection.disconnect();
        } catch (ProtocolException e) {
            Log.e("DisplayRunnersOnMap", e.toString());
            e.printStackTrace();
            return null;
        } catch (MalformedURLException e) {
            Log.e("DisplayRunnersOnMap", e.toString());
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Log.e("DisplayRunnersOnMap", e.toString());
            e.printStackTrace();
            return null;
        }

        return String.valueOf(result);
    }

    /**
     * add the people to the recycleView
     * @param result - the json string of the walkers
     */
    protected void onPostExecute(String result) {
        if (result == null) {
            return;
        }
        // Adapt the Rec View
        RecyclerView mRecyleView = (RecyclerView) this.mView.findViewById(this.viewID);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.mContext);
        mRecyleView.setLayoutManager(layoutManager);

        Log.d("DisplayRunnersOnMap","in parser and the josn string: " + "\n" + result.toString() );
        try {
            // all the users
            final ArrayList<UserData> data= Parser.parseUsers(result);

            // all the runners
            ArrayList<UserData> runners = (ArrayList<UserData>) data.clone();

            // remobe the specific user
            for (UserData user : data) {
                if(!user.isRunning)
                {
                    runners.remove(user);
                }
            }
            // Setup and Handover data to recyclerview

            AdapterUsers mAdapter = new AdapterUsers(mContext, runners);
            mRecyleView.setAdapter(mAdapter);
            mRecyleView.setLayoutManager
                    (new LinearLayoutManager(this.mContext , LinearLayoutManager.HORIZONTAL , false));
            mAdapter.notifyDataSetChanged();

            // desplay users as markers
            mapView.getMapAsync(new OnMapReadyCallback() {

                @Override
                public void onMapReady(GoogleMap googleMap) {



                    for (int i = 0; i < data.size(); i++) {
                        UserData userInfo = data.get(i);
                        if(userInfo.user_id.equals(Constants.user_id))
                            continue;

                        double lat = userInfo.coordinates[1];
                        double lng = userInfo.coordinates[0];
                        LatLng pos = new LatLng(lat, lng);
                        String title;
                        title = userInfo.user_name;


                        //update the marker location
                        if(usersMarkers.containsKey(userInfo.user_id)) {
                            // Pin user location
                            if(userInfo.isRunning)
                                usersMarkers.get(userInfo.user_id).setPosition(pos);
                            else {
                                // Remove a user from the map if he not running
                                usersMarkers.get(userInfo.user_id).remove();
                                usersMarkers.remove(userInfo.user_id);
                            }
                        }
                        else
                        {
                            if(!userInfo.isRunning)
                                continue;
                            Marker marker = googleMap.addMarker(new MarkerOptions().position(pos).title(title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            usersMarkers.put(userInfo.user_id, marker);
                        }
                    }
                }
            });
        }
        catch (Exception e){
            Log.e("UserAtApp","Exception at parser:" + e.toString());
            return;
        }
    }
}