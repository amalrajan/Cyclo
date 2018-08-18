package com.example.amaln.sem3_project;

import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.Set;
import java.util.UUID;

public class RidingActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    /*TEMP*/
    private static String TAG = "RidingActivity.class";
    private final String BLUETOOTH_PASSWORD = "123456789";
    /*TEMP*/

    private BluetoothAdapter mBluetoothAdapter;

    private String mAddress = null;

    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    ProgressBar mProgressBar;

    private TextView timeCounter;
    private TextView totalCost;
    private TextView totalDistance;

    private int seconds = 0;
    private double costPerSecond = 0.002777778;
    private int minimumCost = 5;

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBondStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.e(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    getSupportLoaderManager().restartLoader(Constants.BLUETOOTH_CONNECT_LOADER_ID, null, RidingActivity.this);
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.e(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.e(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    private BroadcastReceiver mPairingRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "BRAODCAST REICEVER");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e(TAG, "Auto-entering pin: " + "1234");
                bluetoothDevice.setPin(Constants.BLUETOOTH_PIN.getBytes());
                abortBroadcast();

                Log.e(TAG, "pin entered and request sent...");
                mBluetoothAdapter.cancelDiscovery();
            }
        }
    };

    private BroadcastReceiver mFindDeviceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.e(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                if (device.getAddress().equals(mAddress)) {
                    Log.e(TAG, "FOUND THE ARDUINO   " + device.getAddress());
                    device.createBond();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riding);

        initiateRide();

        mAddress = getIntent().getStringExtra("bluetooth_address");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        //Registering BroadCastRecievers
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mFindDeviceReceiver, discoverDevicesIntent);

        IntentFilter autoPairDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        autoPairDeviceIntent.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(mPairingRequestReceiver, autoPairDeviceIntent);

        IntentFilter bondStateFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBondStateReceiver, bondStateFilter);


        if (!checkPairedDevices()) {
            Log.e(TAG, "NOT YET PAIRED, TURNING ON DISCOVERY");
            enableDiscoverable();
            discoverDevices();
        } else {
            Log.e(TAG, "ALREADY PAIRED");
            getSupportLoaderManager().restartLoader(Constants.BLUETOOTH_CONNECT_LOADER_ID, null, this);
        }
    }

    private boolean checkPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().equals(mAddress)) {
                return true;
            }
        }
        return false;
    }

    public void enableDiscoverable() {
        Log.e(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 120 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
        startActivity(discoverableIntent);
    }

    public void discoverDevices() {
        Log.e(TAG, "btnDiscover: Looking for unpaired devices.");

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.e(TAG, "btnDiscover: Canceling discovery.");
            mBluetoothAdapter.startDiscovery();
        }
        if (!mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.startDiscovery();
        }
    }

    protected void onDestroy() {
        Log.e(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mFindDeviceReceiver);
        unregisterReceiver(mPairingRequestReceiver);
        unregisterReceiver(mBondStateReceiver);
    }

    private void showSnackbar(View view, String message, int duration) {

        final Snackbar snackbar = Snackbar.make(view, message, duration);

        snackbar.setAction("TRY AGAIN", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportLoaderManager().restartLoader(Constants.BLUETOOTH_CONNECT_LOADER_ID, null, RidingActivity.this);
            }
        });
        snackbar.show();
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new BluetoothConnectLoader(this, mAddress);
    }

    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<String> loader, String data) {
        Log.e(TAG, data);
        if (data.equals(Constants.SUCCESS)) {
            mProgressBar.setVisibility(View.GONE);
        } else if (data.equals(Constants.ERROR)) {
            showSnackbar(findViewById(R.id.activity_riding), "Unable to connect, please try again", Snackbar.LENGTH_INDEFINITE);
        }

    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<String> loader) {

    }

    /*
    InitiateRide: Starts the stopwatch, and displays the dashboard.
     */

    public void initiateRide() {
        Log.e("Reached here", "Success.");

        timeCounter = findViewById(R.id.text_view_time_taken);
        totalCost = findViewById(R.id.text_view_cost);
        totalDistance = findViewById(R.id.text_view_distance);

        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                String time = String.format("%d:%02d:%02d", hours, minutes, secs);

                timeCounter.setText(time);
                totalCost.setText(String.format("%.2f", (minimumCost + (costPerSecond * (hours * 3600 + minutes * 60 + secs)))));

                seconds++;

                handler.postDelayed(this, 1000);
            }
        });
    }
}

