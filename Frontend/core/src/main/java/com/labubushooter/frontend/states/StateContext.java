package com.labubushooter.frontend.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.HashMap;
import java.util.Map;
import com.labubushooter.frontend.GameState;

public class StateContext {
    private Map<GameState, GameStateHandler> states;
    private GameStateHandler currentState;

    public StateContext() {
        states = new HashMap<>();
    }

    public void registerState(GameState stateType, GameStateHandler handler) {
        states.put(stateType, handler);
    }

    public void changeState(GameState newStateType) {
        if (currentState != null) {
            currentState.exit();
        }

        currentState = states.get(newStateType);

        if (currentState != null) {
            currentState.enter();
        }
    }

    public void handleInput() {
        if (currentState != null) {
            currentState.handleInput();
        }
    }

    public void update(float delta) {
        if (currentState != null) {
            currentState.update(delta);
        }
    }

    public void render(SpriteBatch batch) {
        if (currentState != null) {
            currentState.render(batch);
        }
    }
}