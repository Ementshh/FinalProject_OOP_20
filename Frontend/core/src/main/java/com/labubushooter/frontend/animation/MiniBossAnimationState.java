package com.labubushooter.frontend.animation;

import com.badlogic.gdx.graphics.Texture;

/**
 * Interface for MiniBoss animation states.
 * 
 * Design Pattern: State Pattern
 * - Each state represents a different animation behavior
 * - States can be switched at runtime based on game conditions
 * 
 * SOLID Principles:
 * - Single Responsibility: Each state handles one animation behavior
 * - Open/Closed: New states can be added without modifying existing code
 * - Dependency Inversion: Strategy depends on this abstraction
 */
public interface MiniBossAnimationState {
    
    /**
     * Gets the current texture frame for this state.
     * 
     * @return The current texture to render
     */
    Texture getCurrentFrame();
    
    /**
     * Updates the animation state based on elapsed time.
     * 
     * @param delta Time elapsed since last frame in seconds
     */
    void update(float delta);
    
    /**
     * Checks if this state should animate (e.g., walking animation).
     * Static states return false.
     * 
     * @return true if this state animates, false otherwise
     */
    boolean shouldAnimate();
    
    /**
     * Resets the state to its initial condition.
     */
    void reset();
}

