package com.example.grocerylist3;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GroceryAdapter extends RecyclerView.Adapter <GroceryAdapter.GroceryViewHolder> {
    private static final String TAG = "GrocerAdaptr*<*<*<*<*<*";
    private LinearLayoutManager mManager;
    private Context mContext;
    private Cursor mCursor;

    public GroceryAdapter(LinearLayoutManager manager, Cursor cursor, Context context) {
        Log.d(TAG, "GroceryAdapter() called");
        mManager = manager;
        mContext = context;
        mCursor = cursor;
    }

    public class GroceryViewHolder extends RecyclerView.ViewHolder {
        public EditText nameText;
        public EditText aisleText;
        public CheckBox checkBox;

        public GroceryViewHolder(@NonNull View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.edittext_product_name);
            aisleText = itemView.findViewById(R.id.edittext_aisle_number);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    @NonNull
    @Override
    public GroceryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder() called");
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.grocery_item, parent, false);
        return new GroceryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroceryViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder() called");
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        Log.d(TAG, "position is " + position);

        //mManager.scrollToPosition(0); //this always sets the recyclerviewer to go to the top
        //mManager.scrollToPosition(position); //this does crazy shit and I think it's because onBindViewHolder is not just called when addItem is called (I know this for a fact).

        String name = mCursor.getString(mCursor.getColumnIndex(GroceryContract.GroceryEntry.COLUMN_NAME));
        Integer aisle = mCursor.getInt(mCursor.getColumnIndex(GroceryContract.GroceryEntry.COLUMN_MARKET1_AISLE));
        long id = mCursor.getLong(mCursor.getColumnIndex(GroceryContract.GroceryEntry._ID));

        holder.nameText.setText(name);
        Log.d(TAG, "name is " + name);
        holder.itemView.setTag(id); //An ItemView in Android can be described as a single row item in a list. It references an item from where we find the view from its layout file.

        if (aisle.equals(-1)) {
            holder.aisleText.setText("?");
        } else {
            holder.aisleText.setText(String.valueOf(aisle));
        }
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public Integer newItemPosition(String newItemName) {
        Integer position = 0;
        //since we know we just added this item to the list, moveToNext() will never return null
        for (mCursor.moveToFirst();
             !newItemName.equals(mCursor.getString(mCursor.getColumnIndex(GroceryContract.GroceryEntry.COLUMN_NAME)));
             mCursor.moveToNext()) {
            position++;
        }
        Log.d(TAG, "Inside NewItemPosition, position is: " + position);
        return position;
    }

    //every time we update database, we have to pass a new cursor.
    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close(); //close and get rid of cursor
        }

        mCursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged(); //update recyclerview
        }
    }
}
