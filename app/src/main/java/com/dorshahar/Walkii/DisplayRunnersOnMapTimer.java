package com.example.alonsiwek.demomap;

import android.content.Context;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import java.util.TimerTask;

/**
 * Created by Dor on 17-Jun-17.
 */

public class DisplayRunnersOnMapTimer extends TimerTask {

    Context context;
    View view;
    int viewId;
    GoogleMap gMap;
    MapView mapView;

    public DisplayRunnersOnMapTimer(Context context,View view, int viewId,GoogleMap map,MapView mapView){
        this.context = context;
        this.view = view;
        this.viewId = viewId;
        this.gMap = map;
        this.mapView = mapView;
    }

    @Override
    public void run() {
        new DisplayRunnersOnMap(context,view,gMap,mapView,viewId).execute();
    }
}
