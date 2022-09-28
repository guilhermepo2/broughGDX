package com.broughgdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

	TextureRegion monsterSpawnPortal;
	TextureRegion pickupTexture;

	BroughInputProcessor myInputProcessor;
	BroughDungeon theDungeon;
	BroughMonster theHero;
	private int m_playerScore = 0;
	Array<BroughMonster> monstersOnScene;

	// ----------------------------------------
	// Spawning Monsters as the game goes on
	private int m_spawnRate = 15;
	private int m_spawnCounter = m_spawnRate;

	BitmapFont debugFont;
	BitmapFont kenneyMiniSquareMono;

	// ------------------------------------------
	// Sounds
	Sound playerAttack;
	Sound monsterAttack;
	Sound gotTreasure;
	Sound moveSound;
	Sound newMonster;
	
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
		monsterSpawnPortal = new TextureRegion(objectTexture, 112, 32, 8, 8);
		pickupTexture = new TextureRegion(objectTexture, 56, 0, 8, 8);

		monsterBird = new TextureRegion(allHeroes, 32, 104, 8, 8);
		monsterSnake = new TextureRegion(allHeroes, 32, 96, 8, 8);
		monsterBlob = new TextureRegion(allHeroes, 16, 88, 8, 8);
		monsterEater = new TextureRegion(allHeroes, 80, 96, 8, 8);
		monsterJester = new TextureRegion(allHeroes, 120, 88, 8, 8);

		theDungeon = new BroughDungeon();
		theDungeon.GenerateLevel();

		monstersOnScene = new Array<BroughMonster>();

		debugFont = new BitmapFont(Gdx.files.internal("aria-8l.fnt"), false);
		kenneyMiniSquareMono = new BitmapFont(Gdx.files.internal("kenney_mini_square_mono-24.fnt"), false);

		// loading sounds
		playerAttack = Gdx.audio.newSound(Gdx.files.internal("sounds/hurt1.wav"));
		monsterAttack = Gdx.audio.newSound(Gdx.files.internal("sounds/hurt2.wav"));
		gotTreasure = Gdx.audio.newSound(Gdx.files.internal("sounds/treasure.wav"));
		moveSound = Gdx.audio.newSound(Gdx.files.internal("sounds/move.wav"));
		newMonster = Gdx.audio.newSound(Gdx.files.internal("sounds/portal1.wav"));

		Vector2 mainHeroPosition = new Vector2();
		BroughTile startingTile = theDungeon.RandomPassableTile();
		mainHeroPosition.x = startingTile.x * SIZE;
		mainHeroPosition.y = startingTile.y * SIZE;

		theHero = new BroughMonster(mainHero, mainHeroPosition, 3, true);
		TryMove(theHero, 0, 0);

		SpawnRandomMonsterAtRandomPosition();

		for(int i = 0; i < 2; i++) {
			theDungeon.RandomPassableTile().hasTreasure = true;
		}
	}

	private boolean ResolveCombat(BroughMonster attacker, BroughMonster defending) {

		// players can't attack players ( if we ever have more than 1)
		// monsters can't attack monsters, duh.
		if(attacker.IsPlayer() != defending.IsPlayer()) {
			defending.DealDamage(1); // ... that's it?

			if(attacker.IsPlayer()) {
				playerAttack.play(1.0f);
				defending.Stun(true);
			} else {
				monsterAttack.play(1.0f);
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
		boolean didCombat = false;
		if(desiredTile.passable) {
			if(desiredTile.monster == null) {
				actor.Move(dx, dy);
				didMove = true;
			} else {
				ResolveCombat(actor, desiredTile.monster);
				actor.Attack(dx, dy);
				didCombat = true;
			}
		}

		if(didMove) {
			oldTile.monster = null;
			desiredTile.monster = actor;

			if(actor.IsPlayer()) {
				moveSound.play(1.0f);
			}

			// checking for treasures
			if(actor.IsPlayer() && desiredTile.hasTreasure) {
				gotTreasure.play(1.0f);
				desiredTile.hasTreasure = false;
				m_playerScore += 1;
				Gdx.app.log("debug", "player score: " + m_playerScore);
			}
		}

		// Gdx.app.log("debug", "`new position`" + actor.Position());
		return didMove || didCombat;
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

	private void Update() {
		// Updating Offsets
		float TickSpeed = 15.0f;
		theHero.TickOffset(TickSpeed * SIZE * Gdx.graphics.getDeltaTime());

		for(int i = 0; i < monstersOnScene.size; i++) {
			monstersOnScene.get(i).TickOffset(TickSpeed * SIZE * Gdx.graphics.getDeltaTime());
		}

		// Moving the Player
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

		// Moving all enemies
		if(playerMoved) {
			for(int i = 0; i < monstersOnScene.size; i++) {
				MoveAIMonster(monstersOnScene.get(i));
			}

			m_spawnCounter -= 1;
			if(m_spawnCounter < 0) {
				SpawnRandomMonsterAtRandomPosition();
				m_spawnCounter = m_spawnRate;
				m_spawnRate -= 1;
			}
		}
	}

	@Override
	public void render () {

		// the "engine" doesn't have a Update() method - so we just do our own and call it first thing on the render() message
		Update();

		// actually drawing
		ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
		batch.begin();

		// rendering the map
		for(int i = 0; i < BroughDungeon.MAP_WIDTH; i++) {
			for(int j = 0; j < BroughDungeon.MAP_HEIGHT; j++) {

				BroughTile tile = theDungeon.GetTile(i, j);
				if(theDungeon.GetTile(i, j).passable) {
					batch.draw(floor, tile.x * SIZE, tile.y * SIZE, 32, 32);

					if(tile.hasTreasure) {
						batch.draw(pickupTexture, (tile.x * SIZE) + 4, (tile.y * SIZE) + 4, 24, 24);
					}
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
			if(monster.TeleportCount() <= 0) {
				batch.draw(monster.Texture(), monster.RenderPosition().x, monster.RenderPosition().y, 32, 32);

				int monsterHP = monster.HP();
				for(int j = 0; j < monsterHP; j++) {
					batch.draw(uiHeart,
							uiHeartHorizontalOffset_start + monster.RenderPosition().x + (j%3 * uiHeartHorizontalOffset),
							uiHeartVerticalOffset_start + monster.RenderPosition().y - ((j / 3) * uiHeartVerticalOffset),
							uiHeartSize,
							uiHeartSize
					);
				}
			} else {
				batch.draw(monsterSpawnPortal, monster.RenderPosition().x, monster.RenderPosition().y, 32, 32);
			}
		}

		// rendering the hero
		Vector2 mainHeroPosition = theHero.RenderPosition();
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

		// rendering UI
		// todo: MAGIC NUMBERS!!
		int videoWidth = Gdx.graphics.getWidth();
		int videoHeight = Gdx.graphics.getHeight();
		String score = "SCORE:" + String.valueOf(m_playerScore);
		kenneyMiniSquareMono.draw(batch, score, 2 * (videoWidth / 3) + 35, videoHeight - 50);

		// Rendering Debug
		RenderDebug();

		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		allHeroes.dispose();

		debugFont.dispose();
		kenneyMiniSquareMono.dispose();

		// sounds
		playerAttack.dispose();
		monsterAttack.dispose();
		gotTreasure.dispose();
		moveSound.dispose();
		newMonster.dispose();
	}

	// -----------------------------------------------------------------------------------------------
	// Dealing with AI
	// -----------------------------------------------------------------------------------------------
	private void MoveAIMonster(BroughMonster monster) {

		if(monster.TeleportCount() > 0) {
			monster.TickTeleportCount();
			return;
		}

		int dx = 0;
		int dy = 0;
		int tileX = (int)(monster.Position().x / SIZE);
		int tileY = (int)(monster.Position().y / SIZE);
		BroughTile monsterCurrentTile = theDungeon.GetTile(tileX, tileY);


		switch(monster.Type()) {
			case EATER: // todo: should eat walls to recover HP!
			case SNAKE: // todo: should move twice!!
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
		newMonster.play(1.0f);
		BroughTile monsterTile = theDungeon.RandomPassableTile();
		BroughMonster themMonster = CreateRandomMonster(new Vector2(monsterTile.x * SIZE, monsterTile.y * SIZE));
		monstersOnScene.add(themMonster);
		TryMove(themMonster, 0, 0);
	}

	// todo: I don't really like this, the best way is to have a "template" monster for each monster
	// todo: and then I can have a "monster bag" and just return a copy from a random one in the bag
	// todo: This will work for now though.

	// todo: can't this be moved to "BroughMonster"? - Not Sure, because it needs the sprites...
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
