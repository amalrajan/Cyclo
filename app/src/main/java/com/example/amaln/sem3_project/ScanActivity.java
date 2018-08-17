package com.example.amaln.sem3_project;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

public class ScanActivity extends AppCompatActivity {



    private static final String TAG = "ScanActivity";
    private CodeScanner mCodeScanner;
    private CodeScannerView mScannerView;
    private Button mOkayButton;
    private TextView mAcceptPermission;
    private BluetoothAdapter mBluetoothAdapter;


    private final BroadcastReceiver mBluetoothStateChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        mScannerView.setVisibility(View.GONE);
                        enableBluetooth();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        checkPermissionGrantedAndOpenScanner();
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothStateChangeReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mOkayButton = findViewById(R.id.button_okay);
        mAcceptPermission = findViewById(R.id.text_view_accept_permission);
        mScannerView = findViewById(R.id.scanner_view);
        mScannerView.setVisibility(View.GONE);
        mCodeScanner = new CodeScanner(this, mScannerView);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter bluetoothStateIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothStateChangeReceiver, bluetoothStateIntentFilter);

        if(!mBluetoothAdapter.isEnabled()){
            enableBluetooth();
            mScannerView.setVisibility(View.GONE);
        } else {
            mScannerView.setVisibility(View.VISIBLE);
            checkPermissionGrantedAndOpenScanner();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    private void openScanner() {

        final Activity activity = ScanActivity.this;

        updateUI(1);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                Intent ridingIntent = new Intent(ScanActivity.this, RidingActivity.class);
                ridingIntent.putExtra("bluetooth_address", Constants.ARDUINO_ADDRESS);
                startActivity(ridingIntent);
            }
        });
        mScannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    private void checkPermissionGrantedAndOpenScanner() {
        // Check location permission is granted - if it is, start
        // the service, otherwise request the permission
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            updateUI(Constants.PERMSISSION_GRANTED);
            openScanner();
        } else {
            updateUI(Constants.PERMISSION_NOT_GRANTED);
            mOkayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(ScanActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.PERMISSIONS_REQUEST);
                }
            });
        }
    }

    private void enableBluetooth() {
        Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(enableBTIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == Constants.PERMISSIONS_REQUEST && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateUI(Constants.PERMSISSION_GRANTED);
            openScanner();
        }
    }

    private void updateUI(int state) {
        if (state == Constants.PERMSISSION_GRANTED) {
            mAcceptPermission.setVisibility(View.GONE);
            mOkayButton.setVisibility(View.GONE);
            mScannerView.setVisibility(View.VISIBLE);
        } else if (state == Constants.PERMISSION_NOT_GRANTED) {
            mScannerView.setVisibility(View.GONE);
            mAcceptPermission.setVisibility(View.VISIBLE);
            mOkayButton.setVisibility(View.VISIBLE);
        }

    }
}
