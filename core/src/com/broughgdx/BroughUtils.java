package com.broughgdx;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class BroughUtils {
    public static int ManhattanDistance(Vector2 a, Vector2 b) {
        return Math.abs((int)(b.x - a.x)) + Math.abs((int)(b.y - a.y));
    }

}
