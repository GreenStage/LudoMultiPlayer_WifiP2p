package com.ludo3wifi.userInterface.gameui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Created by eduardogomes on 30/05/17.
 */

public class RectDrawer  extends Actor {

    private ShapeRenderer shapeRenderer;
    static private boolean projectionMatrixSet;
    Color color;
    Texture t,defaultT;
    Texture onHover;
    boolean drawTexture = false;

    public RectDrawer(float x0, float y0, float width, float height, Color color){
        shapeRenderer = new ShapeRenderer();
        projectionMatrixSet = false;
        setX(x0);
        setY(y0);
        setWidth(width);
        setHeight(height);
        this.color = color;
    }

    public RectDrawer(float x0, float y0, float width, float height, Texture t, Texture onHover){
        shapeRenderer = new ShapeRenderer();
        projectionMatrixSet = false;
        setX(x0);
        setY(y0);
        setWidth(width);
        setHeight(height);
        this.t = t;
        this.defaultT = t;
        this.onHover = onHover;
        drawTexture = true;
    }

    public void setonHover(boolean o){
        if(!o) t = defaultT;
        else t = onHover;
    }

    @Override
    public void draw(Batch batch, float alpha){


        if(drawTexture){
            batch.draw(t,getX(),getY(),getWidth(),getHeight());
        }
        else{
            batch.end();
            if(!projectionMatrixSet){
                shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            }
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(color);
            shapeRenderer.rect(getX(),getY(),getWidth(),getHeight());
            shapeRenderer.end();
            batch.begin();
        }
    }
}
