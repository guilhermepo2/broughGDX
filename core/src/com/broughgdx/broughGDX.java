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
	// monsters
	TextureRegion monsterBird;
	TextureRegion monsterSnake;
	TextureRegion monsterBlob;
	TextureRegion monsterEater;
	TextureRegion monsterJester;
	TextureRegion wall;
	TextureRegion floor;
	BroughInputProcessor myInputProcessor;

	BroughDungeon theDungeon;

	BroughMonster theHero;


	
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

		monsterBird = new TextureRegion(allHeroes, 32, 104, 8, 8);
		monsterSnake = new TextureRegion(allHeroes, 32, 96, 8, 8);
		monsterBlob = new TextureRegion(allHeroes, 16, 88, 8, 8);
		monsterEater = new TextureRegion(allHeroes, 80, 96, 8, 8);
		monsterJester = new TextureRegion(allHeroes, 120, 88, 8, 8);

		theDungeon = new BroughDungeon();
		theDungeon.GenerateLevel();

		Vector2 mainHeroPosition = new Vector2();
		BroughTile startingTile = theDungeon.RandomPassableTile();
		mainHeroPosition.x = startingTile.x * SIZE;
		mainHeroPosition.y = startingTile.y * SIZE;

		theHero = new BroughMonster(mainHero, mainHeroPosition, 3);
	}

	private void TryMove(BroughMonster actor, int dx, int dy) {

		Vector2 desiredPosition = actor.Position();
		int oldX = (int)(desiredPosition.x / SIZE);
		int oldY = (int)(desiredPosition.y / SIZE);
		BroughTile oldTile = theDungeon.GetTile(oldX, oldY);

		int actualX = (int)( (desiredPosition.x + dx) / SIZE);
		int actualY = (int)( (desiredPosition.y + dy) / SIZE);

		BroughTile desiredTile = theDungeon.GetTile(actualX, actualY);

		boolean didMove = false;
		if(desiredTile.passable) {
			if(desiredTile.monster == null) {
				actor.Move(dx, dy);
				didMove = true;
			} else {
				// todo: combat??
				Gdx.app.log("debug", "combat?");
			}
		}

		if(didMove) {
			oldTile.monster = null;
			desiredTile.monster = actor;
		}

		Gdx.app.log("debug", "new position" + actor.Position());
	}

	@Override
	public void render () {
		if(myInputProcessor.Left()) {
			TryMove(theHero, -SIZE, 0);
		} else if(myInputProcessor.Right()) {
			TryMove(theHero, SIZE, 0);
		} else if(myInputProcessor.Up()) {
			TryMove(theHero, 0, SIZE);
		} else if(myInputProcessor.Down()) {
			TryMove(theHero, 0, -SIZE);
		}

		// actually drawing
		ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
		batch.begin();

		// rendering the map
		for(int i = 0; i < BroughDungeon.MAP_WIDTH; i++) {
			for(int j = 0; j < BroughDungeon.MAP_HEIGHT; j++) {

				if(theDungeon.GetTile(i, j).passable) {
					batch.draw(floor, i * SIZE, j * SIZE, 32, 32);
				} else {
					batch.draw(wall, i * SIZE, j * SIZE, 32, 32);
				}
			}
		}

		// rendering the hero
		Vector2 mainHeroPosition = theHero.Position();
		batch.draw(mainHero, mainHeroPosition.x, mainHeroPosition.y, 32, 32);

		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		allHeroes.dispose();
	}
}
