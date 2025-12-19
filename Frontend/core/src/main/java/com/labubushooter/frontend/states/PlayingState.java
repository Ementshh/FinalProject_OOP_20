package com.labubushooter.frontend.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.labubushooter.frontend.Main;

public class PlayingState implements GameStateHandler {
    private Main game;

    public PlayingState(Main game) {
        this.game = game;
    }

    @Override
    public void enter() {
        Gdx.app.log("GameState", "Entered PLAYING state");
    }

    @Override
    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.pauseGame();
        }
    }

    @Override
    public void update(float delta) {
        game.updateGameplay(delta);
    }

    @Override
    public void render(SpriteBatch batch) {
        game.renderGameplay(batch);
    }

    @Override
    public void exit() {
        Gdx.app.log("GameState", "Exited PLAYING state");
    }
}