package com.example.grocerylist3;

import androidx.annotation.NonNull;

public class Market {
    private String name;
    private Integer ID;

    public Market(String name, Integer ID) {
        this.name = name;
        this.ID = ID;
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

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
