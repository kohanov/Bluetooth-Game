package com.example.home.blgame;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {
    public final static String UUID = "37e562ac-d31a-46f8-b654-2fe4285e7041";
    public final static String AppName = "Blgame";
    private final static String IS_SERVER = "isServer";
    private ViewFlipper viewFlipper;
    private static String MAC;
    private BluetoothAdapter mBluetoothAdapter;
    private final List<BluetoothDevice> discoveredDevices = new ArrayList<BluetoothDevice>();
    private ArrayAdapter<BluetoothDevice> listAdapter;
    private BroadcastReceiver Receiver = null, FinishedReceiver = null;
    private TextView textData;
    private EditText textMessage;
    private Client client = null;
    private Server server = null;

    private class WriteTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... args) {
            try {
                client.getCommunicator().write(args[0]);
            } catch (Exception e) {
                Log.d("WriteTask error: ", e.getClass().getSimpleName() + " " + e.getLocalizedMessage());
            }
            return null;
        }
    }

    private final CommunicatorService communicatorService = new CommunicatorService() {

        @Override
        public Communicator createCommunicatorThread(BluetoothSocket socket) {
            return new Communicator(socket, new Communicator.CommunicationListener() {

                @Override
                public void getMessage(final String message) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (client == null && mBluetoothAdapter.checkBluetoothAddress(message)) {
                                try {
                                    BluetoothDevice temp = mBluetoothAdapter.getRemoteDevice(message);
                                    client = new Client(temp, communicatorService);
                                } catch (IllegalArgumentException e) {
                                    client = null;
                                    Log.d("MainActivity", e.getLocalizedMessage());
                                }

                            }
                            //TODO: обрабатывать входящее сообщение здесь
                            textData.setText(textData.getText().toString() + "\n" + message);
                        }
                    });
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            System.exit(1);
        }
        MAC = mBluetoothAdapter.getAddress();
        Log.d("MainActivity MAC", MAC);
        textData = (TextView) findViewById(R.id.data_text);
        textData.setText("Получено: ");
        viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        textMessage = (EditText) findViewById(R.id.message_text);
        listAdapter = new ArrayAdapter<BluetoothDevice>(getBaseContext(), android.R.layout.simple_list_item_1, discoveredDevices) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                final BluetoothDevice device = getItem(position);
                ((TextView) view.findViewById(android.R.id.text1)).setText(device.getName());
                return view;
            }
        };
        setListAdapter(listAdapter);
        checkBluetoothEnabled();
    }

    public void checkBluetoothEnabled() {
        if (!mBluetoothAdapter.isEnabled()) {
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            Toast.makeText(this, "Сначала включи Bluetooth", Toast.LENGTH_LONG).show();
            TextView buttonText = (TextView) findViewById(R.id.BTstatus);
            buttonText.setText(R.string.enableBT);
        }
    }

    public void enableBluetooth(View view) {
        Intent intent = new Intent(
                BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(intent);
        TextView buttonText = (TextView) findViewById(R.id.BTstatus);
        buttonText.setText("Bluetooth включен");
        Toast.makeText(this, mBluetoothAdapter.getName(), Toast.LENGTH_LONG).show();
    }

    public void discoverDevices(View view) {
        checkBluetoothEnabled();
        discoveredDevices.clear();
        listAdapter.notifyDataSetChanged();
        // Create a BroadcastReceiver for ACTION_FOUND
        Receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add device to an array adapter to show in a ListView
                    if (!discoveredDevices.contains(device)) {
                        discoveredDevices.add(device);
                        listAdapter.notifyDataSetChanged();
                    }
                }
            }
        };
        /*
        if (FinishedReceiver == null) {
            FinishedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    findViewById(R.id.finded).setVisibility(View.VISIBLE);
                    findViewById(android.R.id.list).setEnabled(true);
                    //if (progressDialog != null)
                    //    progressDialog.dismiss();
                    Toast.makeText(getBaseContext(), "Поиск закончен. Выберите устройство.", Toast.LENGTH_LONG).show();
                    unregisterReceiver(FinishedReceiver);
                }
            };
        }
        */
        // Register the BroadcastReceiver
        registerReceiver(Receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        //registerReceiver(FinishedReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        //getListView().setEnabled(false);
        //progressDialog = ProgressDialog.show(this, "Поиск устройств", "Подождите...");
        mBluetoothAdapter.startDiscovery();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBluetoothAdapter.cancelDiscovery();
        if (Receiver != null) {
            try {
                unregisterReceiver(Receiver);
            } catch (Exception e) {
                Log.d("MainActivity", "Не удалось отключить ресивер " + Receiver);
            }
        }
        if (client != null)
            client.cancel();
        if (server != null)
            server.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        server = new Server(communicatorService);
        server.start();
        discoveredDevices.clear();
        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.disable();
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        //client chosen
        if (client != null) {
            client.cancel();
        }
        BluetoothDevice deviceSelected = discoveredDevices.get(position);
        client = new Client(deviceSelected, communicatorService);
        client.start();
        //передаём второму устройству наш MAC адрес для подключения
        new WriteTask().execute(MAC);
        Toast.makeText(this, "Вы подключились к устройству \"" + discoveredDevices.get(position).getName() + "\"", Toast.LENGTH_SHORT).show();
        //показываем окно игры
        viewFlipper.showNext();
        //TODO: отправить сообщение о начале игры
    }

    public void sendMessage(View view) {
        if (client != null) {
            //TODO: подготовить информацию к отправке
            // bool is server
            //value: 00 - rock, 01 - paper, 10 - scissors
            //0 - conenct/ startActivity
            //1: server & ready & values xx - value, xxx - x, xxx - y
            //
            new WriteTask().execute(textMessage.getText().toString());
            textMessage.setText("");
        } else {
            Toast.makeText(this, "Сначала выберите клиента", Toast.LENGTH_SHORT).show();
        }
    }

    public void stopGame(View view) {
        //В конце игры показываем начальное окно
        viewFlipper.showPrevious();
        discoveredDevices.clear();
        listAdapter.notifyDataSetChanged();

    }
}
