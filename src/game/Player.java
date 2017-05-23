package rmsf.eduardodiogo.ludo3wifi.game;

class Player {
	int id;
    int pawns_concluded=0;
    int pawns_board=0;
    int pawns_base=4;

    Pawn[] pawns = new Pawn[4];

    Player(int id){
        this.id = id;
        for (int i = 0; i<4; i++){
            pawns[i] = new Pawn(this, i);
        }
    }

    boolean hasWon(){
        return pawns_concluded==4;
    }

    Pawn getPawn(int id){
        for (int i=0;i<4;i++) {
            if (pawns[i].id == id) {
                return pawns[i];
            }
        }
        System.out.println("Not my pawn");
        System.exit(-1);
        return null;
    }
    
    void printValidPawns(){
        System.out.println("Valid Points");
        for (Pawn p: pawns) {
            System.out.println(p.id);
        }
    }

    void inicializePawn(int[][] spaces, Player[] players){

        Pawn newPawn=null;

        for(Pawn p:pawns){
            if(p.square==0){
                newPawn = p;
                break;
            }
        }

        newPawn.movePawn(newPawn.initialSpace, spaces, players);

        if(newPawn.square ==newPawn.initialSpace){
            pawns_base--;
            pawns_board++;
        }

    }
}
