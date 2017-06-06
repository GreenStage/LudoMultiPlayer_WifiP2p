package com.ludo3wifi.game;

public class Player {
    public int pawns_base = 4;
    public Pawn[] pawns = new Pawn[4];
    int id;
    int pawns_concluded = 0;
    int pawns_board = 0;

    Player(int id) {
        this.id = id;
        for (int i = 0; i < 4; i++) {
            pawns[i] = new Pawn(this, i);
        }
    }

    boolean hasWon() {
        return pawns_concluded == 4;
    }

}
