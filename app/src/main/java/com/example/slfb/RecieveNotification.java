package com.example.slfb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecieveNotification extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient();
    Context cntx;
    boolean ready = false;
    private List<String> usernames = new ArrayList<String>();
    boolean serverError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_give_notification);
        cntx = this;

        EditText username = findViewById(R.id.usernameEditTextRecieve);
        Button getLocations = findViewById(R.id.getLocations);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //See if the username and password is valid.
            String usernamesAsString = getAllUsernames();
            handler.post(() -> {
                usernames = new ArrayList<>(Arrays.asList(usernamesAsString.split(" ")));
                ready = true;
            });
        });

        getLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(username.getText().toString().equals("")){
                    Toast.makeText(cntx , "Please fill in username..." , Toast.LENGTH_SHORT).show();
                }
                else if (!ready){
                    Toast.makeText(cntx , "Fetching data from server, please wait..." , Toast.LENGTH_SHORT).show();
                }
                else if (!usernames.contains(username.getText().toString())){
                    Toast.makeText(cntx , "Username does not exist!" , Toast.LENGTH_SHORT).show();
                }
                else if(username.getText().toString().equals(getApplicationContext().getSharedPreferences("user", MODE_PRIVATE).getString("user","null"))){
                    Toast.makeText(cntx , "Cant add yourself..." , Toast.LENGTH_SHORT).show();
                }
                else{
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());
                    executor.execute(() -> {
                        //See if the username and password is valid.
                        boolean allowed = isUserAllowed(getApplicationContext().getSharedPreferences("user", MODE_PRIVATE).getString("user","null"),username.getText().toString());
                        handler.post(() -> {
                            if(allowed){
                                FirebaseMessaging.getInstance().subscribeToTopic(username.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {}
                                        });
                                Intent intent = new Intent(RecieveNotification.this, HomeActivity.class);
                                startActivity(intent);
                            }
                            else if(!serverError){
                                Toast.makeText(cntx,"The user has not shared location with you!",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(cntx,"The server is currently unreachable. Try later.",Toast.LENGTH_SHORT).show();
                            }

                        });
                    });
                }
            }
        });

    }

    public boolean isUserAllowed(String currentUser , String toUser){
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("from", currentUser)
                .addFormDataPart("to", toUser)
                .build();
        //Prepare the HTTP request by building it. We give it the URL to the server and we say it should be a "POST" type of request.
        Request request = new Request.Builder()
                .url("https://silentlocationfamilybot.000webhostapp.com/getlocations.php")
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String res = response.body().string().trim();
            System.out.println(res);
            if(res.equals("s")){
                serverError = false;
                return true;
            }
            serverError = false;
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        serverError = true;
        return false;
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
            return response.body().string();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}