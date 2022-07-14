package com.example.slfb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditSelectedPhoneNumber extends AppCompatActivity {
    private String phoneNumber , timeFrom, timeToo, location;
    private EditText phoneNumberEditText,timeFromEditText,timeTooEditText;
    private Spinner locationSpinner;
    private Button applyChangesButton;
    private final List<String> locations = new ArrayList<>();
    private Context cntx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_selected_number);
        cntx = this;

        //Here we retrieve the information about the phone number we want to edit (phonenumber, time, location)
        Bundle extras = getIntent().getExtras();
        phoneNumber = extras.getString("phoneNumber");
        timeFrom = extras.getString("timeFrom");
        timeToo = extras.getString("timeToo");
        location = extras.getString("location");

        phoneNumberEditText = findViewById(R.id.editTextPhone2);
        timeFromEditText = findViewById(R.id.fromTime2);
        timeTooEditText = findViewById(R.id.tooTime2);
        locationSpinner = findViewById(R.id.spinnerLocationsRecieved);
        applyChangesButton = findViewById(R.id.editPhoneProfile);

        //We show the user the default settings of the profile they want to edit
        phoneNumberEditText.setText(phoneNumber);
        timeFromEditText.setText(timeFrom);
        timeTooEditText.setText(timeToo);

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

        //We set an adapter to the Spinner. The layout of the Spinner we chose to be the default
        // "android.R.layout.simple.spinner.item" because we think it satisfies our needs for this simple list.
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,locations);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Connect the Spin with the default android adapter.
        locationSpinner.setAdapter(adapterSpinner);

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

        timeFromEditText.setFilters(timeFilter);
        timeTooEditText.setFilters(timeFilter);


        timeFromEditText.addTextChangedListener(new TextWatcher() {
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
        timeTooEditText.addTextChangedListener(new TextWatcher() {
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

        //Here we get the position of the dropdown menu (also known as Spinner) that matches the default value of location
        //prior to potentially editing it by the user.
        int spinnerPosition = -1;
        for (int i=0;i<locationSpinner.getCount();i++){
            if (locationSpinner.getItemAtPosition(i).toString().equalsIgnoreCase(location)){
                spinnerPosition = i;
            }
        }
        locationSpinner.setSelection(spinnerPosition);
        adapterSpinner.notifyDataSetChanged();

        //When the user is satisfied with the changes and presses the button to finish the edit, this method will fire.
        applyChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNumberEditText = findViewById(R.id.editTextPhone2);
                timeFromEditText = findViewById(R.id.fromTime2);
                timeTooEditText = findViewById(R.id.tooTime2);
                locationSpinner = findViewById(R.id.spinnerLocationsRecieved);
                applyChangesButton = findViewById(R.id.editPhoneProfile);

                //Check if all the data has been put in by the user.
                if (!phoneNumberEditText.getText().toString().equals("") && !timeFromEditText.getText().toString().equals("") && !timeTooEditText.getText().toString().equals("")) {
                        //Concatenate the time-from with time-too and location into one string to store it in the shared preference.
                        String chosenLocation = locationSpinner.getSelectedItem().toString();
                        String totalInformation = timeFromEditText.getText().toString() + " " + timeTooEditText.getText().toString() + " " + chosenLocation;

                        //Save the new added phone number to the SharedPreferences for phonenumbers.
                        editor.putString(String.valueOf(phoneNumberEditText.getText()), totalInformation);

                        //If the user has changed the phonenumber, then we need to delete the previous phone number since
                        //phone number is acting like a unique ID for us in shared preferences. If the user did not change
                        //the phone number then we dont need to delete it.
                        if(!phoneNumber.equals(String.valueOf(phoneNumberEditText.getText()))){
                            editor.remove(phoneNumber);
                        }
                        editor.apply();

                        Intent intent = new Intent(EditSelectedPhoneNumber.this , EditPhoneList.class);
                        startActivity(intent);

                    }
                else {
                    Toast toast = Toast.makeText(cntx, "Make sure all fields are filled!", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        });
    }
}
