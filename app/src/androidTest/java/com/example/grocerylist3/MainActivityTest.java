package com.example.grocerylist3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    //private Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void onCreate() {
        Context context = ApplicationProvider.getApplicationContext();

        final Integer SQL_TRUE = 1;
        String aisleNumOfItemsInSelectedMarket = "market1AisleLocation";
        GroceryDBHelper dbHelper = new GroceryDBHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase(); //Create and/or open a database that will be used for reading and writing.

        String expectedName1 = "Aaapple";
        String expectedName2 = "Zzzebra";

        ContentValues cv = new ContentValues();
        cv.put(GroceryContract.GroceryEntry.COLUMN_NAME, expectedName1);
        cv.put(GroceryContract.GroceryEntry.COLUMN_NAME, expectedName2);
        database.insert(GroceryContract.GroceryEntry.TABLE_NAME, null, cv);

        String[] isInListSelectionArgs = new String[]{String.valueOf(SQL_TRUE)};
        Cursor cursor = database.query(
                GroceryContract.GroceryEntry.TABLE_NAME,
                null,
                GroceryContract.GroceryEntry.COLUMN_IN_LIST + " =?", //equivalent to WHERE. A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows for the given table.
                isInListSelectionArgs, //You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
                null,
                null,
                aisleNumOfItemsInSelectedMarket + " ASC, " + GroceryContract.GroceryEntry.COLUMN_NAME + " ASC"
        );

        cursor.moveToFirst();  //YOU ALWAYS FORGET YOU NEED TO MOVE THE CURSOR BEFORE GETTING A VALUE FROM IT.
        String actualName1 = cursor.getString(cursor.getColumnIndex(GroceryContract.GroceryEntry.COLUMN_NAME));
        cursor.moveToLast();  //YOU ALWAYS FORGET YOU NEED TO MOVE THE CURSOR BEFORE GETTING A VALUE FROM IT.
        String actualName2 = cursor.getString(cursor.getColumnIndex(GroceryContract.GroceryEntry.COLUMN_NAME));

        String[] itemNameSelectionArgs = new String[]{expectedName1, expectedName2};
        database.delete(
                GroceryContract.GroceryEntry.TABLE_NAME,
                GroceryContract.GroceryEntry.COLUMN_NAME + " =? OR " +
                        GroceryContract.GroceryEntry.COLUMN_NAME + " =?",
                itemNameSelectionArgs);

        cursor.close();
        database.close();

        assertEquals(expectedName1, actualName1);
        assertEquals(expectedName2, actualName2);
    }
}