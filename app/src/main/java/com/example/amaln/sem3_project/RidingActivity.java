package com.example.amaln.sem3_project;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class RidingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riding);

        String bluetoothAddress = getIntent().getStringExtra("bluetooth_address");

        TextView bluetoothAddressTextView = findViewById(R.id.bluetooth_address);
        bluetoothAddressTextView.setText(bluetoothAddress);

    }
}
