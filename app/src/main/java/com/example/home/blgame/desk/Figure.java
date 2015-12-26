package com.example.home.blgame.desk;

/**
 * Created by Роман on 23.12.2015.
 */



public class Figure {
    public enum FigureBackground {NO_ACTIVITY, CHOSEN}
    public enum FigureImage {ROCK, SCISSORS, PAPER, NONE}
    public enum Team {RED, BLUE, EMPTY}

    FigureBackground figureBackground;
    FigureImage figureImage;
    boolean visible;
    Team team;

    public Figure(FigureBackground figureBackground, FigureImage figureImage, boolean visible, Team team) {
        this.figureBackground = figureBackground;
        this.figureImage = figureImage;
        this.visible = visible;
        this.team = team;
    }

    public Figure(Figure other) {
        this.figureBackground = other.figureBackground;
        this.figureImage = other.figureImage;
        this.visible = other.visible;
        this.team = other.team;
    }

    public FigureBackground getFigureBackground() {
        return figureBackground;
    }

    public void setFigureBackground(FigureBackground figureBackground) {
        this.figureBackground = figureBackground;
    }

    public FigureImage getFigureImage() {
        return figureImage;
    }

    public void setFigureImage(FigureImage figureImage) {
        this.figureImage = figureImage;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
