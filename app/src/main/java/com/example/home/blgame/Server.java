package com.example.home.blgame;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class Server extends Thread {
    private final BluetoothServerSocket mmServerSocket;
    private final CommunicatorService communicatorService;
    private BluetoothSocket socket;

    public Server(CommunicatorService communicatorService) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        this.communicatorService = communicatorService;
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothServerSocket tmp = null;
        try {
            // UUID generated on the web
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("Blgame", UUID.fromString(MainActivity.UUID));
        } catch (IOException e) {
            Log.d("Server create", e.getLocalizedMessage());
        }
        mmServerSocket = tmp;
    }

    public void run() {
        socket = null;
        Log.d("Server", "Start");
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            if (mmServerSocket != null)
                try {
                    // Connect the device through the socket. This will block
                    // until it succeeds or throws an exception
                    socket = mmServerSocket.accept();
                } catch (IOException connectException) {
                    // Unable to connect; close the socket and get out
                    Log.d("Server", "Stop " + connectException.getLocalizedMessage());
                    break;
                }
            if (socket != null) {
                //prepare send data to MainActivity
                communicatorService.createCommunicatorThread(socket).run();
            }
        }
    }

    /* Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        if (mmServerSocket != null)
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.d("Server", e.getLocalizedMessage());
            }
    }
}