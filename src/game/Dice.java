package rmsf.eduardodiogo.ludo3wifi.game;

import java.util.Random;

public class Dice {
    boolean rolled = false;
    int diceVl = 0;
    Random rnd = new Random();

    public int roll(){
        diceVl = rnd.nextInt(6)+1;
        return diceVl;
    }
}
