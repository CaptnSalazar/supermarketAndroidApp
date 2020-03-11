package com.example.grocerylist3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class EditSuperMarketsInfo extends AppCompatActivity {
    private static final String TAG = "EditSuperMarkets0000000";
    public static final String EXTRA_MESSAGE_MARKET_INFO = "com.example.grocerylist3.editsupermarketsinfo.NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_super_markets_info);
    }


    public void onConfirmMarket(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        EditText editTextMarketName = findViewById(R.id.editTextMarketName);
        String messageMarketInfo = editTextMarketName.getText().toString();
        intent.putExtra(EXTRA_MESSAGE_MARKET_INFO, messageMarketInfo);
        //I added this flag so it doesn't add a new instance of Home Activity to the stack.
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
