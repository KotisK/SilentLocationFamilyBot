package com.example.slfb;
//automatic import of button libraries
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditPhoneList extends AppCompatActivity {
    //these will hold all of the time from and time to  numbers which we get from the database.  it goes through all of the phone numbers
    //and saves them locally on the 3 lists mentioned above.
    //the key is the phone number and the value is the time.
    private final List<String> phoneNumbers = new ArrayList<>(), timeFrom = new ArrayList<>(), timeToo = new ArrayList<>(),location = new ArrayList<>();
    private Context cntx;
    private int choosenId = -1;
    private String choosenNumber = "", choosenTimeFrom = "" , choosenTimeToo="", choosenLocation = "";
    private CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_phone_list);
        cntx = this;

        //the first button to edit a phone number in the list and the button to delete a number from the list these codes bind the two buttons with variables
        Button EditPhoneNumberButton = findViewById(R.id.EditPhoneNumberButton);
        Button DeletePhoneNumberButton = findViewById(R.id.DeletePhoneNumberButton);

        //This code binds a variable to the list
        ListView PhoneList = findViewById(R.id.locationsAdded);

        //Takes the Shared preferences. MODE_PRIVATE means only this app can view the phone number list. Shared prefferences is a text file created on your phone. Almost like a databse
        SharedPreferences SP = getApplicationContext().getSharedPreferences("phoneNumbers", MODE_PRIVATE);

        updateList(SP);

        //This code explains how each item should appear on the list. In this case, we instruct the phone number to go to the left
        //And the time to go to the right in the list.

        adapter = new CustomAdapter(this, phoneNumbers, timeFrom, timeToo);
        PhoneList.setAdapter(adapter);

        DeletePhoneNumberButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CommitPrefEdits")
            @Override
            public void onClick(View view) {
                if(choosenId == -1){
                    Toast.makeText(cntx, "Please choose an item in the list to delete...", Toast.LENGTH_SHORT).show();
                }
                else{
                    System.out.println(choosenId);
                    System.out.println(choosenNumber);
                    SharedPreferences.Editor editor = SP.edit();
                    editor.remove(choosenNumber);
                    editor.commit();
                    updateList(SP);
                    adapter.notifyDataSetChanged();

                }
            }
        });
        EditPhoneNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EditPhoneList.this, EditSelectedPhoneNumber.class);
                i.putExtra("phoneNumber",choosenNumber);
                i.putExtra("timeFrom",choosenTimeFrom);
                i.putExtra("timeToo",choosenTimeToo);
                i.putExtra("location" , choosenLocation);
                startActivity(i);
            }
        });
        PhoneList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                choosenId = i;
                choosenNumber = PhoneList.getItemAtPosition(i).toString();
                choosenTimeFrom = timeFrom.get(i);
                choosenTimeToo = timeToo.get(i);
                choosenLocation = location.get(i);
            }
        });
    }
    public void updateList(SharedPreferences SP){
        //loop through everything because the user has multiple phone numbers.
        //gets all the numbers in the data list
        phoneNumbers.clear();
        timeFrom.clear();
        timeToo.clear();
        if (SP.getAll().size() > 0) {
            Map<String, String> keys = (Map<String, String>) SP.getAll();
            //loops all of the keys. gives you a list of all of the keys. For every entry do the above.
            //to loop through data means to go through them one at a time and apply commands over and over differently for each
            for (Map.Entry<String, String> entry : keys.entrySet()) {
                //save the phone numbers in the list
                phoneNumbers.add(entry.getKey());
                //save the time in the list
                String time = entry.getValue();

                //splits the time for the interface to say from one time to another because we insert 2 time values. One from the beginning of when
                //the block takes place the other from until when
                //regular expression AKA regex its a way of manipulating strings
                //so I say split it where there is a space so \\s+ s for space means find a space and then seperate it time from and time to (calls.java)
                //its yellow cuz it recognises it as a regex
                //timesplit 0 timefrom timesplit 1 timetoo

                String[] split = time.split("\\s+");

                timeFrom.add(split[0]);
                timeToo.add(split[1]);
                location.add(split[2]);
            }

        }
    }
}