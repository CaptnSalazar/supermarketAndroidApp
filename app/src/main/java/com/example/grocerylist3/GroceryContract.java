package com.example.grocerylist3;

import android.provider.BaseColumns;

public class GroceryContract {

    private GroceryContract() {}

    public static final class GroceryEntry implements BaseColumns {
        public static final String TABLE_NAME = "groceryList3";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_AISLE = "aisle";
    }
}
