package com.labubushooter.frontend.states;

import com.badlogic.gdx.Gdx;
import com.labubushooter.frontend.Main;

/**
 * Username Input state - handles username input screen.
 * This is the initial state when the game starts.
 */
public class UsernameInputState implements IGameState {
    private final Main main;
    private final GameContext context;

    public UsernameInputState(Main main, GameContext context) {
        this.main = main;
        this.context = context;
    }

    @Override
    public void enter() {
        Gdx.app.log("GameState", "Entering Username Input State");
    }

    @Override
    public void update(float delta) {
        // No updates in username input state
    }

    @Override
    public void render() {
        main.renderUsernameInput();
    }

    @Override
    public void handleInput() {
        main.handleUsernameInput();
    }

    @Override
    public void exit() {
        Gdx.app.log("GameState", "Exiting Username Input State");
    }
}
