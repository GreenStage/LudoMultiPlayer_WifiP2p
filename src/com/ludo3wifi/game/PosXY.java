package com.ludo3wifi.game;

import android.graphics.Color;

/**
 * Created by eduardogomes on 23/05/17.
 */

public class PosXY {
    public float x, y;
    public int color = Color.WHITE;
    private PosXY nextPos;

    public PosXY(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setNextPos(PosXY nextPos) {
        this.nextPos = nextPos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PosXY posXY = (PosXY) o;

        if (Float.compare(posXY.x, x) != 0) return false;
        return Float.compare(posXY.y, y) == 0;

    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }
}
