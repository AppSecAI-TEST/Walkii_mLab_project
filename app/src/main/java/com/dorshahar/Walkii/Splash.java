package com.example.alonsiwek.demomap;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by alonsiwek on 11/12/2016.
 *
 * display App splash screen
 * display App permission
 * handle next activity / fragemnt - depands if user sigh in at the past
 * activate LocationUpdateService
 *
 */

//TODO: decide if delete CONTACTS in the manifest

public class Splash extends AppCompatActivity{

    /** Duration of wait - splash screen **/
    private final int SPLASH_DISPLAY_LENGTH = 1100;

    private final int TOAST_DISPLAY_LENGTH = Toast.LENGTH_LONG;

    /** The code used when requesting permissions */
    private static final int PERMISSIONS_REQUEST = 10;

    /** The code used when requesting location */
    private static final int PICK_LOCATION_RESQUEST = 111;

    private static final String MESSEGE_WHEN_LOCATION_RESQUEST_DENY =
            "The appliction could not get your location," + '\n' + "Hence - the use will be limitet";

    AlertDialog alert = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /**
         * On a post-Android 6.0 devices, check if the required permissions have
         * been granted.
         */
        if (Build.VERSION.SDK_INT >= 23) {
            Log.i("Splash", " Build.VERSION.SDK_INT >= 23 , and checking premissions" );
            checkPermissions();
        }
         else {
            isGpsEnabled();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if(requestCode == PICK_LOCATION_RESQUEST){
            Log.d("Splash", "PICK_LOCATION_RESQUEST");
            // Make sure the request was successful
            if (isLocatiomManager()){
                Log.d("Splash", "RESULT_OK -> startNextActivity");
                Log.d("Splash", "the resultCode is: " + resultCode);
                Log.d("Splash", "isLocatiomManager: " + isLocatiomManager());
                startNextActivity();
            }
            else {
                Log.d("Splash", "RESULT *** NOT *** OK  -> desplay toast");
                Log.d("Splash", "the resultCode is: " + resultCode);
                Log.d("Splash", "isLocatiomManager: " + isLocatiomManager());

               displayToast();
            }
        }
    }

    /**
     * Display Location closed toast , and strat the app without the location
     */
    public void displayToast(){
        Toast.makeText(getApplicationContext(),
                MESSEGE_WHEN_LOCATION_RESQUEST_DENY,TOAST_DISPLAY_LENGTH).show();
        strartNextActivityThread.start();
    }


    /**
     * start the strartNextActivity function IF the displayToast function is called,
     * to suspend the next activity to display the toast.
     *
     * Notice that the toast will be display for some short time at MainPageFrag related xml to make
     * sure the the user see the toast.
     */
    Thread strartNextActivityThread = new Thread(){
        public void run () {
            try{
                Thread.sleep(TOAST_DISPLAY_LENGTH);
                startNextActivity();
            } catch (InterruptedException e) {
                Log.e("Splash", "InterruptedException when Thread.sleep: " + e.toString());
                e.printStackTrace();
            } catch (Exception e){
                Log.e("Splash","Exception when Thread.sleep: " +  e.toString());
                e.printStackTrace();
            }
        }
    };

    /**
     * start the next activity - depend on checkPermissions method
     * handle inside if the user has sign in to the app
     */
    private void isGpsEnabled(){

        boolean statusOfGPS = false;

        try{
            statusOfGPS = isLocatiomManager();
        } catch (IllegalArgumentException ex) {
            Log.e("Splash", "IllegalArgumentException with statusOfGPS: " + ex.toString());
            ex.printStackTrace();
        } catch (Exception ex){
            Log.e("Splash", "Exception with statusOfGPS: " + ex.toString());
            ex.printStackTrace();
        }

        Log.d("Splash","statusOfGPS is: " + statusOfGPS);

        if (statusOfGPS){
            startNextActivity();
        }
        else {
            // show alert for enabling GPS
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(myIntent,PICK_LOCATION_RESQUEST);
                        }

                    })
                    .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                            displayToast();
                        }
                    });
            alert = builder.create();

            Log.d("Splash", " after  alert = builder.create();");
            alert.show();
        }
    }

    /**
     * Activate location maneger
     * @return true - if location is OK (not null)
     *         false - location is null;
     */
    public boolean isLocatiomManager(){
        boolean result = false;
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        result = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return result;
    }

    /**
     * Start the next Activity and LocationUpdateService service
     * depends if the user already sign in:
     *                                  if yes - going to the Main pager
     *                                  if no - going to Login - sign the user to the app
     */
    private void startNextActivity(){
       /* New Handler to start the Menu-Activity
        * and close this Splash-Screen after some seconds.
        */
       new Handler(getMainLooper()).postDelayed(new Runnable() {
           @Override
           public void run() {

        /* Create an Intent that will start the Menu-Activity. */
               SharedPreferences settings = getSharedPreferences("UserInfo", MODE_PRIVATE);
               Constants.loadSharedPrefs(settings);
               if (settings.contains("PhoneNumber")) {

                   // call to service
                   Intent intent = new Intent(Splash.this, LocationUpdateService.class);
                   PendingIntent pintent = PendingIntent.getService(Splash.this, 0, intent, 0);
                   AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                   alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000, pintent);


                   //sent intent to main screen. use by:  new Intent(Splash.this,MapsActivity.class);
                   Intent mainIntent = new Intent(Splash.this, MainScreen.PageAdapter.class);
                   Splash.this.startActivity(mainIntent);
                   Splash.this.finish();
               } else {
                   //the user DID NOT LOGIN
                   // then we will run the service after the first login
                   Intent mainIntent = new Intent(Splash.this, Login.class);
                   Splash.this.startActivity(mainIntent);
                   Splash.this.finish();
               }
           }
       }, SPLASH_DISPLAY_LENGTH);
   }

    /**
     * Get the list of required permissions by searching the manifest.
     *
     * @return new String[]{Manifest.permission. XXXX }
     */
    public String[] getRequiredPermissions() {
        String[] permissions = null;
        try {
            permissions = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_PERMISSIONS).requestedPermissions;
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e("Splash", e.toString());
            e.printStackTrace();
        }
        catch (Exception e){
            Log.e("Splash" , e.toString());
            e.printStackTrace();
        }
        if (permissions == null) {
            return new String[0];
        } else {
            return permissions.clone();
        }
    }

    /**
     * Get the time (in milliseconds) that the splash screen will be on the
     * screen before
     * @return SPLASH_DISPLAY_LENGTH
     */
    public int getTimeoutMillis() {
        return SPLASH_DISPLAY_LENGTH;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            Log.d("Splash", "onRequestPermissionsResult: requestCode is: " + requestCode);
            checkPermissions();
        }
        //TODO: check else
        else {
            Log.d("Splash", "onRequestPermissionsResult: requestCode is: " + requestCode);
            return;
        }
    }

    /**
     * Check if the required permissions have been granted:
     * {@link #startNextActivity()} if they have.
     *
     * Otherwise:
     * {@link #requestPermissions(String[], int)}.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        String[] ungrantedPermissions = requiredPermissionsStillNeeded();
        if (ungrantedPermissions.length == 0) {
            isGpsEnabled();
        } else {
            requestPermissions(ungrantedPermissions, PERMISSIONS_REQUEST);
        }
    }

    /**
     * Convert the array of required permissions to a {@link Set} to remove
     * redundant elements.
     * Then remove already granted permissions.
     * @return an array of ungranted permissions.
     */
    @TargetApi(23)
    private String[] requiredPermissionsStillNeeded() {

        Set<String> permissions = new HashSet<String>();
        for (String permission : getRequiredPermissions()) {
            permissions.add(permission);
        }
        for (Iterator<String> i = permissions.iterator(); i.hasNext();) {
            String permission = i.next();
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(Splash.class.getSimpleName(),
                        "Permission: " + permission + " already granted.");
                i.remove();
            } else {
                Log.d(Splash.class.getSimpleName(),
                        "Permission: " + permission + " not yet granted.");
            }
        }
        return permissions.toArray(new String[permissions.size()]);
    }
}