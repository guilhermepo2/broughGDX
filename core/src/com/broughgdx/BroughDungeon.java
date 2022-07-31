package com.broughgdx;

// a dungeon is basically just a set of tiles?!

import com.badlogic.gdx.Gdx;
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

    // returns the neighbor of a given tile on a given direction
    BroughTile GetNeighbour(BroughTile tile, int dx, int dy) {
        return GetTile(tile.x + dx, tile.y + dy);
    }

    // return all the adjacent neighbors of a tile
    Array<BroughTile> GetAdjacentNeighbours(BroughTile tile) {
        Array<BroughTile> tiles = new Array<BroughTile>();
        tiles.add(GetNeighbour(tile, 0, -1));
        tiles.add(GetNeighbour(tile, 0, 1));
        tiles.add(GetNeighbour(tile, -1, 0));
        tiles.add(GetNeighbour(tile, 1, 0));
        return tiles;
    }

    // returns only passable adjacent neighbors
    Array<BroughTile> GetAdjacentPassableNeighbours(BroughTile tile) {
        Array<BroughTile> tiles = GetAdjacentNeighbours(tile);
        Array<BroughTile> passable = new Array<BroughTile>();

        for(int i = 0; i < tiles.size; i++) {
            if(tiles.get(i).passable) {
                passable.add(tiles.get(i));
            }
        }

        return passable;
    }

    // GET CONNECTED TILES: picks a random starting tile and counts how many tiles are connected to it.
    private int GetConnectedTiles() {
        Array<BroughTile> connectedTiles = new Array<BroughTile>();
        Array<BroughTile> frontier = new Array<BroughTile>();

        int numConnectedTiles = 0;
        frontier.add(RandomPassableTile());
        while(frontier.size > 0) {
            BroughTile tile = frontier.pop();
            numConnectedTiles++;
            connectedTiles.add(tile);

            // 1. getting all neighbors
            Array<BroughTile> neighbours = GetAdjacentPassableNeighbours(tile);

            // 2. add all neighbours to the frontier, if they are not there and if they are not in the connected tiles
            for(int i = 0; i < neighbours.size; i++) {
                if(!frontier.contains(neighbours.get(i), true) && !connectedTiles.contains(neighbours.get(i), true)) {
                    frontier.add(neighbours.get(i));
                }
            }
        }

        // 3. here we should have how much connected tiles we have!
        Gdx.app.log("debug", "connected tiles: " + numConnectedTiles);
        return numConnectedTiles;
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

    private int CreateLevel() {
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

        Gdx.app.log("debug", "passable tiles: " + passableTiles);
        return passableTiles;
    }

    public void GenerateLevel() {
        int numPassable = CreateLevel();
        int numConnectedTiles = GetConnectedTiles();

        while(numPassable != numConnectedTiles) {
            numPassable = CreateLevel();
            numConnectedTiles = GetConnectedTiles();
        }
    }

}
