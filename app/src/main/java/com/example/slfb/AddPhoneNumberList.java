package com.example.slfb;
//imports relevant libraries
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.ghyeok.stickyswitch.widget.StickySwitch;

public class AddPhoneNumberList extends AppCompatActivity {
    //Create the lists to save the phonenumbers, the times and if the user is whitelisted or blocklisted.
    private List<String> phoneNumbers = new ArrayList<>() , timeFrom = new ArrayList<>() , timeToo = new ArrayList<>();
    private EditText phone, fromTime, toTime;

    //This is the switch that changes from whitelist or blacklist. Inspired from https://androidrepo.com/repo/GwonHyeok-StickySwitch-android-button
    //This is for the Spinner (which is basically just a dropdown menu). This is where the user will choose what location.
    private Spinner spin;
    private List<String> locations = new ArrayList<>();

    private Context blockedPhoneContext;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_phone_number_list);
        Context context = this;

        blockedPhoneContext = this;

        //Creates a local storing place on the phone. We use it to save all black-listed phone numbers and the time for them.
        SharedPreferences pref = getApplicationContext().getSharedPreferences("phoneNumbers", MODE_PRIVATE) , locationsPref = getApplicationContext().getSharedPreferences("locations", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        //This is the default location. If the user chooses "All_locations" then it doesnt matter where the user is.
        locations.add("All_locations");

        //Here we loop through all of the saved locations that the user has saved in the SharedPreferences.
        if(locationsPref.getAll().size() > 0){
            Map<String,String> keys = (Map<String, String>) locationsPref.getAll();
            for(Map.Entry<String,String> entry : keys.entrySet()){
                //We add all of the locations from SharedPreferences into the local List "locations"
                locations.add(entry.getKey());
            }
        }

        //Map all relevant stuff from UI to variables in code in order to manipulate them programatically.
        fromTime = findViewById(R.id.fromTime);
        toTime = findViewById(R.id.tooTime);
        spin = findViewById(R.id.spinner);

        //We set an adapter to the Spinner. The layout of the Spinner we chose to be the default
        // "android.R.layout.simple.spinner.item" because we think it satisfies our needs for this simple list.
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,locations);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Connect the Spin with the default android adapter.
        spin.setAdapter(adapterSpinner);

        //We want to make sure that the user can only type the time in a specific way. This can be done by
        //using creating a InputFilter object and using its method "filter" to say how each character entered
        //in the input should be. So in our case, we require the input to be: xx:xx, where x = number 0-9.
        //Inspired by https://stackoverflow.com/a/13121759
        //We made some changes to fit our specific needs.
        InputFilter[] timeFilter = new InputFilter[1];
        timeFilter[0] = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                if (source.length() == 0) {
                    return null;// deleting, keep original editing
                }

                String result = "";
                result += dest.toString().substring(0, dstart);
                result += source.toString().substring(start, end);
                result += dest.toString().substring(dend, dest.length());

                if (result.length() > 5) {
                    return "";// do not allow this edit
                }

                boolean allowEdit = true;
                char c;
                //For the first character, it can only be 0-1-2. It makes no sense to have a time that
                //starts with 3 for example.
                if (result.length() > 0) {
                    c = result.charAt(0);
                    allowEdit &= (c >= '0' && c <= '2' && !(Character.isLetter(c)));
                }
                //For the second character, if the previous character was 0 or 1, then we allow all numbers 0-9.
                //If the previous character was 2, then we only allow up to 4. (25:00 makes no sense)
                if (result.length() > 1) {
                    c = result.charAt(1);
                    if (result.charAt(0) == '1' || result.charAt(0) == '0')
                        allowEdit &= (c >= '0' && c <= '9' && !(Character.isLetter(c)));
                    else {
                        allowEdit &= (c >= '0' && c <= '4' && !(Character.isLetter(c)));
                    }

                }

                //The third character has to always be ":"
                if (result.length() > 2) {
                    c = result.charAt(2);
                    allowEdit &= (c == ':' && !(Character.isLetter(c)));
                }

                //The fourth character can be 0-5, because seconds go from 0-59
                if (result.length() > 3) {
                    c = result.charAt(3);
                    allowEdit &= (c >= '0' && c <= '5' && !(Character.isLetter(c)));
                }

                //The fifth character can be 0-9, because seconds go from 0-59
                if (result.length() > 4) {
                    c = result.charAt(4);
                    allowEdit &= (c >= '0' && c <= '9' && !(Character.isLetter(c)));
                }

                return allowEdit ? null : "";
            }
        };

        toTime.setFilters(timeFilter);
        fromTime.setFilters(timeFilter);


        toTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 2) {
                    editable.append(':');
                }
            }
        });
        fromTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 2) {
                    editable.append(':');
                }
            }
        });


        //This is the listView from the UI.
        ListView lv = findViewById(R.id.addPhoneMenu);
        //We use a custom made adapter for this list because we want it to show in a specific way.
        //We want the phone number to be all the way to the left, the time all the way to the right
        //and the background color to be changed depending on if the user is blocked or not.
        CustomAdapter adapter = new CustomAdapter(this, phoneNumbers, timeFrom, timeToo);
        lv.setAdapter(adapter);

        Button openBlockPhoneNumber = findViewById(R.id.addPhoneNumbersToBlock);

        //This is the button to add a new phone number.
        openBlockPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone = findViewById(R.id.editTextPhone);
                fromTime = findViewById(R.id.fromTime);
                toTime = findViewById(R.id.tooTime);
                spin = findViewById(R.id.spinner);

                //Check if all the data has been put in by the user.
                if (!phone.getText().toString().equals("") && !fromTime.getText().toString().equals("") && !toTime.getText().toString().equals("")) {
                    if (!phoneNumbers.contains(phone.getText().toString())) {

                        //Concatenate the time-from with time-too and location into one string to store it in the shared preference.
                        String chosenLocation = spin.getSelectedItem().toString();
                        String totalInformation = fromTime.getText().toString() + " " + toTime.getText().toString() + " " + chosenLocation;

                        //Save the new added phone number to the SharedPreferences for phonenumbers.
                        editor.putString(String.valueOf(phone.getText()), totalInformation);
                        editor.apply();

                        Notifications object=new Notifications();
                        object.createNotificationChannel(blockedPhoneContext,phone.getText().toString());

                        //Add the information to the local lists to visually show the user that he has added phone numbers to the list by
                        //populating the list
                        phoneNumbers.add(phone.getText().toString());
                        timeFrom.add(fromTime.getText().toString());
                        timeToo.add(toTime.getText().toString());

                        //Reset the input fields after the user had added a phone number.
                        phone.setText("");
                        fromTime.setText("");
                        toTime.setText("");


                        //The array adapter signals the list that there has been changes made. Basically a refresh.
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast toast = Toast.makeText(context, "You have already added this phone number!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else {
                    Toast toast = Toast.makeText(context, "Make sure all fields are filled!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

    }

}