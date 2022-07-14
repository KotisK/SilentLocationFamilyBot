package com.example.slfb;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient();
    private Context cntx;
    //This page is activity. it has 4 buttons that will take you to the relevant page within the app.
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        cntx = this;
        String res = getSharedPreferences("firebaseToken", MODE_PRIVATE).getString("fb", "null");
        if(getApplicationContext().getSharedPreferences("sentToDatabase", MODE_PRIVATE).getString("userSent", "null").equals("null"))
        {
            //When doing resource intensive operations, such as networking, we make use of a thread so that we dont overload the main thread.
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                //See if the username and password is valid.
                boolean checkIfSuccessfullLogin = sendTokenToServer(res);
                handler.post(() -> {
                    if(!checkIfSuccessfullLogin){
                        //The username or password was wrong.
                        Toast toast = Toast.makeText(cntx, "Something went wrong with the server...", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            });
        }

        //After Android version 11, you have to ask for location permission two times from the user.
        //The first time is for getting the users location when the app is active on the phone.
        //The other one for when the app is in the background.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Requiring permission")
                    .setMessage("This app requires access to location at all times. Please make sure to click that option")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(HomeActivity.this,
                                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                    0);
                        }
                    })
                    .create()
                    .show();
        }

        //Map all relevant stuff from UI to variables in code in order to manipulate them programatically.
        Button addPhoneButton = findViewById(R.id.addPhoneListButton);
        Button viewPhoneListButton = findViewById(R.id.viewPhoneListButton);
        Button editPhoneListButton = findViewById(R.id.editPhoneNumberButton);
        Button addLocationButton = findViewById(R.id.addLocationsButton);
        Button notifyUsersButton = findViewById(R.id.notifyButton);
        Button notifyUsersButton2 = findViewById(R.id.notifyButton2);

        //We change how "addLocationButton" button behaves when clicked. It should relocate you to the "AddLocation" page.
        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this , AddLocations.class);
                startActivity(intent);
            }
        });

        //We change how "addPhoneButton" button behaves when clicked. It should relocate you to the "AddPhoneNumberList" page.
        addPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this , AddPhoneNumberList.class);
                startActivity(intent);
            }
        });

        //We change how "viewPhoneListButton" button behaves when clicked. It should relocate you to the "ViewPhoneList" page.
        viewPhoneListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this , ViewPhoneList.class);
                startActivity(intent);
            }
        });

        //We change how "editPhoneListButton" button behaves when clicked. It should relocate you to the "EditPhoneList" page.
        editPhoneListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this , EditPhoneList.class);
                startActivity(intent);
            }
        });
        notifyUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this , NotifyLocations.class);
                startActivity(intent);
            }
        });
        notifyUsersButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this , RecieveNotification.class);
                startActivity(intent);
            }
        });
    }

    public boolean sendTokenToServer(String token){
        //Prepare the form data to be sent to the server. This is like a "form" in html. We have 3 inputs.
        //1. username 2. password and 3. a variable login_user which is set to "LOGIN".
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token",token)
                .addFormDataPart("username", getApplicationContext().getSharedPreferences("user", MODE_PRIVATE).getString("user","null"))
                .build();
        //Prepare the HTTP request by building it. We give it the URL to the server and we say it should be a "POST" type of request.
        Request request = new Request.Builder()
                .url("https://silentlocationfamilybot.000webhostapp.com/token.php")
                .post(requestBody)
                .build();
        try {
            //Execute the request by sending it and get the response from the server.
            Response response = client.newCall(request).execute();
            //System.out.println(Objects.requireNonNull(response.body()).string());
            //If the response is "s" we know the username and password exists in the database.
            if(Objects.requireNonNull(response.body()).string().equals("s")){
                SharedPreferences pref2 = getApplicationContext().getSharedPreferences("sentToDatabase", MODE_PRIVATE);
                SharedPreferences.Editor editor2 = pref2.edit();
                editor2.putString("userSent" , "true");
                editor2.commit();
                return true;
            }
            //If the response is not "s" we know the username or password does not exist in the database.
            else{

                return false;
            }
        } catch (IOException e) {
            //There was an error sending the data to the server.
        }
        return false;
    }
}