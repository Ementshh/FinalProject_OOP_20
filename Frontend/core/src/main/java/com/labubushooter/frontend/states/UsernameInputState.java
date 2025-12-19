package com.labubushooter.frontend.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.labubushooter.frontend.Main;

public class UsernameInputState implements GameStateHandler {
    private Main game;

    public UsernameInputState(Main game) {
        this.game = game;
    }

    @Override
    public void enter() {
        Gdx.app.log("GameState", "Entered USERNAME_INPUT state");
    }

    @Override
    public void handleInput() {
        game.handleUsernameInput();
    }

    @Override
    public void update(float delta) {
        // No update needed for username input
    }

    @Override
    public void render(SpriteBatch batch) {
        game.renderUsernameInput(batch);
    }

    @Override
    public void exit() {
        Gdx.app.log("GameState", "Exited USERNAME_INPUT state");
    }
}
