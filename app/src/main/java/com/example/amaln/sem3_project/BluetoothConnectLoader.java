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

    private final String TAG = "BluetoothConnectLoader";
    private final String BLUETOOTH_PASSWORD = "123456789";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBTSocket;
    private boolean mIsBTConnected;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private String mAddress;

    public BluetoothConnectLoader(Context context, String address) {
        super(context);
        mAddress = address;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mIsBTConnected = false;
    }

    @Override
    public String loadInBackground() {
        boolean connectSuccess = true;

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
                                    return Constants.SUCCESS;
                                } else if(readInput.equals(Constants.WRONG_PASSWORD + "~")){
                                    Log.e(TAG,"WORNG PASSWORD");
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
            Log.e(TAG,"OUTER");
            e.printStackTrace();
        }
        return Constants.ERROR;
    }
    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}
