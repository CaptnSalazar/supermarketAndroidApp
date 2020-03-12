package com.example.grocerylist3;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import java.util.List;

import static android.text.TextUtils.isDigitsOnly;


/* >> Figure out what branching is and if it can help you not delete git stuff permanently like you would if you roll back to previous commit. <<<<<<<*/
/* >> CHECK Future Implementations AT THE BOTTOM OF THIS DOCUMENT TO SEE WHAT TO DO NEXT <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity*<*<*<*<*<*";
    private SQLiteDatabase mDatabase;
    private GroceryAdapter mAdapter;

    private RecyclerView recyclerView;
    boolean mSwipeable;
    private EditText editTextNewItemName;
    private ToggleButton toggleEditAisle;
    private ToggleButton toggleDelete;

    Spinner spinner;
    List<Market> spinnerMarketArray; //I think THIS DOESN'T HAVE TO BE GLOBAL.
    ArrayAdapter<Market> spinnerArrayAdapter;
    private boolean mIsSpinnerBeingEdited = false;

    private final Integer SQL_TRUE = 1;
    private final Integer SQL_FALSE = 0;


    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GroceryDBHelper dbHelper = new GroceryDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase(); //Create and/or open a database that will be used for reading and writing.

        recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        mAdapter = new GroceryAdapter(getAllItems(),this, GroceryAdapter.getSelectedMarketGroceryListColumnName(mDatabase));
        recyclerView.setAdapter(mAdapter);
        mSwipeable = false;

        /*Intent intent = getIntent();
        String messageNewMarket = intent.getStringExtra(EditSuperMarketsInfo.EXTRA_MESSAGE_MARKET_INFO);
        //If we didn't check this, then the app would crash when it started.
        if (messageNewMarket != null) {
            //Log.d(TAG, "onCreate: the new market is: " + messageNewMarket);
            addMarket(messageNewMarket);
        }*/

        setCheckBoxListener();

        toggleDelete = findViewById(R.id.toggleButtonDeleteItem);
        setToggleDeleteListener();

        toggleEditAisle = findViewById(R.id.toggleButtonEditSave);
        toggleEditAisle.setTextColor(Color.GREEN);
        //toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("green")));
        setToggleEditAisleListener();

        editTextNewItemName = findViewById(R.id.editTextNewItem);

        setRecyclerViewSwipeListener();

        // Get a Spinner and bind it to an ArrayAdapter that
        // references an array (eg a String array or a array of custom objects).
        spinnerMarketArray = mAdapter.getMarketsList(mDatabase);
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
        } else {
            spinner.setVisibility(View.GONE);
        }
        setOnSpinnerItemSelectedListener();
    }


    @Override
    protected void onPause() {
        super.onPause();
        //Log.d(TAG, "onPause: >>>>>>closing all cursors<<<<<<<<");
        mAdapter.closeAllCursors();
        if (mAdapter.areAllCursorsClosed()) {
            //Log.d(TAG, "onPause: all cursors are   C L O S E D <<<<<<<<<<<<<<<");
        } else {
            //Log.d(TAG, "onPause: all cursors are   N O T   closed  :(((((((((((");
        }
    }





    public void onDeleteAllRows(View view) {
        mDatabase.execSQL("DELETE FROM " + GroceryContract.GroceryEntry.TABLE_NAME);
        mDatabase.execSQL("DELETE FROM " + GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET);

        mAdapter.swapCursorGrocery(getAllItems());
        //mAdapter.swapCursorMarket(getAllSupermarkets());

        spinnerMarketArray = new ArrayList<Market>();
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

//        //Log.d(TAG, "addItem() called");
//        //Log.d(TAG, "inside addItem and toggleEditAisle value is " + toggleEditAisle.isChecked());
//        //Log.d(TAG, "inside addItem and toggleDelete value is " + toggleDelete.isChecked());
        if (editTextNewItemName.getText().toString().trim ().length() == 0) {
            return;
        }

        String name = editTextNewItemName.getText().toString();
        String nameCapitalised = name.substring(0, 1).toUpperCase() + name.substring(1);

        ContentValues cv = new ContentValues();
        cv.put(GroceryContract.GroceryEntry.COLUMN_NAME, nameCapitalised);
        cv.put(GroceryContract.GroceryEntry.COLUMN_IN_LIST, SQL_TRUE);
        //cv.put(GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY, SQL_FALSE);
        String[] mySelectionArgs = {nameCapitalised};
        int numRowsUpdated = mDatabase.update(GroceryContract.GroceryEntry.TABLE_NAME,
                cv,
                GroceryContract.GroceryEntry.COLUMN_NAME + " =?",
                mySelectionArgs);
        //Log.d("MainActivity", "number of rows updated: " + numRowsUpdated);
        if (numRowsUpdated < 1) {
            mDatabase.insert(GroceryContract.GroceryEntry.TABLE_NAME, null, cv);
        }
        mAdapter.swapCursorGrocery(getAllItems());

        editTextNewItemName.getText().clear();
        recyclerView.scrollToPosition(mAdapter.getItemPosition(nameCapitalised));
    }


    public void onEditSpinner(View view) {
        //Log.d(TAG, "onEditSpinner: ");
        /*Intent intent = new Intent(this, EditSuperMarketsInfo.class);
        startActivity(intent);*/
        setLayoutEditSpinner();
    }


    public void onCancel(View view) {
        //Log.d(TAG, "onCancel: ");
        setLayoutEditSpinner();
    }


    public void onConfirm(View view) {
        //Log.d(TAG, "onConfirm: ");

        EditText editTextName = findViewById(R.id.editTextMarketName);
        String newMarketName = editTextName.getText().toString().trim();
        EditText editTextLocation = findViewById(R.id.editTextMarketLocation);
        String newMarketLocation = editTextLocation.getText().toString().trim();

        if (GroceryAdapter.marketIsAlreadyInTable(mDatabase, newMarketName, newMarketLocation)) {
            closeKeyboard();
            // MAYBE INSTEAD OF SNACKBAR, MAKE A TEXTVIEW APPEAR SAYING IN RED, "MarketName (Location) already exists."
            Snackbar.make(findViewById(R.id.rootLayout), R.string.snack_message_market_already_in_table, Snackbar.LENGTH_SHORT).show();
        } else if ((newMarketName.length() > 0) && (newMarketLocation.length() > 0)){

            addMarketToTable(newMarketName, newMarketLocation);

            TextView textViewSpinnerEmpty = findViewById(R.id.textViewSpinnerEmpty);
            textViewSpinnerEmpty.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);

            spinnerMarketArray = mAdapter.getMarketsList(mDatabase); //not efficient way of adding Market but this won't be done often.
            setLayoutEditSpinner();
            //these thee statements are needed for spinner to refresh.
            spinnerArrayAdapter = new ArrayAdapter<Market> (this,
                    android.R.layout.simple_spinner_item, //the spinner itself will look like this (no radio buttons).
                    spinnerMarketArray); //selected item will look like a spinner set from XML.
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //each item in the spinner will look like this when you click the spinner (with radio buttons).
            spinner.setAdapter(spinnerArrayAdapter);
            spinner.setSelection(mAdapter.getPositionMarketSelected());
        }
    }


    private void addMarketToTable(String newMarketName, String newMarketLocation) {
        ContentValues cv = new ContentValues();
        cv.put(GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME, newMarketName);
        cv.put(GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION, newMarketLocation);
        cv.put(GroceryContract.SupermarketsVisited.COLUMN_IS_MARKET_SELECTED, SQL_TRUE);
        cv.put(GroceryContract.SupermarketsVisited.COLUMN_MARKET_GROCERY_COLUMN, GroceryAdapter.getNewestGroceryListColumnNumber(mDatabase));
        mDatabase.insert(GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET, null, cv);
        selectedMarketUpdate(newMarketName, newMarketLocation);
    }


    private void selectedMarketUpdate(String selectedMarketName, String selectedMarketLocation) {
        //Log.d(TAG, "selectedMarketUpdate: making isSelected true in only " + selectedMarketName + " (" + selectedMarketLocation + ")");
        ContentValues cv = new ContentValues();
        cv.put(GroceryContract.SupermarketsVisited.COLUMN_IS_MARKET_SELECTED, SQL_FALSE);
        String[] selectionArgsForSelectedMarket = {selectedMarketName , selectedMarketLocation};
        mDatabase.update(GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET,
                cv,
                "NOT " + GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME + " =? OR NOT " +
                        GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION + " =?", //practically unnecessary in this context but I wanna learn SQL
                selectionArgsForSelectedMarket);

        ContentValues cvOfSelectedMarket = new ContentValues();
        cvOfSelectedMarket.put(GroceryContract.SupermarketsVisited.COLUMN_IS_MARKET_SELECTED, SQL_TRUE);
        mDatabase.update(GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET,
                cvOfSelectedMarket,
                GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME + " =? AND " +
                        GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION + " =?",
                selectionArgsForSelectedMarket);

        String selectedMarketGroceryColumnName = GroceryAdapter.getSelectedMarketGroceryListColumnName(mDatabase);
        mAdapter.setmSelectedMarketColumnName(selectedMarketGroceryColumnName);
        mAdapter.swapCursorGrocery(getAllItems());
        recyclerView.scrollToPosition(0);
    }


    private void setLayoutEditSpinner() {
        mIsSpinnerBeingEdited = !mIsSpinnerBeingEdited; //toggle mIsSpinnerBeingEdited

        Button buttonEditSpinner = findViewById(R.id.buttonEditSpinner);
        TextView textViewName = findViewById(R.id.textViewMarketName);
        EditText editTextName = findViewById(R.id.editTextMarketName);
        TextView textViewLocation = findViewById(R.id.textViewMarketLocation);
        EditText editTextLocation = findViewById(R.id.editTextMarketLocation);
        Button confirmButton = findViewById(R.id.buttonConfirm);
        Button cancelButton = findViewById(R.id.buttonCancel);
        Button buttonAddItem = findViewById(R.id.buttonAddItem);

        Button buttonDeleteAll = findViewById(R.id.button_delete_all);

        if (mIsSpinnerBeingEdited) {
            buttonEditSpinner.setText(getString(R.string.spinner_edit_back));
            textViewName.setVisibility(View.VISIBLE);
            editTextName.setVisibility(View.VISIBLE);
            textViewLocation.setVisibility(View.VISIBLE);
            editTextLocation.setVisibility(View.VISIBLE);
            confirmButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);

            recyclerView.setVisibility(View.GONE);
            buttonAddItem.setVisibility(View.GONE);
            editTextNewItemName.setVisibility(View.GONE);
            toggleEditAisle.setVisibility(View.GONE);
            toggleDelete.setVisibility(View.GONE);
            buttonDeleteAll.setVisibility(View.GONE);


        } else {
            textViewName.setVisibility(View.GONE);
            editTextName.setVisibility(View.GONE);
            textViewLocation.setVisibility(View.GONE);
            editTextLocation.setVisibility(View.GONE);
            editTextName.setVisibility(View.GONE);
            confirmButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);

            recyclerView.setVisibility(View.VISIBLE);
            buttonAddItem.setVisibility(View.VISIBLE);
            editTextNewItemName.setVisibility(View.VISIBLE);
            toggleEditAisle.setVisibility(View.VISIBLE);
            toggleDelete.setVisibility(View.VISIBLE);
            buttonDeleteAll.setVisibility(View.VISIBLE);

            closeKeyboard();
            buttonEditSpinner.setText(getString(R.string.spinner_edit));
            editTextName.getText().clear();
            editTextLocation.getText().clear();
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
            int numRowsUpdated = mDatabase.update(GroceryContract.GroceryEntry.TABLE_NAME,
                    cv,
                    GroceryContract.GroceryEntry.COLUMN_NAME + " =?",
                    mySelectionArgs);
            //Log.d("MainActivity", "number of rows updated: " + numRowsUpdated);
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
                    //Log.d(TAG, "toggleDelete is checked");
                    mSwipeable = true; // make list swipeable
                } else {
                    //Log.d(TAG, "toggleDelete is NOT checked");
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
                    //Log.d(TAG, "toggleEditAisle is checked");
                    toggleEditAisle.setTextColor(Color.RED);
                    //toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("red")));
                } else {
                    //Log.d(TAG, "toggleEditAisle is NOT checked");
                    toggleEditAisle.setTextColor(Color.GREEN);
                    //toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("green")));
                    if (GroceryAdapter.marketTableIsNotEmpty(mDatabase)) {
                        saveAisles();
                    } else {
                        Snackbar.make(findViewById(R.id.rootLayout), R.string.snack_message_add_a_supermarket, Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    private void saveAisles() {
        //Log.d(TAG, "saveAisles() called");
        View v; // v will be each item (i.e. one set of aisleNumber, productName, checkBox views) in recyclerview.
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            //Log.d(TAG, "saveAisles: we are at the " + String.valueOf(i) + "th value of the recyclerview ------");
            v = recyclerView.getChildAt(i); // v encapsulates a pair of aisle editText and product name EditText.
            EditText aisleEditText = v.findViewById(R.id.edittext_aisle_number);
            EditText productNameEditText = v.findViewById(R.id.edittext_product_name);
            String aisleNumberStr = aisleEditText.getText().toString();
            String productNameStr = productNameEditText.getText().toString();
            //Log.d(TAG, "saveAisles: aisle found --> " + aisleNumberStr);
            //Log.d(TAG, "saveAisles: product found --> " + productNameStr);
            //Log.d(TAG, "saveAisles: selectedMarketGroceryColumnName: " + GroceryAdapter.getSelectedMarketGroceryListColumnName(mDatabase));
            updateAisle(aisleNumberStr, productNameStr);
            //aisleEditText.setKeyListener(null); //makes the EditText non-editable so, it acts like a TextView.
        }
    }


    private void updateAisle(String aisleNumberStr, String productNameStr) {
        //Log.d(TAG, "updateAisle() called");
        if (isDigitsOnly(aisleNumberStr) && (aisleNumberStr.length() > 0) && (productNameStr.length() > 0)) {
            ContentValues cv = new ContentValues();
            cv.put(GroceryAdapter.getSelectedMarketGroceryListColumnName(mDatabase), Integer.parseInt(aisleNumberStr));

            String[] mySelectionArgs = {productNameStr};
            int numRowsUpdated = mDatabase.update(GroceryContract.GroceryEntry.TABLE_NAME,
                    cv,
                    GroceryContract.GroceryEntry.COLUMN_NAME + " =?",
                    mySelectionArgs);
            //Log.d(TAG, "updateAisle: number of rows updated: " + numRowsUpdated); //Integer is automatically converted to String if needed
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
        int numRowsUpdated = mDatabase.update(GroceryContract.GroceryEntry.TABLE_NAME,
                cv,
                GroceryContract.GroceryEntry._ID + " =?",
                mySelectionArgs);
        //Log.d(TAG, "number of rows updated: " + numRowsUpdated); //Integer is automatically converted to String if needed.
        mAdapter.swapCursorGrocery(getAllItems());
    }


    private void setOnSpinnerItemSelectedListener() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMarket = parent.getItemAtPosition(position).toString();
                //Log.d(TAG, "onItemSelected:  updating table so that only " + selectedMarket + " has true value of IsSelected");
                String[] marketInfo = Market.extractNameAndLocation(selectedMarket);
                //MAKE SURE THE USER CANNOT INPUT '(' AS IN MARKET NAME OR LOCATION OTHERWISE THIS WILL HAVE BUGS.
                //Log.d(TAG, "onItemSelected:  market name is " + marketInfo[0] + ", and location is: " + marketInfo[1]);
                selectedMarketUpdate(marketInfo[0], marketInfo[1]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private Cursor getAllItems() {
        //Log.d(TAG, "getAllItems() called");
        String[] mySelectionArgs = new String[]{String.valueOf(SQL_TRUE)};
        return mDatabase.query(
                GroceryContract.GroceryEntry.TABLE_NAME,
                null,
                GroceryContract.GroceryEntry.COLUMN_IN_LIST + " =?", //equivalent to WHERE. A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows for the given table.
                mySelectionArgs, //You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
                null,
                null,
                GroceryAdapter.getSelectedMarketGroceryListColumnName(mDatabase) + " ASC, " + GroceryContract.GroceryEntry.COLUMN_NAME + " ASC"
        );
    }

    public void closeKeyboard() {
        View view = findViewById(R.id.spinner);
        if (view != null) {
            //Log.d("MainActivitee", "Inside if statement of closeKeboard().");
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /*
    PROBLEM: when you do any sequence of activities followed by clicking on the empty spinner (i.e.
    before adding a market or after pressing "Delete All") everything freezes. But if you press "Edit Spinner" and
    add a market to it, then everything works as expected.
    SOLUTION: make the spinner be gone when it is empty.
    */

    /*
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
    */

    /*
    Future implementations:

    Note: when you try to add an item and there is no supermarket selected, it just automatically makes it market1AisleLocation

    >> When you press "Edit Spinner", a window pops up (OR MAYBE WE START ANOTHER ACTIVITY??) that has a "negative" button/option of "change name", and
    a "positive" button/option of "add new". If you press change name, a picker pops up for you to select/find the
    market name you wanna change and 2 editTexts one above the other appear with text to their left saying
    "Location" with editText hint "e.g. Ilam Road", and "Company" with editText hint "e.g. Countdown", and
    two buttons/options, "cancel" and "confirm".
    >> Make the program only accept reasonable values for the item names and aisle numbers and supermarket names.
    >> Make it so that when you are in edit mode, and an aisle editText loses focus you check if it was changed.
    >> Make the EditTexts of the list not allow keyboard pop up when not in "edit mode".
    >> Autocompletion based on the items that already exist in database.
    >> Make the Edit List button glow or point to it, when someone tries to do other things while in Edit mode.
    >> when the user adds an item to the list that has very similar spelling to another,
    make a window pop-up, saying that there already exists a record of [this other item]. Are they they the same?
    Which spelling is right?
    >> Make a feature that allows the user to change the supermarket they updated, in case they made a mistake and updated
    the wrong supermarket. Be like, in your last session, you updated THIS supermarket, but which supermarket did MEAN to update?
    Maybe keep track of updates by date or how distant (time-wise) the last update to a table was, e.g. if you updated the location
    of cocoa powder and then 4 hours later updated the position of flour, if there are not updates in between to connect the two,
    the program will regard these as two separate sessions.
    Make an undo feature, google how to make undo using sqlite with/or in android studio.
    >> Make background thread (see your java google docs for link)
     */

}

