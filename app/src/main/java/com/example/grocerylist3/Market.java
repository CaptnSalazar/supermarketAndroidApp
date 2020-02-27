package com.example.grocerylist3;

import androidx.annotation.NonNull;

public class Market {
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

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
