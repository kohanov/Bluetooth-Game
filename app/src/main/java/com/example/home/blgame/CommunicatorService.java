package com.example.home.blgame;

import android.bluetooth.BluetoothSocket;

interface CommunicatorService {
    Communicator createCommunicatorThread(BluetoothSocket socket);
}