package com.ludo3wifi.userInterface.gameui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;

/**
 * Created by eduardogomes on 31/05/17.
 */

public class AnimationActor extends Actor{
    static private boolean projectionMatrixSet;
    Animation<Texture> an;
    Texture disabledframe;
    float time = 0;
    boolean disabled = false;
    float dWidth,dHeight;

    public AnimationActor(float x0, float y0, float width,float height, Animation<Texture> an){
        projectionMatrixSet = false;
        this.setX(x0);
        this.setY(y0);
        this.an = an;
        Array<Texture> t = new Array<Texture>(an.getKeyFrames());
        dWidth = (width > t.get(0).getWidth())? t.get(0).getWidth(): width;
        dHeight = (width > t.get(0).getWidth())? t.get(0).getHeight(): height;
        setWidth(dWidth);
        setHeight(dHeight);
        setTouchable(Touchable.enabled);
        setBounds(x0,y0,getWidth(),getHeight());
    }

    public void drawFrameFromTexture(Batch batch, Texture t){
        batch.draw(t,getX(),getY(),dWidth,dHeight);
    }

    public void drawFrame(Batch batch, float time){
        batch.draw(an.getKeyFrame(time, true),getX(),getY(),dWidth,dHeight);
        setBounds(getX(),getY(), getWidth(), getHeight());
    }
    public void setDisabledFrame(Texture t){
        this.disabledframe = t;
    }
    public void setTime(float time){
        disabled = false;
        this.time = time;
    }
    public void setDisabled(){
        time = 0;
        disabled = true;
    }
    @Override
    public void draw(Batch batch, float alpha){
        if(!disabled){
            batch.draw(an.getKeyFrame(time),getX(),getY(),dWidth,dHeight);
        }
        else{
            batch.draw(disabledframe,getX(),getY(),dWidth,dHeight);
        }
        setBounds(getX(),getY(), getWidth(), getHeight());
    }
}
