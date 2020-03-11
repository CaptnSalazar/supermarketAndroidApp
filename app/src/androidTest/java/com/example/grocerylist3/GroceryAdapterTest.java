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
    SQLiteDatabase testDatabase;
    Cursor cursorItems;

    String selectedMarketGroceryListColumnName;

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
        dbHelper = new GroceryDBHelper(context, DatabaseTestHelper.TEST_DATABASE_NAME);
        testDatabase = dbHelper.getWritableDatabase(); //Create and/or open a testDatabase that will be used for reading and writing.

        DatabaseTestHelper.populateTableGroceryList(testDatabase);

        DatabaseTestHelper.populateTableSupermarkets(testDatabase);

        selectedMarketGroceryListColumnName = "market1AisleLocation";
        cursorItems = DatabaseTestHelper.getGroceryListCursor(testDatabase, selectedMarketGroceryListColumnName);

        adapter = new GroceryAdapter(cursorItems, context, GroceryAdapter.getSelectedMarketGroceryListColumnName(testDatabase));
    }

    @After
    public void tearDown() throws Exception {
        DatabaseTestHelper.deleteAllTestTables(testDatabase);
        cursorItems.close();
        testDatabase.close();
    }

    @Test
    public void getItemPosition() {
        Integer expectedPosition1 = 0;
        Integer expectedPosition2 = cursorItems.getCount() - 1;

        Integer actualPosition1 = adapter.getItemPosition(DatabaseTestHelper.firstItem);
        Integer actualPosition2 = adapter.getItemPosition(DatabaseTestHelper.lastItem);

        assertEquals(expectedPosition1, actualPosition1);
        assertEquals(expectedPosition2, actualPosition2);
    }

    @Test
    public void getItemName() {
        String expectedItemName1 = DatabaseTestHelper.firstItem;
        String expectedItemName2 = DatabaseTestHelper.lastItem;

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
        cv.put(GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY, DatabaseTestHelper.SQL_TRUE);
        String[] mySelectionArgs = {DatabaseTestHelper.firstItem};

        testDatabase.update(GroceryContract.GroceryEntry.TABLE_NAME,
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
        List<Market> marketsList = adapter.getMarketsList(testDatabase);

        List<String> actualItemNames = new ArrayList<String>();
        int marketsListSize = marketsList.size();
        for (int i = 0; i < marketsListSize; i++) {
            actualItemNames.add(marketsList.get(i).toString());
        }

        List<String> expectedItemNames = new ArrayList<String>();
        expectedItemNames.add(DatabaseTestHelper.newMarketName1 + " (" + DatabaseTestHelper.newMarketLocation1 + ")");
        expectedItemNames.add(DatabaseTestHelper.newMarketName2 + " (" + DatabaseTestHelper.newMarketLocation2 + ")");
        expectedItemNames.add(DatabaseTestHelper.newMarketName3 + " (" + DatabaseTestHelper.newMarketLocation3 + ")");

        assertEquals(expectedItemNames, actualItemNames);
    }

    @Test
    public void getPositionMarketSelected() {

    }

    @Test
    public void getSelectedMarketGroceryListColumnName() {
        String expectedColumnName1 = "market3AisleLocation";
        String actualColumnName1 = GroceryAdapter.getSelectedMarketGroceryListColumnName(testDatabase);

        assertEquals(expectedColumnName1, actualColumnName1);
    }

    @Test
    public void getNewestGroceryListColumnName() {
        String expectedColumnName1 = "market" + 4 + "AisleLocation";
        String actualColumnName1 = "market" + GroceryAdapter.getNewestGroceryListColumnNumber(testDatabase) + "AisleLocation";

        String[] marketNamesSelectionArgs2 = new String[]{DatabaseTestHelper.newMarketName1, DatabaseTestHelper.newMarketLocation1};
        testDatabase.delete(
                GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET,
                        "(" + GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME + " =? AND " +
                        GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION + " =?)",
                marketNamesSelectionArgs2);
        String expectedColumnName2 = "market" + 4 + "AisleLocation";
        String actualColumnName2 = "market" + GroceryAdapter.getNewestGroceryListColumnNumber(testDatabase) + "AisleLocation";

        String[] marketNamesSelectionArgs3 = new String[]{DatabaseTestHelper.newMarketName3, DatabaseTestHelper.newMarketLocation3};
        testDatabase.delete(
                GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET,
                "(" + GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME + " =? AND " +
                        GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION + " =?)",
                marketNamesSelectionArgs3);
        String expectedColumnName3 = "market" + 3 + "AisleLocation";
        String actualColumnName3 = "market" + GroceryAdapter.getNewestGroceryListColumnNumber(testDatabase) + "AisleLocation";

        String[] marketNamesSelectionArgs4 = new String[]{DatabaseTestHelper.newMarketName2, DatabaseTestHelper.newMarketLocation2};
        testDatabase.delete(
                GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET,
                "(" + GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME + " =? AND " +
                        GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION + " =?)",
                marketNamesSelectionArgs4);
        String expectedColumnName4 = "market" + 1 + "AisleLocation";
        String actualColumnName4 = "market" + GroceryAdapter.getNewestGroceryListColumnNumber(testDatabase) + "AisleLocation";

        assertEquals(expectedColumnName1, actualColumnName1);
        assertEquals(expectedColumnName2, actualColumnName2);
        assertEquals(expectedColumnName3, actualColumnName3);
        assertEquals(expectedColumnName4, actualColumnName4);
    }

}