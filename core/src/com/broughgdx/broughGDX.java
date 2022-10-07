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
	private BroughGame m_gameStuff;

	int m_videoWidth;
	int m_videoHeight;

	static int SIZE = 32;
	SpriteBatch batch;
	Texture textureFile;
	TextureRegion mainHero;

	// Monsters
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
	TextureRegion exitPortal;
	BroughInputProcessor myInputProcessor;
	BroughDungeon theDungeon;
	BroughMonster theHero;
	Array<BroughMonster> monstersOnScene;

	BitmapFont debugFont;
	BitmapFont kenneyMiniSquareMono;

	// ------------------------------------------
	// Sounds
	// ------------------------------------------
	Sound playerAttack;
	Sound monsterAttack;
	Sound gotTreasure;
	Sound moveSound;
	Sound newMonster;
	Sound levelUpPortal;

	// -----------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------
	// Setup Stuff !
	// -----------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------
	@Override
	public void create () {
		m_gameStuff = new BroughGame(); // very important!! can't forget!!

		m_gameStuff.SetCurrentState(BroughGame.EGameState.LOADING);
		SetupResources();
		ReinitGame();
		m_gameStuff.SetCurrentState(BroughGame.EGameState.TITLE);
	}

	private void SetupResources() {
		m_videoWidth = Gdx.graphics.getWidth();
		m_videoHeight = Gdx.graphics.getHeight();

		Gdx.app.log("debug", "window width: " + m_videoWidth);
		Gdx.app.log("debug", "window height: " + m_videoHeight);

		myInputProcessor = new BroughInputProcessor();
		Gdx.input.setInputProcessor(myInputProcessor);

		batch = new SpriteBatch();
		textureFile = new Texture("brough.png");

		mainHero = new TextureRegion(textureFile, 0, 0, 8, 8);
		floor = new TextureRegion(textureFile, 0, 16, 8, 8);
		wall = new TextureRegion(textureFile, 8, 16, 8, 8);
		uiHeart = new TextureRegion(textureFile, 0, 24, 8, 8);
		monsterSpawnPortal = new TextureRegion(textureFile, 8, 24, 8, 8);
		pickupTexture = new TextureRegion(textureFile, 16, 24, 8, 8);

		monsterBird = new TextureRegion(textureFile, 0, 8, 8, 8);
		monsterSnake = new TextureRegion(textureFile, 8, 8, 8, 8);
		monsterBlob = new TextureRegion(textureFile, 16, 8, 8, 8);
		monsterEater = new TextureRegion(textureFile, 24, 8, 8, 8);
		monsterJester = new TextureRegion(textureFile, 32, 8, 8, 8);

		exitPortal = new TextureRegion(textureFile, 24, 24, 8, 8);

		theDungeon = new BroughDungeon();

		monstersOnScene = new Array<BroughMonster>();

		debugFont = new BitmapFont(Gdx.files.internal("aria-8l.fnt"), false);
		kenneyMiniSquareMono = new BitmapFont(Gdx.files.internal("kenney_mini_square_mono-24.fnt"), false);

		// loading sounds
		playerAttack = Gdx.audio.newSound(Gdx.files.internal("sounds/hurt1.wav"));
		monsterAttack = Gdx.audio.newSound(Gdx.files.internal("sounds/hurt2.wav"));
		gotTreasure = Gdx.audio.newSound(Gdx.files.internal("sounds/treasure.wav"));
		moveSound = Gdx.audio.newSound(Gdx.files.internal("sounds/move.wav"));
		newMonster = Gdx.audio.newSound(Gdx.files.internal("sounds/portal1.wav"));
		levelUpPortal = Gdx.audio.newSound(Gdx.files.internal("sounds/portal2.wav"));
	}

	public void ReinitGame() {
		m_gameStuff.ResetMonsterSpawning();
		m_gameStuff.ResetPlayerScore();

		CleanLevel();
		InitializeLevel();
	}

	private void CleanLevel() {
		monstersOnScene.clear();
	}

	private void InitializeLevel() {
		theDungeon.GenerateLevel();
		SetupPlayer();

		SpawnRandomMonsterAtRandomPosition();
		for(int i = 0; i < 2; i++) {
			theDungeon.RandomPassableTile().hasTreasure = true;
		}
		theDungeon.RandomPassableTile().isExit = true;
	}

	private void SetupPlayer() {
		Vector2 mainHeroPosition = new Vector2();
		BroughTile startingTile = theDungeon.RandomPassableTile();
		mainHeroPosition.x = startingTile.x * SIZE;
		mainHeroPosition.y = startingTile.y * SIZE;
		theHero = new BroughMonster(mainHero, mainHeroPosition, 3, true);
		TryMove(theHero, 0, 0);

		int playerHP = Math.min(3 + m_gameStuff.GetCurrentLevel(), 6);
		theHero.SetMaxHP(playerHP);
	}

	public void IncrementLevels() {
		m_gameStuff.IncrementLevel(1);
		CleanLevel();
		InitializeLevel();
	}

	// -----------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------
	// Update Stuff !
	// This is a bit weird because usually we would have a "Update(deltaTime)" message
	// but libGDX doesn't do that, just know that this is the first thing called on the render() function
	// as long as we are in the RUNNING state
	// -----------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------
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

			if(m_gameStuff.DecrementSpawnCounter(1)) {
				SpawnRandomMonsterAtRandomPosition();
			}
		}
	}

	// -----------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------
	// Gameplay Stuff !
	// -----------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------

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
				m_gameStuff.AddToPlayerScore(1);
				Gdx.app.log("debug", "player score: " + m_gameStuff.GetPlayerScore());
			}

			// checking for exit
			// either "restart" the game
			// or just end it if the current level is 6
			if(actor.IsPlayer() && desiredTile.isExit) {
				levelUpPortal.play(1.0f);

				if(m_gameStuff.GetCurrentLevel() >= 6) {
					m_gameStuff.SetCurrentState(BroughGame.EGameState.WON);
				} else {
					IncrementLevels();
				}
			}
		}

		return didMove || didCombat;
	}

	private boolean ResolveCombat(BroughMonster attacker, BroughMonster defending) {

		// players can't attack players ( if we ever have more than 1)
		// monsters can't attack monsters, duh.
		if(attacker.IsPlayer() != defending.IsPlayer()) {
			m_gameStuff.SetShakeAmount(SIZE / 4);
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
					m_gameStuff.SetCurrentState(BroughGame.EGameState.DEAD);
					m_gameStuff.StartTimer();
				}

			}

			return true;
		}

		return false;
	}

	// -----------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------
	// Rendering Stuff !
	// -----------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------
	@Override
	public void render () {

		if(m_gameStuff.GetCurrentState() == BroughGame.EGameState.TITLE) {
			RenderTitleScreen();
			return;
		} else if (m_gameStuff.GetCurrentState() == BroughGame.EGameState.DEAD) {
			RenderDeadScreen();
			return;
		} else if (m_gameStuff.GetCurrentState() == BroughGame.EGameState.WON) {
			RenderVictoryScreen();
			return;
		}

		// If we are here, then we are just rendering the *actual* videogame.
		Update();
		m_gameStuff.TickScreenShake();

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

					if(tile.isExit) {
						batch.draw(exitPortal, (tile.x * SIZE), (tile.y * SIZE), 32, 32);
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
				batch.draw(monster.Texture(), monster.RenderPosition().x + m_gameStuff.GetShakeX(), monster.RenderPosition().y + m_gameStuff.GetShakeY(), 32, 32);

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
		batch.draw(mainHero, mainHeroPosition.x + m_gameStuff.GetShakeX(), mainHeroPosition.y + m_gameStuff.GetShakeY(), 32, 32);
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
		String score = "SCORE:" + String.valueOf(m_gameStuff.GetPlayerScore());
		kenneyMiniSquareMono.draw(batch, score, 2 * (m_videoWidth / 3) + 35, m_videoHeight - 50);

		// Rendering Debug
		// RenderDebug();

		batch.end();
	}

	public void RenderTitleScreen() {
		ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
		batch.begin();
		kenneyMiniSquareMono.draw(batch, "brough GDX", (m_videoWidth / 2) - 80, m_videoHeight / 2);
		kenneyMiniSquareMono.draw(batch, "click anywhere to start!", (m_videoWidth / 2) - 190, (m_videoHeight / 2) - 30);

		kenneyMiniSquareMono.draw(batch, "developed by gueepo", (m_videoWidth / 2) - 150, 25);
		batch.end();

		if(Gdx.input.isTouched()) {
			m_gameStuff.SetCurrentState(BroughGame.EGameState.RUNNING);
		}
	}

	public void RenderDeadScreen() {
		ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
		batch.begin();
		kenneyMiniSquareMono.draw(batch, "YOU DIED", (m_videoWidth / 2) - 80, m_videoHeight / 2);
		kenneyMiniSquareMono.draw(batch, ":(", (m_videoWidth / 2) - 10, (m_videoHeight / 2) - 25);
		batch.end();

		m_gameStuff.TickTimer(Gdx.graphics.getDeltaTime());

		if(m_gameStuff.CanAdvanceScreen()) {
			ReinitGame();
			m_gameStuff.SetCurrentState(BroughGame.EGameState.TITLE);
		}
	}

	public void RenderVictoryScreen() {
		ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
		batch.begin();
		kenneyMiniSquareMono.draw(batch, "YOU WON!", (m_videoWidth / 2) - 80, m_videoHeight / 2);
		kenneyMiniSquareMono.draw(batch, ":)", (m_videoWidth / 2) - 10, (m_videoHeight / 2) - 25);
		batch.end();

		m_gameStuff.TickTimer(Gdx.graphics.getDeltaTime());

		if(m_gameStuff.CanAdvanceScreen()) {
			ReinitGame();
			m_gameStuff.SetCurrentState(BroughGame.EGameState.TITLE);
		}
	}

	private void RenderDebug() {
		debugFont.setColor(Color.RED);
		Array<BroughTile> allTiles = theDungeon.GetTiles();

		for(int i = 0; i < allTiles.size; i++) {
			if(allTiles.get(i).monster != null) {
				debugFont.draw(batch, "m", 8 + (allTiles.get(i).x * SIZE), SIZE + (allTiles.get(i).y * SIZE) );
			}

			// drawing passable/unpassable tiles
			if(allTiles.get(i).passable) {
				debugFont.draw(batch, "0", 8 + (allTiles.get(i).x * SIZE), SIZE + (allTiles.get(i).y * SIZE) );
			} else {
				debugFont.draw(batch, "1", 8 + (allTiles.get(i).x * SIZE), SIZE + (allTiles.get(i).y * SIZE) );
			}
		}
	}

	// -----------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------
	// Cleaning everything up
	// -----------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------
	@Override
	public void dispose () {
		batch.dispose();
		textureFile.dispose();
		debugFont.dispose();
		kenneyMiniSquareMono.dispose();
		playerAttack.dispose();
		monsterAttack.dispose();
		gotTreasure.dispose();
		moveSound.dispose();
		newMonster.dispose();
	}

	// -----------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------
	// Dealing with AI
	// -----------------------------------------------------------------------------------------------
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

		Array<BroughTile> passableNeighbours = theDungeon.GetAdjacentPassableNeighbours(monsterCurrentTile);
		if(passableNeighbours.size > 0) {
			BroughTile chosenTile = passableNeighbours.get(0);
			int distanceToHero = BroughUtils.ManhattanDistance(new Vector2(chosenTile.x * SIZE, chosenTile.y * SIZE), theHero.Position());

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
	// -----------------------------------------------------------------------------------------------
	// Monster Factories
	// -----------------------------------------------------------------------------------------------
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
