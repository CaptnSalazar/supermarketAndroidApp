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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GroceryAdapter extends RecyclerView.Adapter <GroceryAdapter.GroceryViewHolder> {
    private static final String TAG = "GrocerAdaptr*<*<*<*<*<*";
    private Context mContext;
    private Cursor mCursorGrocery;
    private Cursor mCursorMarket;
    private OnItemClickListener mListener;
    private int positionMarketSelected;


    public interface OnItemClickListener {
        /*An interface is a completely "abstract class" that is used to group related methods with empty bodies.
        * To access the interface methods, the interface must be "implemented" (kinda like inherited) by another
        *  class with the implements keyword (instead of extends). The body of the interface method is provided
        * by the "implement" class (see onCheckBox in the MainActivity class).  */

        //void onItemClick(int position);
        void onCheckBox(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }


    public GroceryAdapter(Cursor cursorGrocery, Cursor cursorMarket, Context context) {
        Log.d(TAG, "GroceryAdapter() called");
        mContext = context;
        mCursorGrocery = cursorGrocery;
        mCursorMarket = cursorMarket;
    }


    public class GroceryViewHolder extends RecyclerView.ViewHolder {
        public EditText nameText;
        public EditText aisleText;
        public CheckBox checkBox;

        public GroceryViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            nameText = itemView.findViewById(R.id.edittext_product_name);
            aisleText = itemView.findViewById(R.id.edittext_aisle_number);
            checkBox = itemView.findViewById(R.id.checkBox);

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onCheckBox(position);
                        }
                    }
                }
            });
        }
    }


    @NonNull
    @Override
    public GroceryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder() called");
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.grocery_item, parent, false);
        return new GroceryViewHolder(view, mListener);
    }


    @Override
    public void onBindViewHolder(@NonNull GroceryViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder() called");
        if (!mCursorGrocery.moveToPosition(position)) {
            return;
        }

        String name = mCursorGrocery.getString(mCursorGrocery.getColumnIndex(GroceryContract.GroceryEntry.COLUMN_NAME));
        Integer aisle = mCursorGrocery.getInt(mCursorGrocery.getColumnIndex(GroceryContract.GroceryEntry.COLUMN_MARKET1_AISLE));
        Integer isInTrolley = mCursorGrocery.getInt(mCursorGrocery.getColumnIndex(GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY));
        long id = mCursorGrocery.getLong(mCursorGrocery.getColumnIndex(GroceryContract.GroceryEntry._ID));

        holder.nameText.setText(name);
        holder.checkBox.setChecked(integerToBoolean(isInTrolley));
        holder.itemView.setTag(id); //An ItemView in Android can be described as a single row item in a list. It references an item from where we find the view from its layout file.

        if (aisle.equals(-1)) {
            holder.aisleText.setText("?");
        } else {
            holder.aisleText.setText(String.valueOf(aisle));
        }
    }


    @Override
    public int getItemCount() {
        //This returns the total number of rows, not the number of rows currently in the list.
        return mCursorGrocery.getCount();
    }

    public int getMarketCount() { return mCursorMarket.getCount(); }


    private boolean integerToBoolean(Integer number) {
        if (number == 0) {
            return false;
        } else {
            return true;
        }
    }


    public Integer getItemPosition(String itemName) {
        Integer position = 0;
        //since we know we just added this item to the list, moveToNext() will never return null
        for (mCursorGrocery.moveToFirst();
             !itemName.equals(mCursorGrocery.getString(mCursorGrocery.getColumnIndex(GroceryContract.GroceryEntry.COLUMN_NAME)));
             mCursorGrocery.moveToNext()) {
            position++;
        }
        Log.d(TAG, "Inside NewItemPosition, position is: " + position);
        return position;
    }


    public String getItemName(int position) {
        mCursorGrocery.moveToPosition(position);
        String nameOfItemChecked = mCursorGrocery.getString(mCursorGrocery.getColumnIndex(GroceryContract.GroceryEntry.COLUMN_NAME));
        Log.d(TAG, "Inside addItemToTrolley(), product checked is: " + nameOfItemChecked);
        return nameOfItemChecked;
    }

    public Integer getInTrolleyValue(int position) {
        mCursorGrocery.moveToPosition(position);
        Integer isInTrolley = mCursorGrocery.getInt(mCursorGrocery.getColumnIndex(GroceryContract.GroceryEntry.COLUMN_IN_TROLLEY));
        Log.d(TAG, "Inside getInTrolleyValue(), WAS product in trolley? " + isInTrolley);
        return isInTrolley;
    }


    public List<Market> getMarketList() {
        Log.d(TAG, "getMarketList: ");
        List<Market> newSpinnerArray = new ArrayList<Market>();
        //iterate through all rows and append the name/location and ID to Market list.
        boolean moveSucceeded = mCursorMarket.moveToFirst();
        int position = 0;
        while (moveSucceeded) {
            Log.d(TAG, "getMarketList:       " + position + "th iteration of while loop.");
            String marketName = mCursorMarket.getString(mCursorMarket.getColumnIndex(GroceryContract.SupermarketsVisited.COLUMN_MARKET_NAME));
            String marketLocation = mCursorMarket.getString(mCursorMarket.getColumnIndex(GroceryContract.SupermarketsVisited.COLUMN_MARKET_LOCATION));
            Integer marketID = mCursorMarket.getInt(mCursorMarket.getColumnIndex(GroceryContract.SupermarketsVisited._ID));
            boolean marketIsSelected = integerToBoolean(mCursorMarket.getInt(mCursorMarket.getColumnIndex(GroceryContract.SupermarketsVisited.COLUMN_IS_MARKET_SELECTED)));
            Market market = new Market(marketName, marketLocation , marketID, marketIsSelected);
            newSpinnerArray.add(market);
            if (marketIsSelected) {
                positionMarketSelected = position;
                Log.d(TAG, "getMarketList:  market" + marketName + " is selected and has position: " + positionMarketSelected);
            }
            Log.d(TAG, "getMarketList: marketName: " + marketName);
            position++;
            moveSucceeded = mCursorMarket.moveToNext();
        }
        Log.d(TAG, "getMarketList:  F I N I S H E D !!!");
        return newSpinnerArray;
    }


    public int getPositionMarketSelected() {
        Log.d(TAG, "getPositionMarketSelected:   position of marketSelected is " + positionMarketSelected);
        return positionMarketSelected;
    }


    public void closeAllCursors() {
        if (mCursorMarket != null) {
            mCursorMarket.close(); //close and get rid of cursor
        }
        if (mCursorGrocery != null) {
            mCursorGrocery.close(); //close and get rid of cursor
        }
    }


    public boolean areAllCursorsClosed() {
        return mCursorGrocery.isClosed() && mCursorMarket.isClosed();
    }


    //every time we update database, we have to pass a new cursor..
    public void swapCursorGrocery(Cursor newCursor) {
        if (mCursorGrocery != null) {
            mCursorGrocery.close(); //close and get rid of cursor
        }

        mCursorGrocery = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged(); //update recyclerview
        }
    }


    //every time we update database, we have to pass a new cursor..
    public void swapCursorMarket(Cursor newCursor) {
        if (mCursorMarket != null) {
            mCursorMarket.close(); //close and get rid of cursor
        }

        mCursorMarket = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged(); //update recyclerview
        }
    }
}
