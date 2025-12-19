package com.labubushooter.frontend.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.labubushooter.frontend.Main;

public class PausedState implements GameStateHandler {
    private Main game;

    public PausedState(Main game) {
        this.game = game;
    }

    @Override
    public void enter() {
        Gdx.app.log("GameState", "Entered PAUSED state");
    }

    @Override
    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.resumeGame();
        }
        game.handlePauseMenuInput();
    }

    @Override
    public void update(float delta) {
        // Paused - no update needed
    }

    @Override
    public void render(SpriteBatch batch) {
        game.renderPauseMenu(batch);
    }

    @Override
    public void exit() {
        Gdx.app.log("GameState", "Exited PAUSED state");
    }
}