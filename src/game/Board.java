package rmsf.eduardodiogo.ludo3wifi.game;

import android.support.v4.app.Fragment;

import java.util.Scanner;

public class Board extends Fragment{
    private Dice dice;
    public static int ROLLDICE = 0x01;
    public int playersTurn = 0;

    public interface GameEventListener{
        void onSuccess(int value);
        void onFailure(String reason);
    }

    public void rollDice(int player,GameEventListener listener){
        if(playersTurn != player){
            listener.onFailure("Not your turn.");
        }
        else if(dice.rolled){
            listener.onFailure("You already rolled the dice.");
        }
        else{
            dice.rolled = true;
            dice.roll();
            listener.onSuccess(dice.diceVl);
        }
    }

    //
	public Board() {
		// TODO Auto-generated method stub

		int nextPlayer = 0;
		boolean gameFinished = false;
		Player[] players = new Player[4];
		int[][] spaces = new int[18][4];
		dice = new Dice();
	
		players[0] = new Player(0);
        players[1] = new Player(1);
        players[2] = new Player(2);
        players[3] = new Player(3);

        System.out.println("Game began!!");

	}

	public

    static Pawn inputPawn(Player player) {
        Pawn pawn = null;
        System.out.println("Which pawn to move?");
        Scanner reader = new Scanner(System.in);
        Boolean invalid = true;
        int a = -1;
        
        while (invalid) {
        	boolean b= reader.hasNext();
        	if(reader.hasNext()){
	    		if(reader.hasNextInt()){
	    			a = reader.nextInt();
		            if(a!=0&&a!=1&&a!=2&&a!=3){
		            	System.out.println("Invalid Input");
		            }
		            else {
		            	pawn = player.getPawn(a);
		            	if(pawn.fase==3 || pawn.square==0){
		            		System.out.println("Pawn not valid");
		            	}
		            	else
		            		invalid = false;
		            }       
	    		}else{
	    			reader.next();
	    		}
        	}
        }
        return pawn;
    }
    
    public int getNextPlayer(int i){
        if(i<4){
            return ++i;
        }else{
            return 0;
        }
    }

}
