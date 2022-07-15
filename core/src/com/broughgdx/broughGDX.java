package com.broughgdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

class Tile {
	float x;
	float y;
	public boolean passable;

	Tile(float _x, float _y, boolean _passable) {
		this.x = _x;
		this.y = _y;
		this.passable = _passable;
	}
}

public class broughGDX extends ApplicationAdapter {
	static int SIZE = 32;
	static int MAP_WIDTH = 12;
	static int MAP_HEIGHT = 12;

	SpriteBatch batch;
	Texture allHeroes;
	Texture environmentTexture;
	TextureRegion mainHero;
	boolean isHeroFacingRight;
	TextureRegion wall;
	TextureRegion floor;
	Vector2 mainHeroPosition;
	BroughInputProcessor myInputProcessor;

	Array<Tile> dungeonTiles;
	
	@Override
	public void create () {
		myInputProcessor = new BroughInputProcessor();
		Gdx.input.setInputProcessor(myInputProcessor);

		batch = new SpriteBatch();
		allHeroes = new Texture("lofi_char.png");
		environmentTexture = new Texture("lofi_environment.png");
		mainHero = new TextureRegion(allHeroes, 0, 0, 8, 8);
		floor = new TextureRegion(environmentTexture, 32, 0, 8, 8);
		wall = new TextureRegion(environmentTexture, 112, 96, 8, 8);

		mainHeroPosition = new Vector2(320, 320);

		dungeonTiles = new Array<Tile>();

		// generating dungeon
		for(int i = 0; i < MAP_WIDTH; i++) {
			for(int j = 0; j < MAP_HEIGHT; j++) {
				boolean passable = true;

				if(MathUtils.random(1.0f) < 0.2f) {
					passable = false;
				}

				if( i == 0 || j == 0 || i == MAP_WIDTH - 1 || j == MAP_HEIGHT - 1) {
					passable = false;
				}

				dungeonTiles.add(new Tile(i * SIZE, j * SIZE, passable));
			}
		}

		dungeonTiles.get(0).passable = false; // should be top-left?
	}

	@Override
	public void render () {
		// Updating things... Isn't there a Render function?
		// Handling Hero Movement
		if(myInputProcessor.Left()) {
			mainHeroPosition.x -= SIZE;
			isHeroFacingRight = false;
		} else if(myInputProcessor.Right()) {
			mainHeroPosition.x += SIZE;
			isHeroFacingRight = true;
		} else if(myInputProcessor.Up()) {
			mainHeroPosition.y += SIZE;
		} else if(myInputProcessor.Down()) {
			mainHeroPosition.y -= SIZE;
		}

		// actually drawing
		ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
		batch.begin();

		// rendering the map
		float nextX = 4 * SIZE;
		float nextY = 12 * SIZE;
		for(int i = 0; i < MAP_WIDTH; i++) {
			for(int j = 0; j < MAP_HEIGHT; j++) {
				if(dungeonTiles.get(i * MAP_WIDTH + j).passable) {
					batch.draw(floor, nextX, nextY, 32, 32);
				} else {
					batch.draw(wall, nextX, nextY, 32, 32);
				}

				nextX += SIZE;
			}
			nextX = 4 * SIZE;
			nextY -= SIZE;
		}

		float heroWidth = 32;
		float positionCorrection = 0;
		int sign = isHeroFacingRight ? 1 : -1;
		if (sign < 0) {
			positionCorrection = SIZE;
		}
		batch.draw(mainHero, mainHeroPosition.x + positionCorrection, mainHeroPosition.y, sign * heroWidth, 32);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		allHeroes.dispose();
	}
}