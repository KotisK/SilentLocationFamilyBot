package com.example.slfb;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.Connection;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

//This is the class we extend in order to take control of the phones calling service.
@RequiresApi(api = Build.VERSION_CODES.N)
public class Calls extends CallScreeningService {
    private GpsTracker gpsTracker = null;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    //This method is called whenever "new incoming or outgoing call is added".
    //Source: https://developer.android.com/reference/android/telecom/CallScreeningService#onScreenCall(android.telecom.Call.Details)
    public void onScreenCall(@NonNull Call.Details callDetails) {
        if(gpsTracker == null)
            gpsTracker = new GpsTracker(Calls.this);
        boolean isIncoming = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isIncoming = callDetails.getCallDirection() == Call.Details.DIRECTION_INCOMING;
        }


        //If the the call is INCOMING and not OUTGOING
        if (isIncoming) {
            //Bring the shared preferences for the phone numbers we have stored.
            SharedPreferences pref = getApplicationContext().getSharedPreferences("phoneNumbers", MODE_PRIVATE);

            //callDetails.getHandle() gives us information about the caller. We specifically want the phone number
            //so we split the information on the character ":".
            String[] split = callDetails.getHandle().toString().split(":");

            //prints phone number in console. For testing purposes only
            System.out.println("PHONE NUMBER " + split[1]);

            //Check if the phone number that is calling is matching a phone number we have saved in our shared preference.
            if (pref.contains(split[1])) {
                //We use string manipulation to get all of the relevant information
                //(starting time, ending time, location name and whitelist/blacklist).
                //Because we have stored these data as "timeFrom timeToo locationName blocked" we split the string on spaces.
                String time = pref.getString(split[1], ""), lon = "", lat = "", radius = "";
                String[] splitString = time.split("\\s+");
                String timeFrom = splitString[0], timeToo = splitString[1], locationName = splitString[2];

                //Get the current time
                LocalTime currentTime = LocalTime.parse(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

                //Check if the time for the incoming phone call is within the time limit put in by the user.
                boolean timeNotAllowed = (currentTime.isAfter(LocalTime.parse(timeFrom)) && currentTime.isBefore(LocalTime.parse(timeToo)));

                //If the location name for the current incoming phonecall is "All_locations" then we just check if the time
                //is within the allowed time-span.
                if(locationName.equals("All_locations")){
                    if(timeNotAllowed)
                        respondToCall(callDetails,new CallResponse.Builder().setDisallowCall(true).setRejectCall(true).build());
                }
                else{
                    //Since the location name is not "All_locations", we have to check where the user is right now
                    //and compare it to the saved location to see if it is allowed or not.
                    SharedPreferences sp = getApplicationContext().getSharedPreferences("locations", MODE_PRIVATE);

                    //Split the string from SharedPreferences into the relevant variables.
                    String locationInfo = sp.getString(locationName, "none");
                    String[] splitLocation = locationInfo.split("\\s+");
                    lon = splitLocation[0];
                    lat = splitLocation[1];
                    radius = splitLocation[2];

                    //Because we have saved the longitude,latitude and radius as a string, we need to convert
                    //them back into double in order to do distance calculations on them.
                    double radiusAsDouble = Double.parseDouble(radius) , longitudeAsDouble = Double.parseDouble(lon) , latitudeAsDouble = Double.parseDouble(lat);
                    double resultingLatitude;
                    double resultingLongitude;

                    //Try to get the current location coordinates
                    if(gpsTracker.canGetLocation()){
                        resultingLatitude = gpsTracker.getLatitude();
                        resultingLongitude = gpsTracker.getLongitude();

                    }else{
                        resultingLatitude = latitudeAsDouble;
                        resultingLongitude = longitudeAsDouble;
                    }
                    System.out.println(resultingLatitude + " " + resultingLongitude);

                    float[] results = new float[1];
                    //This method that is within the class "Location" takes in two coordinates and checks if they are within
                    //a specific radius from eachother.
                    Location.distanceBetween(latitudeAsDouble, longitudeAsDouble, resultingLatitude, resultingLongitude, results);
                    float distanceInMeters = results[0];
                    //If true, then we are in the radius.
                    boolean isWithinRadius = distanceInMeters < radiusAsDouble;
                    System.out.println("time NOT allowed: " + timeNotAllowed + "   "+isWithinRadius);
                    System.out.println(latitudeAsDouble + " " + longitudeAsDouble + " | " + resultingLatitude + " " + resultingLongitude + "  d: " + distanceInMeters);

                    //check if the time is NOT allowed AND we are NOT within the radius THEN we reject the call.
                    if(timeNotAllowed && isWithinRadius){
                        respondToCall(callDetails,new CallResponse.Builder().setDisallowCall(true).setRejectCall(true).setSkipCallLog(true).setSkipNotification(true).build());
                        stopService(new Intent(Calls.this,GpsTracker.class));
                    }
                }

            }
        }
    }

}