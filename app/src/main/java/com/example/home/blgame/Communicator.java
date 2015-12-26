package com.example.home.blgame;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class Communicator extends Thread {

    interface CommunicationListener {
        void getMessage(String message);
    }

    private final BluetoothSocket mmsocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final CommunicationListener listener;

    public Communicator(BluetoothSocket socket, CommunicationListener listenerTwo) {
        mmsocket = socket;
        listener = listenerTwo;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.d("Communicator", e.getLocalizedMessage());
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024]; // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                Log.d("Communicator", "Read " + bytes + " bytes");
                // Send the obtained bytes to the UI activity
                if (listener != null) {
                    listener.getMessage(new String(buffer).substring(0, bytes));
                }
            } catch (IOException e) {
                Log.d("Communicator", e.getLocalizedMessage());
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(String message) {
        try {
            Log.d("Communicator Try write", message);
            mmOutStream.write(message.getBytes());
        } catch (IOException e) {
            Log.d("Communicator writeerror", e.getLocalizedMessage());
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmsocket.close();
        } catch (IOException e) {
            Log.d("Communicator closeerror", e.getLocalizedMessage());
        }
    }
}