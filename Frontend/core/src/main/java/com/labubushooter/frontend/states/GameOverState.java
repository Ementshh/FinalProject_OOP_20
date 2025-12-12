package com.labubushooter.frontend.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.labubushooter.frontend.Main;

/**
 * Game Over state - handles game over screen logic.
 * This state is entered when the player dies.
 */
public class GameOverState implements IGameState {
    private final Main main;
    private final GameContext context;

    public GameOverState(Main main, GameContext context) {
        this.main = main;
        this.context = context;
    }

    @Override
    public void enter() {
        Gdx.app.log("GameState", "Entering Game Over State");
    }

    @Override
    public void update(float delta) {
        // No updates in game over state
    }

    @Override
    public void render() {
        main.renderGameOver();
    }

    @Override
    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            main.restartToUsernameInput();
        }
    }

    @Override
    public void exit() {
        Gdx.app.log("GameState", "Exiting Game Over State");
    }
}
