package com.example.alonsiwek.demomap;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dor on 27-May-17.
 */

public class Parser {
    static public ArrayList<UserData> parseUsers(String userData)
    {

        ArrayList<UserData> data = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(userData);

            // Extract data from json and store into ArrayList as class objects
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject_data = jsonArray.getJSONObject(i);
                UserData userInfo = new UserData();
                userInfo.user_name = jsonObject_data.getString("user_name");
                userInfo.user_phone = jsonObject_data.getString("user_phone");
                userInfo.user_id = jsonObject_data.getString("_id");
                userInfo.isRunning = jsonObject_data.getBoolean("is_running");

                // get the location
                JSONObject get_loc = jsonObject_data.getJSONObject("loc");
                JSONArray coor = get_loc.getJSONArray("coordinates");
                userInfo.coordinates[0] = coor.getDouble(0);;
                userInfo.coordinates[1] = coor.getDouble(1);

                Log.d("UserAtApp", "userData.user_name:  " + userInfo.user_name);
                Log.d("UserAtApp", "user_id:  " + userInfo.user_id);
                Log.d("UserAtApp", "userData.coordinate[0]" + userInfo.coordinates[0]);
                Log.d("UserAtApp", "userData.coordinate[1]" + userInfo.coordinates[1]);

                data.add(userInfo);
            }
        }
        catch (JSONException e){
            Log.e("UserAtApp","JSONException at parser:" + e.toString());
            return null;        }
        catch (Exception e) {
            Log.e("UserAtApp", "Exception at parser:" + e.toString());
            return null;
        }

        return data;
    }
}
