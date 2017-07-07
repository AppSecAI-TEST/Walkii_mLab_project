package com.example.alonsiwek.demomap;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;
import java.util.TimerTask;

/**
 * Created by Dor on 27-May-17.
 */

public class UserAtAppTimer extends TimerTask {

    Context ctx;
    View view;
    int viewId;

    public UserAtAppTimer(Context c, View v, int viewId) {
        this.view = v;
        this.ctx = c;
        this.viewId = viewId;
    }

    @Override
    public void run() {
        new UserAtApp(this.ctx, this.view, this.viewId).execute();
    }
}
