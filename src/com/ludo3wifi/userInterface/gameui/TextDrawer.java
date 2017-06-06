package com.ludo3wifi.userInterface.gameui;

/**
 * Created by eduardogomes on 31/05/17.
 */

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;


/**
 * Created by eduardogomes on 30/05/17.
 */

public class TextDrawer extends Actor {
    static private boolean projectionMatrixSet;
    String text;
    BitmapFont font;

    public TextDrawer(float x0, float y0, float width,String text,BitmapFont font){
        projectionMatrixSet = false;
        this.setX(x0);
        this.setY(y0);
        this.setWidth(width);
        this.setHeight(width);
        this.text = text;
        this.font = font;
        setTouchable(Touchable.enabled);
        setBounds(x0,y0,width,width);
    }
    public String getText(){ return text;}
    public void setText(String text){
        this.text = text;
    }
    @Override
    public void draw(Batch batch, float alpha){
        font.draw(batch,text,getX(),getY());
        setBounds(getX(),getY(), getWidth(), getHeight());
    }
}
