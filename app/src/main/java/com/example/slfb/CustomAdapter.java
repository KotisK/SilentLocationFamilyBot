package com.example.slfb;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

//This class extends "ArrayAdapter" class. We want to customize how the items will populate the listview.
public class CustomAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private List<String> phoneNumber = new ArrayList<>() , timeFrom = new ArrayList<>() , timeToo = new ArrayList<>(), blocked = new ArrayList<>();

    //Constructor where we set all local variables with the parameters passed from the caller.
    public CustomAdapter(Activity context, List<String> phoneNumber, List<String> timeFrom , List<String> timeToo) {
        super(context, R.layout.custom_list_view, phoneNumber);
        this.context = context;
        this.phoneNumber = phoneNumber;
        this.timeFrom = timeFrom;
        this.timeToo = timeToo;
    }

    //Populate the list with phone numbers and times FOR EACH ROW
    //Followed this guide to populate the list https://developer.android.com/reference/android/widget/ArrayAdapter#getView(int,%20android.view.View,%20android.view.ViewGroup)
    public View getView(int position, View view, ViewGroup parent) {
        //Auto generated code for inflating the context.
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.custom_list_view, null,true);

        //get the textviews from the UI.
        TextView phoneNumberTextView = rowView.findViewById(R.id.phoneNumberListView);
        TextView timeTooTextView = rowView.findViewById(R.id.timeFrom);
        TextView timeFromTextView = rowView.findViewById(R.id.timeToo);

        //Since we are looping through all of the list items, we have the parameter "position".
        //This position will go from 0 to the size of the items we have.
        //We set the text of each textview with the specific phonenumber,timefrom and timetoo.
        phoneNumberTextView.setText(this.phoneNumber.get(position));
        timeTooTextView.setText(this.timeFrom.get(position));
        timeFromTextView.setText(this.timeToo.get(position));

        return rowView;

    }
}
