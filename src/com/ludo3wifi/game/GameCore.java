package com.ludo3wifi.game;

//import com.ludo3wifi.userInterface.GameRoomFrag;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameCore {
    public static final int MOVE_PAWN = 0x8;
    public static final int ROLL_DICE = ~MOVE_PAWN;
    public static final int[] PLAYER_TURN = {
            0x1, //p1
            0x2, //p2
            0x3, //p3
            0x4, //p4
    };
    public TurnShifter tf;
    //public GameRoomFrag mUI;
    public int nPlayers;
    public boolean[] activePlayers = new boolean[4];
    public Dice dice;
    public int playersTurn = PLAYER_TURN[0];
    public Player[] players;
    int nMoves = 1;
    int playerWon = -1;
    int[] spaces = new int[84];
    //
    public GameCore(int nPlayers, TurnShifter tf) {
        // TODO Auto-generated method stub
        //this.mUI = ui;
        int nextPlayer = 0;
        this.tf = tf;
        boolean gameFinished = false;
        players = new Player[nPlayers];
        int[][] spaces = new int[18][4];
        dice = new Dice();

        for (int i = 0; i < nPlayers; i++) {
            players[i] = new Player(i);
            activePlayers[i] = true;
        }
        this.nPlayers = nPlayers;
        System.out.println("Game began!!");

    }

    public boolean isPlayerTurn(int pId) {
        if (pId == (playersTurn & ROLL_DICE) - 1) {
            return true;
        }
        return false;
    }

    public boolean isRollTurn() {
        if ((playersTurn & 0x7) == playersTurn) {
            return true;
        }
        return false;
    }

    public boolean isMoveTurn() {
        if (MOVE_PAWN == (playersTurn & MOVE_PAWN)) {
            return true;
        }
        return false;
    }

    public void rollDice(int player, DiceRoller listener) {
        nMoves = 1;
        if ((playersTurn & 0x7) != player + 1) {
            listener.onFailRolling(player, "Not your turn.");
        } else if ((playersTurn & MOVE_PAWN) == MOVE_PAWN) {
            listener.onFailRolling(player, "You already rolled the dice.");
        } else {
            dice.roll();

            System.out.println("Dice value : " + dice.diceVl);
            if (dice.diceVl == 6) nMoves++;
            getNextTurn();
            listener.onDiceRolled(player, dice.diceVl);
        }
    }

    public int pawn_step_one(Pawn pawn, boolean isFinal, final PawnMover pD) {
        int nextPos = pawn.getNextPos(); //TODO REMOVE PAWN FROM SPACE
        int moveAction = 0;
        int currentPos = pawn.getPos();
        if (pawn.getNextPos() < 0)
            return -1;

        if ((spaces[nextPos] >> 12) != 0) { //Next space is occupied
            int pawns = spaces[nextPos] & 0xFFF;
            int userId = (spaces[nextPos] >> 12) - 1;
            if (userId == pawn.owner.id) { // By this user, overlap
                pawns = (pawns << 3) + (pawn.id + 1);
                spaces[nextPos] = ((pawn.owner.id + 1) << 12) + pawns;
                pD.overlap(nextPos);
                moveAction = 0x1;
            } else { // by someoneelse , send them home or fail in case its no the final pos
                if (!isFinal) {
                    moveAction = 0x0;
                } else {
                    for (int i = pawns; i > 0; i = i >> 3) { //Send other pawns home
                        pD.removeOverlap(nextPos);
                        players[userId].pawns[(i & 0x7) - 1].move(players[userId].pawns[(i & 0x7) - 1].homePos);
                        pD.move(userId, players[userId].pawns[(i & 0x7) - 1].id, players[userId].pawns[(i & 0x7) - 1].homePos);
                        players[userId].pawns_base++;
                    }

                    spaces[nextPos] = ((pawn.owner.id + 1) << 12) + pawn.id + 1;
                    moveAction = 0x1;
                }

            }
        } else {
            spaces[nextPos] = ((pawn.owner.id + 1) << 12) + (pawn.id + 1);
            moveAction = 0x1;
        }
        if (moveAction != 0x1) return -1;

        if ((spaces[currentPos] & 0xFFF) > 0x07) { //CURRENT SPACE WAS overlap
            int cUserId = pawn.owner.id + 1;
            int cPawns = spaces[currentPos] & 0xFFF;
            int newPawns;
            int pawnA;
            for (newPawns = 0, pawnA = cPawns & 0x7; cPawns > 0; cPawns = cPawns >> 3, pawnA = cPawns & 0x7) {
                if (pawnA - 1 != pawn.id)
                    newPawns = (newPawns << 3) + pawnA;
            }
            if (newPawns == 0) {
                spaces[currentPos] = 0;
            } else spaces[currentPos] = (cUserId << 12) + newPawns;
            pD.removeOverlap(currentPos);

        } else {
            spaces[pawn.getPos()] = 0;
        }
        pawn.move(nextPos);
        pD.move(pawn.owner.id, pawn.id, nextPos);

        return nextPos;
    }

    public int calcNextPos(int pID, int owner, int pawnID) {
        int steps = dice.diceVl;
        if (pID != (playersTurn & ROLL_DICE) - 1) {
            return -1;
        } else if (pID != owner) {
            return -1;
        } else if ((playersTurn & MOVE_PAWN) != MOVE_PAWN) {
            return -1;
        } else if (players[owner].pawns[pawnID].getPos() >= 52) {
            if (dice.diceVl != 6) {
                return -1;
            } else {
                return players[owner].pawns[pawnID].initialSpace;
            }
        } else if (players[owner].pawns[pawnID].getPos() == players[owner].pawns[pawnID].finalSpace) {
            return -2;
        } else {
            int retval = players[owner].pawns[pawnID].getPos() + dice.diceVl;
            if (retval >= 52) retval = retval - 52;
            return retval;
        }
    }

    public void movePawn(int pID, final int owner, final int pawnID, final PawnMover pD) {
        int steps = dice.diceVl;
        int currentValue = players[owner].pawns[pawnID].currentPos;
        if (pID != (playersTurn & ROLL_DICE) - 1) {
            pD.onFailMoving(pID, "It's player " + String.valueOf(playersTurn & ROLL_DICE) + " turn!");
            return;
        } else if (pID != owner) {
            pD.onFailMoving(pID, "Cant move other players pawns!");
            return;
        } else if ((playersTurn & MOVE_PAWN) != MOVE_PAWN) {
            pD.onFailMoving(pID, "Please roll the dice first!");
            return;
        } else if (players[owner].pawns[pawnID].getPos() >= 52) {
            if (dice.diceVl != 6) {
                pD.onFailMoving(pID, "Cant move pawn from home without dice = 6!");
                return;
            } else {
                steps = 1;
                players[pID].pawns_base--;
                nMoves--;
            }
        } else {
            if (nMoves < 0) {
                pD.onFailMoving(pID, "You dont have any moves left!");
                return;
            }
        }
        final long TIME_STEP = 150;
        int currentTime = 0;
        final int nSteps = steps;
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(steps);
        for (int i = 0; i < steps; i++) {
            if (executor.isShutdown()) break;
            final int a = i;
            Thread r = new Thread() {
                @Override
                public void run() {
                    int own = owner, pId = pawnID, pos = players[own].pawns[pId].getPos();
                    int np;
                    if ((np = pawn_step_one(players[owner].pawns[pawnID], (a == nSteps - 1) ? true : false, pD)) > 67) {
                        players[owner].pawns_concluded++;
                        if (players[owner].pawns_concluded == 4) {
                            playerWon = owner;
                            tf.onWin(owner);
                        }
                    }
                    if (-1 == np || a == nSteps - 1) {
                        executor.shutdown();
                        pD.onMovingSuccess(owner, pawnID, players[owner].pawns[pawnID].getPos());
                    }

                }
            };
            executor.schedule(r, currentTime + TIME_STEP, TimeUnit.MILLISECONDS);
            currentTime += TIME_STEP;
        }
        executor.shutdown();
        nMoves--;
        getNextTurn();
    }

    public void getNextTurn() {
        if (playerWon < 0 && (playersTurn & MOVE_PAWN) == MOVE_PAWN) {
            if (nMoves > 0) {
                playersTurn &= ROLL_DICE;
            } else {
                nextTurn();
            }
        } else if (playerWon < 0) {
            if (dice.diceVl != 6 && players[(playersTurn & 0x7) - 1].pawns_base == 4) {
                nextTurn();
            } else {
                playersTurn |= MOVE_PAWN;
            }
        } else {
            tf.onWin(playerWon);
        }

    }

    public void nextTurn() {
        do {
            if ((playersTurn & 0x7) == nPlayers) {
                playersTurn = PLAYER_TURN[0];
            } else {
                playersTurn &= ROLL_DICE;  //next player needs to roll dice first
                playersTurn++;

            }
        } while (activePlayers[playersTurn - 1] == false);
        nMoves = 1;
        tf.onTurnChanged(playersTurn & 0x7);

    }

    public void removePlayer(int pID) {
        activePlayers[pID] = false;
        if ((playersTurn & ROLL_DICE) == pID) {
            nextTurn();
        }
    }

    public void loadState(GameState state, PawnMover mv, TurnShifter tn, DiceRoller dR) {
        if (state == null)
            return;

        if (playersTurn != state.playerTurn) {
            int aux = playersTurn;
            playersTurn = state.playerTurn;
            if ((aux & 0x7) != (state.playerTurn & 0x7))
                tn.onTurnChanged(playersTurn & 0x7);
        }
        if (dice.diceVl != state.diceValue) {
            dice.diceVl = state.diceValue;
            dR.onDiceRolled(state.playerTurn, dice.diceVl);
        }

        nMoves = state.nPlays;
        for (int i = 0; i < nPlayers; i++) {
            for (int j = 0; j < 4; j++) {
                if (players[i].pawns[j].currentPos != state.pawnPos[i][j]) {
                    int it;
                    if (state.pawnPos[i][j] > 51) {
                        players[i].pawns[j].currentPos = state.pawnPos[i][j];
                        mv.move(i, j, state.pawnPos[i][j]);
                    } else {
                        for (it = 0; it < 6 && players[i].pawns[j].currentPos != state.pawnPos[i][j]; it++) {
                            mv.move(i, j, players[i].pawns[j].getNextPos());
                            players[i].pawns[j].currentPos = players[i].pawns[j].getNextPos();
                        }
                        if (it == 6) {
                            players[i].pawns[j].currentPos = state.pawnPos[i][j];
                            mv.move(i, j, state.pawnPos[i][j]);
                        }
                    }
                }
            }
        }
    }

    public GameState getState() {
        int[][] pawnPos = new int[4][4];
        for (int i = 0; i < nPlayers; i++) {
            for (int j = 0; j < 4; j++) {
                pawnPos[i][j] = players[i].pawns[j].currentPos;
            }
        }
        //TODO WINNER
        return new GameState(playersTurn, dice.diceVl, nMoves, 0, pawnPos, nPlayers);
    }

    public interface PawnMover {
        void move(int pId, int pawnID, int nextPos);

        void onFailMoving(int playerID, String reason);

        void overlap(int pos);

        void removeOverlap(int pos);

        void onMovingSuccess(int player, int pawnId, int fPos);
    }

    public interface TurnShifter {
        void onTurnChanged(int playerTurn);

        void onWin(int playerID);
    }

    public interface DiceRoller {
        void onDiceRolled(int player, int value);

        void onFailRolling(int playerID, String reason);
    }
}
