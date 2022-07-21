package com.broughgdx;

// a dungeon is basically just a set of tiles?!

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class BroughDungeon {
    public static int MAP_WIDTH = 12;
    static int MAP_HEIGHT = 12;
    private Array<BroughTile> dungeonTiles;
    public Array<BroughTile> GetTiles() {
        return dungeonTiles;
    }

    BroughDungeon() {
        dungeonTiles = new Array<BroughTile>();
    }

    public boolean IsInBounds(int x, int y) {
        return x > 0 && y > 0 && x <= MAP_WIDTH - 1 && y <= MAP_HEIGHT - 1;
    }

    BroughTile GetTile(int x, int y) {
        if(IsInBounds(x, y)) {
            return dungeonTiles.get(y * MAP_WIDTH + x);
        } else {
            return new BroughTile(x, y, false);
        }
    }

    BroughTile RandomPassableTile() {
        BroughTile t = new BroughTile(0, 0, false);

        int tries = 0;
        while(tries < 10) {
            int x = MathUtils.random(0, MAP_WIDTH - 1);
            int y = MathUtils.random(0, MAP_HEIGHT - 1);
            t = GetTile(x, y);

            if(t.passable) {
                break;
            }

            tries++;
        }

        return t;
    }

    public void GenerateLevel() {
        // generating dungeon
        if (dungeonTiles.size > 0) {
            dungeonTiles.clear();
        }

        int passableTiles = 0;
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                boolean passable = true;

                if (MathUtils.random(1.0f) < 0.2f) {
                    passable = false;
                }

                if (i == 0 || j == 0 || i == MAP_WIDTH - 1 || j == MAP_HEIGHT - 1) {
                    passable = false;
                }

                if (passable) {
                    passableTiles++;
                }

                dungeonTiles.add(new BroughTile(i, j, passable));
            }
        }
    }

}
