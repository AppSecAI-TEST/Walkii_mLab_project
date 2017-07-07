package com.example.alonsiwek.demomap;

import android.app.Service;
import android.content.Intent;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.text.LoginFilter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Time;
import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


/**
 * POST the location all the time that the app is running
 */
public class LocationUpdateService extends Service {

    private Handler mHandler = new Handler();

    private String user_id;
    private Timer mTimer;

    //dummy constructor
    public LocationUpdateService(){
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("LocationUpdateService", "service is started");
        try {
            getDeviceLocationForLocationService();
        } catch (IOException e) {
            Log.d("LocationUpdateService", "IOException: " + e.toString());
            e.printStackTrace();
        } catch (JSONException e) {
            Log.d("LocationUpdateService", "JSONException: " + e.toString());
            e.printStackTrace();
        }

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, 1000);
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        getDeviceLocationForLocationService();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            });
        }
    }
    /**
     * Gets the current location of the device
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getDeviceLocationForLocationService() throws IOException, JSONException {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (currentLocation != null) {
            Log.d("LocationUpdateService", "getDeviceLocationForLocationService: " + currentLocation);
        } else {
            Log.d("LocationUpdateService", "getDeviceLocationForLocationService: currentLocation: " + currentLocation);
            return;
        }

        final JSONObject LocationUpdateServiceOnTheRun = new JSONObject();
        JSONObject inside_loc = new JSONObject();

        // Pass Long & Lat
        JSONArray coordinates = new JSONArray();
        coordinates.put(currentLocation.getLongitude());  //long
        coordinates.put(currentLocation.getLatitude());  //lat

        // Pass the time
        SimpleDateFormat jsonDateFromat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
        jsonDateFromat.setTimeZone(TimeZone.GMT_ZONE);
        String currentDateTime = jsonDateFromat.format(Calendar.getInstance());  // Calendar rightNow = Calendar.getInstance();
        LocationUpdateServiceOnTheRun.put("time", currentDateTime);

        Log.d("LocationUpdateService", "time in JSON:" + '\n' + currentDateTime);

        inside_loc.put("type","Point");
        inside_loc.put("coordinates",coordinates);
        LocationUpdateServiceOnTheRun.put("loc",inside_loc);

        // This Thread is for the connection - we cannot conect is service without Thread
        Thread t = new Thread(new Runnable() {
            JSONObject obj = LocationUpdateServiceOnTheRun;

            public void run() {
                try {
                    Log.d("LocationUpdateService", "the json obj to send: " + '\n' + LocationUpdateServiceOnTheRun.toString());
                    postDataToServer(obj);
                } catch (IOException e) {
                    Log.e("LocationUpdateService"," error in thread: IOException"
                            + '\n' + "error at: " +  e.toString());
                    e.printStackTrace();
                } catch (Exception e){
                    Log.e("LocationUpdateService"," error in thread: tException"
                            + '\n' + "error at: " +  e.toString());
                    e.printStackTrace();
                }
            }
        });

        t.start();
    }

    /**
     * Posting the data of the JSON to the server
     * @param obj - the JSON object
     * @throws IOException
     */
    void postDataToServer(JSONObject obj) throws IOException {

        String update_user_location_url = Constants.SERVER_URL + Constants.LOC_STATUS_PATH + Constants.user_id;


        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        StringBuilder result = new StringBuilder();

        try {

            // Open connection to the server
            // Open url for reading
            URL url = new URL(update_user_location_url);

            HttpURLConnection urlConnection = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                Log.d("LocationUpdateService" ," urlConnection is: " + '\n' + String.valueOf(urlConnection));
            } catch ( IOException e){
                Log.e("LocationUpdateService" ,"IOException: " + String.valueOf(e));
            }


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

            try {
                urlConnection.connect();
                Log.d("LocationUpdateService", "try to: urlConnection.connect()");
            }
            catch(Exception e) {
                Log.e("LocationUpdateService","Connect exception: " + e.toString());
                e.printStackTrace();
                return;
            }
            Log.d("LocationUpdateService","Connecting");

            //Post data to server
            OutputStream outputStream = urlConnection.getOutputStream();
            bufferedWriter = new BufferedWriter((new OutputStreamWriter(outputStream)));
            bufferedWriter.write(obj.toString());

            Log.d("LocationUpdateService","written to server");

            bufferedWriter.flush();

            //Read the response from the server
            //////////////    FOR DEBUG - CATCH ERROR WHEN CONNECTING     ///////////////////////////////////////
            InputStream inputStream = null;

            try{
                inputStream = urlConnection.getInputStream();
                Log.d("LocationUpdateService", " urlConnection.getInputStream() : " + String.valueOf(urlConnection.getInputStream()));
            } catch (IOException ioe){
                Log.e(this.getClass().toString(), "Error getting response from server");
                inputStream = urlConnection.getErrorStream();
                Log.e("LocationUpdateService","urlConnection.getErrorStream() : " +  String.valueOf(urlConnection.getErrorStream()));
                ioe.printStackTrace();
            }
            //////////////////////////////////////////////////////////////////////////////
            Log.d("LocationUpdateService","connection OK");

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = bufferedReader.readLine()) != null){
                result.append(line).append("\n" );
            }

            Log.d("LocationUpdateService", " ** the FINAL result string is: **"  + '\n' + String.valueOf(result));

            // disconnect
            urlConnection.disconnect();
        } catch (ProtocolException e) {
            Log.e("LocationUpdateService", "ProtocolException: " + e.toString() );
            e.printStackTrace();
        } catch (MalformedURLException e) {
            Log.e("LocationUpdateService", "MalformedURLException: " + e.toString() );
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("LocationUpdateService", "IOException: " + e.toString() );
            e.printStackTrace();
        } finally {
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
        }

    }
}