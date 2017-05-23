package rmsf.eduardodiogo.ludo3wifi.userInterface;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import rmsf.eduardodiogo.ludo3wifi.R;

import rmsf.eduardodiogo.ludo3wifi.game.Board;

/**
 * Created by eduardogomes on 22/05/17.
 */
public class GameRoomFrag extends RoomFrag {
    int players = 4;
    int playerID;
    Board mGame;
    Canvas mainBoard;
    float posSide;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gamelayout, container, false);
        this.listView = (ListView) v.findViewById(R.id.listrpeers);
        Bundle args = getArguments();
        playerID = args.getInt("userID", 0);
        return v;
    }

    @Override
    public void addMember(){
        players++;
    }

    @Override
    public void rcvMsg(String msg){
        if(msg.length() >= 4 && msg.substring(0,4).equals("TRY_")){
            int player = Integer.valueOf(msg.substring(4,5));
            int tryAction = Integer.valueOf(msg.substring(5));
            if(tryAction == Board.ROLLDICE){
                mGame.rollDice(player, new Board.GameEventListener() {
                    @Override
                    public void onSuccess(int value) {
                        mRoomActivity.SendTestMsg("ACTN_ROLLED_DICE" + playerID +"" + value);
                    }

                    @Override
                    public void onFailure(String reason) {

                    }
                });
            }
        }
        if(msg.length() >= 11 && msg.substring(0,11).equals("ROLLED_DICE")){
            Toast.makeText(mRoomActivity,"Player "+ msg.substring(11) +" rolled" + msg.substring(12), Toast.LENGTH_SHORT);
        }
        else if(msg.length() >= 4 && msg.equals("ROLL")){

        }
    }

    private void drawTriangle(int color,Point a, Point b, Point c){
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.lineTo(b.x, b.y);
        path.lineTo(c.x, c.y);
        path.lineTo(a.x, a.y);
        path.close();
        mainBoard.drawPath(path, paint);
    }

    private void draw_board(){
        float x0,y0;
        x0 = posSide * 6 + 1;
        y0 = 0;
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);

        for(int yit = 0; yit < 6 ; yit ++){
            float yAux = yit * posSide + y0;
            for(int xit = 0; xit < 3;xit++){
                float xAux = xit * posSide+ x0;
                mainBoard.drawRect(xAux + 1,yAux + 1,xAux + posSide -1,yAux + posSide -1, paint);
            }
        }
        x0 = posSide * 6 + 1;
        y0 = posSide * 9;
        for(int yit = 0; yit < 6 ; yit ++){
            float yAux = yit * posSide + y0;
            for(int xit = 0; xit < 3;xit++){
                float xAux = xit * posSide+ x0;
                mainBoard.drawRect(xAux + 1,yAux + 1,xAux + posSide -1,yAux + posSide -1, paint);
            }
        }
        x0 = 1;
        y0 = posSide * 6;
        for(int yit = 0; yit < 3 ; yit ++){
            float yAux = yit * posSide + y0;
            for(int xit = 0; xit < 6;xit++){
                float xAux = xit * posSide+ x0;
                mainBoard.drawRect(xAux + 1,yAux + 1,xAux + posSide -1,yAux + posSide -1, paint);
            }
        }
        x0 = 1 + posSide * 9;
        y0 = posSide * 6;
        for(int yit = 0; yit < 3 ; yit ++){
            float yAux = yit * posSide + y0;
            for(int xit = 0; xit < 6;xit++){
                float xAux = xit * posSide+ x0;
                mainBoard.drawRect(xAux + 1,yAux + 1,xAux + posSide -1,yAux + posSide -1, paint);
            }
        }

        /*Draw colored paths*/
        paint.setColor(Color.GREEN);
        x0 = posSide + 1;
        y0 = 6*posSide + 1;
        mainBoard.drawRect(x0,y0,x0+ posSide -1,y0 + posSide -1, paint);
        for(int i = 1; i < 6; i++){
            x0 = i*posSide + 1;
            y0= 7*posSide + 1;
            mainBoard.drawRect(x0,y0,x0+ posSide -1,y0 + posSide -1, paint);
        }

        paint.setColor(Color.RED);
        x0 = 8*posSide + 1;
        y0 = posSide + 1;
        mainBoard.drawRect(x0,y0,x0+ posSide -1,y0 + posSide -1, paint);
        for(int i = 1; i < 6; i++){
            x0 = 7*posSide + 1;
            y0= i*posSide + 1;
            mainBoard.drawRect(x0,y0,x0+ posSide -1,y0 + posSide -1, paint);
        }

        paint.setColor(Color.BLUE);
        x0 = 13*posSide + 1;
        y0 = 8*posSide + 1;
        mainBoard.drawRect(x0,y0,x0+ posSide -1,y0 + posSide -1, paint);
        for(int i = 1; i < 6; i++){
            x0 = (14 - i) *posSide + 1;
            y0= 7*posSide + 1;
            mainBoard.drawRect(x0,y0,x0+ posSide -1,y0 + posSide -1, paint);
        }

        paint.setColor(Color.YELLOW);
        x0 = 6*posSide + 1;
        y0 = 13*posSide + 1;
        mainBoard.drawRect(x0,y0,x0+ posSide -1,y0 + posSide -1, paint);
        for(int i = 1; i < 6; i++){
            x0 = 7* posSide + 1;
            y0= (14-i)*posSide + 1;
            mainBoard.drawRect(x0,y0,x0+ posSide -1,y0 + posSide -1, paint);
        }

        /*Draw center triangles*/
        drawTriangle(Color.GREEN,
                new Point((int)posSide * 6 ,(int)posSide * 6),
                new Point((int)(posSide * 7.5),(int)(posSide * 7.5)),
                new Point((int)posSide * 6 ,(int)posSide * 9));
        drawTriangle(Color.RED,
                new Point((int)posSide * 6 ,(int)posSide * 6),
                new Point((int)(posSide * 7.5),(int)(posSide * 7.5)),
                new Point((int)posSide * 9,(int)posSide * 6));
        drawTriangle(Color.BLUE,
                new Point((int)posSide * 9,(int)posSide * 9),
                new Point((int)(posSide * 7.5),(int)(posSide * 7.5)),
                new Point((int)posSide * 9,(int)posSide * 6));
        drawTriangle(Color.YELLOW,
                new Point((int)posSide * 9,(int)posSide * 9),
                new Point((int)(posSide * 7.5),(int)(posSide * 7.5)),
                new Point((int)posSide * 6 ,(int)posSide * 9));

    }
    private void draw_home_case(int caseId){
        float x0,y0;
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        switch(caseId){
            case 0:
                x0 = 0;
                y0 = 0;
                paint.setColor(Color.GREEN);
                break;
            case 1:
                x0 = mainBoard.getWidth() - posSide * 6 -1;
                y0 = 0;
                paint.setColor(Color.RED);
                break;
            case 2:
                x0 = 0  ;
                y0 = mainBoard.getHeight() - posSide * 6 -1;
                paint.setColor(Color.YELLOW);
                break;
            case 3:
                x0 = mainBoard.getWidth() - posSide * 6 -1;
                y0 = mainBoard.getHeight() - posSide * 6 -1;
                paint.setColor(Color.BLUE );
                break;
            default:
                return;
        }


        mainBoard.drawRect(x0,y0,x0 + posSide * 6,y0 + posSide * 6, paint);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGame = new Board();

        mRoomActivity = (RoomActivity) getActivity();
        playerID =mRoomActivity.self.peerID;

        Point size = new Point();
        mRoomActivity.getWindowManager().getDefaultDisplay().getSize(size);
        posSide = size.x / 15;

            Bitmap bitmap = Bitmap.createBitmap(size.x, size.x, Bitmap.Config.RGB_565);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        mainBoard = new Canvas(bitmap);
        mainBoard.drawRect(0, 0, size.x -1, size.x -1, paint);


        ImageView imageView = new ImageView(mRoomActivity);
        imageView.setImageBitmap(bitmap);

// Create a simple layout and add our image view to it.
        RelativeLayout layout = new RelativeLayout(mRoomActivity);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layout.addView(imageView, params);
        layout.setBackgroundResource(R.drawable.bg);
        draw_board();
        for(int i = 0; i < 4; i++){
            draw_home_case(i);
        }

        Button rollDiceBtn = new Button(mRoomActivity);
        rollDiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGame.rollDice(playerID,new Board.GameEventListener() {
                    @Override
                    public void onSuccess(int value) {
                        Log.d("TAG","sending dice value " + value);
                        //TODO send actin intent by object so room owner can decide if asks or forces
                        mRoomActivity.SendTestMsg("ACTN_ROLL");
                    }

                    @Override
                    public void onFailure(String reason) {

                    }
                });
            }
        });
        layout.addView(rollDiceBtn);
// Show this layout in our activity.
        mRoomActivity.setContentView(layout);
    }


}
