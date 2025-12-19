package com.labubushooter.frontend.screens;

import com.labubushooter.frontend.GameState;

/**
 * Interface untuk semua screen dalam game.
 * Mengikuti Strategy Pattern untuk screen management.
 */
public interface Screen {
    
    /**
     * Called when this screen becomes the current screen.
     */
    void show();
    
    /**
     * Called when this screen is no longer the current screen.
     */
    void hide();
    
    /**
     * Handle user input for this screen.
     * @param delta Time since last frame
     */
    void handleInput(float delta);
    
    /**
     * Update screen logic.
     * @param delta Time since last frame
     */
    void update(float delta);
    
    /**
     * Render the screen.
     * @param delta Time since last frame
     */
    void render(float delta);
    
    /**
     * Called when the screen is resized.
     * @param width New width
     * @param height New height
     */
    void resize(int width, int height);
    
    /**
     * Called when this screen should release resources.
     */
    void dispose();
    
    /**
     * Get the next screen state (for transitions).
     * @return The next GameState, or null if no transition needed
     */
    GameState getNextState();
    
    /**
     * Reset the next state after transition.
     */
    void clearNextState();
}
