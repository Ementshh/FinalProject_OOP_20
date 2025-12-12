package com.labubushooter.frontend.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.labubushooter.frontend.Main;

/**
 * Victory state - handles victory screen.
 */
public class VictoryState implements IGameState {
    private final Main main;
    private final GameContext context;

    public VictoryState(Main main, GameContext context) {
        this.main = main;
        this.context = context;
    }

    @Override
    public void enter() {
        Gdx.app.log("GameState", "Entering Victory State");
    }

    @Override
    public void update(float delta) {
        // No updates in victory state
    }

    @Override
    public void render() {
        main.renderVictory();
    }

    @Override
    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            main.restartToUsernameInput();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override
    public void exit() {
        Gdx.app.log("GameState", "Exiting Victory State");
    }
}
