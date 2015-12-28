package com.example.home.blgame.desk;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.nfc.Tag;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.home.blgame.MainActivity;
import com.example.home.blgame.R;

import static com.example.home.blgame.desk.Figure.*;
import static com.example.home.blgame.MainActivity.*;


/**
 * Created by Роман on 20.12.2015.
 */
public class Desk extends View {

    public final int countFiguresInRow = 6;
    private int deskSize;
    private int fieldSize;

    private int chosenColumn;
    private int chosenRow;

    private final int[] dColumn = {0, 1, 0, -1};
    private final int[] dRow = {-1, 0, 1, 0};

    private final Figure EMPTY_FIELD = new Figure(FigureBackground.NO_ACTIVITY, FigureImage.NONE, false, Team.EMPTY);

    //Fields
    private Bitmap fBackground;
    private Bitmap cfBackground;

    //Bitmaps of Figures
    //Prefixes: "f"  - figure, "cf" - chosen figure
    private Bitmap fUnknownRed;
    private Bitmap fUnknownBlue;
    private Bitmap fScissorsRed;
    private Bitmap fScissorsBlue;
    private Bitmap fRockRed;
    private Bitmap fRockBlue;
    private Bitmap fPaperRed;
    private Bitmap fPaperBlue;

    private Bitmap cfUnknownRed;
    private Bitmap cfUnknownBlue;
    private Bitmap cfScissorsRed;
    private Bitmap cfScissorsBlue;
    private Bitmap cfRockRed;
    private Bitmap cfRockBlue;
    private Bitmap cfPaperRed;
    private Bitmap cfPaperBlue;


    public Figure[][] figures;
    public MainActivity mainActivity;
    public int myFigures;// count of my alive figures
    public int opponentFigures;// count of opponent alive figures


    enum Fight {WIN, LOSE, DRAW, BEFORE_FIGHT, CHANGED, EMPTY}

    Fight resultOfFight;

    public Desk(Context context, AttributeSet attr) {
        super(context, attr);
        Log.d(TAG, "onCreate");
        Resources src = getResources();
        fBackground = BitmapFactory.decodeResource(src, R.drawable.back_ground_no_activity);
        fUnknownRed = BitmapFactory.decodeResource(src, R.drawable.red_unknown);
        fUnknownBlue = BitmapFactory.decodeResource(src, R.drawable.blue_unknown);
        fScissorsRed = BitmapFactory.decodeResource(src, R.drawable.red_cut);
        fScissorsBlue = BitmapFactory.decodeResource(src, R.drawable.blue_cut);
        fRockRed = BitmapFactory.decodeResource(src, R.drawable.red_stone);
        fRockBlue = BitmapFactory.decodeResource(src, R.drawable.blue_stone);
        fPaperRed = BitmapFactory.decodeResource(src, R.drawable.red_paper);
        fPaperBlue = BitmapFactory.decodeResource(src, R.drawable.blue_paper);

        cfBackground = BitmapFactory.decodeResource(src, R.drawable.back_ground_chosen);
        cfUnknownRed = BitmapFactory.decodeResource(src, R.drawable.red_unknown_chosen);
        cfUnknownBlue = BitmapFactory.decodeResource(src, R.drawable.blue_unknown_chosen);
        cfScissorsRed = BitmapFactory.decodeResource(src, R.drawable.red_cut_chosen);
        cfScissorsBlue = BitmapFactory.decodeResource(src, R.drawable.blue_cut_chosen);
        cfRockRed = BitmapFactory.decodeResource(src, R.drawable.red_stone_chosen);
        cfRockBlue = BitmapFactory.decodeResource(src, R.drawable.blue_stone_chosen);
        cfPaperRed = BitmapFactory.decodeResource(src, R.drawable.red_paper_chosen);
        cfPaperBlue = BitmapFactory.decodeResource(src, R.drawable.blue_paper_chosen);

        initDesk();

        invalidate();
    }

    public void initDesk() {
        Log.d(TAG, "initDesk");

        figures = new Figure[countFiguresInRow][countFiguresInRow];

        myFigures = countFiguresInRow * 2;
        opponentFigures = countFiguresInRow * 2;

        for (int column = 0; column < countFiguresInRow; column++) {
            for (int row = 0; row < countFiguresInRow; row++) {
                if (row < 2) {
                    figures[column][row] = new Figure(FigureBackground.NO_ACTIVITY, FigureImage.ROCK, false, OPPONENT_COLOR);
                } else if (row >= countFiguresInRow - 2) {
                    figures[column][row] = new Figure(FigureBackground.NO_ACTIVITY, FigureImage.ROCK, false, MY_COLOR);
                } else {
                    figures[column][row] = EMPTY_FIELD;
                }
            }
        }
    }


    private static boolean shouldScale = true;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw");

        if (shouldScale) {
            shouldScale = false;
            fBackground = fBackground.createScaledBitmap(fBackground, fieldSize, fieldSize, false);
            fUnknownRed = fUnknownRed.createScaledBitmap(fUnknownRed, fieldSize, fieldSize, false);
            fUnknownBlue = fUnknownBlue.createScaledBitmap(fUnknownBlue, fieldSize, fieldSize, false);
            fScissorsRed = fScissorsRed.createScaledBitmap(fScissorsRed, fieldSize, fieldSize, false);
            fScissorsBlue = fScissorsBlue.createScaledBitmap(fScissorsBlue, fieldSize, fieldSize, false);
            fRockRed = fRockRed.createScaledBitmap(fRockRed, fieldSize, fieldSize, false);
            fRockBlue = fRockBlue.createScaledBitmap(fRockBlue, fieldSize, fieldSize, false);
            fPaperRed = fPaperRed.createScaledBitmap(fPaperRed, fieldSize, fieldSize, false);
            fPaperBlue = fPaperBlue.createScaledBitmap(fPaperBlue, fieldSize, fieldSize, false);

            cfBackground = cfBackground.createScaledBitmap(cfBackground, fieldSize, fieldSize, false);
            cfUnknownRed = cfUnknownRed.createScaledBitmap(cfUnknownRed, fieldSize, fieldSize, false);
            cfUnknownBlue = cfUnknownBlue.createScaledBitmap(cfUnknownBlue, fieldSize, fieldSize, false);
            cfScissorsRed = cfScissorsRed.createScaledBitmap(cfScissorsRed, fieldSize, fieldSize, false);
            cfScissorsBlue = cfScissorsBlue.createScaledBitmap(cfScissorsBlue, fieldSize, fieldSize, false);
            cfRockRed = cfRockRed.createScaledBitmap(cfRockRed, fieldSize, fieldSize, false);
            cfRockBlue = cfRockBlue.createScaledBitmap(cfRockBlue, fieldSize, fieldSize, false);
            cfPaperRed = cfPaperRed.createScaledBitmap(cfPaperRed, fieldSize, fieldSize, false);
            cfPaperBlue = cfPaperBlue.createScaledBitmap(cfPaperBlue, fieldSize, fieldSize, false);
        }

        for (int column = 0; column < countFiguresInRow; column++) {
            for (int row = 0; row < countFiguresInRow; row++) {
                drawField(canvas, column, row, column * fieldSize, row * fieldSize);
            }
        }
    }

    protected void drawField(Canvas canvas, int column, int row, float x, float y) {
//        Log.d(TAG, "drawField");
        if (figures[column][row].getTeam() == MY_COLOR) {
            canvas.drawBitmap(selectImage(column, row), x, y, null);
        } else if (figures[column][row].getTeam() == OPPONENT_COLOR) {
            if (figures[column][row].isVisible()) {
                canvas.drawBitmap(selectImage(column, row), x, y, null);
            } else {
                if (figures[column][row].getFigureBackground() == FigureBackground.NO_ACTIVITY) {
                    canvas.drawBitmap((figures[column][row].getTeam() == Team.RED ? fUnknownRed : fUnknownBlue), x, y, null);
                } else {
                    canvas.drawBitmap((figures[column][row].getTeam() == Team.RED ? cfUnknownRed : cfUnknownBlue), x, y, null);
                }
            }
        } else { // EMPTY
            canvas.drawBitmap((figures[column][row].getFigureBackground() == FigureBackground.NO_ACTIVITY ? fBackground : cfBackground), x, y, null);
        }
    }

    private Bitmap selectImage(int column, int row) {
//        Log.d(TAG, "selectImage");
        switch (figures[column][row].getFigureImage()) {
            case ROCK:
                switch (figures[column][row].getFigureBackground()) {
                    case NO_ACTIVITY:
                        return figures[column][row].getTeam() == Team.RED ? fRockRed : fRockBlue;
                    case CHOSEN:
                        return figures[column][row].getTeam() == Team.RED ? cfRockRed : cfRockBlue;
                }
            case SCISSORS:
                switch (figures[column][row].getFigureBackground()) {
                    case NO_ACTIVITY:
                        return figures[column][row].getTeam() == Team.RED ? fScissorsRed : fScissorsBlue;
                    case CHOSEN:
                        return figures[column][row].getTeam() == Team.RED ? cfScissorsRed : cfScissorsBlue;
                }
                break;
            case PAPER:
                switch (figures[column][row].getFigureBackground()) {
                    case NO_ACTIVITY:
                        return figures[column][row].getTeam() == Team.RED ? fPaperRed : fPaperBlue;
                    case CHOSEN:
                        return figures[column][row].getTeam() == Team.RED ? cfPaperRed : cfPaperBlue;
                }
                break;
            default:
                Log.d(TAG, "NULL selectImage");
                return null;
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onToucheEvent");

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                int row = 0;
                int column = 0;
                while ((y -= fieldSize) > 0) row++;
                while ((x -= fieldSize) > 0) column++;
                switch (status) {
                    case BEFORE_START:
                        Log.d(TAG, "BEFORE_START");

                        if (figures[column][row].getTeam() == MY_COLOR) {
                            nextFigure(column, row);
                            invalidate();
                        }
                        break;
                    case MY_TURN:
                        Log.d(TAG, "MY_TURN");

                        if (figures[column][row].getTeam() == MY_COLOR) {
                            chosenColumn = column;
                            chosenRow = row;
                            figures[chosenColumn][chosenRow].setFigureBackground(FigureBackground.CHOSEN);
                            highlight(chosenColumn, chosenRow, true);
                            status = Status.MOVE;
                            invalidate();
                        }
                        break;
                    case MOVE:
                        Log.d(TAG, "MOVE");

                        if ((chosenRow == row) && (chosenColumn == column)) break; //the same field

                        int oldColumn = chosenColumn;
                        int oldRow = chosenRow;
                        resultOfFight = Fight.BEFORE_FIGHT;

                        for (int contactedField = 0; contactedField < 4; contactedField++) {
                            int drow = dRow[contactedField];
                            int dcolumn = dColumn[contactedField];
                            if ((column == chosenColumn + dcolumn) && (row == chosenRow + drow)) {
                                if (tryMove(column, row)) {
                                    break;
                                }
                            }
                        }
                        StringBuilder step = new StringBuilder();
                        step.append(countFiguresInRow - oldColumn - 1).append(countFiguresInRow - oldRow - 1).append(countFiguresInRow - chosenColumn - 1).append(countFiguresInRow - chosenRow - 1);

                        switch (resultOfFight) {
                            case WIN:
                                --opponentFigures;
                                step.reverse().append("ws").reverse();
                                MainActivity.sendPrepared(step.toString());
                                ((Button) mainActivity.findViewById(R.id.status)).setText(R.string.opponentTurn);
                                if (opponentFigures == 0) {
                                    Log.d(TAG, "you are WINNER!");
                                    showWin();
                                    break;
                                }
                                status = Status.OPPONENT_TURN;

                                break;
                            case LOSE:
                                --myFigures;
                                step.reverse().append("ls").reverse();
                                MainActivity.sendPrepared(step.toString());
                                ((Button) mainActivity.findViewById(R.id.status)).setText(R.string.opponentTurn);
                                if (myFigures == 0) {
                                    Log.d(TAG, "you are LOSER!");
                                    showLose();
                                    break;
                                }
                                status = Status.OPPONENT_TURN;

                                break;
                            case DRAW:
                                step.reverse().append("ds").reverse();
                                MainActivity.sendPrepared(step.toString());
                                status = Status.OPPONENT_TURN;
                                ((Button) mainActivity.findViewById(R.id.status)).setText(R.string.opponentTurn);
                                break;
                            case EMPTY:
                                step.reverse().append("es").reverse();
                                MainActivity.sendPrepared(step.toString());
                                status = Status.OPPONENT_TURN;
                                ((Button) mainActivity.findViewById(R.id.status)).setText(R.string.opponentTurn);
                                break;
                            case CHANGED:
                                status = Status.MOVE;
                                break;
                            case BEFORE_FIGHT:
                                tryChangeFigure(column, row);
                                status = Status.MOVE;
                                break;
                        }

                        break;
                    default:
                        Log.d(TAG, "OPPONENT_TURN. you should wait!");
                        break;
                }
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void tryChangeFigure(int column, int row) {
        Log.d(TAG, "tryChangeFigure");

        if (figures[column][row].getTeam() == MY_COLOR) {
            highlight(chosenColumn, chosenRow, false);
            figures[chosenColumn][chosenRow].setFigureBackground(FigureBackground.NO_ACTIVITY);
            chosenColumn = column;
            chosenRow = row;
            highlight(chosenColumn, chosenRow, true);
            figures[chosenColumn][chosenRow].setFigureBackground(FigureBackground.CHOSEN);
        }
    }

    private boolean tryMove(int column, int row) {
        //out of desk
        if (!checkBorders(column, row)) {
            return false;
        }

        //move to empty field
        if (figures[column][row].getTeam() == Team.EMPTY) {
            highlight(chosenColumn, chosenRow, false);
            figures[column][row] = figures[chosenColumn][chosenRow];
            figures[column][row].setFigureBackground(FigureBackground.NO_ACTIVITY);
            figures[chosenColumn][chosenRow] = EMPTY_FIELD;

            chosenColumn = column;
            chosenRow = row;

            resultOfFight = Fight.EMPTY;
            return true;
        }

        //change chosen figure (still my turn)
        if (figures[column][row].getTeam() == MY_COLOR) {
            highlight(chosenColumn, chosenRow, false);
            figures[chosenColumn][chosenRow].setFigureBackground(FigureBackground.NO_ACTIVITY);
            chosenColumn = column;
            chosenRow = row;
            highlight(chosenColumn, chosenRow, true);
            figures[chosenColumn][chosenRow].setFigureBackground(FigureBackground.CHOSEN);

            resultOfFight = Fight.CHANGED;
            return true;
        }

        //attack opponent
        if (figures[column][row].getTeam() == OPPONENT_COLOR) {
            status = Status.OPPONENT_TURN;

            highlight(chosenColumn, chosenRow, false);
            drowShiftFigure(chosenColumn, chosenRow, column, row);
            return true;
        }
        return false; // unreachable statement, only for IDEA
    }

    //update view with received data
    public void redrowReceived(int fromColumn, int fromRow, int toColumn, int toRow) {
        if (figures[toColumn][toRow].getTeam() == Team.EMPTY) {
            figures[toColumn][toRow] = figures[fromColumn][fromRow];
            figures[fromColumn][fromRow] = EMPTY_FIELD;
        } else {
            drowShiftFigure(fromColumn, fromRow, toColumn, toRow);
        }
    }

    private boolean drowShiftFigure(int fromColumn, int fromRow, int toColumn, int toRow) {
        switch (figures[fromColumn][fromRow].getFigureImage()) {
            case ROCK:
                switch (figures[toColumn][toRow].getFigureImage()) {
                    case ROCK:
                        return drowDraw(fromColumn, fromRow, toColumn, toRow);
                    case SCISSORS:
                        return drowWin(fromColumn, fromRow, toColumn, toRow);
                    case PAPER:
                        return drowLose(fromColumn, fromRow, toColumn, toRow);
                }
            case SCISSORS:
                switch (figures[toColumn][toRow].getFigureImage()) {
                    case ROCK:
                        return drowLose(fromColumn, fromRow, toColumn, toRow);
                    case SCISSORS:
                        return drowDraw(fromColumn, fromRow, toColumn, toRow);
                    case PAPER:
                        return drowWin(fromColumn, fromRow, toColumn, toRow);
                }
            case PAPER:
                switch (figures[toColumn][toRow].getFigureImage()) {
                    case ROCK:
                        return drowWin(fromColumn, fromRow, toColumn, toRow);
                    case SCISSORS:
                        return drowLose(fromColumn, fromRow, toColumn, toRow);
                    case PAPER:
                        return drowDraw(fromColumn, fromRow, toColumn, toRow);
                }
            default:
                Log.d(TAG, "drowShiftFigure");
                return false;
        }
    }

    private boolean drowDraw(int fromColumn, int fromRow, int toColumn, int toRow) {
        resultOfFight = Fight.DRAW;

        figures[fromColumn][fromRow].setFigureBackground(FigureBackground.NO_ACTIVITY);
        figures[toColumn][toRow].setFigureBackground(FigureBackground.NO_ACTIVITY);
        figures[fromColumn][fromRow].setVisible(true);
        figures[toColumn][toRow].setVisible(true);

        chosenColumn = toColumn;
        chosenRow = toRow;
        return true;
    }

    private boolean drowWin(int fromColumn, int fromRow, int toColumn, int toRow) {
        resultOfFight = Fight.WIN;

        figures[fromColumn][fromRow].setFigureBackground(FigureBackground.NO_ACTIVITY);
        figures[fromColumn][fromRow].setVisible(true);
        figures[toColumn][toRow] = figures[fromColumn][fromRow];
        figures[fromColumn][fromRow] = EMPTY_FIELD;

        chosenColumn = toColumn;
        chosenRow = toRow;
        return true;
    }

    private boolean drowLose(int fromColumn, int fromRow, int toColumn, int toRow) {
        resultOfFight = Fight.LOSE;

        figures[toColumn][toRow].setVisible(true);
        figures[fromColumn][fromRow] = EMPTY_FIELD;

        chosenColumn = toColumn;
        chosenRow = toRow;
        return true;
    }

    private boolean checkBorders(int column, int row) {
        return ((0 <= column) && (column < countFiguresInRow) && (0 <= row) && (row < countFiguresInRow));
    }

    private void highlight(int column, int row, boolean chosen) {
//        Log.d(TAG, "highlight from [" + row + "][" + column + "]");
    }

    private void nextFigure(int column, int row) {
        Log.d(TAG, "nextFigure");

        switch (figures[column][row].getFigureImage()) {
            case ROCK:
                figures[column][row].setFigureImage(FigureImage.SCISSORS);
                break;
            case SCISSORS:
                figures[column][row].setFigureImage(FigureImage.PAPER);
                break;
            case PAPER:
                figures[column][row].setFigureImage(FigureImage.ROCK);
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure");
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);

        deskSize = Math.min(measuredHeight, measuredWidth);
        fieldSize = deskSize / countFiguresInRow;

        int measuredSize = MeasureSpec.makeMeasureSpec(deskSize, MeasureSpec.EXACTLY);
        super.onMeasure(measuredSize, measuredSize);
    }

    public void showWin() {
        Toast.makeText(mainActivity, R.string.congratulation, Toast.LENGTH_LONG).show();
        initGame();
        MainActivity.viewFlipper.showPrevious();
    }

    public void showLose() {
        Toast.makeText(mainActivity, R.string.itsapity, Toast.LENGTH_LONG).show();
        initGame();
        MainActivity.viewFlipper.showPrevious();
    }

    public void initGame() {
        initDesk();
        Log.d(TAG, "initGame");
        if (MY_COLOR == Team.BLUE) {
            Log.d(TAG, "Blue Team");
            mainActivity.findViewById(R.id.status).setBackgroundColor(getResources().getColor(R.color.fullred));
            mainActivity.findViewById(R.id.back).setBackgroundColor(getResources().getColor(R.color.fullred));
            for (int column = 0; column < countFiguresInRow; column++) {
                for (int row = 0; row < countFiguresInRow; row++) {
                    if (row < 2) {
                        figures[column][row].setTeam(MY_COLOR);
                    } else if (row >= countFiguresInRow - 2) {
                        figures[column][row].setTeam(OPPONENT_COLOR);
                    }
                }
            }
        }
        mainActivity.isFirst = true;
        ((Button) mainActivity.findViewById(R.id.status)).setText(R.string.ensureReady);
        mainActivity.findViewById(R.id.status).setClickable(true);
        MY_COLOR = Team.RED;
        OPPONENT_COLOR = Team.BLUE;
        status = Status.BEFORE_START;
    }

    private final String TAG = "DESK";
}