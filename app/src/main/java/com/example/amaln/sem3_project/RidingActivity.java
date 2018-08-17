package com.example.amaln.sem3_project;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;


import java.io.IOException;
import java.util.UUID;

public class RidingActivity extends AppCompatActivity {

    /*TEMP*/
    private static String TAG = "RidingActivity.class";
    private final String BLUETOOTH_PASSWORD = "123456789";
    /*TEMP*/
    private BluetoothDevice mBTDevice;
    private BluetoothAdapter mBluetoothAdapter;


    private String mAddress = null;
    private ProgressBar mProgressBar;
    private BluetoothSocket mBTSocket = null;
    private boolean mIsBTConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");



    private BroadcastReceiver mPairingRequestReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG,"BRAODCAST REICEVER");
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action))
            {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e(TAG,"Auto-entering pin: " + "1234");
                bluetoothDevice.setPin("1234".getBytes());
                abortBroadcast();

                //bluetoothDevice.createBond();
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
                    mBTDevice = device;
                    Log.e(TAG, "FOUND THE ARDUINO   " + mBTDevice.getAddress());
                    pairDevice(mBTDevice);
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

        //Registering BroadCastRecievers
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mFindDeviceReceiver, discoverDevicesIntent);

        IntentFilter autoPairDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        autoPairDeviceIntent.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(mPairingRequestReciever,autoPairDeviceIntent);

        enableDiscoverable();
        discoverDevices();

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

            //check BT permissions in manifest
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();
        }
        if(!mBluetoothAdapter.isDiscovering()){
            //check BT permissions in manifest
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();
        }
    }

    private void pairDevice(BluetoothDevice device) {
        Log.e(TAG, "PAIRING...");
        device.createBond();
        Log.e(TAG,"FINISHED PAIRING");
        new ConnectBT().execute();
    }

    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.e(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    protected void onDestroy() {
        Log.e(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mFindDeviceReceiver);
        unregisterReceiver(mPairingRequestReciever);
    }

    private void showSnackbar(View view, String message, int duration) {

        final Snackbar snackbar = Snackbar.make(view, message, duration);

        snackbar.setAction("TRY AGAIN", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ConnectBT().execute();
            }
        });
        snackbar.show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean connectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... devices) { //while the mProgress dialog is shown, the connection is done in background

            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;

            try {
                if (mBTSocket == null || !mIsBTConnected) {
                    BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(mAddress);//connects to the device's mAddress and checks if it's available
                    mBTSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    mBluetoothAdapter.cancelDiscovery();
                    mBTSocket.connect();//start connection

                    //sending data
                    mBTSocket.getOutputStream().write((BLUETOOTH_PASSWORD+"~").getBytes());

                    //Recieving data from arduino
                    while (true) {
                        try {
                            bytes += mBTSocket.getInputStream().read(buffer, bytes, buffer.length - bytes);
                            for(int i = begin; i < bytes; i++) {
                                if(buffer[i] == "~".getBytes()[0]) {
                                    String readInput = new String(buffer,0,bytes);
                                    Log.e(TAG,readInput);
                                    begin = i + 1;
                                    if(i == bytes - 1) {
                                        bytes = 0;
                                        begin = 0;
                                    }
                                    if(readInput.equals((BLUETOOTH_PASSWORD+"~"))) {
                                        Log.e(TAG,"UNLOCK SUCCES");
                                    }
                                }
                            }
                        } catch (IOException e) {
                            break;
                        }
                    }



                }
            } catch (IOException e) {
                connectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) { //after the doInBackground, it checks if everything went fine
         super.onPostExecute(result);

            if (!connectSuccess) {
                showSnackbar(findViewById(R.id.activity_riding),"Connection Failed", Snackbar.LENGTH_INDEFINITE);
            } else {
                mIsBTConnected = true;
            }
            mProgressBar.setVisibility(View.GONE);
        }
    }
}

