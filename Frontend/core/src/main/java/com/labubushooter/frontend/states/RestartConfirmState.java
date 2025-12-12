package com.labubushooter.frontend.states;

import com.badlogic.gdx.Gdx;
import com.labubushooter.frontend.Main;

/**
 * Restart Confirm state - confirms restart action.
 */
public class RestartConfirmState implements IGameState {
    private final Main main;
    private final GameContext context;

    public RestartConfirmState(Main main, GameContext context) {
        this.main = main;
        this.context = context;
    }

    @Override
    public void enter() {
        Gdx.app.log("GameState", "Entering Restart Confirm State");
    }

    @Override
    public void update(float delta) {
        // No updates
    }

    @Override
    public void render() {
        main.renderRestartConfirm();
    }

    @Override
    public void handleInput() {
        main.handleRestartConfirm();
    }

    @Override
    public void exit() {
        Gdx.app.log("GameState", "Exiting Restart Confirm State");
    }
}
