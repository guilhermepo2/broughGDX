package com.broughgdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
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
	private int MonsterTotal = 5;
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
	Array<BroughMonster> monstersOnScene;

	BitmapFont debugFont;
	
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

		monstersOnScene = new Array<BroughMonster>();

		debugFont = new BitmapFont(Gdx.files.internal("aria-8l.fnt"), false);

		Vector2 mainHeroPosition = new Vector2();
		BroughTile startingTile = theDungeon.RandomPassableTile();
		mainHeroPosition.x = startingTile.x * SIZE;
		mainHeroPosition.y = startingTile.y * SIZE;

		theHero = new BroughMonster(mainHero, mainHeroPosition, 3);
		TryMove(theHero, 0, 0);

		SpawnRandomMonsterAtRandomPosition();
	}

	private boolean TryMove(BroughMonster actor, int dx, int dy) {

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

		// Gdx.app.log("debug", "`new position`" + actor.Position());
		return didMove;
	}

	private void RenderDebug() {
		debugFont.setColor(Color.RED);
		Array<BroughTile> allTiles = theDungeon.GetTiles();

		for(int i = 0; i < allTiles.size; i++) {
			if(allTiles.get(i).monster != null) {
				// debugFont.draw(batch, "m", 8 + (allTiles.get(i).x * SIZE), SIZE + (allTiles.get(i).y * SIZE) );
			}

			// drawing passable/unpassable tiles
			if(allTiles.get(i).passable) {
				// debugFont.draw(batch, "0", 8 + (allTiles.get(i).x * SIZE), SIZE + (allTiles.get(i).y * SIZE) );
			} else {
				// debugFont.draw(batch, "1", 8 + (allTiles.get(i).x * SIZE), SIZE + (allTiles.get(i).y * SIZE) );
			}
		}
	}

	@Override
	public void render () {
		boolean playerMoved = false;
		if(myInputProcessor.Left()) {
			playerMoved = TryMove(theHero, -SIZE, 0);
		} else if(myInputProcessor.Right()) {
			playerMoved = TryMove(theHero, SIZE, 0);
		} else if(myInputProcessor.Up()) {
			playerMoved = TryMove(theHero, 0, SIZE);
		} else if(myInputProcessor.Down()) {
			playerMoved = TryMove(theHero, 0, -SIZE);
		}

		// if the player moved, then we have to move all the enemies as well!
		if(playerMoved) {
			for(int i = 0; i < monstersOnScene.size; i++) {
				boolean willMoveVertically = MathUtils.randomBoolean();
				boolean willMovePositive = MathUtils.randomBoolean();

				int dx = willMoveVertically ? 0 : SIZE;
				int dy = willMoveVertically ? SIZE : 0;

				if(!willMovePositive) {
					dx = -dx;
					dy = -dy;
				}

				TryMove(monstersOnScene.get(i), dx, dy);
			}
		}

		// actually drawing
		ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
		batch.begin();

		// rendering the map
		for(int i = 0; i < BroughDungeon.MAP_WIDTH; i++) {
			for(int j = 0; j < BroughDungeon.MAP_HEIGHT; j++) {

				BroughTile tile = theDungeon.GetTile(i, j);
				if(theDungeon.GetTile(i, j).passable) {
					batch.draw(floor, tile.x * SIZE, tile.y * SIZE, 32, 32);
				} else {
					batch.draw(wall, tile.x * SIZE, tile.y * SIZE, 32, 32);
				}
			}
		}

		// rendering the hero
		Vector2 mainHeroPosition = theHero.Position();
		batch.draw(mainHero, mainHeroPosition.x, mainHeroPosition.y, 32, 32);

		// rendering all monsters
		for(int i = 0; i < monstersOnScene.size; i++) {
			BroughMonster monster = monstersOnScene.get(i);
			batch.draw(monster.Texture(), monster.Position().x, monster.Position().y, 32, 32);
		}

		// some debug
		RenderDebug();

		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		allHeroes.dispose();
	}

	// -----------------------------------------------------------------------------------------------
	// Monster Factories
	// -----------------------------------------------------------------------------------------------
	public void SpawnRandomMonsterAtRandomPosition() {
		BroughTile monsterTile = theDungeon.RandomPassableTile();
		BroughMonster themMonster = CreateRandomMonster(new Vector2(monsterTile.x * SIZE, monsterTile.y * SIZE));
		monstersOnScene.add(themMonster);
		TryMove(themMonster, 0, 0);
	}

	// todo: I don't really like this, the best way is to have a "template" mosnter for each monster
	// todo: and then I can have a "monster bag" and just return a copy from a random one in the bag
	// todo: This will work for now though.
	public BroughMonster CreateRandomMonster(Vector2 position) {
		int randomMonster = MathUtils.random(MonsterTotal);

		switch(randomMonster) {
			case 0:
				return CreateBird(position);
			case 1:
				return CreateBlob(position);
			case 2:
				return CreateEater(position);
			case 3:
				return CreateJester(position);
			case 4:
				return CreateSnake(position);
		}

		return CreateBird(position);
	}

	public BroughMonster CreateBird(Vector2 position) {
		BroughMonster newBird = new BroughMonster(monsterBird, position, 1);
		return newBird;
	}

	public BroughMonster CreateSnake(Vector2 position) {
		BroughMonster newSnake = new BroughMonster(monsterSnake, position, 1);
		return newSnake;
	}

	public BroughMonster CreateBlob(Vector2 position) {
		BroughMonster newBlob = new BroughMonster(monsterBlob, position, 1);
		return newBlob;
	}

	public BroughMonster CreateJester(Vector2 position) {
		BroughMonster newJester = new BroughMonster(monsterJester, position, 1);
		return newJester;
	}

	public BroughMonster CreateEater(Vector2 position) {
		BroughMonster newEater = new BroughMonster(monsterEater, position, 1);
		return newEater;
	}
}
