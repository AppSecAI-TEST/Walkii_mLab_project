package com.example.alonsiwek.demomap;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import java.util.concurrent.ExecutionException;

/**
 * Created by Dor on 29-Apr-17.
 * Login by phone number and user name - data is save in SharedPreferences
 * Happening only once when user register to the app
 */

public class Login extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        final Button submit = (Button) findViewById(R.id.login_btn);
        final EditText phoneNumber = (EditText) findViewById(R.id.phone_number_for_DB);
        final EditText userName = (EditText) findViewById(R.id.user_name_for_DB);

        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //save the data
                SharedPreferences preferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("PhoneNumber", phoneNumber.getText().toString());
                editor.putString("Name", userName.getText().toString());
                editor.commit();
                // new PostLocationAtFirstLogin(Login.this.getApplicationContext()).execute();
                PostLocationAtFirstLogin postLocationAtFirstLogin =
                        new PostLocationAtFirstLogin(Login.this.getApplicationContext());
                postLocationAtFirstLogin.execute();
                String dataOut = null;
                try {
                    dataOut = postLocationAtFirstLogin.get().toString();
                } catch (InterruptedException e) {
                    Log.d("Login", "InterruptedException - cannot get data outpostLocationAtFirstLogin ");
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    Log.d("Login", "ExecutionException - cannot get data outpostLocationAtFirstLogin ");
                    e.printStackTrace();
                }

                //get the user ID out
                try {
                    JSONObject json = new JSONObject(dataOut);
                    String id = json.getString("_id");
                    editor.putString("uid", id);
                    Log.d("Login"," the user ID is:" + json.getString("_id")) ;
                    editor.commit();
                    Constants.loadSharedPrefs(preferences);
                } catch (JSONException e) {
                    Log.d("Login"," could NOT get USER_ID") ;
                    e.printStackTrace();
                    return;
                }

                // call to service
                Intent intent = new Intent(Login.this, LocationUpdateService.class);
                PendingIntent pintent = PendingIntent.getService(Login.this, 0, intent, 0);
                AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000, pintent);

                //switch to main page
                Intent toMainPage = new Intent(Login.this, MainScreen.PageAdapter.class);
                Login.this.startActivity(toMainPage);
                Login.this.finish();
            }
        });
    }

}


