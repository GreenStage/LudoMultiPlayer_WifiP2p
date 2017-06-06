package com.ludo3wifi.userInterface.gameui;

import android.net.wifi.p2p.WifiP2pGroup;
import android.util.Log;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.ludo3wifi.LudoApplication;
import com.ludo3wifi.game.GameCore;
import com.ludo3wifi.game.GameState;
import com.ludo3wifi.userInterface.GameLauncher;
import com.ludo3wifi.net.GroupManager;

/**
 * Created by eduardogomes on 30/05/17.
 */

public class GameUI extends ApplicationAdapter implements GroupManager.ConnectionListener, GameCore.PawnMover, GameCore.TurnShifter, GameCore.DiceRoller {

    /*MULTIPLAYER PACKET IDENTIFIERS*/
    public static final String ROLL_DICE = "ROLL";
    public static final String MOVE_PAWN = "MOVE";
    public static final String SET_STATE = "STAT";
    public static final String FAILURE = "FAIL";

    /*Alert Strings*/
    public static final String CANT_MOVE_PAWN = "Cannot move pawn!";
    public static final String CANNOT_ROLL_DICE = "Cannot roll the dice!";
    public static final String UNKNOWN_MSG = "UNKNOWN MESSAGE";
    GroupManager mGroupManager;

    GameState savedState;
    int playerID = -1;
    int nPlayers = 0;
    int playerWon = -1;
    boolean rolling = false, doingAction = false, drawTurn = false, stopped = false, pawnsMoved = false;
    int rolledValue = 1;
    float stateTime = 0f, stateTime2 = 0f, stateTime3 = 0f, stateTime4 = 0f, stateTime5 = 0f;
    float width, height, squareSide, bYStart;
    Stage mStage;
    Dialog failDialog;
    GameLauncher mainLauncher;
    LudoApplication AppContext;
    Batch batch;
    Texture squareT, midle, onHoverT;
    Texture[] pawnImgs = new Texture[4];
    Texture pawnO;
    Texture[] diceT = new Texture[6];
    Texture[] diceA = new Texture[3];
    Texture[] ptNames = new Texture[4];
    Texture[] ptbgs = new Texture[4];
    Texture[] pWon = new Texture[4];
    Texture diceD;
    Texture bgTexture;
    Animation<Texture> diceAn;
    Animation<Texture> diceBtnAn;
    AnimationActor diceActor;
    AnimationActor rollDiceBtn;
    RectDrawer rects[] = new RectDrawer[84];
    Actor home[] = new Actor[4];
    PawnDrawer pawns[][] = new PawnDrawer[4][4];
    Actor mid;
    Actor board;

    PlayerTurnActor[] ptActors = new PlayerTurnActor[4];
    Skin skin;
    BitmapFont font;
    TextDrawer[] texts = new TextDrawer[52];

    GameCore mGame;

    public GameUI(GameLauncher mainLauncher, int playerID, int nPlayers) {
        this.mainLauncher = mainLauncher;
        this.playerID = playerID;
        this.nPlayers = nPlayers;
        AppContext = (LudoApplication) mainLauncher.getApplication();
        mGroupManager = AppContext.getGroupManager();
        mGroupManager.setListener(this);
    }


    @Override
    public void create() {
        batch = new SpriteBatch();
        mStage = new Stage();
        Gdx.input.setInputProcessor(mStage);
        mGame = new GameCore(nPlayers, this);
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        squareSide = width / 15;
        squareT = new Texture("squareblacnk.png");
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        bgTexture = new Texture("bg.jpg");
        pawnO = new Texture("pawO.png");

        for(int i = 0; i <4;i++){
            pWon[i] = new Texture("p" + String.valueOf(i +1) + "w.png");
        }
        final GameUI self = this;

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("helvetica.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 16;
        font = generator.generateFont(parameter);
        font.setColor(1f, 1f, 1f, 1f);
        font.getData().setScale(2);
        generator.dispose();

        onHoverT = new Texture("onHover.png");
        bYStart = (height - width) / 2;

        board = new RectDrawer(0, bYStart, width, width, Color.BLACK);
        createBoard();

        midle = new Texture("midle.png");
        mid = new RectDrawer(6 * squareSide, 6 * squareSide + bYStart, 3 * squareSide, 3 * squareSide, midle, midle);

        for (int i = 0; i < 6; i++) {
            diceT[i] = new Texture("dice" + String.valueOf(i + 1) + ".png");
        }
        diceAn = new Animation<Texture>(0.1f, new Array<Texture>(diceT), Animation.PlayMode.LOOP);
        diceActor = new AnimationActor(0, 0, bYStart * 0.8f, bYStart * 0.8f, diceAn);

        diceA[0] = new Texture("rolla1.png");
        diceA[1] = new Texture("rolla2.png");
        diceA[2] = new Texture("rolla3.png");
        diceBtnAn = new Animation<Texture>(0.2f, new Array<Texture>(diceA), Animation.PlayMode.LOOP);
        rollDiceBtn = new AnimationActor(squareSide * 7, 0, 3 * 1.62f * squareSide, 3 * squareSide, diceBtnAn);

        diceD = new Texture("rolld.png");
        rollDiceBtn.setDisabledFrame(diceD);

        rollDiceBtn.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if ((mGame.playersTurn & 0x7) - 1 == playerID && !rolling)
                    return true;
                else return false;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                if (playerID == 0) {
                    mGame.rollDice(playerID, self);
                } else {
                    mGroupManager.rqstMsg(ROLL_DICE + String.valueOf(playerID));
                    doingAction = true;
                    rollDiceBtn.setDisabled();
                }

            }
        });
        mStage.addActor(rollDiceBtn);


        for (int i = 0; i < 4; i++) {
            pawnImgs[i] = new Texture("pawn" + i + "n.png");
            float x0 = (i == 0 || i == 3) ? squareSide : squareSide * 10;
            float y0 = (i < 2) ? bYStart + squareSide : bYStart + squareSide * 10;
            for (int j = 0; j < 4; j++) {
                float xa = (j < 2) ? x0 : x0 + 3 * squareSide;
                float ya = (j % 2 == 0) ? y0 : y0 + 3 * squareSide;
                rects[52 + i * 4 + j] = new RectDrawer(xa, ya, squareSide, squareSide, Color.BLACK);
                pawns[i][j] = new PawnDrawer(xa, ya, squareSide, pawnImgs[i], pawnO);

                final int c = i, d = j;
                pawns[i][j].addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        int f;
                        if ((mGame.playersTurn & 0x7) - 1 != playerID)
                            return false;
                        if (rolling)
                            return false;
                        if ((f = mGame.calcNextPos(playerID, c, d)) != -1) {
                            rects[f].setonHover(true);
                        }
                        return true;
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        int f;
                        if (( f = mGame.calcNextPos(playerID, c, d)) < 0) {
                            return;
                        }
                        rects[f].setonHover(false);
                        if (playerID == 0) {
                            movePawn(c, d, f);

                        } else {
                            mGroupManager.rqstMsg(MOVE_PAWN + String.valueOf(c) + String.valueOf(d));
                            doingAction = true;
                        }

                    }
                });

                mStage.addActor(pawns[i][j]);
            }

        }
        for (int i = 0; i < 4; i++) {
            ptbgs[i] = new Texture("p" + String.valueOf(i + 1) + "tb.png");
            if (i != playerID)
                ptNames[i] = new Texture("tp1t.png");
            else ptNames[i] = new Texture("yt.png");

            ptActors[i] = new PlayerTurnActor(0.05f * width, height, width * 0.9f, ptbgs[i], ptNames[i]);

        }


    }

    public void movePawn(final int pID, final int pawnId, final int target) {
        mGame.movePawn(playerID, pID, pawnId, this);

    }


    public void showErrorMessage(String title, String msg) {
        failDialog = new Dialog(title, skin) {
            @Override
            protected void result(Object object) {
                hide();
            }
        };
        failDialog.text(msg, new Label.LabelStyle(font, Color.WHITE));
        failDialog.setWidth(width * 0.90f);
        failDialog.button("OK", 1);
        failDialog.getButtonTable().setHeight(40);
        failDialog.show(mStage);
        failDialog.setWidth(width * 0.90f);
        failDialog.setX(0.05f * width);
        failDialog.getButtonTable().setHeight(40);
        failDialog.getTitleLabel().setHeight(30);
    }

    @Override
    public void onNewRequest(String message) {

        if (playerID == 0) { //Only Room Owner should process requests
            if (message.length() >= ROLL_DICE.length() + 1 && message.substring(0, ROLL_DICE.length()).equals(ROLL_DICE)) {
                final int player = Integer.valueOf(message.substring(ROLL_DICE.length(), ROLL_DICE.length() + 1));
                mGame.rollDice(player, this);
            }

            else if (message.length() >= MOVE_PAWN.length() + 2 && message.substring(0, MOVE_PAWN.length()).equals(MOVE_PAWN)) {
                final int player = Integer.valueOf(message.substring(MOVE_PAWN.length(), MOVE_PAWN.length() + 1));
                final int pawn = Integer.valueOf(message.substring(MOVE_PAWN.length() + 1, MOVE_PAWN.length() + 2));
                mGame.movePawn(player, player, pawn, this);
            }


        }
    }

    @Override
    public void onNewPost(String message) {
        if (playerID != 0) { //Room Owner should not process posts
            if (message.length() > SET_STATE.length() && message.substring(0, SET_STATE.length()).equals(SET_STATE)) {
                GameState st = new GameState(message.substring(SET_STATE.length()));
                mGame.loadState(st, this, this, this);
            }
            else if (message.length() >= FAILURE.length() + 1 && message.substring(0, FAILURE.length()).equals(FAILURE)) {
                String msg = message.substring(FAILURE.length() + 1);
                int pID = Integer.valueOf(msg.substring(0,1));
                String title = msg.substring(2,msg.indexOf('/',2));
                String desc = msg.substring(msg.indexOf('/',3),msg.lastIndexOf('/'));
                if (Integer.valueOf(message.substring(FAILURE.length(), FAILURE.length() + 1)) == playerID) {
                    showErrorMessage(title, desc);
                }

            }
        }
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {

    }

    @Override
    public void onPeerDisconnected(String extraMsg) {
        int id = Integer.valueOf(extraMsg);
        mGame.removePlayer(id);
        showErrorMessage("Player disconnect", "Player " + id + " has disconnected from the game");
    }

    @Override
    public void onGroupConnected() {

    }

    @Override
    public void onDisconnected(String extraMsg) {
        showErrorMessage("Disconnected","Disconnected from group");
    }

    @Override
    public void onTcpConnected() {

    }

    public void end() {
        if(mGroupManager != null){
            mGroupManager.disconnect(String.valueOf(playerID + 1));
            mGroupManager.Destroy();
        }

    }

    @Override
    public void move(int pId, int pawnID, int nextPos) {
        pawns[pId][pawnID].setX(rects[nextPos].getX());
        pawns[pId][pawnID].setY(rects[nextPos].getY() + squareSide / 3);
        pawns[pId][pawnID].setBounds(pawns[pId][pawnID].getX(), pawns[pId][pawnID].getY(), pawns[pId][pawnID].getWidth(), pawns[pId][pawnID].getHeight());
    }

    @Override
    public void onMovingSuccess(int player, int pawnId, int fPos) {
        if (playerID == 0) {
            GameState mState = mGame.getState();
            mGroupManager.postMsg(SET_STATE + mState.toString());
        }
    }

    @Override
    public void overlap(int pos) {
        if (texts[pos] == null) {
            texts[pos] = new TextDrawer(rects[pos].getX() + squareSide * 0.285f, rects[pos].getY() + squareSide * 1.4f, squareSide, String.valueOf(2), font);
            mStage.addActor(texts[pos]);
        } else {
            int a = Integer.valueOf(texts[pos].getText());
            texts[pos].setText(String.valueOf(a + 1));
        }

    }

    @Override
    public void removeOverlap(int pos) {
        if (texts[pos] != null) {
            int a = Integer.valueOf(texts[pos].getText());
            if (a == 2) {
                texts[pos].remove();
                texts[pos] = null;
            } else {
                texts[pos].setText(String.valueOf(a - 1));
            }
        }
    }

    @Override
    public void onWin(int playerID) {
        playerWon = playerID;
    }

    @Override
    public void onTurnChanged(int playerTurn) {
        doingAction = false;
        drawTurn = true;
        if (playerID == 0) {
            GameState mState = mGame.getState();
            mGroupManager.postMsg(SET_STATE + mState.toString());
            Log.d("TAG", "posting state");
        }
    }

    @Override
    public void onFailMoving(int playerID,String reason) {
        if(playerID == 0 && playerID == this.playerID){
            showErrorMessage(CANT_MOVE_PAWN, reason);
        }
        else if(this.playerID != playerID && this.playerID != 0){
            mGroupManager.postMsg(FAILURE + '/' + playerID + '/' + CANT_MOVE_PAWN + '/'+ reason);
        }
    }

    @Override
    public void onDiceRolled(int player, int value) {
        rolling = true;
        rolledValue = value;
        if (playerID == 0) {
            GameState mState = mGame.getState();
            mGroupManager.postMsg(SET_STATE + mState.toString());
        }

    }

    @Override
    public void onFailRolling(int playerID,String reason) {
        if(playerID == 0 && playerID == this.playerID){
            showErrorMessage(CANNOT_ROLL_DICE, reason);
        }
        else if(this.playerID != playerID && this.playerID != 0){
            mGroupManager.postMsg(FAILURE + '/' + playerID + '/' + CANNOT_ROLL_DICE + '/'+ reason);
        }
    }



    private void createBoard() {
        float x0, y0, yAux = 0, xAux = 0;
        int pit = 0;
        x0 = squareSide * 6;
        y0 = bYStart;

        //Draw green
        home[0] = new RectDrawer(0, bYStart, squareSide * 9, squareSide * 6, Color.valueOf("#A8FB71"));
        home[1] = new RectDrawer(squareSide * 9, bYStart, squareSide * 6, squareSide * 9, Color.valueOf("#3494FF"));
        home[2] = new RectDrawer(squareSide * 6, bYStart + squareSide * 9, squareSide * 9, squareSide * 6, Color.valueOf("#FFE44D"));
        home[3] = new RectDrawer(0, bYStart + squareSide * 6, squareSide * 6, squareSide * 9, Color.valueOf("#DC5C5C"));

        /*fill 3 horizontally left->right*/
        yAux = y0;

        for (int xit = 0; xit < 3; xit++) {
            xAux = xit * squareSide + x0;
            rects[pit++] = new RectDrawer(xAux, yAux, squareSide, squareSide, squareT, onHoverT);
        }

        y0 = squareSide + bYStart;
        xAux = 8 * squareSide;
        /*fill 5 vertifcally down->top*/
        for (int yit = 0; yit < 5; yit++) {
            yAux = yit * squareSide + y0;
            rects[pit++] = new RectDrawer(xAux, yAux, squareSide, squareSide, squareT, onHoverT);
        }

        x0 = squareSide * 9;
        yAux += squareSide;

        /*fill 5 horizontally left->right*/
        for (int xit = 0; xit < 5; xit++) {
            xAux = xit * squareSide + x0;
            rects[pit++] = new RectDrawer(xAux, yAux, squareSide, squareSide, squareT, onHoverT);
        }

        /*fill 3 vertically down->top*/
        y0 = 6 * squareSide + bYStart;
        xAux = squareSide * 14;
        for (int yit = 0; yit < 3; yit++) {
            yAux = yit * squareSide + y0;
            rects[pit++] = new RectDrawer(xAux, yAux, squareSide, squareSide, squareT, onHoverT);
        }

        x0 = 9 * squareSide;
        yAux = squareSide * 8 + bYStart;
        /*fill 5 horizontally right->left*/
        for (int xit = 4; xit >= 0; xit--) {
            xAux = xit * squareSide + x0;
            rects[pit++] = new RectDrawer(xAux, yAux, squareSide, squareSide, squareT, onHoverT);
        }

        y0 = 9 * squareSide + bYStart;
        xAux = 8 * squareSide;
        /*fill 5 vertifcally top->down*/
        for (int yit = 0; yit < 5; yit++) {
            yAux = yit * squareSide + y0;
            rects[pit++] = new RectDrawer(xAux, yAux, squareSide, squareSide, squareT, onHoverT);
        }

        /*fill 3 horizontaly right->left*/
        yAux = 14 * squareSide + bYStart;
        x0 = squareSide * 6;
        for (int xit = 2; xit >= 0; xit--) {
            xAux = xit * squareSide + x0;
            rects[pit++] = new RectDrawer(xAux, yAux, squareSide, squareSide, squareT, onHoverT);
        }

        y0 = 9 * squareSide + bYStart;
        xAux = 6 * squareSide;
        /*fill 5 vertifcally down->top*/
        for (int yit = 4; yit >= 0; yit--) {
            yAux = yit * squareSide + y0;
            rects[pit++] = new RectDrawer(xAux, yAux, squareSide, squareSide, squareT, onHoverT);
        }

        /*fill 5 horizontaly right->left*/
        yAux = 8 * squareSide + bYStart;
        x0 = squareSide;
        for (int xit = 4; xit >= 0; xit--) {
            xAux = xit * squareSide + x0;
            rects[pit++] = new RectDrawer(xAux, yAux, squareSide, squareSide, squareT, onHoverT);
        }

        y0 = 6 * squareSide + bYStart;
        xAux = 0;
        /*fill 3 vertifcally down->top*/
        for (int yit = 2; yit >= 0; yit--) {
            yAux = yit * squareSide + y0;
            rects[pit++] = new RectDrawer(xAux, yAux, squareSide, squareSide, squareT, onHoverT);
        }

        yAux = 6 * squareSide + bYStart;
        x0 = squareSide;
        /*fill 5 horizontally left->right*/
        for (int xit = 0; xit < 5; xit++) {
            xAux = xit * squareSide + x0;
            rects[pit++] = new RectDrawer(xAux, yAux, squareSide, squareSide, squareT, onHoverT);
        }

        y0 = squareSide + bYStart;
        xAux = 6 * squareSide;
        /*fill 5 vertifcally down->top*/
        for (int yit = 4; yit >= 0; yit--) {
            yAux = yit * squareSide + y0;
            rects[pit++] = new RectDrawer(xAux, yAux, squareSide, squareSide, squareT, onHoverT);
        }

        pit = 68;
        x0 = squareSide *7;
        y0 = squareSide *2 + bYStart;
        for(int i = 0; i < 4; i++){
            yAux = i * squareSide +y0;
            rects[pit++] = new RectDrawer(x0, yAux, squareSide, squareSide, Color.valueOf("#A8FB71"));
        }

        x0 = squareSide *12;
        y0 = squareSide *7 + bYStart;
        for(int i = 0; i < 4; i++){
            xAux = x0 - i*squareSide ;
            rects[pit++] = new RectDrawer(xAux, y0, squareSide, squareSide, Color.valueOf("#3494FF"));
        }

        x0 = squareSide *7;
        y0 = squareSide *12 + bYStart;
        for(int i = 0; i < 4; i++){
            yAux = y0 - i*squareSide ;
            rects[pit++] = new RectDrawer(x0, yAux, squareSide, squareSide, Color.valueOf("#FFE44D"));
        }

        x0 = squareSide *2;
        y0 = squareSide *7 + bYStart;
        for(int i = 0; i < 4; i++){
            xAux = i*squareSide +x0;
            rects[pit++] = new RectDrawer(xAux, y0, squareSide, squareSide, Color.valueOf("#DC5C5C"));
        }

    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(bgTexture, 0, 0,width,height);
        board.draw(batch, 1);
        for (int i = 0; i < 4; i++) {
            home[i].draw(batch, 1);
        }
        for (int i = 0; i < 52; i++) {
            rects[i].draw(batch, 1);
        }


        mid.draw(batch, 1);


        if (mGame.isPlayerTurn(playerID) && mGame.isRollTurn() && !rolling) {
            stateTime2 += Gdx.graphics.getDeltaTime();
            rollDiceBtn.setTime(stateTime2);
        } else {
            stateTime2 = 0;
            rollDiceBtn.setDisabled();
        }
        if (rolling) {
            ptActors[(mGame.playersTurn & 0x7) - 1].resetPos();
            stateTime += Gdx.graphics.getDeltaTime();
            diceActor.drawFrame(batch, stateTime);
        } else {

            diceActor.drawFrameFromTexture(batch, diceT[rolledValue - 1]);
        }
        if (stateTime >= 1f) {
            rolling = false;
            stateTime = 0;
            diceActor.drawFrameFromTexture(batch, diceT[rolledValue - 1]);
        }

        if (!rolling && (mGame.playersTurn & 0x7) - 1 == playerID && (mGame.playersTurn & mGame.MOVE_PAWN) == mGame.MOVE_PAWN) {
            pawnsMoved = false;
            stateTime5 += Gdx.graphics.getDeltaTime();
            if (stateTime5 > 0.400f) {
                stateTime5 = 0;
                for (int i = 0; i < 4; i++) {
                    if (mGame.dice.diceVl != 6 && mGame.players[playerID].pawns[i].currentPos ==
                            mGame.players[playerID].pawns[i].homePos)
                        continue;
                    if(mGame.players[playerID].pawns[i].currentPos > 67)
                        continue;
                    pawns[playerID][i].setHover(!pawns[playerID][i].getHover());
                }
            }
        } else if (!pawnsMoved) {
            for (int i = 0; i < 4; i++) {
                pawns[playerID][i].setHover(false);
            }
            pawnsMoved = true;
            stateTime5 = 0;
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                pawns[i][j].draw(batch, 1);
            }
        }


        if (drawTurn && !rolling) {
            stateTime3 += Gdx.graphics.getDeltaTime();
            if (stateTime4 < 0) {
                drawTurn = false;
                ptActors[(mGame.playersTurn & 0x7) - 1].resetPos();
            } else if (stateTime4 > 0) {
                ptActors[(mGame.playersTurn & 0x7) - 1].draw(batch, 1);
                stateTime4 -= Gdx.graphics.getDeltaTime();
            } else {
                if (stateTime3 > 0.02f) {
                    stateTime3 = 0;
                    ptActors[(mGame.playersTurn & 0x7) - 1].move(0, -bYStart / 20);
                }
                ptActors[(mGame.playersTurn & 0x7) - 1].draw(batch, 1);
            }
            if (!stopped && ptActors[(mGame.playersTurn & 0x7) - 1].getY() < bYStart + width) {
                ptActors[(mGame.playersTurn & 0x7) - 1].draw(batch, 1);
                stopped = true;
                stateTime4 = 2;
            }

        } else {
            stopped = false;
            stateTime3 = 0;
            stateTime4 = 0;


        }

        rollDiceBtn.draw(batch, 1);

        if(playerWon >= 0){
            float tWidth = width * 0.9f;
            float tHeight = tWidth * pWon[playerWon].getHeight()/pWon[playerWon].getWidth();
            float y0 = (height - tHeight)/2;
            batch.draw(pWon[playerWon],width*0.05f,y0,tWidth,tHeight);
        }

        batch.end();
        mStage.act();
        mStage.draw();

    }

    @Override
    public void dispose() {
        batch.dispose();

        squareT.dispose();
        midle.dispose();
        for (int i = 0; i < 6; i++) {
            diceT[i].dispose();
        }
        diceA[0].dispose();
        diceA[1].dispose();
        diceA[2].dispose();
        diceD.dispose();
        for (int i = 0; i < 4; i++) {
            pawnImgs[i].dispose();
            ptNames[i].dispose();
            ptbgs[i].dispose();
        }
        bgTexture.dispose();
        pawnO.dispose();
    }

    public void onResume(){

        if(mGame != null && savedState != null){
            mGroupManager.postMsg(SET_STATE + savedState);
            mGame.loadState(savedState,this,this,this);
        }

    }

    public void onPause(){
        if(mGame != null) {
            savedState = mGame.getState();
        }
    }
}
