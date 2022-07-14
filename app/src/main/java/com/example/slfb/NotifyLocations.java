package com.example.slfb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotifyLocations extends AppCompatActivity {
    private Context cntx;
    private String locations;
    private final OkHttpClient client = new OkHttpClient();
    private String token = "";
    private boolean fetchedUsernames = false;
    private List<String> allUsernames = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_locations);
        cntx = this;

        EditText username = findViewById(R.id.usernameEditText);
        Button setLocations = findViewById(R.id.setLocations);

        SharedPreferences locationsPref = getApplicationContext().getSharedPreferences("locations", MODE_PRIVATE);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
                    //See if the username and password is valid.
            allUsernames = new ArrayList<>(Arrays.asList(getAllUsernames().split(" ")));
                    handler.post(() -> {
                        fetchedUsernames = true;
                    });
                });

        //This is the default location. If the user chooses "All_locations" then it doesnt matter where the user is.

        //Here we loop through all of the saved locations that the user has saved in the SharedPreferences.
        if(locationsPref.getAll().size() > 0){
            Map<String,String> keys = (Map<String, String>) locationsPref.getAll();
            for(Map.Entry<String,String> entry : keys.entrySet()){
                //We add all of the locations from SharedPreferences into the local List "locations"
                locations += entry.getKey() + " ";
            }
        }

        setLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fetchedUsernames) {
                    Toast.makeText(cntx, "Fetching data from server, please hold on...", Toast.LENGTH_SHORT).show();
                } else if (locations.equals("")) {
                    Toast.makeText(cntx, "You have not added any locations! Please add them in the previous page.", Toast.LENGTH_SHORT).show();
                }
                else if(username.getText().toString().equals("")){
                    Toast.makeText(cntx, "Please fill in username...", Toast.LENGTH_SHORT).show();
                }
                else if(username.getText().toString().equals(getApplicationContext().getSharedPreferences("user", MODE_PRIVATE).getString("user","null"))){
                    Toast.makeText(cntx, "You cant add yourself!", Toast.LENGTH_SHORT).show();
                }
                else if(!allUsernames.contains(username.getText().toString())){
                    Toast.makeText(cntx, "User does not exist!", Toast.LENGTH_SHORT).show();
                }
                else {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());
                    executor.execute(() -> {
                        //See if the username and password is valid.
                        boolean checkIfSuccesfullyAddedFriend = putFriend(getApplicationContext().getSharedPreferences("user", MODE_PRIVATE).getString("user","null"),username.getText().toString());
                        handler.post(() -> {
                            if(checkIfSuccesfullyAddedFriend){
                                FirebaseMessaging.getInstance().subscribeToTopic(getApplicationContext().getSharedPreferences("user", MODE_PRIVATE).getString("user","null"))
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {}
                                        });
                                Toast.makeText(cntx, "Shared location with " + username.getText().toString(), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(NotifyLocations.this, HomeActivity.class);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(cntx, "Something went wrong! Try again later", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(NotifyLocations.this, HomeActivity.class);
                                startActivity(intent);
                            }

                        });
                    });
                }
            }
        });
    }

    public String getAllUsernames(){
        //Prepare the form data to be sent to the server. This is like a "form" in html. We have 3 inputs.
        //1. username 2. password and 3. a variable login_user which is set to "LOGIN".
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("fromApp", "yes")
                .build();
        //Prepare the HTTP request by building it. We give it the URL to the server and we say it should be a "POST" type of request.
        Request request = new Request.Builder()
                .url("https://silentlocationfamilybot.000webhostapp.com/getusernames.php")
                .post(requestBody)
                .build();
        try {
            //Execute the request by sending it and get the response from the server.
            Response response = client.newCall(request).execute();
            String res = response.body().string();
            return res;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean putFriend(String user, String friend){
        //Prepare the form data to be sent to the server. This is like a "form" in html. We have 3 inputs.
        //1. username 2. password and 3. a variable login_user which is set to "LOGIN".
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", user)
                .addFormDataPart("friends", friend)
                .build();
        //Prepare the HTTP request by building it. We give it the URL to the server and we say it should be a "POST" type of request.
        Request request = new Request.Builder()
                .url("https://silentlocationfamilybot.000webhostapp.com/setfriends.php")
                .post(requestBody)
                .build();
        try {
            //Execute the request by sending it and get the response from the server.
            Response response = client.newCall(request).execute();
            //System.out.println(Objects.requireNonNull(response.body()).string());
            String responseFromServer =  Objects.requireNonNull(response.body()).string().trim();
            System.out.println(responseFromServer);
            if(responseFromServer.equals("s")){
                return true;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}