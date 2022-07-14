package com.example.slfb;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.firebase.messaging.FirebaseMessaging;

//Code inspired from https://gist.github.com/ashmeh6/dfa95b3a386e78ac36292566298c7839
public class GpsTracker extends Service implements LocationListener {
    private Context mContext;

    private final OkHttpClient client = new OkHttpClient();
    private SharedPreferences preferenceLocation;

    public static boolean isRunning  = false;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 50; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 120; // 2 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public GpsTracker() {
        getLocation();
    }
    //Constructor is the first steps in initializing an instance of the class.
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public GpsTracker(Context context) {
        preferenceLocation = context.getApplicationContext().getSharedPreferences("currentLocation", MODE_PRIVATE);
        this.mContext = context;
        getLocation();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    //check the network permission
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) mContext, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                        //check the network permission
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity) mContext, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 101);
                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */

    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GpsTracker.this);
        }
    }

    /**
     * Function to get latitude
     * */

    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */

    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    //Gets called everytime the location changes
    @Override
    public void onLocationChanged(Location location) {
        System.out.println("Location changed!");
        double lon = location.getLongitude();
        double lat = location.getLatitude();

        //locations = Key = location  Value = long + lat + rad
        SharedPreferences locations = mContext.getApplicationContext().getSharedPreferences("locations", MODE_PRIVATE);
        if(locations.getAll().size() > 0){
            Map<String,String> keys = (Map<String, String>) locations.getAll();
            for(Map.Entry<String,String> entry : keys.entrySet()){
                String[] splitLocation = entry.getValue().split("\\s+");
                String lon2 = splitLocation[0];
                String lat2 = splitLocation[1];
                String radius = splitLocation[2];
                double radiusAsDouble = Double.parseDouble(radius) , longitudeAsDouble = Double.parseDouble(lon2) , latitudeAsDouble = Double.parseDouble(lat2);
                //If true, then we are in the radius.
                boolean isWithinRadius = distance_between(latitudeAsDouble,longitudeAsDouble,lat,lon) < radiusAsDouble;
                if(isWithinRadius){
                    SharedPreferences.Editor editor = preferenceLocation.edit();
                    if(preferenceLocation.getString("location" , "null").equals("null") || preferenceLocation.getString("location" , "null").equals("")){
                        editor.putString("location", entry.getKey());
                        editor.commit();
                        System.out.println(preferenceLocation.getString("location" , "null") + "<----");
                        Firebase.senPushdNotification(mContext.getApplicationContext().getSharedPreferences("user", MODE_PRIVATE).getString("user","null"), entry.getKey());
                        break;
                    }
                    else if(!preferenceLocation.getString("location" , "null").equals(entry.getKey())){
                        editor.putString("location", entry.getKey());
                        editor.commit();
                        Firebase.senPushdNotification(mContext.getApplicationContext().getSharedPreferences("user", MODE_PRIVATE).getString("user","null"), entry.getKey());
                        break;
                    }
                    break;

                }
            }
        }

    }
    //Mathematical formula for calculating distance between two locations given both of their longitude and latitude.
    double distance_between(double lat1 , double lon1 , double lat2 , double lon2)
    {
        double dLat = (lat2-lat1)*Math.PI/180;
        double dLon = (lon2-lon1)*Math.PI/180;
        lat1 = lat1*Math.PI/180;
        lat2 = lat2*Math.PI/180;

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return 6371 * c * 1000;
    }

    public String retrieveTokenFromServer(List<String> username){
        //Prepare the form data to be sent to the server. This is like a "form" in html. We have 3 inputs.
        //1. username 2. password and 3. a variable login_user which is set to "LOGIN".
        String result = String.join("|", username);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("usernames",result)
                .build();
        //Prepare the HTTP request by building it. We give it the URL to the server and we say it should be a "POST" type of request.
        Request request = new Request.Builder()
                .url("https://silentlocationfamilybot.000webhostapp.com/gettoken.php")
                .post(requestBody)
                .build();
        try {
            //Execute the request by sending it and get the response from the server.
            Response response = client.newCall(request).execute();
            //System.out.println(Objects.requireNonNull(response.body()).string());
            //If the response is "s" we know the username and password exists in the database.
            String res = Objects.requireNonNull(response.body()).string();
            if(!res.equals("fail")){
                return res;
            }
            //If the response is not "s" we know the username or password does not exist in the database.
            else{
                //Add some error handling
            }
        } catch (IOException e) {
            //There was an error sending the data to the server.
        }
        return "";
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy(){
        stopUsingGPS();
        isGPSEnabled = false;
        isNetworkEnabled = false;
        canGetLocation = false;
    }
}