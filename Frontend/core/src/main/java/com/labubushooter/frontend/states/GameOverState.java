package com.labubushooter.frontend.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.labubushooter.frontend.Main;

public class GameOverState implements GameStateHandler {
    private Main game;

    public GameOverState(Main game) {
        this.game = game;
    }

    @Override
    public void enter() {
        Gdx.app.log("GameState", "Entered GAME_OVER state");
    }

    @Override
    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.restartGame();
        }
    }

    @Override
    public void update(float delta) {
        // No update needed
    }

    @Override
    public void render(SpriteBatch batch) {
        game.renderGameOver(batch);
    }

    @Override
    public void exit() {
        Gdx.app.log("GameState", "Exited GAME_OVER state");
    }
}
