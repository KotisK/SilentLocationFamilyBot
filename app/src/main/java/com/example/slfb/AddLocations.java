//Automatically generated code for Java application. So it says this belongs to SLFB
package com.example.slfb;
//Imports relevant Libraries some of those are gray because they are not used.
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//we create a public class that is called add locations and extends (means
//we want to change the default activities of this class)
//AppCompatActivity is the base class for all activities so you have to extend it to get all functionalities and stuff
public class AddLocations extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient();
    //This is to see if GPS access is ready.
    //when we make something private in the class only the class can change the values of it. Good for programming habits.
    private boolean GPSIsReady = false;


    //puts the vatiables in the UI Longitude, Latitude, Radius, Location Name and the button to add the location
    private TextView lo, la;
    private EditText radius, locationName;
    private Button addLocationButton;

    //An Android Context is an Interface
    // (in the general sense, not in the Java sense; in Java, Context is actually an abstract class!)
    // that allows access to application specific resources and class and information about application environment.
    Context context;
    private GpsTracker gpsTracker;

    //This is where we save the longitude and latitude data.
    double[] longitude = {0.0};
    double[] latitude = {0.0};

    //Automatic Code generated by android studio
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_locations);
        context = this;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //gives longitude and latitude the customly created variables lo and la from the resources
        lo = findViewById(R.id.longitude);
        la = findViewById(R.id.latitude);

        //SharedPreferences is an inbuild class in android that lets you store values locally on the phone.
        //It stores them as a map (<key,value>).
        //MODE_PRIVATE makes the data private for the application only so it wont create technical difficulties if theres other apps
        //on the phone
        SharedPreferences pref = getApplicationContext().getSharedPreferences("locations", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        radius = findViewById(R.id.radius);
        locationName = findViewById(R.id.nameOfLocationEditText);

        addLocationButton = findViewById(R.id.addNewLocationButton);
        //Check if we have the rights to get the users location.
        getLocation();

        //When you press "add location", we first need to see if GPSIsReady is true. If it is we know we have gotten a GPS location
        //from the users phone.
        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addLocationButton.setEnabled(false);
                if (GPSIsReady) {
                    //If the user has not entered the radius
                    //LENGTH_SHORT shows the message for a short duration
                    if (radius.getText().toString().equals("")) {
                        Toast.makeText(context, "You have to add radius!", Toast.LENGTH_SHORT).show();
                    }
                    //If the user has not entered the name of the location
                    else if (locationName.getText().toString().equals("")) {
                        Toast.makeText(context, "You have to add a location name!", Toast.LENGTH_SHORT).show();
                    }
                    else if(!getApplicationContext().getSharedPreferences("locations", MODE_PRIVATE).getString(locationName.getText().toString(),"empty").equals("empty")){
                        Toast.makeText(context, "Location already exists!", Toast.LENGTH_SHORT).show();
                    }
                    //The user has entered all the important information
                    else {
                        //Get the users input to location name and radius.
                        String location = locationName.getText().toString(), lon = Double.toString(longitude[0]), lat = Double.toString(latitude[0]), rad = radius.getText().toString();
                        //Put the location name, longitude, latitude and radius in the shared preference.

                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Handler handler = new Handler(Looper.getMainLooper());
                        executor.execute(() -> {
                            boolean locations = addLocation(location,getApplicationContext().getSharedPreferences("user", MODE_PRIVATE).getString("user", "null"));
                            handler.post(() -> {
                                        if(locations){
                                            editor.putString(location, lon + " " + lat + " " + rad);
                                            editor.commit();
                                            addLocation(locationName.getText().toString() , getApplicationContext().getSharedPreferences("user", MODE_PRIVATE).getString("user","null"));
                                            //Inform the user that a new location has been added successfully.
                                            Toast toast = Toast.makeText(context, "New location added!", Toast.LENGTH_SHORT);
                                            toast.show();
                                            //Take the user back to the previous page.
                                            //Intent: how you relocate from one page to another
                                            //The .this means from the addlocations page to home activity class
                                            Intent intent = new Intent(AddLocations.this, HomeActivity.class);
                                            //startActivity(intent) starts the intent mentioned before
                                            startActivity(intent);
                                        }
                                        else{
                                            Toast.makeText(context, "There was an issue putting adding the location. Try again later", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(AddLocations.this, HomeActivity.class);
                                            //startActivity(intent) starts the intent mentioned before
                                            startActivity(intent);
                                        }
                                    });
                                });
                    }

                }
            }
        });

    }
    public boolean addLocation(String location, String username){
        //Prepare the form data to be sent to the server. This is like a "form" in html. We have 3 inputs.
        //1. username 2. password and 3. a variable login_user which is set to "LOGIN".
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("location", location)
                .addFormDataPart("username", username)
                .build();
        //Prepare the HTTP request by building it. We give it the URL to the server and we say it should be a "POST" type of request.
        Request request = new Request.Builder()
                .url("https://silentlocationfamilybot.000webhostapp.com/setlocation.php")
                .post(requestBody)
                .build();
        try {
            //Execute the request by sending it and get the response from the server.
            Response response = client.newCall(request).execute();
            //System.out.println(Objects.requireNonNull(response.body()).string());
            //If the response is "s" we know the username and password exists in the database.
            //If the response is not "s" we know the username or password does not exist in the database.
            String responseFromServer =  Objects.requireNonNull(response.body()).string().trim();
            if(responseFromServer.equals("s")){
                return true;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    //This method is intended to get the longitude and latitude from the users current location.
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void getLocation(){
        //GpsTracker is a class we got inspired from here https://gist.github.com/ashmeh6/dfa95b3a386e78ac36292566298c7839
        //It checks if we have network access OR GPS access to get the location in terms of longitude and latitude.
        gpsTracker = new GpsTracker(AddLocations.this);
        //Check if the gpsTracker can get the location
        if(gpsTracker.canGetLocation()){
            //Get the longitude and latitude
            double lat = gpsTracker.getLatitude();
            double lon = gpsTracker.getLongitude();
            //Set the textview in the UI to the values of the longitude and latitude.
            la.setText(String.valueOf(lat));
            lo.setText(String.valueOf(lon));
            //Save the longitude and latitude values in the arrays.
            latitude[0] = lat;
            longitude[0] = lon;
            //We now can set GPSIsReady to true since we have coordinates.
            GPSIsReady = true;

        }else{
            gpsTracker.showSettingsAlert();
        }
    }

}





