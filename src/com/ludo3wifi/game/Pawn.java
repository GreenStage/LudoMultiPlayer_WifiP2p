package com.ludo3wifi.game;

public class Pawn {
    static int[] finalPath = {-1, -1, -1, -1, -1, -1};
    public int homePos;
    public int initialSpace;
    public int currentPos;
    public int finalSpace;
    Player owner;
    int id;

    Pawn(Player p, int id) {
        this.owner = p;
        this.id = id;
        if (p.id == 0) {
            initialSpace = 0;
            finalSpace = 51;
        } else if (p.id == 1) {
            initialSpace = 13;
            finalSpace = 12;
        } else if (p.id == 2) {
            initialSpace = 26;
            finalSpace = 25;
        } else {
            initialSpace = 39;
            finalSpace = 38;
        }
        this.homePos = 52 + owner.id * 4 + id;
        this.currentPos = this.homePos;
    }

    public int getNextPos() {
        if (currentPos < 52 && currentPos != finalSpace) {
            return currentPos + 1;
        } else if (currentPos == finalSpace) {
            return (3 - owner.pawns_concluded) + owner.id * 4 + 68;
        } else if (currentPos > 67) {
            return -1; // finish
        } else {
            return initialSpace;
        }

    }

    int getPos() {
        return this.currentPos;
    }

    void move(int newLocation) {
        currentPos = newLocation;
    }


}
