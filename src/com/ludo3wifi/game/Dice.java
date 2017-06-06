package com.ludo3wifi.game;

import java.util.Random;

public class Dice {
    public int diceVl = 0;
    Random rnd = new Random();

    public int roll() {
        diceVl = rnd.nextInt(6) + 1;
       // diceVl = (diceVl == 6)? 1 : 6;
       // diceVl = 6;
        return diceVl;
    }

    public int roll(int vl) {
        diceVl = vl % 7;
        if (diceVl == 0) diceVl = 1;
        return diceVl;
    }
}
