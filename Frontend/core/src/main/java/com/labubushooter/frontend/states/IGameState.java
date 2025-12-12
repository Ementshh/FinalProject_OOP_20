package com.labubushooter.frontend.states;

/**
 * State Pattern interface for game states.
 * This interface enables extensibility without modifying existing code (OCP).
 * Each state encapsulates its own behavior for update, render, and input
 * handling.
 */
public interface IGameState {
    /**
     * Called when entering this state.
     * Use for initialization and setup.
     */
    void enter();

    /**
     * Update game logic for this state.
     * 
     * @param delta Time since last frame in seconds
     */
    void update(float delta);

    /**
     * Render graphics for this state.
     */
    void render();

    /**
     * Handle user input for this state.
     */
    void handleInput();

    /**
     * Called when exiting this state.
     * Use for cleanup.
     */
    void exit();
}
