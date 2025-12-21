package com.labubushooter.frontend.animation;

import com.badlogic.gdx.graphics.Texture;

/**
 * Interface for Final Boss animation states.
 * 
 * Design Pattern: State Pattern
 * - Each state represents a different animation behavior for the Final Boss
 * - States can be switched at runtime based on phase and game conditions
 * - Supports three phases with walking, jumping, and big attack states
 * 
 * SOLID Principles:
 * - Single Responsibility: Each state handles one animation behavior
 * - Open/Closed: New states can be added without modifying existing code
 * - Interface Segregation: Minimal interface with only essential methods
 * - Dependency Inversion: FinalBossAnimationStrategy depends on this abstraction
 * 
 * Usage:
 * - Walking states: Animate between two frames
 * - Jumping states: Static single frame
 * - Big attack states: Static single frame for attack preparation
 */
public interface BossAnimationState {
    
    /**
     * Gets the current texture frame for this state.
     * 
     * @return The current texture to render
     */
    Texture getCurrentFrame();
    
    /**
     * Updates the animation state based on elapsed time.
     * Only called if shouldAnimate() returns true.
     * 
     * @param delta Time elapsed since last frame in seconds
     */
    void update(float delta);
    
    /**
     * Checks if this state should animate (e.g., walking animation).
     * Static states (jumping, big attack) return false.
     * 
     * @return true if this state animates, false otherwise
     */
    boolean shouldAnimate();
    
    /**
     * Resets the state to its initial condition.
     * Called when transitioning between states or phases.
     */
    void reset();
}
