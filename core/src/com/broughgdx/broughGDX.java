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


public class broughGDX extends ApplicationAdapter {
	static int SIZE = 32;
	SpriteBatch batch;
	Texture allHeroes;
	Texture environmentTexture;
	TextureRegion mainHero;
	boolean isHeroFacingRight;
	TextureRegion wall;
	TextureRegion floor;
	Vector2 mainHeroPosition;
	BroughInputProcessor myInputProcessor;

	BroughDungeon theDungeon;
	
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

		theDungeon = new BroughDungeon();
		theDungeon.GenerateLevel();
		// validating dungeon
		// end of validating dungeon

		BroughTile startingTile = theDungeon.RandomPassableTile();
		mainHeroPosition.x = startingTile.x * SIZE;
		mainHeroPosition.y = startingTile.y * SIZE;
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
		Array<BroughTile> allTiles = theDungeon.GetTiles();
		for(int i = 0; i < BroughDungeon.MAP_WIDTH; i++) {
			for(int j = 0; j < BroughDungeon.MAP_HEIGHT; j++) {
				if(allTiles.get(i * BroughDungeon.MAP_WIDTH + j).passable) {
					batch.draw(floor, i * SIZE, j * SIZE, 32, 32);
				} else {
					batch.draw(wall, i * SIZE, j * SIZE, 32, 32);
				}
			}
		}

		// rendering the hero
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
