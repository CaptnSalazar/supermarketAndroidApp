package com.example.grocerylist3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.google.android.material.snackbar.Snackbar;

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

    private String currentColumnMarketAisles = GroceryContract.GroceryEntry.COLUMN_MARKET1_AISLE;
    private Integer SQL_TRUE = 1;
    private Integer SQL_FALSE = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GroceryDBHelper dbHelper = new GroceryDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        /*recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new GroceryAdapter(this, getAllItems());
        recyclerView.setAdapter(mAdapter);
        Changed the 4 lines above to the following: */
        recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        mAdapter = new GroceryAdapter(manager, getAllItems(), this);
        recyclerView.setAdapter(mAdapter);
        mSwipeable = false;

        mAdapter.setOnItemClickListener(new GroceryAdapter.OnItemClickListener() {
            @Override
            public void onCheckBox(int position) {
                toggleInTrolley(position); //indirectly tick the checkbox
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
        toggleEditAisle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "toggleEditAisle is checked");
                } else {
                    Log.d(TAG, "toggleEditAisle is NOT checked");
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
                boolean swipeable = mSwipeable;
                return swipeable;
            }
        }).attachToRecyclerView(recyclerView);

        mEditTextName = findViewById(R.id.edittext_new_product);

        Button buttonAdd = findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });
    }


    public void toggleInTrolley(int position) {
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
        //maybe call swapCursor because getItemName changes the cursor position?
        mAdapter.swapCursor(getAllItems());
        //recyclerView.scrollToPosition(mAdapter.getItemPosition(nameCapitalised)); //maybe call this?
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

        if (!mAdapter.isItemInList(nameCapitalised)) {
            Log.d(TAG, "item is not in list");
            ContentValues cv = new ContentValues();
            cv.put(GroceryContract.GroceryEntry.COLUMN_NAME, nameCapitalised);
            cv.put(GroceryContract.GroceryEntry.COLUMN_IN_LIST, SQL_TRUE);
            cv.put(GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY, SQL_FALSE);
            String[] mySelectionArgs = {nameCapitalised};
            Integer numRowsUpdated = mDatabase.update(GroceryContract.GroceryEntry.TABLE_NAME,
                    cv,
                    GroceryContract.GroceryEntry.COLUMN_NAME + " =?",
                    mySelectionArgs);
            Log.d("MainActivity", "number of rows updated: " + String.valueOf(numRowsUpdated));
            if (numRowsUpdated < 1) {
                mDatabase.insert(GroceryContract.GroceryEntry.TABLE_NAME, null, cv);
            }
            mAdapter.swapCursor(getAllItems());
        }

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

/* I can now toggle the swipe feature of deletion by pressing the delete button. Also, ticking the checkboxes
make the inTrolley value toggle, which causes the checkbox to be ticked or not ticked.
PROBLEM: when i try to add an item to a list that's already in the list and is ticked, it unticked that item,
because I didn't check to see if the item was already in the list.
POSSIBLE SOLUTION:
1) when you add an item onto a list, don't do cv.put(GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY, SQL_FALSE);
just let it be its default value or if it already exists, then it will be that already existing value. Which
will be ticked if the item is already in the list and in the trolley, or if the item was removed from the
list then it will be unticked because I untick it before removing it from a list.
2) check if the item is already in the list - I tried this and the function isItemInList() causes bugs
sometimes when the item is NOT in the list --> it accesses an index 1+ out of range.
Future implementation:
1) when the user adds an item to the list that has very similar spelling to another,
make a window pop-up, saying that there already exists a record of [this other item]. Are they they the same?
Which spelling is right?
2) Autocompletion based on the items that already exist in database.
 */