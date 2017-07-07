package com.example.alonsiwek.demomap;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;


import com.google.android.gms.maps.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UserAtApp extends AsyncTask<Void,Void,String> {

    String mAllUsers_asString = null;
    RecyclerView showUserData;
    Context mCtx;
    View mView;
    int viewId;
    private List<UserData> data;

    public UserAtApp()
    {

    }

    public UserAtApp(Context c, View v, int view_id)
    {
        this.mView = v;
        this.mCtx = c;
        this.viewId = view_id;
    }

    @Override
    protected String doInBackground(Void... params) {
        String users = getAllUser();
        if (users == null){
            Log.e(this.getClass().toString(),  "Error fetching users");
            return null;
        }

        return users;
    }

    /**
     * the GET method
     * return - a string with all the users
     */
    String getAllUser () {
        String get_all_users_url = Constants.SERVER_URL + Constants.LOC_STATUS_PATH;


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
            Log.e("UsersAtApp", e.toString());
            e.printStackTrace();
            return null;
        } catch (MalformedURLException e) {
            Log.e("UsersAtApp", e.toString());
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Log.e("UsersAtApp", e.toString());
            e.printStackTrace();
            return null;
        } catch (RuntimeException e){
            Log.e("UsersAtApp", e.toString());
            e.printStackTrace();
            return null;
        } catch (Exception e){
            Log.e("UsersAtApp", e.toString());
            e.printStackTrace();
            return null;
        }

        return String.valueOf(result);
    }

    protected void onPostExecute(String result){
        if(result == null) {
            return;
        }

        // get the recycle view
        RecyclerView mRecyleView = (RecyclerView) this.mView.findViewById(this.viewId);

        // return the layout
        RecyclerView.LayoutManager layout = mRecyleView.getLayoutManager();

        // case of null - layout did not create yet -> set it up
        if (layout == null) {
            layout = new LinearLayoutManager(this.mCtx);
            mRecyleView.setLayoutManager(layout);
        }

        // get the layout state and save it to prevent jumps at the updates
        Parcelable recyclerViewState;
        recyclerViewState = mRecyleView.getLayoutManager().onSaveInstanceState();

        Log.d("UserAtApp","in parser and the josn string: " + "\n" + result.toString() );
        try {
            ArrayList<UserData> data = Parser.parseUsers(result);

            for (int i = 0; i < data.size(); i++){
                if (data.get(i).isRunning == false)
                    data.remove(i);
            }
            // Setup and Handover data to recyclerview
            AdapterUsers mAdapter = new AdapterUsers(mCtx, data);
            mRecyleView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();

            mRecyleView.getLayoutManager().onRestoreInstanceState(recyclerViewState);


        }

        catch (Exception e){
            Log.e("UserAtApp","Exception at parser:" + e.toString());
            return;
        }
    }

}