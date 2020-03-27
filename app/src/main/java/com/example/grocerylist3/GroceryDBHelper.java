package com.example.grocerylist3;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class GroceryDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "grocerylist3.db";
    public static final int DATABASE_VERSION = 6;

    public GroceryDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Overloading: when you have methods with the same name but different parameters are accepted. One use is to replace Python's optional parameters.
    public GroceryDBHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_GROCERYLIST_TABLE = "CREATE TABLE " +
                GroceryContract.GroceryEntry.TABLE_NAME + " (" +
                GroceryContract.GroceryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GroceryContract.GroceryEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                GroceryContract.GroceryEntry.COLUMN_TEMP_QUANTITY + " TEXT, " +
                GroceryContract.GroceryEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                GroceryContract.GroceryEntry.COLUMN_IN_LIST + " INTEGER DEFAULT 1, " +
                GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY + " INTEGER DEFAULT 0, " +
                GroceryContract.GroceryEntry.COLUMN_MARKET1_AISLE + " INTEGER DEFAULT -1, " +
                GroceryContract.GroceryEntry.COLUMN_MARKET2_AISLE + " INTEGER DEFAULT -1, " +
                GroceryContract.GroceryEntry.COLUMN_MARKET3_AISLE + " INTEGER DEFAULT -1, " +
                GroceryContract.GroceryEntry.COLUMN_MARKET4_AISLE + " INTEGER DEFAULT -1, " +
                GroceryContract.GroceryEntry.COLUMN_MARKET5_AISLE + " INTEGER DEFAULT -1" +
                ");";

        final String SQL_CREATE_SUPERMARKETS_VISITED_TABLE = "CREATE TABLE " +
                GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET + " (" +
                GroceryContract.SupermarketsVisited._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GroceryContract.SupermarketsVisited.COLUMN_IS_MARKET_SELECTED + " INTEGER, " +
                GroceryContract.SupermarketsVisited.COLUMN_MARKET_GROCERY_COLUMN + " INTEGER, " +
                GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME + " TEXT NOT NULL, " +
                GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION + " TEXT NOT NULL" +
                ");";

        db.execSQL(SQL_CREATE_GROCERYLIST_TABLE);
        db.execSQL(SQL_CREATE_SUPERMARKETS_VISITED_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + GroceryContract.GroceryEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET);
        onCreate(db);
    }
}
