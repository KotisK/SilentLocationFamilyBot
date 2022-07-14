package com.example.slfb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//This activity shows a list where you can see all of the phonenumbers you have added.
public class ViewPhoneList extends AppCompatActivity {
    //Local list variables that contain the phone numbers, times and whitelist/blocklist
    private final List<String> phoneNumbers = new ArrayList<>() , timeFrom = new ArrayList<>() , timeToo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_phone_list);

        //Map all relevant stuff from UI to variables in code in order to manipulate them programatically.
        ListView lv = findViewById(R.id.viewPhoneList);

        //SharedPreferences is an inbuild class in android that lets you store values locally on the phone.
        //It stores them as a map (<key,value>).
        SharedPreferences pref = getApplicationContext().getSharedPreferences("phoneNumbers", MODE_PRIVATE);

        //Here we loop through all of the saved locations that the user has saved in the SharedPreferences.
        if(pref.getAll().size() > 0){
            Map<String,String> keys = (Map<String, String>) pref.getAll();
            for(Map.Entry<String,String> entry : keys.entrySet()){
                //We add all of the locations from SharedPreferences into the local List "locations"
                phoneNumbers.add(entry.getKey());

                //Since the "key" is the phone number, the "value" is the timeFrom, timeToo and whitelist/blacklist.
                //The value is a single string with 3 data and are separated by a space.
                //We split the string on space and get a string array where: 0=> timeFrom , 1=> timeToo , 2=> whitelist/blacklist
                String time = entry.getValue();
                String[] split = time.split("\\s+");

                //Store the data into the local lists.
                timeFrom.add(split[0]);
                timeToo.add(split[1]);
            }
        }
        //We use a custom made adapter for this list because we want it to show in a specific way.
        //We want the phone number to be all the way to the left, the time all the way to the right
        //and the background color to be changed depending on if the user is blocked or not.
        CustomAdapter adapter=new CustomAdapter(this, phoneNumbers, timeFrom, timeToo);
        lv.setAdapter(adapter);

    }
}
