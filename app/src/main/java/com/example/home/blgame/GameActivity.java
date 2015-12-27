package com.example.home.blgame;

import android.app.Activity;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.home.blgame.desk.Desk;
import com.example.home.blgame.desk.Figure;

public final class GameActivity extends Activity {

    private Desk desk;

    private ImageView myIcon;
    private ImageView opponentIcon;
    private TextView players;

    //    public static boolean isServer;
    private final String IS_SERVER = "isServer";

    public enum Status{BEFORE_START, MY_TURN, MOVE, OPPONENT_TURN}
    public static Status status;

    public static Figure.Team MY_COLOR;
    public static Figure.Team OPPONENT_COLOR;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        isServer = savedInstanceState.getBoolean(IS_SERVER);
        MY_COLOR = Figure.Team.RED;
        OPPONENT_COLOR = Figure.Team.BLUE;
        status = Status.MY_TURN;

        setContentView(R.layout.activity_game);

        desk = (Desk) findViewById(R.id.desk);
        myIcon = (ImageView) findViewById(R.id.myIcon);
        opponentIcon = (ImageView) findViewById(R.id.opponentIcon);
        players = (TextView) findViewById(R.id.playersView);

        myIcon.setImageResource(R.drawable.red_unknown);
        opponentIcon.setImageResource(R.drawable.blue_unknown);
        players.setText("Red\n vs\n Blue");

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private static final String TAG = "debug Main";
}
