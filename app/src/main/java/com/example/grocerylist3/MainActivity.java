package com.example.grocerylist3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity*<*<*<*<*<*";
    private SQLiteDatabase mDatabase;
    private GroceryAdapter mAdapter;
    private EditText mEditTextName;
    private EditText mEditTextAisle;
    private String currentColumnMarketAisles = GroceryContract.GroceryEntry.COLUMN_MARKET1_AISLE;
    private Integer sqlTrue = 1;
    private Integer sqlFalse = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GroceryDBHelper dbHelper = new GroceryDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new GroceryAdapter(this, getAllItems());
        recyclerView.setAdapter(mAdapter);

        mEditTextName = findViewById(R.id.edittext_name);
        mEditTextAisle = findViewById(R.id.edittext_aisle);

        Button buttonAdd = findViewById(R.id.button_add);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });
    }

    private void addItem() {

        if (mEditTextName.getText().toString().trim ().length() == 0) {
            return;
        }

        String name = mEditTextName.getText().toString();
        String nameCapitalised = name.substring(0, 1).toUpperCase() + name.substring(1);
        Integer aisle = Integer.parseInt(mEditTextAisle.getText().toString());
        ContentValues cv = new ContentValues();
        cv.put(GroceryContract.GroceryEntry.COLUMN_NAME, nameCapitalised);
        cv.put(currentColumnMarketAisles, aisle);

        mDatabase.insert(GroceryContract.GroceryEntry.TABLE_NAME, null, cv);
        mAdapter.swapCursor(getAllItems());
    }

    public void onDeleteAllRows(View view) {
        mDatabase.execSQL("DELETE FROM " + GroceryContract.GroceryEntry.TABLE_NAME);
        mDatabase.execSQL("DELETE FROM " + GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET);
        mAdapter.swapCursor(getAllItems());
    }


    private Cursor getAllItems() {
        String[] mySelectionArgs = new String[]{String.valueOf(sqlTrue)};
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
