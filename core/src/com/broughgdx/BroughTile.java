package com.broughgdx;

public class BroughTile {
    float x;
    float y;
    public boolean passable;

    BroughTile(float _x, float _y, boolean _passable) {
        this.x = _x;
        this.y = _y;
        this.passable = _passable;
    }
}
