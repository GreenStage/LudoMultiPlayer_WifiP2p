package com.ludo3wifi.userInterface.gameui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

/**
 * Created by eduardogomes on 30/05/17.
 */

public class PawnDrawer extends Actor{
    static private boolean projectionMatrixSet;
    boolean hover = false;
    Texture pawnText;
    Texture moText;
    public PawnDrawer(float x0, float y0, float width, Texture text,Texture mouseHover){
        projectionMatrixSet = false;
        this.setX(x0);
        this.setY(y0);
        this.setWidth(width);
        this.setHeight(1.252f * width);
        this.pawnText = text;
        this.moText = mouseHover;

        setTouchable(Touchable.enabled);
        setBounds(x0,y0,width,1.252f * width);
    }

    public boolean getHover(){
        return hover;
    }
    public void setHover(boolean s){
        hover = s;
    }

    @Override
    public void draw(Batch batch, float alpha){

        if(hover){
            batch.draw(moText,getX(),getY(),getWidth(),getHeight());
        }
        else{
            batch.draw(pawnText,getX(),getY(),getWidth(),getHeight());
        }
    }
}
