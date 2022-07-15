package com.broughgdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

public class broughGDX extends ApplicationAdapter {
	static int SIZE = 32;

	SpriteBatch batch;
	Texture allHeroes;
	TextureRegion mainHero;
	Vector2 mainHeroPosition;
	BroughInputProcessor myInputProcessor;
	
	@Override
	public void create () {
		myInputProcessor = new BroughInputProcessor();
		Gdx.input.setInputProcessor(myInputProcessor);

		batch = new SpriteBatch();
		allHeroes = new Texture("lofi_char.png");

		mainHero = new TextureRegion(allHeroes, 0, 0, 8, 8);
		mainHeroPosition = new Vector2(320, 320);
	}

	@Override
	public void render () {
		// Updating things... Isn't there a Render function?
		// Handling Hero Movement
		if(myInputProcessor.Left()) {
			mainHeroPosition.x -= SIZE;
		} else if(myInputProcessor.Right()) {
			mainHeroPosition.x += SIZE;
		} else if(myInputProcessor.Up()) {
			mainHeroPosition.y += SIZE;
		} else if(myInputProcessor.Down()) {
			mainHeroPosition.y -= SIZE;
		}

		// actually drawing
		ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
		batch.begin();
		batch.draw(mainHero, mainHeroPosition.x, mainHeroPosition.y, 32, 32);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		allHeroes.dispose();
	}
}
