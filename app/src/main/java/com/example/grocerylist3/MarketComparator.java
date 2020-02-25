package com.example.grocerylist3;

import java.util.Comparator;

public class MarketComparator implements Comparator<Market> {
    @Override
    public int compare(Market o1, Market o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
