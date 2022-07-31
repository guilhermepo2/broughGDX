package com.broughgdx;

public class BroughTile {
    public int x;
    public int y;
    public boolean passable;
    public BroughMonster monster;

    BroughTile(int _x, int _y, boolean _passable) {
        this.x = _x;
        this.y = _y;
        this.passable = _passable;
        monster = null;
    }
}
