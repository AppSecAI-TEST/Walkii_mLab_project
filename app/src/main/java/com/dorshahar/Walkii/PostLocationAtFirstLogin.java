package com.example.alonsiwek.demomap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

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
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Dor on 01-May-17.
 * Upload the user basic data to Date Base at the first time that the user use the app
 * Note: USER_ID is actually the id of THE DOCUMENT IN THE MONGO-DB
 */

public class PostLocationAtFirstLogin extends AsyncTask<JSONObject,String,String> {

    ProgressDialog progressDialog;
    private Context ctx;

    //Only use for the get the context of the app for SharedPreferences
    public PostLocationAtFirstLogin(Context context){
        super();
        this.ctx = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    final protected String doInBackground(JSONObject... params) {

        try {

            String url_path = Constants.SERVER_URL + Constants.LOC_STATUS_PATH; //  192.168.56.1
            return postData(url_path);
        } catch (IOException ex){
            Log.d("PostLocation", "fail to connect!");
            Log.d("PostLocation", "doInBackground: " + ex);
            return "NETWORK ERROR";
        } catch (JSONException ex){
            Log.d("PostLocation", "Problem with json");
            Log.d("PostLocation", "doInBackground" + ex );
            return "DATA INVALID";
        }

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    private String postData(String urlPath) throws IOException , JSONException{

        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        JSONObject firstPostOnDB = new JSONObject();
        StringBuilder result = new StringBuilder();
        // Posting
        try {
            //get the data of phone number, user name and user ID from SharedPreferences
            SharedPreferences getSharedPreff = ctx.getSharedPreferences("UserInfo",MODE_PRIVATE);
            String data_phone = getSharedPreff.getString("PhoneNumber" , "NUMBER IS NOT AVAILABLE");
            String data_name = getSharedPreff.getString("Name", "NAME IS NOT AVAILABLE");

            Log.d("PostLocationFirstLogin"," user phone : " + data_phone);
            Log.d("PostLocationFirstLogin", " user name : " + data_name);

            //Build JSON
            firstPostOnDB.put("user_name", data_name);
            firstPostOnDB.put("user_phone", data_phone);

            // Open connection to the server
            // Open url for reading
            URL url = new URL(urlPath);
            HttpURLConnection urlConnection = null;

            // try to connect the url
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                Log.d("PostLocationFirstLogin" , String.valueOf(urlConnection));
            } catch ( IOException e){
                Log.d("PostLocationFirstLogin" , String.valueOf(e));
            }

            urlConnection.setDoOutput(true);
            //set the time to read from url - in miliSec
            urlConnection.setReadTimeout(10000);
            //set time to be used when opening a communications link
            // to the resource referenced by this URLConnection connect to url
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestMethod("POST");
            // enable output
            urlConnection.setDoOutput(true);
            //set header
            urlConnection.setRequestProperty("Content-Type","application/json");

            urlConnection.connect();
            Log.d("PostLocation","Connecting");

            //Post data to server
            OutputStream outputStream = urlConnection.getOutputStream();
            bufferedWriter = new BufferedWriter((new OutputStreamWriter(outputStream)));
            bufferedWriter.write(firstPostOnDB.toString());

            Log.d("PostLocation","written to server");

            bufferedWriter.flush();

            //Read the response from the server

            //////////////    FOR DEBUG - CATCH ERROR WHEN CONNECTING   ///////////////////////////////////////
            InputStream inputStream = null;

            try{
                inputStream = urlConnection.getInputStream();
                Log.d("PostLocation", " urlConnection.getInputStream() : " + String.valueOf(urlConnection.getInputStream()));
            } catch (IOException ioe){
                if ( urlConnection instanceof  HttpURLConnection){
                    HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                    int statusCode = httpURLConnection.getResponseCode();
                    Log.d("PostLocation", " error code number: " + String.valueOf(statusCode));
                    if (statusCode != 200){
                        inputStream = httpURLConnection.getErrorStream();
                        Log.d("PostLocation", String.valueOf(inputStream));
                    }
                }
            }
            //////////////////////////////////////////////////////////////////////////////
            Log.d("PostLocation","connection OK");

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = bufferedReader.readLine()) != null){
                result.append(line).append("\n" );

                Log.d("PostLocation", String.valueOf(result));

            }
            // disconnect
            urlConnection.disconnect();
        } finally {
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }

        }
        Log.d("PostLocation", String.valueOf(result));
        Log.d("PostLocation","return");

        return result.toString();

    }
}