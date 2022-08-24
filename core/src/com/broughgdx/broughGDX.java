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
	Texture objectTexture;

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
	TextureRegion uiHeart;

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
		objectTexture = new Texture("lofi_obj.png");

		mainHero = new TextureRegion(allHeroes, 0, 0, 8, 8);
		floor = new TextureRegion(environmentTexture, 32, 0, 8, 8);
		wall = new TextureRegion(environmentTexture, 112, 96, 8, 8);
		uiHeart = new TextureRegion(objectTexture, 120, 56, 8, 8);

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

		theHero = new BroughMonster(mainHero, mainHeroPosition, 3, true);
		TryMove(theHero, 0, 0);

		SpawnRandomMonsterAtRandomPosition();
	}

	private boolean ResolveCombat(BroughMonster attacker, BroughMonster defending) {

		// players can't attack players ( if we ever have more than 1)
		// monsters can't attack monsters, duh.
		if(attacker.IsPlayer() != defending.IsPlayer()) {
			defending.DealDamage(1); // ... that's it?

			if(attacker.IsPlayer()) {
				defending.Stun(true);
			}

			if(defending.HP() <= 0) {
				if(!defending.IsPlayer()) {
					Vector2 defendingTilePosition = defending.Position();
					int tileX = (int)(defendingTilePosition.x / SIZE);
					int tileY = (int)(defendingTilePosition.y / SIZE);
					BroughTile defendingTile = theDungeon.GetTile(tileX, tileY);
					defendingTile.monster = null;
					monstersOnScene.removeValue(defending, true);
				} else {
					// todo: game over
				}

			}

			return true;
		}

		return false;
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
				ResolveCombat(actor, desiredTile.monster);
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

		// Update
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
				MoveAIMonster(monstersOnScene.get(i));
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

		int uiHeartSize = 16;
		int uiHeartHorizontalOffset_start = -3;
		int uiHeartVerticalOffset_start = -10;
		int uiHeartHorizontalOffset = 12;
		int uiHeartVerticalOffset = 12;

		// rendering all monsters
		for(int i = 0; i < monstersOnScene.size; i++) {
			BroughMonster monster = monstersOnScene.get(i);
			batch.draw(monster.Texture(), monster.Position().x, monster.Position().y, 32, 32);

			int monsterHP = monster.HP();
			for(int j = 0; j < monsterHP; j++) {
				batch.draw(uiHeart,
						uiHeartHorizontalOffset_start + monster.Position().x + (j%3 * uiHeartHorizontalOffset),
						uiHeartVerticalOffset_start + monster.Position().y - ((j / 3) * uiHeartVerticalOffset),
						uiHeartSize,
						uiHeartSize
				);
			}
		}

		// rendering the hero
		Vector2 mainHeroPosition = theHero.Position();
		batch.draw(mainHero, mainHeroPosition.x, mainHeroPosition.y, 32, 32);
		int mainHeroHP = theHero.HP();
		for(int i = 0; i < mainHeroHP; i++) {
			batch.draw(uiHeart,
					uiHeartHorizontalOffset_start + mainHeroPosition.x + (i%3 * uiHeartHorizontalOffset),
					uiHeartVerticalOffset_start + mainHeroPosition.y - ((i / 3) * uiHeartVerticalOffset),
					uiHeartSize,
					uiHeartSize
			);
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
	// Dealing with AI
	// -----------------------------------------------------------------------------------------------
	private void MoveAIMonster(BroughMonster monster) {
		int dx = 0;
		int dy = 0;
		int tileX = (int)(monster.Position().x / SIZE);
		int tileY = (int)(monster.Position().y / SIZE);
		BroughTile monsterCurrentTile = theDungeon.GetTile(tileX, tileY);


		switch(monster.Type()) {
			case EATER: // todo: should eat walls to recover HP!
			case SNAKE: // #todo: should move twice!!
			case TANK:
			case BIRD:
				BroughTile moveTo = GetOneCloserToPlayer(monster);
				dx = (moveTo.x - monsterCurrentTile.x) * SIZE;
				dy = (moveTo.y - monsterCurrentTile.y) * SIZE;
				break;
			case JESTER:
				boolean willMoveVertically = MathUtils.randomBoolean();
				boolean willMovePositive = MathUtils.randomBoolean();

				dx = willMoveVertically ? 0 : SIZE;
				dy = willMoveVertically ? SIZE : 0;

				if(!willMovePositive) {
					dx = -dx;
					dy = -dy;
				}
				break;
		}

		if(!monster.Stunned()) {
			TryMove(monster, dx, dy);

			if(monster.Type() == BroughMonster.MonsterType.TANK) {
				monster.Stun(true);
			}
		} else {
			monster.Stun(false);
		}

	}

	private BroughTile GetOneCloserToPlayer(BroughMonster monster) {
		int tileX = (int)(monster.Position().x / SIZE);
		int tileY = (int)(monster.Position().y / SIZE);
		BroughTile monsterCurrentTile = theDungeon.GetTile(tileX, tileY);
		// Gdx.app.log("onecloser", "tileX/tileY: (" + tileX + ", " + tileY + ")");
		// Gdx.app.log("onecloser", "monster current: (" + monsterCurrentTile.x + ", " + monsterCurrentTile.y + ")");

		Array<BroughTile> passableNeighbours = theDungeon.GetAdjacentPassableNeighbours(monsterCurrentTile);
		if(passableNeighbours.size > 0) {
			BroughTile chosenTile = passableNeighbours.get(0);
			int distanceToHero = BroughUtils.ManhattanDistance(new Vector2(chosenTile.x * SIZE, chosenTile.y * SIZE), theHero.Position());
			// Gdx.app.log("onecloser", "monster current: (" + monsterCurrentTile.x + ", " + monsterCurrentTile.y + ")");

			for(int i = 0; i < passableNeighbours.size; i++) {
				Vector2 tilePosition = new Vector2(passableNeighbours.get(i).x * SIZE, passableNeighbours.get(i).y * SIZE);
				int distance = BroughUtils.ManhattanDistance(tilePosition, theHero.Position());

				if(distance < distanceToHero) {
					chosenTile = passableNeighbours.get(i);
					distanceToHero = distance;
				}
			}

			return chosenTile;
		}

		return null;
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
		BroughMonster newBird = new BroughMonster(monsterBird, position, 1, BroughMonster.MonsterType.BIRD);
		return newBird;
	}

	public BroughMonster CreateSnake(Vector2 position) {
		BroughMonster newSnake = new BroughMonster(monsterSnake, position, 1, BroughMonster.MonsterType.SNAKE);
		return newSnake;
	}

	public BroughMonster CreateBlob(Vector2 position) {
		BroughMonster newBlob = new BroughMonster(monsterBlob, position, 2, BroughMonster.MonsterType.TANK);
		return newBlob;
	}

	public BroughMonster CreateJester(Vector2 position) {
		BroughMonster newJester = new BroughMonster(monsterJester, position, 2, BroughMonster.MonsterType.JESTER);
		return newJester;
	}

	public BroughMonster CreateEater(Vector2 position) {
		BroughMonster newEater = new BroughMonster(monsterEater, position, 1, BroughMonster.MonsterType.EATER);
		return newEater;
	}
}
