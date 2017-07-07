package com.example.alonsiwek.demomap;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dor on 1/11/2017.
 * This class is the Fragment calls of the MapActivity.
 */

public class MapActivityFrag extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    Boolean mIsRunning_atMAF;
    ////////////////////////////////////Timer on map. number in seconds
    public int DISPLAY_TIMER_ON_MAP_COUNTER = 120;
    ///////////////////////////////////
    private static int MAP_ZOOM_RATE = 15;
    private static final int UPDATE_RECYCLE_VIEW_DURATION = 5000;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView  = inflater.inflate(R.layout.activity_maps, null, false);


        mMapView = (MapView) rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            Log.e(MapActivityFrag.class.toString(),"error to display map: " + e.toString());
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                googleMap.setMyLocationEnabled(true);

                /////////////////////for DEMO ///////////////
                double latFake = 34.832102;
                double lngFake = 32.173139;
                LatLng posFake = new LatLng(lngFake, latFake);

                mMap.addMarker(new MarkerOptions().position(posFake).title("Ofra Vaygertan")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                ///////////////////////////////////////////

                // for display my current location at the map (without marker)
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));


                if (location != null) {
                    double myLat = location.getLatitude();
                    double myLong = location.getLongitude();

                    Log.d("MapActivityFrag","lat: " + myLat);
                    Log.d("MapActivityFrag","long: " + myLong);

                    // For zooming automatically to my location
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLat, myLong), MAP_ZOOM_RATE);
                    mMap.animateCamera(cameraUpdate);
                }
                else{
                    Log.d("MapActivityFrag","location is: NULL ");
                }
            }
        });
        //////// display timer on map. Fake ETA
        final TextView tm= (TextView) rootView.findViewById(R.id.timer);

        new CountDownTimer(DISPLAY_TIMER_ON_MAP_COUNTER * 1000, 1000){
            public void onTick(long millisUntilFinished){

                tm.setText(String.valueOf("  ETA: " + (DISPLAY_TIMER_ON_MAP_COUNTER / 60) + ":" +
                        (DISPLAY_TIMER_ON_MAP_COUNTER % 60)));


                DISPLAY_TIMER_ON_MAP_COUNTER--;

            }
            public  void onFinish(){
                tm.setText("Your friend is here!");
            }
        }.start();


        ImageButton finish = (ImageButton)rootView.findViewById(R.id.finish_btn);

        ////////////// Part of UPDATE DB ////////////////////////////////

        /* update DB only when mIsRunning_atMAF == false.
         * update DB only when mIsRunning == true  will be with btn_go button.
         */
        finish.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mIsRunning_atMAF = false;




                Log.d("MapActivityFrag","mIsRunning_atMAF :" + mIsRunning_atMAF.toString());

                if (mIsRunning_atMAF == false) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                MainPageFrag.updateRunningState(mIsRunning_atMAF);
                            } catch (IOException e) {
                                Log.e(MapActivityFrag.class.toString(), e.toString());
                                e.printStackTrace();
                                return;
                            }
                        }
                    }).start();
                }
                ((MainScreen.PageAdapter)getActivity()).setCurrentItem
                        (MainScreen.PageAdapter.FRAGMENT_THREE_SUMMARY , true);


            }
        });

        // set Timer to map
        TimerTask task = new DisplayRunnersOnMapTimer
                (getActivity(),rootView,R.id.runners_list_at_map_frag,googleMap,mMapView);
        new Timer().scheduleAtFixedRate(task,0,UPDATE_RECYCLE_VIEW_DURATION);



        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


}