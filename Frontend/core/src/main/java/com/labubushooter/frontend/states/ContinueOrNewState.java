package com.labubushooter.frontend.states;

import com.badlogic.gdx.Gdx;
import com.labubushooter.frontend.Main;

/**
 * Continue or New state - allows player to continue or start new game.
 */
public class ContinueOrNewState implements IGameState {
    private final Main main;
    private final GameContext context;

    public ContinueOrNewState(Main main, GameContext context) {
        this.main = main;
        this.context = context;
    }

    @Override
    public void enter() {
        Gdx.app.log("GameState", "Entering Continue or New State");
    }

    @Override
    public void update(float delta) {
        // No updates
    }

    @Override
    public void render() {
        main.renderContinueOrNew();
    }

    @Override
    public void handleInput() {
        main.handleContinueOrNew();
    }

    @Override
    public void exit() {
        Gdx.app.log("GameState", "Exiting Continue or New State");
    }
}
