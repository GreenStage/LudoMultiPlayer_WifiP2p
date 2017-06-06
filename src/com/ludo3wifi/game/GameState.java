package com.ludo3wifi.game;

/**
 * Created by eduardogomes on 03/06/17.
 */

public class GameState {
    public int playerTurn, diceValue, nPlays, winner, nPlayers;
    public int[][] pawnPos = new int[4][4];

    public GameState(int playerTurn, int diceValue, int nPlays, int winner, int[][] pawnPos, int nPlayers) {
        for (int i = 0; i < nPlayers; i++) {
            for (int j = 0; j < 4; j++) {
                this.pawnPos[i][j] = pawnPos[i][j];
            }
        }
        this.winner = winner;
        this.nPlays = nPlays;
        this.diceValue = diceValue;
        this.playerTurn = playerTurn;
        this.nPlayers = nPlayers;
    }

    public GameState(String str) {
        int parcel = Integer.valueOf(str.substring(0, str.indexOf('/')));
        this.winner = parcel & 0x7;
        parcel = parcel >> 3;

        this.nPlays = parcel & 0x3;
        parcel = parcel >> 2;

        this.diceValue = parcel & 0x7;
        parcel = parcel >> 3;

        this.nPlayers = parcel & 0x7;
        parcel = parcel >> 3;

        this.playerTurn = parcel & 0xF;

        String pString = str;

        for (int i = 0; i < nPlayers; i++) {
            for (int j = 0; j < 4; j++) {
                pString = pString.substring(pString.indexOf('/') + 1);
                this.pawnPos[i][j] = Integer.valueOf(pString.substring(0, pString.indexOf('/')));
            }
        }
    }

    @Override
    public String toString() {
        int parcel;
        parcel = playerTurn; //4bits

        parcel = parcel << 3;
        parcel += nPlayers;

        parcel = parcel << 3;
        parcel += diceValue; //3bits

        parcel = parcel << 2;
        parcel += nPlays;    //2bits

        parcel = parcel << 3;
        parcel += winner;    //3bits

        String retval = String.valueOf(parcel) + '/';
        for (int i = 0; i < nPlayers; i++) {
            for (int j = 0; j < 4; j++) {
                retval += String.valueOf(pawnPos[i][j]) + '/';
            }
        }
        return retval;
    }

}
