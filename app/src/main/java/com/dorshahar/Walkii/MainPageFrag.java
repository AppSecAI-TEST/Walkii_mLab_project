package com.example.alonsiwek.demomap;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dor on 1/11/2017.
 * This class is the Fragment calls of the main screen
 */

public class MainPageFrag extends Fragment {

    Boolean mIsRunning = false;
    private static final int UPDATE_RECYCLE_VIEW_DURATION = 5000;

    // 3200 is Toast.Length long
    private static final int SWIPE_TO_MAPS_FRAG_DURATION_GO_BUTTON = 3200 + 50;
    private static final int SWIPE_TO_MAPS_FRAG_DURATION_RIGHT_ARROW_BUTTON = 150;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.main_screen_frag, null);

        // set Timer to recycle list
        TimerTask task = new UserAtAppTimer(getActivity(), view, R.id.users_list);
        new Timer().scheduleAtFixedRate(task,0,UPDATE_RECYCLE_VIEW_DURATION);

        // get the widgets reference from Fragment XML layout
        final ImageButton btn_go = (ImageButton) view.findViewById(R.id.go_walking_btn);
        final ImageButton btn_rightArrow = (ImageButton) view.findViewById(R.id.right_arrow);
        final ImageButton btn_leftArrow = (ImageButton) view.findViewById(R.id.left_arrow);
        final RecyclerView mRecyleView = (RecyclerView) view.findViewById(R.id.users_list);
        final TextView tv = (TextView) view.findViewById(R.id.your_friends_tv);
        final ImageButton btn_bell = (ImageButton) view.findViewById(R.id.bell);

        final TextView numOfNotification = (TextView) view.findViewById(R.id.red_cycle);
        numOfNotification.setText(String.valueOf("  1"));

        // set the viability functionality
        btn_bell.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                btn_bell.setVisibility(View.GONE);
                numOfNotification.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
                mRecyleView.setVisibility(View.VISIBLE);

                // make GO button + arrow invisible
                btn_go.setVisibility(View.GONE);
                btn_rightArrow.setVisibility(View.GONE);
                btn_leftArrow.setVisibility(View.GONE);
            }
        });

        //make Invitation text clickable and return the normal layout
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_go.setVisibility(View.VISIBLE);
                btn_leftArrow.setVisibility(View.VISIBLE);
                btn_rightArrow.setVisibility(View.VISIBLE);
            }
        });


        /* set functionality as Go button
        * Toast of the Main button
        *  Set a click listener for Fragment button
        *  Auto swipe to the next screen
        */
        btn_go.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // show the Toast
                activateToast(R.layout.go_massage_toast ,savedInstanceState, Toast.LENGTH_LONG);

                //  UPDATE DB
                mIsRunning = true;

                /* update DB only when mIsRunning == true.
                 * update DB only when mIsRunning == false will be with FINISH button.
                 */
                Log.d(MainPageFrag.class.toString(),"mIsRunning:" +  String.valueOf(mIsRunning));
                activate_GoButton(mIsRunning);

                // auto swipe to next screen
                activateOnClickSwipe(SWIPE_TO_MAPS_FRAG_DURATION_GO_BUTTON);
            }
        });

        // Swipe to map fragment
        btn_rightArrow.setOnClickListener(new  View.OnClickListener(){

            @Override
            public void onClick(View v) {
                activateOnClickSwipe(SWIPE_TO_MAPS_FRAG_DURATION_RIGHT_ARROW_BUTTON);
            }
        });

        return view;
    }

    /**
     * Swipe to Maps fragemnt after delay
     * @param duration - delay duration
     */
    public void activateOnClickSwipe(int duration){

        new Handler(getActivity().getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                ((MainScreen.PageAdapter)getActivity()).setCurrentItem
                        (MainScreen.PageAdapter.FRAGMENT_TWO_MAP , true);

            }
        } , duration);

    }

    /**
     * Call the Thread that activate updateRunningState Method
     * @param mIsRunningStatus
     */
    public static void activate_GoButton(final boolean mIsRunningStatus){
        if (mIsRunningStatus) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        updateRunningState(mIsRunningStatus);
                    } catch (IOException e) {
                        Log.e(MainPageFrag.class.toString(), e.toString());
                        e.printStackTrace();
                        return;
                    }
                }
            }).start();
        }
    }

    /**
     * Display Toast to the screen
     * @param toastLayout - the Toast layot
     * @param savedInstanceState
     * @param toastLength - duration of Toast
     */
    public  void activateToast(int toastLayout, Bundle savedInstanceState, int toastLength){

        // Get the application context
        Toast toast = new Toast(getContext());
        // Set the Toast display position layout center
        toast.setGravity(Gravity.CENTER, 0, 0);

        LayoutInflater inflater = getLayoutInflater(savedInstanceState);
        View layout = inflater.inflate(toastLayout, null);

        // Set the Toast duration
        toast.setDuration(toastLength);

        // Set the Toast custom layout
        toast.setView(layout);
        toast.show();
    }

    /**
     * Update the DB with the boolean state field of "is_running"
     * @param state - true or flase
     * @throws IOException
     */
    static void updateRunningState(Boolean state) throws IOException {

        JSONObject json = new JSONObject();
        URL url;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        try {
            json.put("is_running", state);
        } catch (JSONException e) {
            Log.e(MainPageFrag.class.toString(), "json error: " + e.toString());
            e.printStackTrace();
            return;
        }

        try {
           url = new URL(Constants.SERVER_URL + Constants.LOC_STATUS_PATH + Constants.user_id);
        } catch (MalformedURLException e) {
            Log.e(MainPageFrag.class.toString(), "error at url: "  + e.toString());
            e.printStackTrace();
            return;
        }

        HttpURLConnection urlConnection = null;
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        //set the time to read from url - in miliSec
        urlConnection.setReadTimeout(10000);
        //set time to be used when opening a communications link
        // to the resource referenced by this URLConnection connect to url
        urlConnection.setConnectTimeout(10000);
        urlConnection.setRequestMethod("PUT");

        // enable output
        urlConnection.setDoOutput(true);
        //set header
        urlConnection.setRequestProperty("Content-Type","application/json");

        urlConnection.connect();

        Log.d(MainPageFrag.class.toString(),"Connecting");

        //Post data to server
        OutputStream outputStream = null;

        outputStream = urlConnection.getOutputStream();
        bufferedWriter = new BufferedWriter((new OutputStreamWriter(outputStream)));
        bufferedWriter.write(json.toString());

        Log.d(MainPageFrag.class.toString(),"written to server");
        bufferedWriter.flush();

        if ( urlConnection.getResponseCode() != 200) {
            Log.e(MainPageFrag.class.toString()," response code error:" + urlConnection.getResponseCode());
            return;
        }

        // disconnect
        urlConnection.disconnect();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}



