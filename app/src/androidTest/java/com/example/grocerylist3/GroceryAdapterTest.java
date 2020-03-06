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

public class GroceryAdapterTest {
    GroceryAdapter adapter;
    GroceryDBHelper dbHelper;
    SQLiteDatabase database;
    Cursor cursorItems;

    String aisleNumOfItemsInSelectedMarket;

    String firstItem = "Aaapple";
    String secondItem = "Aabpple";
    String thirdItem = "Aacpple";
    String lastItem = "Zzzebra";

    int NUMBER_OF_SUPERMARKETS = 3;
    String newMarketName1 = "Kountdown";
    String newMarketLocation1 = "Capao";
    String newMarketName2 = "Kountdown";
    String newMarketLocation2 = "Porto Alegre";
    String newMarketName3 = "Pack And Save";
    String newMarketLocation3 = "Porto Alegre";

    final int SQL_TRUE = 1;
    final int SQL_FALSE = 0;

    @Before
    public void setUp() throws Exception {
            /* So when to use getContext() vs getTargetContext()?
The documentation doesn't do a great job of explaining the differences so here it is from my POV:
You know that when you do instrumentation tests on Android then you have two apps:
    The test app, that executes your test logic and tests your "real" app
    The "real" app (that your users will see)
So when you are writing your tests and you want to load a resource of your real app, use getTargetContext().
If you want to use a resource of your test app (e.g. a test input for one of your tests) then call getContext(). */
        //Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = new GroceryDBHelper(context);
        database = dbHelper.getWritableDatabase(); //Create and/or open a database that will be used for reading and writing.

        database.execSQL("DELETE FROM " + GroceryContract.GroceryEntry.TABLE_NAME);
        database.execSQL("DELETE FROM " + GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET);

        /* adding ITEMS to table */
        aisleNumOfItemsInSelectedMarket = "market1AisleLocation";

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


        String[] isInListSelectionArgs = new String[]{String.valueOf(SQL_TRUE)};
        cursorItems = database.query(
                GroceryContract.GroceryEntry.TABLE_NAME,
                null,
                GroceryContract.GroceryEntry.COLUMN_IN_LIST + " =?", //equivalent to WHERE. A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows for the given table.
                isInListSelectionArgs, //You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
                null,
                null,
                aisleNumOfItemsInSelectedMarket + " ASC, " + GroceryContract.GroceryEntry.COLUMN_NAME + " ASC"
        );

        adapter = new GroceryAdapter(cursorItems, context, GroceryAdapter.getSelectedMarketGroceryListColumnName(database));
    }

    @After
    public void tearDown() throws Exception {
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

        cursorItems.close();
        database.close();
    }

    @Test
    public void getItemPosition() {
        Integer expectedPosition1 = 0;
        Integer expectedPosition2 = cursorItems.getCount() - 1;

        Integer actualPosition1 = adapter.getItemPosition(firstItem);
        Integer actualPosition2 = adapter.getItemPosition(lastItem);

        assertEquals(expectedPosition1, actualPosition1);
        assertEquals(expectedPosition2, actualPosition2);
    }

    @Test
    public void getItemName() {
        String expectedItemName1 = firstItem;
        String expectedItemName2 = lastItem;

        int position1 = 0;
        int position2 = cursorItems.getCount() - 1;

        String actualItemName1 = adapter.getItemName(position1);
        String actualItemName2 = adapter.getItemName(position2);

        assertEquals(expectedItemName1, actualItemName1);
        assertEquals(expectedItemName2, actualItemName2);
    }

    @Test
    public void getInTrolleyValue() {
        ContentValues cv = new ContentValues();
        cv.put(GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY, SQL_TRUE);
        String[] mySelectionArgs = {firstItem};

        database.update(GroceryContract.GroceryEntry.TABLE_NAME,
                cv,
                GroceryContract.GroceryEntry.COLUMN_NAME + " =?",
                mySelectionArgs);

        int expectedInTrolley1 = 1;
        int expectedInTrolley2 = 0;

        int actualInTrolley1 = adapter.getInTrolleyValue(0);
        int actualInTrolley2 = adapter.getInTrolleyValue(cursorItems.getCount() - 1);

        assertEquals(expectedInTrolley1, actualInTrolley1);
        assertEquals(expectedInTrolley2, actualInTrolley2);
    }

    @Test
    public void getMarketsList() {
        List<Market> marketsList = adapter.getMarketsList(database);

        List<String> actualItemNames = new ArrayList<String>();
        int marketsListSize = marketsList.size();
        for (int i = 0; i < marketsListSize; i++) {
            actualItemNames.add(marketsList.get(i).toString());
        }

        List<String> expectedItemNames = new ArrayList<String>();
        expectedItemNames.add(newMarketName1 + " (" + newMarketLocation1 + ")");
        expectedItemNames.add(newMarketName2 + " (" + newMarketLocation2 + ")");
        expectedItemNames.add(newMarketName3 + " (" + newMarketLocation3 + ")");

        assertEquals(expectedItemNames, actualItemNames);
    }

    @Test
    public void getPositionMarketSelected() {

    }

    @Test
    public void getSelectedMarketGroceryListColumnName() {
        String expetedColumnName1 = "market3AisleLocation";
        String actualColumnName1 = GroceryAdapter.getSelectedMarketGroceryListColumnName(database);

        assertEquals(expetedColumnName1, actualColumnName1);
    }

    @Test
    public void getNewestGroceryListColumnName() {
        String expectedColumnName1 = "market" + 4 + "AisleLocation";
        String actualColumnName1 = "market" + GroceryAdapter.getNewestGroceryListColumnNumber(database) + "AisleLocation";

        String[] marketNamesSelectionArgs2 = new String[]{newMarketName1, newMarketLocation1};
        database.delete(
                GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET,
                        "(" + GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME + " =? AND " +
                        GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION + " =?)",
                marketNamesSelectionArgs2);
        String expectedColumnName2 = "market" + 4 + "AisleLocation";
        String actualColumnName2 = "market" + GroceryAdapter.getNewestGroceryListColumnNumber(database) + "AisleLocation";

        String[] marketNamesSelectionArgs3 = new String[]{newMarketName3, newMarketLocation3};
        database.delete(
                GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET,
                "(" + GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME + " =? AND " +
                        GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION + " =?)",
                marketNamesSelectionArgs3);
        String expectedColumnName3 = "market" + 3 + "AisleLocation";
        String actualColumnName3 = "market" + GroceryAdapter.getNewestGroceryListColumnNumber(database) + "AisleLocation";

        String[] marketNamesSelectionArgs4 = new String[]{newMarketName2, newMarketLocation2};
        database.delete(
                GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET,
                "(" + GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME + " =? AND " +
                        GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION + " =?)",
                marketNamesSelectionArgs4);
        String expectedColumnName4 = "market" + 1 + "AisleLocation";
        String actualColumnName4 = "market" + GroceryAdapter.getNewestGroceryListColumnNumber(database) + "AisleLocation";

        assertEquals(expectedColumnName1, actualColumnName1);
        assertEquals(expectedColumnName2, actualColumnName2);
        assertEquals(expectedColumnName3, actualColumnName3);
        assertEquals(expectedColumnName4, actualColumnName4);
    }

}