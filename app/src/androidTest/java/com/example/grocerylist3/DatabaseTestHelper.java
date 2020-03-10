package com.example.grocerylist3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DatabaseTestHelper {
    public static final String TEST_DATABASE_NAME = "grocerylist3test.db";

    public static String firstItem = "Aaapple";
    public static String secondItem = "Aabpple";
    public static String thirdItem = "Aacpple";
    public static String lastItem = "Zzzebra";

    public static String newMarketName1 = "Kountdown";
    public static String newMarketLocation1 = "Capao";
    public static String newMarketName2 = "Kountdown";
    public static String newMarketLocation2 = "Porto Alegre";
    public static String newMarketName3 = "Pack And Save";
    public static String newMarketLocation3 = "Porto Alegre";

    public static final int SQL_TRUE = 1;
    public static final int SQL_FALSE = 0;


    static public void deleteAllTables(SQLiteDatabase database) {
        String[] itemNameSelectionArgs = new String[]{firstItem, secondItem, thirdItem, lastItem};
        database.delete(
                GroceryContract.GroceryEntry.TABLE_NAME,
                GroceryContract.GroceryEntry.COLUMN_NAME + " =? OR " +
                        GroceryContract.GroceryEntry.COLUMN_NAME + " =? OR " +
                        GroceryContract.GroceryEntry.COLUMN_NAME + " =? OR " +
                        GroceryContract.GroceryEntry.COLUMN_NAME + " =?",
                itemNameSelectionArgs);

        String[] marketNamesSelectionArgs = new String[]{newMarketName1, newMarketLocation1, newMarketName2, newMarketLocation2, newMarketName3, newMarketLocation3};
        database.delete(
                GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET,
                "(" + GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME + " =? AND " +
                        GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION + " =?) OR " +
                        "(" + GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME + " =? AND " +
                        GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION + " =?) OR " +
                        "(" + GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME + " =? AND " +
                        GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION + " =?)",
                marketNamesSelectionArgs);
    }


    static public void populateTableGroceryList(SQLiteDatabase database) {
        database.execSQL("DELETE FROM " + GroceryContract.GroceryEntry.TABLE_NAME);

        /* adding ITEMS to groceryList table */
        ContentValues cvItem1 = new ContentValues();
        cvItem1.put(GroceryContract.GroceryEntry.COLUMN_NAME, firstItem);
        database.insert(GroceryContract.GroceryEntry.TABLE_NAME, null, cvItem1); //Must insert one row at a time.

        ContentValues cvItem2 = new ContentValues();
        cvItem2.put(GroceryContract.GroceryEntry.COLUMN_NAME, secondItem);
        database.insert(GroceryContract.GroceryEntry.TABLE_NAME, null, cvItem2); //Must insert one row at a time.

        ContentValues cvItem3 = new ContentValues();
        cvItem3.put(GroceryContract.GroceryEntry.COLUMN_NAME, thirdItem);
        database.insert(GroceryContract.GroceryEntry.TABLE_NAME, null, cvItem3); //Must insert one row at a time.

        ContentValues cvItem4 = new ContentValues();
        cvItem4.put(GroceryContract.GroceryEntry.COLUMN_NAME, lastItem);
        database.insert(GroceryContract.GroceryEntry.TABLE_NAME, null, cvItem4); //Must insert one row at a time.
    }


    static public void populateTableSupermarkets(SQLiteDatabase database) {
        database.execSQL("DELETE FROM " + GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET);

        /* adding SUPERMARKETS to table */
        Integer lowestUnusedColumnNumber = 1;

        ContentValues cvMarket1 = new ContentValues();
        cvMarket1.put(GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME, newMarketName1);
        cvMarket1.put(GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION, newMarketLocation1);
        cvMarket1.put(GroceryContract.SupermarketsVisited.COLUMN_IS_MARKET_SELECTED, SQL_FALSE);
        cvMarket1.put(GroceryContract.SupermarketsVisited.COLUMN_MARKET_GROCERY_COLUMN, lowestUnusedColumnNumber++);
        database.insert(GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET, null, cvMarket1);

        ContentValues cvMarket2 = new ContentValues();
        cvMarket2.put(GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME, newMarketName2);
        cvMarket2.put(GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION, newMarketLocation2);
        cvMarket2.put(GroceryContract.SupermarketsVisited.COLUMN_IS_MARKET_SELECTED, SQL_FALSE);
        cvMarket2.put(GroceryContract.SupermarketsVisited.COLUMN_MARKET_GROCERY_COLUMN, lowestUnusedColumnNumber++);
        database.insert(GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET, null, cvMarket2);

        ContentValues cvMarket3 = new ContentValues();
        cvMarket3.put(GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME, newMarketName3);
        cvMarket3.put(GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION, newMarketLocation3);
        cvMarket3.put(GroceryContract.SupermarketsVisited.COLUMN_IS_MARKET_SELECTED, SQL_TRUE);
        cvMarket3.put(GroceryContract.SupermarketsVisited.COLUMN_MARKET_GROCERY_COLUMN, lowestUnusedColumnNumber++);
        database.insert(GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET, null, cvMarket3);
    }


    static public Cursor getGroceryListCursor(SQLiteDatabase database, String selectedMarketGroceryListColumnName) {
        String[] isInListSelectionArgs = new String[]{String.valueOf(SQL_TRUE)};
        return database.query(
                GroceryContract.GroceryEntry.TABLE_NAME,
                null,
                GroceryContract.GroceryEntry.COLUMN_IN_LIST + " =?", //equivalent to WHERE. A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows for the given table.
                isInListSelectionArgs, //You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
                null,
                null,
                selectedMarketGroceryListColumnName + " ASC, " + GroceryContract.GroceryEntry.COLUMN_NAME + " ASC"
        );
    }
}
