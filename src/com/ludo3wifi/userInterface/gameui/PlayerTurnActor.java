package com.ludo3wifi.userInterface.gameui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

/**
 * Created by eduardogomes on 31/05/17.
 */

public class PlayerTurnActor extends Actor {
    static private boolean projectionMatrixSet;
    String text;
    Texture bg;
    Texture textT;
    float stX,stY;
    float tx0,ty0,twidth,theight;

    public PlayerTurnActor(float x0, float y0, float width,Texture bg, Texture textT){
        projectionMatrixSet = false;
        this.setX(x0);
        this.setY(y0);
        this.setWidth(width);
        this.setHeight(0.3333f * width);
        this.twidth = width *0.6f;
        this.theight = 0.1133f * width;
        float text =getHeight();
        stX = x0;
        stY = y0;
        tx0 = x0 + 0.3083f * width;
        ty0 = y0 + 0.1116f * width;
        this.bg = bg;
        this.textT = textT;
        setBounds(x0,y0,width,getHeight());
    }
    public void resetPos(){
        setX(stX);
        setY(stY);
        tx0 = stX + 0.3083f * getWidth();;
        ty0 = stY+ 0.1116f * getWidth();
    }
    public void move(float x,float y){
        setX(x +getX());
        setY(y +getY());
        ty0 += y;
        tx0 += x;
    }

    @Override
    public void draw(Batch batch, float alpha){
        batch.draw(bg,getX(),getY(),getWidth(),getHeight());
        batch.draw(textT,tx0,ty0,twidth,theight);
    }
}
