package com.example.grocerylist3;

import android.util.Log;

import androidx.annotation.NonNull;

public class Market {
    private static final String TAG = "Market&&&&&&&&";

    private String name;
    private String location;
    private Integer ID; //We need the ID if we want to change/update the name or location of a supermarket. SQLite ID starts at 1, not 0.
    private boolean isSelected;


    public Market(String name, String location, Integer ID, boolean isSelected) {
        this.name = name;
        this.location = location;
        this.ID = ID;
        this.isSelected = isSelected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean checkIsSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    static public String[] extractNameAndLocation(String nameWithLocation) {
        //MAKE SURE THE USER CANNOT INPUT '(' AS IN MARKET NAME OR LOCATION OTHERWISE THIS WILL HAVE BUGS.
        //Log.d(TAG, "extractNameAndLocation: ");
        //by using double quotes you create String constant ("p"), while with single quotes it's a char constant ('p').
        int i = 0;
        for (; nameWithLocation.charAt(i) != '('; i++) {
            //if condition holds, then we increment i, otherwise exit for loop.
        }
        String name = nameWithLocation.substring(0, i-1);
        String location = nameWithLocation.substring(i+1, nameWithLocation.length()-1);
        //Log.d(TAG, "extractNameAndLocation:  market name is: " + name + ", and location is: " + location);
        String[] nameAndLocation = {name, location};
        return nameAndLocation;
    }

    @NonNull
    @Override
    public String toString() {
        return name + " (" + location + ")";
    }
}
