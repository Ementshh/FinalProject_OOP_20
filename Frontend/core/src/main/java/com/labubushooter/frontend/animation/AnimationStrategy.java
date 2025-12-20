package com.labubushooter.frontend.animation;

import com.badlogic.gdx.graphics.Texture;

/**
 * Strategy interface for animation behaviors.
 * 
 * Design Pattern: Strategy Pattern
 * - Allows different animation types to be implemented
 * - Enables runtime switching of animation behaviors
 * 
 * SOLID Principles:
 * - Open/Closed: New animation types can be added without modifying existing code
 * - Dependency Inversion: Classes depend on this abstraction, not concrete implementations
 */
public interface AnimationStrategy {
    
    /**
     * Updates the animation state based on elapsed time.
     * 
     * @param delta Time elapsed since last frame in seconds
     */
    void update(float delta);
    
    /**
     * Gets the current frame texture to be rendered.
     * 
     * @return The current texture frame
     */
    Texture getCurrentFrame();
    
    /**
     * Checks if the animation is facing left.
     * 
     * @return true if facing left, false if facing right
     */
    boolean isFacingLeft();
    
    /**
     * Sets the facing direction of the animation.
     * 
     * @param facingLeft true to face left, false to face right
     */
    void setFacingLeft(boolean facingLeft);
    
    /**
     * Sets whether the entity is grounded (on the ground).
     * Used to control animation behavior (e.g., pause walking animation when airborne).
     * 
     * @param grounded true if grounded, false if airborne
     */
    void setGrounded(boolean grounded);
    
    /**
     * Resets the animation to its initial state.
     */
    void reset();
}

