package com.labubushooter.frontend.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.labubushooter.frontend.Main;

/**
 * Paused state - handles pause menu logic.
 * This state is entered when the player pauses the game.
 */
public class PausedState implements IGameState {
    private final Main main;
    private final GameContext context;

    public PausedState(Main main, GameContext context) {
        this.main = main;
        this.context = context;
    }

    @Override
    public void enter() {
        Gdx.app.log("GameState", "Entering Paused State");
    }

    @Override
    public void update(float delta) {
        // No game logic updates while paused
    }

    @Override
    public void render() {
        main.renderPauseMenu();
    }

    @Override
    public void handleInput() {
        // Resume with ESC
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            context.setState(new PlayingState(main, context));
            Gdx.app.log("Game", "RESUMED");
            return;
        }

        // Delegate pause menu input handling to Main
        main.handlePauseMenu();
    }

    @Override
    public void exit() {
        Gdx.app.log("GameState", "Exiting Paused State");
    }
}
