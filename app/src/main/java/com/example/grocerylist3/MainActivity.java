package com.example.grocerylist3;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.snackbar.Snackbar;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/* >> Figure out what branching is and if it can help you not delete git stuff permanently like you would if you roll back to previous commit. <<<<<<<*/
/* >> CHECK Future Implementations AT THE BOTTOM OF THIS DOCUMENT TO SEE WHAT TO DO NEXT <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/
//@RequiresApi (api = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) //tried this is because for some reason android studio started thinking i wanted api 1
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity*<*<*<*<*<*";
    private SQLiteDatabase mDatabase;
    private GroceryAdapter mAdapter;

    private RecyclerView recyclerView;
    boolean mSwipeable;

    private AutoCompleteTextView autoCompTxtViewItemName;
    private ToggleButton toggleEditAisle;
    private ToggleButton toggleDelete;
    private Handler mHandlerToggleFlash = new Handler();

    Spinner spinner;
    List<Market> spinnerMarketArray; //I think THIS DOESN'T HAVE TO BE GLOBAL.
    ArrayAdapter<Market> spinnerArrayAdapter;
    private boolean mIsSpinnerBeingEdited = false;
    private final Integer SQL_TRUE = 1;
    private final Integer SQL_FALSE = 0;
    private int mCurrentOrder = 0;
    int ORDER_AISLE_THEN_ALPHABET = 0; // for convenient shopping
    int ORDER_IN_TROLLEY_THEN_AISLE_THEN_ALPHABET = 1;  // for convenient shopping
    int ORDER_ALPHABET = 2;  //for checking items you added to list.
    int requiredItemDeletionSuccession = 2;

    private static final String[] ITEM_SUGGESTIONS = new String[] {"Apple", "Abacus", "Amplifier", "Appendix", "Apostrophe", "Appple"};

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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

        setRecyclerViewListeners();

        toggleDelete = findViewById(R.id.toggleButtonDeleteItem);
        toggleDelete.setTextColor(Color.BLACK);
        toggleDelete.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(107, 214, 213)));
        setToggleDeleteListener();

        toggleEditAisle = findViewById(R.id.toggleButtonEditSave);
        toggleEditAisle.setTextColor(Color.BLACK);
        toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(107, 214, 213)));
        //toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("green")));
        setToggleEditAisleListener();

        autoCompTxtViewItemName = findViewById(R.id.autoCompTextViewNewItem);
        ArrayAdapter<String> adapterAutoComp = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mAdapter.getItemArrayFromGroceryTable(mDatabase));
        autoCompTxtViewItemName.setAdapter(adapterAutoComp);

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
            /* TextView textviewSpinnerEmpty = findViewById(R.id.textViewSpinnerEmpty);
            textviewSpinnerEmpty.setVisibility(View.GONE); */
            spinner.setSelection(mAdapter.getPositionMarketSelected());
        } else {
            Button buttonEditSpinner = findViewById(R.id.buttonEditSpinner);
            buttonEditSpinner.setText(getString(R.string.spinner_edit_add_new_market));
            buttonEditSpinner.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
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


    private void updateAutoCompleteTextViewArray() {
        ArrayAdapter<String> adapterAutoComp = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mAdapter.getItemArrayFromGroceryTable(mDatabase));
        autoCompTxtViewItemName.setAdapter(adapterAutoComp);
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
        /*TextView textviewSpinnerEmpty = findViewById(R.id.textViewSpinnerEmpty);
        textviewSpinnerEmpty.setVisibility(View.VISIBLE); */
        Button buttonEditSpinner = findViewById(R.id.buttonEditSpinner);
        buttonEditSpinner.setText(getString(R.string.spinner_edit_add_new_market));
        buttonEditSpinner.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
    }


    public void onAddItem(View view) {
        if (toggleEditAisle.isChecked()) {
            mHandlerToggleFlash.postDelayed(mToggleFlashRunnable, 400);
            mHandlerToggleFlash.postDelayed(mToggleFlashRunnable, 1100);
            myShowSnackBar(R.string.snack_message_press_save_changes);
            //Snackbar.make(findViewById(R.id.rootLayout), R.string.snack_message_press_save_changes, Snackbar.LENGTH_SHORT).show();
        } else if (toggleDelete.isChecked()) {
            mHandlerToggleFlash.postDelayed(mToggleFlashRunnable, 400);
            mHandlerToggleFlash.postDelayed(mToggleFlashRunnable, 1100);
            myShowSnackBar(R.string.snack_message_press_done_deleting);
            //Snackbar.make(findViewById(R.id.rootLayout), R.string.snack_message_press_done_deleting, Snackbar.LENGTH_SHORT).show();
        } else {
            addItem();
        }
    }


    private void addItem() {
        /*Get item from edittext_new_product and check that it's got a phrase and if so, capitalise it.
         * Then, try to update the grocery items table with that phrase/item name, and if you couldn't
         * update it, it means the item isn't on it, so insert the new item. Then scroll to the position
         * of the item - including if the item was already on the recyclerview list. */
        //Log.d(TAG, "addItem() called");
        //Log.d(TAG, "inside addItem and toggleEditAisle value is " + toggleEditAisle.isChecked());
        //Log.d(TAG, "inside addItem and toggleDelete value is " + toggleDelete.isChecked());
        String name = autoCompTxtViewItemName.getText().toString().trim();
        autoCompTxtViewItemName.getText().clear();

        if (name.length() == 0) {
            return;
        }

        String nameCapitalised = name.substring(0, 1).toUpperCase() + name.substring(1);
        /*String nameSuggestionShortcut = null;
        if (name.length() > 5) {
            nameSuggestionShortcut = name.substring(0, 4);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // On JellyBean & above, you can provide a shortcut and an explicit Locale
            UserDictionary.Words.addWord(this, nameCapitalised, 250, nameSuggestionShortcut, Locale.getDefault());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            UserDictionary.Words.addWord(this, nameCapitalised, 250, UserDictionary.Words.LOCALE_TYPE_CURRENT);
        } */

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
            updateAutoCompleteTextViewArray();
        }
        mAdapter.swapCursorGrocery(getAllItems());
        recyclerView.scrollToPosition(mAdapter.getItemPosition(nameCapitalised));
    }


    public void onEditSpinner(View view) {
        //Log.d(TAG, "onEditSpinner: ");
        /*Intent intent = new Intent(this, EditSuperMarketsInfo.class);
        startActivity(intent);*/
        if (toggleDelete.isChecked()) {
            mHandlerToggleFlash.postDelayed(mToggleFlashRunnable, 700);
            mHandlerToggleFlash.postDelayed(mToggleFlashRunnable, 1400);
            myShowSnackBar(R.string.snack_message_press_done_deleting);
            //Snackbar.make(findViewById(R.id.rootLayout), R.string.snack_message_press_done_deleting, Snackbar.LENGTH_SHORT).show();
        } else if (toggleEditAisle.isChecked()) {
            mHandlerToggleFlash.postDelayed(mToggleFlashRunnable, 700);
            mHandlerToggleFlash.postDelayed(mToggleFlashRunnable, 1400);
            myShowSnackBar(R.string.snack_message_press_save_changes);
            //Snackbar.make(findViewById(R.id.rootLayout), R.string.snack_message_press_save_changes, Snackbar.LENGTH_SHORT).show();
        } else {
            closeKeyboard();
            setLayoutEditSpinner();
        }
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

        if ((newMarketName.length() == 0) || (newMarketLocation.length() == 0)){
            myShowSnackBar(R.string.snack_message_market_name_or_location_empty);
            //showDialogUnacceptableAisle();
            return;
        }

        if (GroceryAdapter.marketIsAlreadyInTable(mDatabase, newMarketName, newMarketLocation)) {
            closeKeyboard();
            // MAYBE INSTEAD OF SNACKBAR, MAKE A TEXTVIEW APPEAR SAYING IN RED, "MarketName (Location) already exists."
            myShowSnackBar(R.string.snack_message_market_already_in_table);
            //Snackbar.make(findViewById(R.id.rootLayout), R.string.snack_message_market_already_in_table, Snackbar.LENGTH_SHORT).show();
        } else {

            addMarketToTable(newMarketName, newMarketLocation);

            /*TextView textViewSpinnerEmpty = findViewById(R.id.textViewSpinnerEmpty);
            textViewSpinnerEmpty.setVisibility(View.GONE); */
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

        //Button buttonDeleteAll = findViewById(R.id.button_delete_all);

        if (mIsSpinnerBeingEdited) {
            buttonEditSpinner.setText(getString(R.string.button_spinner_edit_back));
            buttonEditSpinner.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            textViewName.setVisibility(View.VISIBLE);
            editTextName.setVisibility(View.VISIBLE);
            textViewLocation.setVisibility(View.VISIBLE);
            editTextLocation.setVisibility(View.VISIBLE);
            confirmButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);

            recyclerView.setVisibility(View.GONE);
            buttonAddItem.setVisibility(View.GONE);
            autoCompTxtViewItemName.setVisibility(View.GONE);
            toggleEditAisle.setVisibility(View.GONE);
            toggleDelete.setVisibility(View.GONE);
            findViewById(R.id.buttonReorder).setVisibility(View.GONE);

            //buttonDeleteAll.setVisibility(View.GONE);

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
            autoCompTxtViewItemName.setVisibility(View.VISIBLE);
            toggleEditAisle.setVisibility(View.VISIBLE);
            toggleDelete.setVisibility(View.VISIBLE);
            findViewById(R.id.buttonReorder).setVisibility(View.VISIBLE);

            //buttonDeleteAll.setVisibility(View.VISIBLE);

            closeKeyboard();
            if (spinnerMarketArray.size() == 0) {
                buttonEditSpinner.setText(getString(R.string.spinner_edit_add_new_market));
                buttonEditSpinner.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);

            } else {
                buttonEditSpinner.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                buttonEditSpinner.setText(getString(R.string.button_spinner_edit));
            }
            editTextName.getText().clear();
            editTextLocation.getText().clear();
        }
    }


    public void onReorder(View view) {
        if (toggleEditAisle.isChecked()) {
            mHandlerToggleFlash.postDelayed(mToggleFlashRunnable, 700);
            mHandlerToggleFlash.postDelayed(mToggleFlashRunnable, 1400);
            myShowSnackBar(R.string.snack_message_press_save_changes);
        } else if (toggleDelete.isChecked()) {
            mHandlerToggleFlash.postDelayed(mToggleFlashRunnable, 700);
            mHandlerToggleFlash.postDelayed(mToggleFlashRunnable, 1400);
            myShowSnackBar(R.string.snack_message_press_done_deleting);
        } else {
            mCurrentOrder = (mCurrentOrder + 1) % 3;
            mAdapter.swapCursorGrocery(getAllItems());
            if (mCurrentOrder == ORDER_AISLE_THEN_ALPHABET) {
                myShowSnackBar(R.string.snack_message_reorder_aisle);
            } else if (mCurrentOrder == ORDER_IN_TROLLEY_THEN_AISLE_THEN_ALPHABET) {
                myShowSnackBar(R.string.snack_message_reorder_in_trolley);
            } else if (mCurrentOrder == ORDER_ALPHABET) {
                myShowSnackBar(R.string.snack_message_reorder_alphabetic);
            }
        }
    }

    private void setRecyclerViewListeners() {
        Log.d(TAG, "setRecyclerViewListeners: ");
        mAdapter.setOnItemListener(new GroceryAdapter.OnItemListener() {
            @Override
            public void onCheckBox(int position) {
                Log.d(TAG, "onCheckBox:");
                toggleInTrolley(position); //indirectly tick/untick the checkbox.
            }
            /* @Override
            public void onTextViewProductName(int position, boolean hasFocus) {
                Log.d(TAG, "onTextViewProductName: Position of textView whose focus changed: " + position + ". Has focus? " + hasFocus);
            } */
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
            myShowSnackBar(R.string.snack_message_press_save_changes);
            //Snackbar.make(findViewById(R.id.rootLayout), R.string.snack_message_press_save_changes, Snackbar.LENGTH_SHORT).show();
        }

        mAdapter.swapCursorGrocery(getAllItems());
    }


    private void setToggleDeleteListener() {
        toggleDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && toggleEditAisle.isChecked()) { //only one toggle should checked at a time
                    toggleDelete.setChecked(false);
                    mHandlerToggleFlash.postDelayed(mToggleFlashRunnable, 600);
                    mHandlerToggleFlash.postDelayed(mToggleFlashRunnable, 1200);
                    myShowSnackBar(R.string.snack_message_press_save_changes);
                    //Snackbar.make(findViewById(R.id.rootLayout), R.string.snack_message_press_save_changes, Snackbar.LENGTH_SHORT).show();
                } else if (isChecked) {
                    closeKeyboard();
                    requiredItemDeletionSuccession = 2;
                    //Log.d(TAG, "toggleDelete is checked");
                    toggleDelete.setTextColor(Color.DKGRAY);
                    toggleDelete.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(224, 67, 91)));
                    mSwipeable = true; // make list swipeable
                    myShowSnackBar(R.string.snack_message_swipe_to_delete);
                } else {
                    //Log.d(TAG, "toggleDelete is NOT checked");
                    toggleDelete.setTextColor(Color.BLACK);
                    toggleDelete.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(107, 214, 213)));
                    mSwipeable = false; // make list unswipeable
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void clearGroceryList() {
        int numberOfItemsInList = mAdapter.getItemCount();
        int numberOfItemsTicked = mAdapter.getTickedCount();
        ContentValues cv = new ContentValues();

        if (numberOfItemsTicked == 0) {
            //remove all items from list and make
            cv.put(GroceryContract.GroceryEntry.COLUMN_IN_LIST, SQL_FALSE);
            cv.put(GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY, SQL_FALSE);
            String[] mySelectionArgs = {String.valueOf(SQL_TRUE)};
            //update(java.lang.String, android.content.ContentValues, java.lang.String, java.lang.String[])
            int numRowsUpdated = mDatabase.update(GroceryContract.GroceryEntry.TABLE_NAME,
                    cv,
                    GroceryContract.GroceryEntry.COLUMN_IN_LIST + " =?",
                    mySelectionArgs);
            //Log.d(TAG, "number of rows updated: " + numRowsUpdated); //Integer is automatically converted to String if needed.
            mAdapter.swapCursorGrocery(getAllItems());
            toggleDelete.setChecked(false);
            toggleDelete.setTextColor(Color.BLACK);
            toggleDelete.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(107, 214, 213)));
            mSwipeable = false; // make list unswipeable
            Log.d(TAG, "clearGroceryList: " + numRowsUpdated);

        } else if (numberOfItemsInList > numberOfItemsTicked) {
            //remove all ticked items from list
            cv.put(GroceryContract.GroceryEntry.COLUMN_IN_LIST, SQL_FALSE);
            cv.put(GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY, SQL_FALSE);
            String[] mySelectionArgs = {String.valueOf(SQL_TRUE)};
            //update(java.lang.String, android.content.ContentValues, java.lang.String, java.lang.String[])
            int numRowsUpdated = mDatabase.update(GroceryContract.GroceryEntry.TABLE_NAME,
                    cv,
                    GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY + " =?",
                    mySelectionArgs);
            //Log.d(TAG, "number of rows updated: " + numRowsUpdated); //Integer is automatically converted to String if needed.
            mAdapter.swapCursorGrocery(getAllItems());
            Log.d(TAG, "onClear: number of items cleared: " + numRowsUpdated);

        } else {
            //something went wrong
        }
    }


    private void setToggleEditAisleListener() {
        toggleEditAisle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && toggleDelete.isChecked()) { //only one toggle should checked at a time
                    toggleEditAisle.setChecked(false);
                    mHandlerToggleFlash.postDelayed(mToggleFlashRunnable, 600);
                    mHandlerToggleFlash.postDelayed(mToggleFlashRunnable, 1200);
                    myShowSnackBar(R.string.snack_message_press_done_deleting);
                    //Snackbar.make(findViewById(R.id.rootLayout), R.string.snack_message_press_done_deleting, Snackbar.LENGTH_SHORT).show();
                } else if (isChecked) {
                    //Log.d(TAG, "toggleEditAisle is checked");
                    toggleEditAisle.setTextColor(Color.DKGRAY);
                    toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(224, 67, 91)));
                    mAdapter.alternateIsToggleEditAisleCheckedValue();
                    //toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("red")));
                } else {
                    //Log.d(TAG, "toggleEditAisle is NOT checked");
                    toggleEditAisle.setTextColor(Color.BLACK);
                    toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(107, 214, 213)));
                    mAdapter.alternateIsToggleEditAisleCheckedValue();
                    //toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("green")));
                    if (GroceryAdapter.marketTableIsNotEmpty(mDatabase)) {
                        saveAisles();
                    } else {
                        myShowSnackBar(R.string.snack_message_add_a_supermarket);
                        //Snackbar.make(findViewById(R.id.rootLayout), R.string.snack_message_add_a_supermarket, Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    private Runnable mToggleFlashRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            flash();
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void flash() {
        if (toggleEditAisle.isChecked() && (!toggleDelete.isChecked())) {
            int color = toggleEditAisle.getCurrentTextColor();
            if (color == Color.BLACK) {
                toggleEditAisle.setTextColor(Color.DKGRAY);
                toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(224, 67, 91)));
            } else {
                toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(107, 214, 213)));
                toggleEditAisle.setTextColor(Color.BLACK);
            }
        } else if (toggleDelete.isChecked() && (!toggleEditAisle.isChecked())) {
            int color = toggleDelete.getCurrentTextColor();
            if (color == Color.BLACK) {
                toggleDelete.setTextColor(Color.DKGRAY);
                toggleDelete.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(224, 67, 91)));
            } else {
                toggleDelete.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(107, 214, 213)));
                toggleDelete.setTextColor(Color.BLACK);
            }
        } else {
            Log.d(TAG, "flash: ERROR - somehow, both toggles where checked at the time this fxn was called :(");
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void saveAisles() {
        //Log.d(TAG, "saveAisles() called");
        View v; // v will be each item (i.e. one set of aisleNumber, productName, checkBox views) in recyclerview.
        boolean atLeastOneAisleIsInvalid = false;
        String nameOfFirstItemWithInvalidAisle = "";
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            //Log.d(TAG, "saveAisles: we are at the " + String.valueOf(i) + "th value of the recyclerview ------");
            v = recyclerView.getChildAt(i); // v encapsulates a pair of aisle editText and product name EditText.
            EditText aisleEditText = v.findViewById(R.id.editTextAisleNumber);
            TextView productNameTextView = v.findViewById(R.id.textViewProductName);
            String aisleNumberStr = aisleEditText.getText().toString().trim();
            String productNameStr = productNameTextView.getText().toString().trim();
            Log.d(TAG, "saveAisles: aisle found --> " + aisleNumberStr);
            Log.d(TAG, "saveAisles: product found --> " + productNameStr);
            Log.d(TAG, "saveAisles:  the position of the item is: " + i);
            //Log.d(TAG, "saveAisles: selectedMarketGroceryColumnName: " + GroceryAdapter.getSelectedMarketGroceryListColumnName(mDatabase));
            if (aisleOrProductIsValid(aisleNumberStr, productNameStr)) {
                updateAisle(aisleNumberStr, productNameStr);
            } else {
                if (!atLeastOneAisleIsInvalid) {//check if atLeastOneAisleIsInvalid was already set to true because we will scroll to the first invalid aisle.
                    atLeastOneAisleIsInvalid = true;
                    nameOfFirstItemWithInvalidAisle = productNameStr;
                }
                //break;
            }
            //aisleEditText.setKeyListener(null); //makes the EditText non-editable so, it acts like a TextView.
        }
        if (atLeastOneAisleIsInvalid) {
            toggleEditAisle.setChecked(true);
            toggleEditAisle.setTextColor(Color.DKGRAY);
            toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(224, 67, 91)));
            mAdapter.alternateIsToggleEditAisleCheckedValue();
            recyclerView.scrollToPosition(mAdapter.getItemPosition(nameOfFirstItemWithInvalidAisle));
        }
    }


    private boolean aisleOrProductIsValid(String aisleNumberStr, String productNameStr) {
        if (!aisleNumberStr.equals("?") && (aisleNumberStr.length() > 0) && (!isNumeric(aisleNumberStr))) {
            //myShowSnackBar(R.string.snack_message_invalid_aisle);
            showDialogUnacceptableAisle();
            return false;
        /*} else if (productNameStr.length() == 0) {
            showUnacceptableInputDialog(EMPTY_TEXT);
            return false; */
        } else {
            return true;
        }
    }


    private void updateAisle(String aisleNumberStr, String productNameStr) {
        ContentValues cv = new ContentValues();
        if (aisleNumberStr.equals("?")) {
            cv.put(GroceryAdapter.getSelectedMarketGroceryListColumnName(mDatabase), -1);
        } else if (aisleNumberStr.length() == 0) {
            cv.put(GroceryAdapter.getSelectedMarketGroceryListColumnName(mDatabase), mAdapter.getAisleFromName(productNameStr));
        } else {
            cv.put(GroceryAdapter.getSelectedMarketGroceryListColumnName(mDatabase), Integer.parseInt(aisleNumberStr));
        }
        String[] mySelectionArgs = {productNameStr};
        int numRowsUpdated = mDatabase.update(GroceryContract.GroceryEntry.TABLE_NAME,
                cv,
                GroceryContract.GroceryEntry.COLUMN_NAME + " =?",
                mySelectionArgs);
        //Log.d(TAG, "updateAisle: number of rows updated: " + numRowsUpdated); //Integer is automatically converted to String if needed
        mAdapter.swapCursorGrocery(getAllItems());
    }


    private boolean isNumeric(String strNum) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
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
                    requiredItemDeletionSuccession--;
                    int SUCCESSIVE_DELETIONS_TRIGGER = 0;
                    int MIN_ITEM_AMOUNT_FOR_CLEAR_LIST = 3; //if there are 4 remaining items on the list at the time.
                    if ((requiredItemDeletionSuccession == SUCCESSIVE_DELETIONS_TRIGGER) && (mAdapter.getItemCount() >= MIN_ITEM_AMOUNT_FOR_CLEAR_LIST)) {
                        showDialogAskToClearList();
                        /* show a dialog pop up that asks, would you like to clear the list?
                        If the user presses "Yes, Clear List" then another pop up appears asking if they wanna clear all or clear just the ticked or if they wanna cancel.
                        If the user presses "No", then set itemsDeletedInSuccession to -5, so that next time it will take 5 deletes, instead of 2, to trigger the "clear all?" pop up.
                        Also, take into account how many items are in the list. If there are 4 or less, then "clear all" will not pop up.
                        */
                    }
                }
                /* else {
                    Snackbar.make(findViewById(R.id.rootLayout), R.string.snack_message_press_delete_items, Snackbar.LENGTH_SHORT).show();
                    mAdapter.swapCursorGrocery(getAllItems()); //before I figured out proper way (i.e. disabling swipe altogether).
                } */
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
        if (mCurrentOrder == ORDER_AISLE_THEN_ALPHABET) {
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

        } else if (mCurrentOrder == ORDER_IN_TROLLEY_THEN_AISLE_THEN_ALPHABET) {
            String[] mySelectionArgs = new String[]{String.valueOf(SQL_TRUE)};
            return mDatabase.query(
                    GroceryContract.GroceryEntry.TABLE_NAME,
                    null,
                    GroceryContract.GroceryEntry.COLUMN_IN_LIST + " =?", //equivalent to WHERE. A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows for the given table.
                    mySelectionArgs, //You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
                    null,
                    null,
                    GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY + " ASC, " + GroceryAdapter.getSelectedMarketGroceryListColumnName(mDatabase) + " ASC, " + GroceryContract.GroceryEntry.COLUMN_NAME + " ASC"
            );

        } else if (mCurrentOrder == ORDER_ALPHABET) {
            String[] mySelectionArgs = new String[]{String.valueOf(SQL_TRUE)};
            return mDatabase.query(
                    GroceryContract.GroceryEntry.TABLE_NAME,
                    null,
                    GroceryContract.GroceryEntry.COLUMN_IN_LIST + " =?", //equivalent to WHERE. A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows for the given table.
                    mySelectionArgs, //You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
                    null,
                    null,
                    GroceryContract.GroceryEntry.COLUMN_NAME + " ASC"
            );

        } else {
            Log.d(TAG, "getAllItems: error: mCurrentOrder should be 0-2 inclusive");
        }
        return null; //something went wrong
    }


    public void closeKeyboard() {
        View view = findViewById(R.id.spinner);
        if (view != null) {
            //Log.d("MainActivitee", "Inside if statement of closeKeboard().");
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void myShowSnackBar(int resourceID) {
        Snackbar snackbar =   Snackbar.make(findViewById(R.id.rootLayout), resourceID, Snackbar.LENGTH_LONG);

        TextView snackbarActionTextView = snackbar.getView().findViewById( com.google.android.material.R.id.snackbar_action );
        snackbarActionTextView.setTextSize( 30 );
        //snackbarActionTextView.setTypeface(snackbarActionTextView.getTypeface(), Typeface.BOLD);
        snackbar.setAction("Ok", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //your click action.
            }
        });

        //Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout)snackbar.getView();
        //layout.setMinimumHeight(300);//your custom height.
        TextView snackbarTextView = snackbar.getView().findViewById( com.google.android.material.R.id.snackbar_text );
        snackbarTextView.setTextSize( 30 );
        snackbar.show();
    }


    private void showDialogUnacceptableAisle() {
        String title = "Aisle must be either:\nNUMBER, EMPTY, or '?'";
        String message = "EMPTY: sets aisle to what it was previously.\n'?' sets the aisle to unknown, like it was originally.";
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("ok", listener)
                .create().show();
    }


    private void showDialogAskToClearList() {
        int numberOfItemsInList = mAdapter.getItemCount();
        int numberOfItemsTicked = mAdapter.getTickedCount();
        Log.d(TAG, "showDialogAskToClearList: numberOfItemsInList: " + numberOfItemsInList);
        Log.d(TAG, "showDialogAskToClearList: numberOfItemsTicked: " + numberOfItemsTicked);
        String title = "Clear List?";
        String message = "";
        if (numberOfItemsTicked == 0) {
            Log.d(TAG, "showDialogAskToClearList: numberOfItemsInList == numberOfItemsTicked");
            message = "Remove all items from list?";
        } else if (numberOfItemsInList > numberOfItemsTicked) {
            Log.d(TAG, "showDialogAskToClearList: numberOfItemsInList > numberOfItemsTicked");
            message = "Remove all ticked items from list?";
        }

        DialogInterface.OnClickListener listenerYes = new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "onClick: about to call clearGroceryList()");
                requiredItemDeletionSuccession = 2;
                clearGroceryList();
                dialog.dismiss();
            }
        };

        DialogInterface.OnClickListener listenerNo = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "onClick: don't delte anything");
                requiredItemDeletionSuccession = 5;
                dialog.dismiss();
            }
        };

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", listenerYes)
                .setNegativeButton("No", listenerNo)
                .create().show();
    }

    /*
    Notes:
    1) when you try to add an item and there is no supermarket selected, it just automatically makes it market1AisleLocation
    2) remember that you decided that when you press the checkbox, it doesn't update the aisle, it just sets inTrolley to true/false.
    3) remember that the onCreate method is called every time for some reason, so your assumption that it will be called may cause problems in the actual phone.
    */

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
    Done implementations:
    >> Make the program only accept reasonable values for the item names and aisle numbers and supermarket names.
    >> Make the toggle button that needs to be pressed flash between blue and red for a few seconds (i.e. change its color to blue and wait a few seconds then change to red).
    >> A textView above the ticked column in the list that displays the x/y, where x is number of ticked items and y is number of items in trolley. This value of this textview is
    initiated in onCreate and updated whenever an item is added or removed.
    >> When you press "Delete Item(s)", the "clear" button appears and it clears all ticked items or (if there are none) it clears all unticked items.
    */

    /*
    Future implementations:
    >> Allow user to input quantity, e.g. 4.5kg x Carrot. The default is Item name (without quantity)
    >> Make the EditTexts of the list not allow keyboard pop up when not in "edit mode".
    >> Make background thread (see your java google docs for link)
    >> Have a button to recover all deleted, so you don't have to display which ones were deleted.
    >> Have a dictionary that looks at all items you added
    >> Allow copy and paste if it's separated by a comma or
    >> If you long press an item or supermarket, you delete it (except you can recover all supermarkets)
     */

}

