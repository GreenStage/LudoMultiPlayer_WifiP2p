package rmsf.eduardodiogo.ludo3wifi.game;

class Pawn {
	Player owner;
    int square = 0;
    int fase = 1;

    int id;
    int initialSpace;
    int finalSpace;
    static int[] finalPath = {-1, -1, -1, -1, -1, -1};

    Pawn(Player p, int id) {
        this.owner = p;
        this.id = id;
        if(p.id==0){
            initialSpace = 1;
            finalSpace = 39;
        }else if(p.id==1){
            initialSpace = 14;
            finalSpace = 13;
        }else{
            initialSpace = 27;
            finalSpace = 26;
        }
    }

    void movePawn(int move, int[][] spaces, Player[] players) {
        Pawn oldPawn = null;
        int novaPos = square + move;

        if (fase == 1) {

            if (madeALap(novaPos)) {
                int diff = novaPos - finalSpace;
                pawnToFase3(spaces,diff);
            } else {
                if(novaPos > 39)
                	novaPos = novaPos-39;
                if (spaces[novaPos][0] == -1) {
                    moveSquare(novaPos, spaces);
                } else {
                    if (spaces[novaPos][1] == owner.id) {
                        System.out.println("Didn't move, space with piece of same owner");
                        return;
                    } else {
                        for (Player p : players) {
                            if (p.id == spaces[novaPos][1]) {
                                oldPawn = p.getPawn(spaces[novaPos][0]);
                            }
                        }
                        oldPawn.moveToBase();

                        moveSquare(novaPos, spaces);
                    }
                }
            }
        }else{
            moveFinalPath(novaPos);
        }
    }

    void moveSquare(int novPos, int[][] spaces){
        spaces[square][0] = -1;
        spaces[square][1] = -1;

        spaces[novPos][0] = id;
        spaces[novPos][1] = owner.id;

        square = novPos;

        System.out.println("Pawn " + id + " of player " + owner.id + " moved to " + novPos + ".");

    }

    void moveToBase(){
        square = 0;
        owner.pawns_base++;
        owner.pawns_board--;

        System.out.println("Pawn " + id + " of player " + owner.id + " moved to base!");
    }

    void moveBack(int novPos, int[][] spaces){
        novPos = 58 - (novPos - 58);

        if(spaces[novPos][1] == owner.id) {
            System.out.println("Didn't move, space with piece of same owner");
            return;
        }

        moveSquare(novPos, spaces);
    }

    void pawnToFase3(int[][] spaces, int novPos){
        if(finalPath[novPos]!=-1){
            return;
        }
        spaces[square][0] = -1;
        spaces[square][1] = -1;
        
        novPos = novPos-1;
        square = novPos;
        finalPath[novPos] = id;
        System.out.println("Pawn to final path in position " + square);
        fase = 2;
    }

    void removePawn(){
        finalPath[square] = -1;
        System.out.println("Pawn " + id + " of player " + owner.id + " removed!");

        owner.pawns_board--;
        owner.pawns_concluded++;

        fase = 3;
    }

    boolean madeALap(int novaPos){
        if(square==0)
            return false;

        if(novaPos>finalSpace && square<=finalSpace)
            return true;

        return false;
    }

    void moveFinalPath(int novaPos){
        if(novaPos>5)
            novaPos = 5 - (novaPos-5);
        if(novaPos==5)
            removePawn();
        if(Pawn.finalPath[novaPos]==-1 && fase==2){
            finalPath[square] = -1;
            finalPath[novaPos] = id;
            square = novaPos;
            System.out.println("Pawn moved to position " + square + " of final path!");

        }else
            return;
    }
}
