package com.labubushooter.frontend.states;

import com.badlogic.gdx.Gdx;
import com.labubushooter.frontend.Main;

/**
 * Loading Player Data state - displays loading screen while fetching data.
 */
public class LoadingPlayerDataState implements IGameState {
    private final Main main;
    private final GameContext context;

    public LoadingPlayerDataState(Main main, GameContext context) {
        this.main = main;
        this.context = context;
    }

    @Override
    public void enter() {
        Gdx.app.log("GameState", "Entering Loading Player Data State");
    }

    @Override
    public void update(float delta) {
        // Waiting for backend response
    }

    @Override
    public void render() {
        main.renderLoading();
    }

    @Override
    public void handleInput() {
        // No input during loading
    }

    @Override
    public void exit() {
        Gdx.app.log("GameState", "Exiting Loading Player Data State");
    }
}
