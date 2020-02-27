package com.example.grocerylist3;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.text.TextUtils.isDigitsOnly;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity*<*<*<*<*<*";
    private SQLiteDatabase mDatabase;
    private GroceryAdapter mAdapter;

    private RecyclerView recyclerView;
    boolean mSwipeable;
    private EditText mEditTextName;
    private ToggleButton toggleEditAisle;
    private ToggleButton toggleDelete;

    Spinner spinner;
    List<Market> spinnerMarketArray; //I think THIS DOESN'T HAVE TO BE GLOBAL.
    ArrayAdapter<Market> spinnerArrayAdapter;
    private boolean mIsSpinnerBeingEdited = false;

    private String currentColumnMarketAisles = GroceryContract.GroceryEntry.COLUMN_MARKET1_AISLE;
    private final Integer SQL_TRUE = 1;
    private final Integer SQL_FALSE = 0;


    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GroceryDBHelper dbHelper = new GroceryDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase(); //Create and/or open a database that will be used for reading and writing.

        recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        //mAdapter = new GroceryAdapter(getAllItems(), this);
        //mAdapter = new GroceryAdapter(getAllItems(), getAllSupermarkets(), this);
        mAdapter = new GroceryAdapter(getAllItems(), this);
        recyclerView.setAdapter(mAdapter);
        mSwipeable = false;

        /*Intent intent = getIntent();
        String messageNewMarket = intent.getStringExtra(EditSuperMarketsInfo.EXTRA_MESSAGE_MARKET_INFO);
        //If we didn't check this, then the app would crash when it started.
        if (messageNewMarket != null) {
            Log.d(TAG, "onCreate: the new market is: " + messageNewMarket);
            addMarket(messageNewMarket);
        }*/

        setCheckBoxListener();

        toggleDelete = findViewById(R.id.toggleButtonDeleteItem);
        setToggleDeleteListener();

        toggleEditAisle = findViewById(R.id.toggleButtonEditSave);
        toggleEditAisle.setTextColor(Color.GREEN);
        //toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("green")));
        setToggleEditAisleListener();

        mEditTextName = findViewById(R.id.edittext_new_product);

        setRecyclerViewSwipeListener();

        // Get a Spinner and bind it to an ArrayAdapter that
        // references an array (eg a String array or a array of custom objects).

        spinnerMarketArray = mAdapter.getMarketList(mDatabase);
        //spinnerMarketArray = new ArrayList<Market>();

        spinner = findViewById(R.id.spinner);
        spinnerArrayAdapter = new ArrayAdapter<Market> (this,
                android.R.layout.simple_spinner_item, //the spinner itself will look like this (no radio buttons).
                spinnerMarketArray); //selected item will look like a spinner set from XML.
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //each item in the spinner will look like this when you click the spinner (with radio buttons).
        spinner.setAdapter(spinnerArrayAdapter);
        if (spinnerMarketArray.size() > 0) { // In the actual app, spinnerMarketArray.size() will only be 0 before a supermarket has been added.
            TextView textviewSpinnerEmpty = findViewById(R.id.textViewSpinnerEmpty);
            textviewSpinnerEmpty.setVisibility(View.GONE);
            spinner.setSelection(mAdapter.getPositionMarketSelected());
        } //make textView gone and spinner.setSelected(position)
        else {
            spinner.setVisibility(View.GONE);
        }
        setOnSpinnerItemSelectedListener();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: >>>>>>closing all cursors<<<<<<<<");
        mAdapter.closeAllCursors();
        if (mAdapter.areAllCursorsClosed()) {
            Log.d(TAG, "onPause: all cursors are   C L O S E D <<<<<<<<<<<<<<<");
        } else {
            Log.d(TAG, "onPause: all cursors are   N O T   closed  :(((((((((((");
        }
    }


    public void onDeleteAllRows(View view) {
        Log.d(TAG, "onDeleteAllRows() called");
        mDatabase.execSQL("DELETE FROM " + GroceryContract.GroceryEntry.TABLE_NAME);
        mDatabase.execSQL("DELETE FROM " + GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET);

        mAdapter.swapCursorGrocery(getAllItems());
        //mAdapter.swapCursorMarket(getAllSupermarkets());

        spinnerMarketArray = mAdapter.getMarketList(mDatabase);
        //these thee statements are needed for spinner to refresh.
        spinnerArrayAdapter = new ArrayAdapter<Market> (this,
                android.R.layout.simple_spinner_item, //the spinner itself will look like this (no radio buttons).
                spinnerMarketArray); //selected item will look like a spinner set from XML.
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //each item in the spinner will look like this when you click the spinner (with radio buttons).
        spinner.setAdapter(spinnerArrayAdapter);
        TextView textviewSpinnerEmpty = findViewById(R.id.textViewSpinnerEmpty);
        textviewSpinnerEmpty.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.GONE);
    }


    public void onAddItem(View view) {
        if (toggleEditAisle.isChecked()) {
            //memo: also make "Edit List" button glow
            Snackbar.make(findViewById(R.id.rootLayout), R.string.snack_message_press_save_changes, Snackbar.LENGTH_SHORT).show();
        } else {
            addItem();
        }
    }


    private void addItem() {
        /*Get item from edittext_new_product and check that it's got a phrase and if so, capitalise it.
         * Then, try to update the grocery items table with that phrase/item name, and if you couldn't
         * update it, it means the item isn't on it, so insert the new item. Then scroll to the position
         * of the item - including if the item was already on the recyclerview list. */

        Log.d(TAG, "addItem() called");
        Log.d(TAG, "inside addItem and toggleEditAisle value is " + toggleEditAisle.isChecked());
        Log.d(TAG, "inside addItem and toggleDelete value is " + toggleDelete.isChecked());
        if (mEditTextName.getText().toString().trim ().length() == 0) {
            return;
        }

        String name = mEditTextName.getText().toString();
        String nameCapitalised = name.substring(0, 1).toUpperCase() + name.substring(1);

        ContentValues cv = new ContentValues();
        cv.put(GroceryContract.GroceryEntry.COLUMN_NAME, nameCapitalised);
        cv.put(GroceryContract.GroceryEntry.COLUMN_IN_LIST, SQL_TRUE);
        //cv.put(GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY, SQL_FALSE);
        String[] mySelectionArgs = {nameCapitalised};
        Integer numRowsUpdated = mDatabase.update(GroceryContract.GroceryEntry.TABLE_NAME,
                cv,
                GroceryContract.GroceryEntry.COLUMN_NAME + " =?",
                mySelectionArgs);
        Log.d("MainActivity", "number of rows updated: " + numRowsUpdated);
        if (numRowsUpdated < 1) {
            mDatabase.insert(GroceryContract.GroceryEntry.TABLE_NAME, null, cv);
        }
        mAdapter.swapCursorGrocery(getAllItems());

        mEditTextName.getText().clear();
        recyclerView.scrollToPosition(mAdapter.getItemPosition(nameCapitalised));
    }


    public void onEditSpinner(View view) {
        Log.d(TAG, "onEditSpinner: ");
        /*Intent intent = new Intent(this, EditSuperMarketsInfo.class);
        startActivity(intent);*/
        setLayoutEditSpinner();
    }


    public void onCancel(View view) {
        Log.d(TAG, "onCancel: ");
        setLayoutEditSpinner();
    }


    public void onConfirm(View view) {
        Log.d(TAG, "onConfirm: ");

        if (spinnerMarketArray.size() == 0) {  //if we are adding item for the first time.
            TextView textViewSpinnerEmpty = findViewById(R.id.textViewSpinnerEmpty);
            textViewSpinnerEmpty.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
        }

        EditText editTextName = findViewById(R.id.editTextName);
        String newMarketName = editTextName.getText().toString();
        addMarketToTable(newMarketName);
        spinnerMarketArray = mAdapter.getMarketList(mDatabase); //not efficient way of adding Market but this won't be done often.

        setLayoutEditSpinner();

        //these thee statements are needed for spinner to refresh.
        spinnerArrayAdapter = new ArrayAdapter<Market> (this,
                android.R.layout.simple_spinner_item, //the spinner itself will look like this (no radio buttons).
                spinnerMarketArray); //selected item will look like a spinner set from XML.
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //each item in the spinner will look like this when you click the spinner (with radio buttons).
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(mAdapter.getPositionMarketSelected()); //DOESN'T WORK PROPERLY: getPositionMarketSelected DOESN'T SET PREVIOUSLY SELECTED MARKET TO FALSE
    }


    private void addMarketToTable(String newMarket) {
        Log.d(TAG, "addMarketToTable: ");
        if (newMarket.trim().length() == 0) {
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME, newMarket);
        cv.put(GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION, "Temporary Location");
        cv.put(GroceryContract.SupermarketsVisited.COLUMN_IS_MARKET_SELECTED, SQL_TRUE);
        mDatabase.insert(GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET, null, cv);
        updateTableSelectedMarket(newMarket);
        //mAdapter.swapCursorMarket(getAllSupermarkets(mDatabase));
    }


    private void updateTableSelectedMarket(String selectedMarketName) {
        ContentValues cv = new ContentValues();
        cv.put(GroceryContract.SupermarketsVisited.COLUMN_IS_MARKET_SELECTED, SQL_FALSE);

        String[] selectionArgsForSelectedMarket = {selectedMarketName};
        Integer numRowsUpdated = mDatabase.update(GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET,
                cv,
                "NOT " + GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME + " =?", //practically unnecessary in this context but I wanna learn SQL
                selectionArgsForSelectedMarket);
        Log.d(TAG, "updateTableSelectedMarket:  number of rows updated: " + numRowsUpdated); //Integer is automatically converted to String if needed

        ContentValues cvOfSelectedMarket = new ContentValues();
        cvOfSelectedMarket.put(GroceryContract.SupermarketsVisited.COLUMN_IS_MARKET_SELECTED, SQL_TRUE);


        Integer numRowsUpdated2 = mDatabase.update(GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET,
                cvOfSelectedMarket,
                GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME + " =?",
                selectionArgsForSelectedMarket);
        Log.d(TAG, "updateTableSelectedMarket:  number of rows updated: " + numRowsUpdated2); //Integer is automatically converted to String if needed

    }


    private void setLayoutEditSpinner() {
        mIsSpinnerBeingEdited = !mIsSpinnerBeingEdited; //toggle mIsSpinnerBeingEdited

        Button buttonEditSpinner = findViewById(R.id.buttonEditSpinner);
        EditText editTextName = findViewById(R.id.editTextName);
        Button confirmButton = findViewById(R.id.buttonConfirm);
        Button cancelButton = findViewById(R.id.buttonCancel);

        if (mIsSpinnerBeingEdited) {
            buttonEditSpinner.setText("Back");
            editTextName.setVisibility(View.VISIBLE);
            confirmButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            editTextName.getText().clear();
            buttonEditSpinner.setText("Edit");
            editTextName.setVisibility(View.GONE);
            confirmButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }


    private void setCheckBoxListener() {
        mAdapter.setOnItemClickListener(new GroceryAdapter.OnItemClickListener() {
            @Override
            public void onCheckBox(int position) {
                toggleInTrolley(position); //indirectly tick/untick the checkbox.
            }
        });
    }


    public void toggleInTrolley(int position) {
        if (!toggleEditAisle.isChecked()) {
            String productName = mAdapter.getItemName(position);
            ContentValues cv = new ContentValues();
            Integer inTrolley = mAdapter.getInTrolleyValue(position);
            if (inTrolley.equals(SQL_TRUE)) {
                cv.put(GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY, SQL_FALSE);
            } else {
                cv.put(GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY, SQL_TRUE);
            }
            String[] mySelectionArgs = {productName};
            Integer numRowsUpdated = mDatabase.update(GroceryContract.GroceryEntry.TABLE_NAME,
                    cv,
                    GroceryContract.GroceryEntry.COLUMN_NAME + " =?",
                    mySelectionArgs);
            Log.d("MainActivity", "number of rows updated: " + numRowsUpdated);
        } else {
            Snackbar.make(findViewById(R.id.rootLayout), R.string.snack_message_press_save_changes, Snackbar.LENGTH_SHORT).show();
        }

        mAdapter.swapCursorGrocery(getAllItems());
        //recyclerView.scrollToPosition(mAdapter.getItemPosition(nameCapitalised)); //maybe call this?
    }


    private void setToggleDeleteListener() {
        toggleDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "toggleDelete is checked");
                    mSwipeable = true; // make list swipeable
                } else {
                    Log.d(TAG, "toggleDelete is NOT checked");
                    mSwipeable = false; // make list unswipeable
                    //saveAisles(); <Wrong. This was for the toggleEditAisle, not toggleDeleteAisle
                }
            }
        });
    }


    private void setToggleEditAisleListener() {
        toggleEditAisle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "toggleEditAisle is checked");
                    toggleEditAisle.setTextColor(Color.RED);
                    //toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("red")));
                } else {
                    Log.d(TAG, "toggleEditAisle is NOT checked");
                    toggleEditAisle.setTextColor(Color.GREEN);
                    //toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("green")));
                    saveAisles();
                }
            }
        });
    }


    private void saveAisles() {
        Log.d(TAG, "saveAisles() called");
        View v; // v will be each item (i.e. one set of aisleNumber, productName, checkBox views) in recyclerview.
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            Log.d(TAG, "we are at the " + String.valueOf(i) + "th value of the recyclerview ------");
            v = recyclerView.getChildAt(i); // v encapsulates a pair of aisle editText and product name EditText.
            EditText aisleEditText = v.findViewById(R.id.edittext_aisle_number);
            EditText productNameEditText = v.findViewById(R.id.edittext_product_name);
            String aisleNumberStr = aisleEditText.getText().toString();
            String productNameStr = productNameEditText.getText().toString();
            Log.d("MainActivity", "aisle found --> " + aisleNumberStr);
            Log.d("MainActivity", "product found --> " + productNameStr);
            updateAisle(aisleNumberStr, productNameStr);
            //aisleEditText.setKeyListener(null); //makes the EditText non-editable so, it acts like a TextView.
        }
    }


    private void updateAisle(String aisleNumberStr, String productNameStr) {
        Log.d(TAG, "updateAisle() called");
        if (isDigitsOnly(aisleNumberStr) && (aisleNumberStr.length() > 0) && (productNameStr.length() > 0)) {
            ContentValues cv = new ContentValues();
            cv.put(currentColumnMarketAisles, Integer.parseInt(aisleNumberStr));

            String[] mySelectionArgs = {productNameStr};
            Integer numRowsUpdated = mDatabase.update(GroceryContract.GroceryEntry.TABLE_NAME,
                    cv,
                    GroceryContract.GroceryEntry.COLUMN_NAME + " =?",
                    mySelectionArgs);
            Log.d(TAG, "number of rows updated: " + numRowsUpdated); //Integer is automatically converted to String if needed
            mAdapter.swapCursorGrocery(getAllItems());
        }
    }


    private void setRecyclerViewSwipeListener() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (toggleDelete.isChecked()) {
                    removeItem((long) viewHolder.itemView.getTag());
                } else {
                    Snackbar.make(findViewById(R.id.rootLayout), R.string.snack_message_press_delete_items, Snackbar.LENGTH_SHORT).show();
                    //mAdapter.swapCursorGrocery(getAllItems()); //before I figured out proper way (i.e. disabling swipe altogether).
                }

            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                //We override otherwise this would always return true and always allow swiping.
                boolean swipeable = mSwipeable; //swipeable says it's redundant but causes problems if you just use mSwipeable.
                return swipeable;
            }
        }).attachToRecyclerView(recyclerView);
    }


    private void removeItem(long id) {
        ContentValues cv = new ContentValues();
        cv.put(GroceryContract.GroceryEntry.COLUMN_IN_LIST, SQL_FALSE);
        cv.put(GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY, SQL_FALSE);
        String[] mySelectionArgs = {String.valueOf(id)};
        //update(java.lang.String, android.content.ContentValues, java.lang.String, java.lang.String[])
        Integer numRowsUpdated = mDatabase.update(GroceryContract.GroceryEntry.TABLE_NAME,
                cv,
                GroceryContract.GroceryEntry._ID + " =?",
                mySelectionArgs);
        Log.d(TAG, "number of rows updated: " + numRowsUpdated); //Integer is automatically converted to String if needed.
        mAdapter.swapCursorGrocery(getAllItems());
    }


    private void setOnSpinnerItemSelectedListener() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMarketName = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "onItemSelected:  updating table so that only " + selectedMarketName +
                        " has true value of IsSelected");
                updateTableSelectedMarket(selectedMarketName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private Cursor getAllItems() {
        Log.d(TAG, "getAllItems() called");
        String[] mySelectionArgs = new String[]{String.valueOf(SQL_TRUE)};
        return mDatabase.query(
                GroceryContract.GroceryEntry.TABLE_NAME,
                null,
                GroceryContract.GroceryEntry.COLUMN_IN_LIST + " =?", //equivalent to WHERE. A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows for the given table.
                mySelectionArgs, //You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
                null,
                null,
                currentColumnMarketAisles + " ASC, " + GroceryContract.GroceryEntry.COLUMN_NAME + " ASC"
        );
    }

}

/*
PROBLEM: when you do any sequence of activities followed by clicking on the empty spinner (i.e.
before adding a market or after pressing "Delete All") everything freezes. But if you press "Edit Spinner" and
add a market to it, then everything works as expected.
SOLUTION: make the spinner be gone when it is empty.

Future implementation:
WHEN EDIT SPINNER BUTTON IS PRESSED, WE SOMETIMES (IT DOESN'T ALWAYS HAPPENS, SO TRY MULTIPLE TIMES,
ABOUT 4 USUALLY MAKES THE MESSAGE APPEAR) GET "A resource failed to call close" ONCE, AND I DON'T
KNOW WHY BECAUSE I CLOSE THE CURSORS BEFORE GOING TO THE OTHER ACTIVITY. I think the problem might
actually >>>not be caused by the cursor<<< and may be caused by other things to do with the activity.
So, learn how to check if there is a memory leak (which is caused when you don't close a resource and
the gargabe collector can't release that unused resource because it has no reference to it).
--I think android wants me to close the cursor after onBindViewHolder. <<<NO! this makes it crash.
>>>>>>>>>>>>>>
>>>>> E A S I E S T  SOLUTION: DON'T MAKE ANOTHER ACTIVITY, JUST HIDE THE RECYCLERVIEW AND DISPLAY TWO<<<<<<<
>>>>> OTHER HARDER POSSIBLE SOLUTION:  ANDROID WANTED ME TO CLOSE THE DATABASE ITSELF mDatabase.close() <<<<<<<<<<


0.5) When you press "Edit Spinner", a window pops up (OR MAYBE WE START ANOTHER ACTIVITY??) that has a "negative" button/option of "change name", and
a "positive" button/option of "add new". If you press change name, a picker pops up for you to select/find the
market name you wanna change and 2 editTexts one above the other appear with text to their left saying
"Location" with editText hint "e.g. Ilam Road", and "Company" with editText hint "e.g. Countdown", and
two buttons/options, "cancel" and "confirm".
NOTE: not sure if I should make it so that when an aisle editText loses focus you check if it was changed because a
person might butt type or whatever, although that's unlikely, and the program (will) expect a reasonable.
1) Make the EditTexts of the list not allow keyboard pop up when not in "edit mode".
2) Autocompletion based on the items that already exist in database.
3) Make the Edit List button glow or point to it, when someone tries to do other things while in Edit mode.
4) when the user adds an item to the list that has very similar spelling to another,
make a window pop-up, saying that there already exists a record of [this other item]. Are they they the same?
Which spelling is right?
5) Make background thread (see your java google docs for link)
 */