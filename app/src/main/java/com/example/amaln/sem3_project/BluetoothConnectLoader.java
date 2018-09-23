package com.example.amaln.sem3_project;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnectLoader extends android.support.v4.content.AsyncTaskLoader<String> {

    /**
     * Input format: <password(10)>/<instruction>~
     */


    //TODO: Prevent user from going back to scan activity if they are riding

    private final String TAG = "BluetoothConnectLoader";
    private final String BLUETOOTH_PASSWORD = "123456789";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBTSocket;

    //this value is true until the cycle is returned
    private boolean mIsRiding;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private String mAddress;

    public BluetoothConnectLoader(Context context, String address) {
        super(context);
        mAddress = address;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //TODO: Add persistence so state(riding, not riding) is maintained even if app is restarted
        mIsRiding = false;
    }

    @Override
    public String loadInBackground() {

        byte[] buffer = new byte[1024];
        int begin = 0;
        int bytes = 0;

        try {
            if (mBTSocket == null && !mIsRiding) {
                BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(mAddress);//connects to the device's mAddress and checks if it's available
                mBTSocket = bluetoothDevice.createRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                mBluetoothAdapter.cancelDiscovery();
                mBTSocket.connect();//start connection

                //sending data
                mBTSocket.getOutputStream().write((BLUETOOTH_PASSWORD + "/" + Constants.UNLOCK_INSTRUCTION + "~").getBytes());

                //Recieving data from arduino
                while (true) {
                    try {
                        bytes += mBTSocket.getInputStream().read(buffer, bytes, buffer.length - bytes);
                        for(int i = begin; i < bytes; i++) {
                            if(buffer[i] == "~".getBytes()[0]) {
                                String readInput = new String(buffer,0,bytes);
                                if(readInput.equals((Constants.SUCCESS+"~"))) {
                                    Log.e(TAG,"UNLOCK SUCCESS");
                                    resetConnection();
                                    mIsRiding = true;
                                    return Constants.SUCCESS;
                                } else if(readInput.equals(Constants.WRONG_PASSWORD + "~")){
                                    Log.e(TAG,"WORNG PASSWORD");
                                    resetConnection();
                                    return Constants.WRONG_PASSWORD;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resetConnection();
        if(mIsRiding) {
            return Constants.RIDING;
        } else {
            return Constants.ERROR;
        }
    }
    @Override
    protected void onStartLoading() {
        forceLoad();
    }


    private void resetConnection() {
        if (mBTSocket != null) {
            try {mBTSocket.close();} catch (Exception e) {}
            mBTSocket = null;
        }

    }
}
