package com.example.grocerylist3;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.google.android.material.snackbar.Snackbar;

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
    List<Market> spinnerMarketArray;
    ArrayAdapter<Market> spinnerArrayAdapter;

    private String currentColumnMarketAisles = GroceryContract.GroceryEntry.COLUMN_MARKET1_AISLE;
    private Integer SQL_TRUE = 1;
    private Integer SQL_FALSE = 0;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GroceryDBHelper dbHelper = new GroceryDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        mAdapter = new GroceryAdapter(getAllItems(), this);
        recyclerView.setAdapter(mAdapter);
        mSwipeable = false;

        Intent intent = getIntent();
        String messageNewMarket = intent.getStringExtra(EditSuperMarketsInfo.EXTRA_MESSAGE_MARKET_INFO);
        //If we didn't check this, then the app would crash when it started.
        if (messageNewMarket != null) {
            Log.d(TAG, "onCreate: the new market is: " + messageNewMarket);
            addMarket(messageNewMarket);

        }

        mAdapter.setOnItemClickListener(new GroceryAdapter.OnItemClickListener() {
            @Override
            public void onCheckBox(int position) {
                toggleInTrolley(position); //indirectly tick/untick the checkbox.
            }
        });

        toggleDelete = findViewById(R.id.toggleButtonDeleteItem);
        toggleDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "toggleDelete is checked");
                    mSwipeable = true; // make list swipeable
                } else {
                    Log.d(TAG, "toggleDelete is NOT checked");
                    mSwipeable = false; // make list unswipeable
                    saveAisles();
                }
            }
        });

        toggleEditAisle = findViewById(R.id.toggleButtonEditSave);
        toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("green")));
        toggleEditAisle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "toggleEditAisle is checked");
                    toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("red")));
                } else {
                    Log.d(TAG, "toggleEditAisle is NOT checked");
                    toggleEditAisle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("green")));
                    saveAisles();
                }
            }
        });

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
                    mAdapter.swapCursor(getAllItems()); //don't wanna figure out proper way (i.e. disabling swipe altogether).
                }

            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                boolean swipeable = mSwipeable; //swipeable says it's redundant but causes problems if you just use mSwipeable.
                return swipeable;
            }
        }).attachToRecyclerView(recyclerView);

        mEditTextName = findViewById(R.id.edittext_new_product);

        Button buttonAdd = findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleEditAisle.isChecked()) {
                    //also make "Edit List" button glow
                    Snackbar.make(findViewById(R.id.rootLayout), R.string.snack_message_press_save_changes, Snackbar.LENGTH_SHORT).show();
                } else {
                    addItem();
                }
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

        mAdapter.swapCursor(getAllItems());
        //recyclerView.scrollToPosition(mAdapter.getItemPosition(nameCapitalised)); //maybe call this?
    }

    private void addMarket(String newMarket) {
        if (mEditTextName.getText().toString().trim ().length() == 0) {
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME, newMarket);
        cv.put(GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION, "Temporary Location");
        cv.put(GroceryContract.SupermarketsVisited.COLUMN_IS_MARKET_SELECTED, SQL_TRUE);
        mDatabase.insert(GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET, null, cv);
        mAdapter.swapCursor(getAllItems());
    }


    private void addItem() {
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
        mAdapter.swapCursor(getAllItems());

        mEditTextName.getText().clear();
        recyclerView.scrollToPosition(mAdapter.getItemPosition(nameCapitalised));
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
        mAdapter.swapCursor(getAllItems());
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
            mAdapter.swapCursor(getAllItems());
        }
    }


    public void onCheckboxClicked(View view) {
        EditText editTextProductName = view.findViewById(R.id.edittext_product_name);
        String productName = editTextProductName.getText().toString();

        ContentValues cv = new ContentValues();
        cv.put(GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY, SQL_TRUE);
        String[] mySelectionArgs = {productName};
        Integer numRowsUpdated = mDatabase.update(GroceryContract.GroceryEntry.TABLE_NAME,
                cv,
                GroceryContract.GroceryEntry.COLUMN_NAME + " =?",
                mySelectionArgs);
        Log.d(TAG, "number of rows updated: " + numRowsUpdated); //Integer is automatically converted to String if needed.
        mAdapter.swapCursor(getAllItems());

    }


    public void onEditSpinner(View view) {
        Intent intent = new Intent(this, EditSuperMarketsInfo.class);
        startActivity(intent);
    }


    public void onDeleteAllRows(View view) {
        Log.d(TAG, "onDeleteAllRows() called");
        mDatabase.execSQL("DELETE FROM " + GroceryContract.GroceryEntry.TABLE_NAME);
        mDatabase.execSQL("DELETE FROM " + GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET);
        mAdapter.swapCursor(getAllItems());
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
Future implementation:
0.2) For the spinner, just make a list from the database in the onCreate() method of main, and update
that list every time a new item is added. Also, add a column to SQL_CREATE_SUPERMARKETS_VISITED_TABLE
which has a value true/1 or false/0 that tells you whether that market is selected, otherwise the app
will reset back to the default market every time you close and open it.
0.5) When you press "Edit Spinner", a window pops up (OR MAYBE WE START ANOTHER ACTIVITY??) that has a "negative" button/option of "change name", and
a "positive" button/option of "add new". If you press change name, a picker pops up for you to select/find the
market name you wanna change and 2 editTexts one above the other appear with text to their left saying
"Location" with editText hint "e.g. Ilam Road", and "Company" with editText hint "e.g. Countdown", and
two buttons/options, "cancel" and "confirm".
1) Make the EditTexts of the list not allow keyboard pop up when not in "edit mode".
2) Autocompletion based on the items that already exist in database.
3) Make the Edit List button glow or point to it, when someone tries to do other things while in Edit mode.
4) when the user adds an item to the list that has very similar spelling to another,
make a window pop-up, saying that there already exists a record of [this other item]. Are they they the same?
Which spelling is right?
5) Make background thread (see your java google docs for link)
 */