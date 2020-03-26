package com.example.grocerylist3;

import android.provider.BaseColumns;

public class GroceryContract {

    private GroceryContract() {}

    public static final class GroceryEntry implements BaseColumns {
        public static final String TABLE_NAME = "groceryList3";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TEMP_QUANTITY = "temporaryQuantity";
        public static final String COLUMN_IN_LIST = "isInList";
        public static final String COLUMN_IN_TROLLEY = "isInTrolley";
        public static final String COLUMN_TIMESTAMP = "times";
        public static final String COLUMN_MARKET1_AISLE = "market1AisleLocation";
        public static final String COLUMN_MARKET2_AISLE = "market2AisleLocation";
        public static final String COLUMN_MARKET3_AISLE = "market3AisleLocation";
        public static final String COLUMN_MARKET4_AISLE = "market4AisleLocation";
        public static final String COLUMN_MARKET5_AISLE = "market5AisleLocation";
    }

    public static final class SupermarketsVisited implements BaseColumns {
        public static final String TABLE_NAME_MARKET = "supermarkets";
        public static final String COLUMN_MARKET_GROCERY_COLUMN = "groceryListColumnNumber"; // i.e. the relative ID of the supermarkets
        public static final String COLUMN_MARKET_NAME = "supermarketName";  //e.g. new world
        public static final String COLUMN_MARKET_LOCATION = "supermarketLocation";  //e.g. islam
        public static final String COLUMN_IS_MARKET_SELECTED = "isMarketSelected";  //e.g. islam
    }
}
