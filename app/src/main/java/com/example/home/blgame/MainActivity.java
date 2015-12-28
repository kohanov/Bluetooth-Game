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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.example.home.blgame.desk.Desk;
import com.example.home.blgame.desk.Figure;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {
    public final static String UUID = "37e562ac-d31a-46f8-b654-2fe4285e7041";
    private final String TAG = "MainActivity";
    public static ViewFlipper viewFlipper;
    private boolean isStarted = false;
    public boolean isFirst = true;
    private BluetoothAdapter mBluetoothAdapter;
    private final List<BluetoothDevice> discoveredDevices = new ArrayList<BluetoothDevice>();
    private ArrayAdapter<BluetoothDevice> listAdapter;
    private BroadcastReceiver Receiver = null, FinishedReceiver = null;
    private ProgressDialog progressDialog;
    private static Button gotoGame;
    public static Client client;
    private Server server = null;


    private Desk desk;
    private ImageView myIcon;
    private ImageView opponentIcon;
    private TextView players;

    public enum Status {BEFORE_START, MY_TURN, MOVE, OPPONENT_TURN}

    public static Status status;
    public static Figure.Team MY_COLOR;
    public static Figure.Team OPPONENT_COLOR;

    private static class WriteTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... args) {
            Log.d(tag, "doInBackground");
            try {
                client.getCommunicator().write(args[0]);
                Log.d(tag, args[0]);
            } catch (Exception e) {
                Log.d(tag, "Error: " + e.getClass().getSimpleName() + " " + e.getLocalizedMessage());
            }
            return null;
        }

        private final String tag = "WriteTask";
    }

    private final CommunicatorService communicatorService = new CommunicatorService() {

        @Override
        public Communicator createCommunicatorThread(BluetoothSocket socket) {
            return new Communicator(socket, new Communicator.CommunicationListener() {

                @Override
                public void getMessage(final String newMessage) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("MainActivity", newMessage);
                            //Parse entering message
                            String message = newMessage;
                            while (message.compareTo("") != 0) {
                                switch (message.charAt(0)) {
                                    case 'a'://opponent is ready
                                        if (message.charAt(1) == 's') {
                                            status = Status.MY_TURN;
                                            ((Button) findViewById(R.id.status)).setText("Твой ход");
                                        } else {
                                            //change colours to default
                                            MY_COLOR = Figure.Team.BLUE;
                                            OPPONENT_COLOR = Figure.Team.RED;
                                            for (int column = 0; column < desk.countFiguresInRow; column++) {
                                                for (int row = 0; row < desk.countFiguresInRow; row++) {
                                                    if (row < 2) {
                                                        desk.figures[column][row].setTeam(OPPONENT_COLOR);
                                                    } else if (row >= desk.countFiguresInRow - 2) {
                                                        desk.figures[column][row].setTeam(MY_COLOR);
                                                    }
                                                }
                                            }
                                            isFirst = false;
                                        }
                                        message = message.substring(2, message.length());
                                        break;
                                    case 'b'://set opponent's values
                                        int i = 1;
                                        for (int column = desk.countFiguresInRow - 1; column >= 0; --column) {
                                            for (int row = 1; row >= 0; --row) {
                                                switch (message.charAt(i)) {
                                                    case '1':
                                                        Log.d(TAG, message.charAt(i) + "ROCK");
                                                        desk.figures[column][row].setFigureImage(Figure.FigureImage.ROCK);
                                                        break;
                                                    case '2':
                                                        Log.d(TAG, message.charAt(i) + "SCISSORS");
                                                        desk.figures[column][row].setFigureImage(Figure.FigureImage.SCISSORS);
                                                        break;
                                                    case '3':
                                                        Log.d(TAG, message.charAt(i) + "PAPER");
                                                        desk.figures[column][row].setFigureImage(Figure.FigureImage.PAPER);
                                                        break;
                                                }
                                                ++i;
                                            }
                                        }
                                        message = message.substring(1 + desk.countFiguresInRow*2,message.length());
                                        break;
                                    case 's'://step done
                                        switch (message.charAt(1)){
                                            case 'w':
                                                --desk.myFigures;
                                                break;
                                            case 'l':
                                                --desk.opponentFigures;
                                                break;
                                            default:
                                                //to empty or draw
                                                break;
                                        }
                                        if (desk.myFigures == 0) {
                                            desk.showLose();
                                            message = "";
                                            break;
                                        }
                                        if (desk.opponentFigures == 0) {
                                            desk.showWin();
                                            message = "";
                                            break;
                                        }
                                        int oldColumn = (int) message.charAt(2) - (int) '0';
                                        int oldRow = (int) message.charAt(3) - (int) '0';
                                        int newColumn = (int) message.charAt(4) - (int) '0';
                                        int newRow = (int) message.charAt(5) - (int) '0';
                                        desk.redrowReceived(oldColumn, oldRow, newColumn, newRow);
                                        status = Status.MY_TURN;
                                        ((Button) findViewById(R.id.status)).setText("Твой ход");
                                        message = message.substring(6,message.length());
                                        break;
                                }
                            }
                            desk.invalidate();
                        }
                    });
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        MY_COLOR = Figure.Team.RED;
        OPPONENT_COLOR = Figure.Team.BLUE;
        status = Status.BEFORE_START;

        setContentView(R.layout.activity_main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            System.exit(1);
        }
        viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        gotoGame = (Button) findViewById(R.id.gotoGame);
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
        desk = (Desk) findViewById(R.id.desk);
        desk.mainActivity = this;
    }

    public void checkBluetoothEnabled() {
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Сначала включи Bluetooth", Toast.LENGTH_LONG).show();
            TextView buttonText = (TextView) findViewById(R.id.BTstatus);
            buttonText.setText(R.string.enableBT);
        }
    }

    public void enableBluetooth(View view) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(intent);
        TextView buttonText = (TextView) findViewById(R.id.BTstatus);
        buttonText.setText("Bluetooth включен");
        Toast.makeText(this, mBluetoothAdapter.getName(), Toast.LENGTH_LONG).show();
    }

    public void discoverDevices(View view) {
        if (isStarted) {
            desk.initGame();
        }
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
        if (FinishedReceiver == null) {
            FinishedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    findViewById(android.R.id.list).setEnabled(true);
                    if (progressDialog != null)
                        progressDialog.dismiss();
                    Toast.makeText(getBaseContext(), "Поиск закончен. Выберите устройство.", Toast.LENGTH_LONG).show();
                    unregisterReceiver(FinishedReceiver);
                }
            };
        }
        // Register the BroadcastReceiver
        registerReceiver(Receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(FinishedReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        getListView().setEnabled(false);
        progressDialog = ProgressDialog.show(this, "Поиск устройств", "Подождите...");
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
        Log.d(TAG, "onListItemClick");
        unregisterReceiver(Receiver);
        if (client != null) {
            client.cancel();
        }
        BluetoothDevice deviceSelected = discoveredDevices.get(position);
        mBluetoothAdapter.cancelDiscovery();
        client = new Client(deviceSelected, communicatorService);
        client.start();
        Toast.makeText(this, "Вы подключились к устройству \"" + discoveredDevices.get(position).getName() + "\"", Toast.LENGTH_SHORT).show();
        findViewById(R.id.gotoGame).setVisibility(View.VISIBLE);
        findViewById(R.id.gotoGame).setClickable(true);
        isStarted = true;
        new WriteTask().execute("Hello!");
        viewFlipper.showNext();
    }

    public void pauseGame(View view) {
        //Go to main window
        viewFlipper.showPrevious();
        discoveredDevices.clear();
        listAdapter.notifyDataSetChanged();

    }

    public void gotoGame(View view) {
        //Go to game window
        viewFlipper.showNext();
    }

    public void setReady(View view) {
        //Send to opponent that user is ready
        Log.d(TAG, "setReady");
        findViewById(R.id.status).setClickable(false);
        if (isFirst) {
            new WriteTask().execute("ap");
            ((Button) findViewById(R.id.status)).setText("Красный");
            status = Status.OPPONENT_TURN;
        } else {
            new WriteTask().execute("as");
            ((Button) findViewById(R.id.status)).setText("Ход противника");
            findViewById(R.id.status).setBackgroundColor(getResources().getColor(R.color.fullblue));
            findViewById(R.id.back).setBackgroundColor(getResources().getColor(R.color.fullblue));
            status = Status.OPPONENT_TURN;
        }
        sendReady();
    }

    public static void sendPrepared(String message) {
        //send message from Desk
        Log.d("MainActivity", "sendMessage");
        if (client != null) {
            new WriteTask().execute(message);
        }
    }

    public void sendReady() {
        //prepare and send info about figure values
        Log.d(TAG, "sendReady");
        StringBuilder message = new StringBuilder("b");
        for (int column = 0; column < desk.countFiguresInRow; column++) {
            for (int row = desk.countFiguresInRow - 2; row < desk.countFiguresInRow; row++) {
                switch (desk.figures[column][row].getFigureImage()) {
                    case ROCK:
                        message.append("1");
                        break;
                    case SCISSORS:
                        message.append("2");
                        break;
                    case PAPER:
                        message.append("3");
                        break;
                }
            }
        }
        new WriteTask().execute(message.toString());
    }

    public static void noConnection() {
        //create an Exception, because client cannot connect to user
        viewFlipper.showPrevious();
        gotoGame.setVisibility(View.VISIBLE);
        gotoGame.setText("Ошибка, найдите ещё раз");
    }
}
