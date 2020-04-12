package main;

import java.awt.*;

public class Player {
    private int Score;
    private Color color;
    private boolean isNpc;
    private boolean currentPlayer;

    public Player() {
        this.currentPlayer = false;
    }

    public boolean isCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(boolean currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    void setColor(Color color) {
        this.color = color;
    }

    Color getColor() {
        return color;
    }

    public boolean isNpc() {
        return isNpc;
    }

    public void setNpc(boolean npc) {
        isNpc = npc;
    }
}
