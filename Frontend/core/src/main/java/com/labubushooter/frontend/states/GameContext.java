package com.labubushooter.frontend.states;

/**
 * Context class that manages game state transitions.
 * Implements State Pattern to eliminate switch statements and enable OCP
 * compliance.
 */
public class GameContext {
    private IGameState currentState;

    /**
     * Transitions to a new state.
     * Calls exit() on the current state and enter() on the new state.
     * 
     * @param newState The state to transition to
     */
    public void setState(IGameState newState) {
        if (currentState != null) {
            currentState.exit();
        }
        currentState = newState;
        if (currentState != null) {
            currentState.enter();
        }
    }

    /**
     * Updates the current state.
     * 
     * @param delta Time since last frame in seconds
     */
    public void update(float delta) {
        if (currentState != null) {
            currentState.update(delta);
        }
    }

    /**
     * Renders the current state.
     */
    public void render() {
        if (currentState != null) {
            currentState.render();
        }
    }

    /**
     * Handles input for the current state.
     */
    public void handleInput() {
        if (currentState != null) {
            currentState.handleInput();
        }
    }

    /**
     * Gets the current state.
     * 
     * @return Current state instance
     */
    public IGameState getCurrentState() {
        return currentState;
    }
}
