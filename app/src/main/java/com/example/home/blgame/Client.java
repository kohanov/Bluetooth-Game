package com.example.home.blgame;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class Client extends Thread {
    private volatile Communicator communicator;
    private BluetoothAdapter mBluetoothAdapter;
    private final BluetoothSocket mmSocket;
    private final CommunicatorService communicatorService;

    public Client(BluetoothDevice device, CommunicatorService communicatorService) {
        this.communicatorService = communicatorService;
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(MainActivity.UUID));
        } catch (IOException e) {
            Log.d("Client create", e.getLocalizedMessage());
        }
        mmSocket = tmp;
    }

    public synchronized Communicator getCommunicator() {
        return communicator;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            Log.d("Client", "Try to connect");
            mmSocket.connect();
            Log.d("Client", "Connected");
            //create new Thread to send data
            synchronized (this) {
                communicator = communicatorService.createCommunicatorThread(mmSocket);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Client", "Started");
                    communicator.run();
                }
            }).start();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.d("Client", closeException.getLocalizedMessage());
            }
        }
    }

    /**
     * Will cancel an in-progress connection, and close the socket
     */
    public void cancel() {
        if (communicator != null)
            communicator.cancel();
    }
}