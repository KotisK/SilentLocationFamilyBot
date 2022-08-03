package com.example.slfb;
//import relevant libraries.
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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


//This is the class for the first page in the application. Each such application page class will extend
//"AppCompatActivity" which is a baseclass for applications.



public class MainActivity extends AppCompatActivity {
    //create OkHttpClient object to connect to the server
    private final OkHttpClient client = new OkHttpClient();
    //boolean variable to see if there wa a connection error to the server. If its true, we inform the user that
    //the server is currently unreachable and should try later.
    private boolean connectionError = false;
    //Declare the EditText variables
    private EditText username, password;
    //Declare the TextView variable
    private TextView signUp;
    //Declare the Button variable
    private Button loginButton;

    //Declare a string to hold the family role
    private String familyRole = "";

    ActivityResultLauncher<Intent> askForRoleManagerAsCallScreener = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
            });

    //This is a method that is from the AppCompatActivity base class that we override.
    //Here we tell the application what to do when the page is created.
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = this;

        //REMOVE ME
        getApplicationContext().getSharedPreferences("sentToDatabase", MODE_PRIVATE).edit().clear().commit();


        //Require all permissions needed for this application to work programmatically.
        requestTheNececcasryRequirements();

        //Map all relevant stuff from UI to variables in code in order to manipulate them programatically.
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        signUp = findViewById(R.id.signUp);
        loginButton = findViewById(R.id.loginButton);

        //setOnClickListener creates a listener on the login button. When it is clicked by a user, we check that the username or password field is not empty.
        //If it is not, we proceed to send this to the server for verification. If it is empty, we notify the user.
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Get the entered username and password from the login page.
                String usernameString = username.getText().toString(), passwordString = password.getText().toString();
                //Check if the username or password is empty.
                if (usernameString.equals("") || passwordString.equals("")) {
                    //Create a Toast instance, which is used to create text to notify the user of something.
                    Toast toast = Toast.makeText(context, "Either the entered username or password was empty!", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    //When doing resource intensive operations, such as networking, we make use of a thread so that we dont overload the main thread.
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());

                    executor.execute(() -> {
                        //See if the username and password is valid.
                        boolean checkIfSuccessfullLogin = logIn(usernameString,passwordString);
                        handler.post(() -> {
                            if(checkIfSuccessfullLogin){
                                //The username and password was valid. We can now go to the next page "HomeActivity".
                                SharedPreferences pref = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("user" , username.getText().toString());
                                editor.apply();
                                //Save the family role in a shared preference to know what role the current user has.
                                SharedPreferences pref2 = getApplicationContext().getSharedPreferences("role", MODE_PRIVATE);
                                SharedPreferences.Editor editor2 = pref2.edit();
                                editor2.putString("role" , familyRole);
                                editor2.apply();

                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }
                            else if(connectionError){
                                //The server is not reachable or there was an error.
                                Toast toast = Toast.makeText(context, "The server is currently unreachable. Try again later.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                            else{
                                //The username or password was wrong.
                                Toast toast = Toast.makeText(context, "Wrong username or password given! Try again.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    });
                }
            }
        });

        //onClickListener for "signup" button. When its pressed we want to send the user to the website for regestering an account.
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Take the user to the website for registering an account.
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://silentlocationfamilybot.000webhostapp.com/register.php"));
                startActivity(browserIntent);
            }
        });
    }

    //Method to send the inputs to username and password that the user has written in. This is to see if the login credentials are valid. The server
    //either responds with "s" for success or "f" for failure.
    private boolean logIn(String username,String password) {
        //Prepare the form data to be sent to the server. This is like a "form" in html. We have 3 inputs.
        //1. username 2. password and 3. a variable login_user which is set to "LOGIN".
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username",username)
                .addFormDataPart("password", password)
                .addFormDataPart("login_user","LOGIN")
                .build();
        //Prepare the HTTP request by building it. We give it the URL to the server and we say it should be a "POST" type of request.
        Request request = new Request.Builder()
                .url("https://silentlocationfamilybot.000webhostapp.com/server.php")
                .post(requestBody)
                .build();
        try {
            //Execute the request by sending it and get the response from the server.
            Response response = client.newCall(request).execute();
            //If the response is "f" we know the username and password does not exist in the database.
            String role = Objects.requireNonNull(response.body()).string();
            if(role.equals("f")){
                connectionError = false;
                return false;
            }
            //If the response is not "f" we know the username or password does exist in the database.
            else{
                familyRole = role;
                connectionError = false;
                return true;
            }
        } catch (IOException e) {
            //There was an error sending the data to the server.
            connectionError = true;
        }
        return false;
    }
    //This method is to make sure we have all of the permissions we need for this app to work.
    //We need: internet access, GPS access and access to make changes to the phones calling service.
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestTheNececcasryRequirements() {
        RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);

        Intent intent = Objects.requireNonNull(roleManager).createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);

        askForRoleManagerAsCallScreener.launch(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.MODIFY_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE
                }, 0);
            }
        }

    }
}


