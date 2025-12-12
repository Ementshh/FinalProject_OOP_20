package com.labubushooter.frontend.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.labubushooter.frontend.Main;

/**
 * Playing state - handles main gameplay logic.
 * This state encapsulates all game update and rendering logic when the game is
 * active.
 */
public class PlayingState implements IGameState {
    private final Main main;
    private final GameContext context;

    public PlayingState(Main main, GameContext context) {
        this.main = main;
        this.context = context;
    }

    @Override
    public void enter() {
        Gdx.app.log("GameState", "Entering Playing State");
    }

    @Override
    public void update(float delta) {
        // Check if player is dead
        if (main.player.isDead()) {
            context.setState(new GameOverState(main, context));
            Gdx.app.log("Game", "GAME OVER");
            return;
        }

        // All game update logic will be delegated to Main
        main.updateGameLogic(delta);
    }

    @Override
    public void render() {
        main.renderGame();
    }

    @Override
    public void handleInput() {
        // Check for PAUSE
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            context.setState(new PausedState(main, context));
            Gdx.app.log("Game", "PAUSED");
            return;
        }

        // All game input handling will be delegated to Main
        main.handleGameInput();
    }

    @Override
    public void exit() {
        Gdx.app.log("GameState", "Exiting Playing State");
    }
}
