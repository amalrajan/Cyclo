package com.example.amaln.sem3_project;

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



    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBondStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.e(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    getSupportLoaderManager().restartLoader(Constants.BLUETOOTH_CONNECT_LOADER_ID,null,RidingActivity.this);
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
            Log.e(TAG,"BRAODCAST REICEVER");
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action))
            {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e(TAG,"Auto-entering pin: " + "1234");
                bluetoothDevice.setPin(Constants.BLUETOOTH_PIN.getBytes());
                abortBroadcast();

                Log.e(TAG,"pin entered and request sent...");
                mBluetoothAdapter.cancelDiscovery();
            }
        }
    };

    private BroadcastReceiver mFindDeviceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.e(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                Log.e(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                if(device.getAddress().equals(mAddress)) {
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

        mAddress = getIntent().getStringExtra("bluetooth_address");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        //Registering BroadCastRecievers
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mFindDeviceReceiver, discoverDevicesIntent);

        IntentFilter autoPairDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        autoPairDeviceIntent.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(mPairingRequestReceiver,autoPairDeviceIntent);

        IntentFilter bondStateFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBondStateReceiver, bondStateFilter);


        startBT();
    }

    private void startBT() {
        if(!checkPairedDevices()) {
            Log.e(TAG, "NOT YET PAIRED, TURNING ON DISCOVERY");
            enableDiscoverable();
            discoverDevices();
        } else {
            Log.e(TAG,"ALREADY PAIRED");
            getSupportLoaderManager().restartLoader(Constants.BLUETOOTH_CONNECT_LOADER_ID,null,this);
        }
    }

    private boolean checkPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        for(BluetoothDevice device : pairedDevices) {
            if(device.getAddress().equals(mAddress)) {
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

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.e(TAG, "btnDiscover: Canceling discovery.");
            mBluetoothAdapter.startDiscovery();
        }
        if(!mBluetoothAdapter.isDiscovering()){
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
                startBT();
                snackbar.dismiss();
                updateUI(Constants.ERROR);
            }
        });
        snackbar.show();
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new BluetoothConnectLoader(this, mAddress);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String state) {
        Log.e(TAG,state);
        updateUI(state);

    }

    //TODO: remove snackbars and use try again button and textview
    private void updateUI(String state) {
        if(state.equals(Constants.SUCCESS)) {
            mProgressBar.setVisibility(View.GONE );
            showSnackbar(findViewById(R.id.activity_riding),"Connect succes", Snackbar.LENGTH_SHORT);
        }
        else if(state.equals(Constants.ERROR)) {
            mProgressBar.setVisibility(View.VISIBLE);
            showSnackbar(findViewById(R.id.activity_riding),"Unable to connect, please try again", Snackbar.LENGTH_INDEFINITE);
        }
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<String> loader) {
        Log.e(TAG,"RESET");
    }
}

