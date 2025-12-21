package com.labubushooter.frontend.animation.states;

import com.badlogic.gdx.graphics.Texture;
import com.labubushooter.frontend.animation.PlayerAnimationState;

/**
 * In-air animation state - displays crouch frame when player is airborne.
 * Shows the player in a tucked/crouched position during jumps.
 * 
 * Design Pattern: State Pattern
 * - Implements PlayerAnimationState for airborne behavior
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles in-air animation
 * - Open/Closed: Can be extended without modifying other states
 */
public class PlayerInAirState implements PlayerAnimationState {
    
    private final Texture crouchTexture;
    
    /**
     * Creates a new in-air animation state.
     * 
     * @param crouchTexture The crouch texture to display when airborne (player_crouch)
     */
    public PlayerInAirState(Texture crouchTexture) {
        if (crouchTexture == null) {
            throw new IllegalArgumentException("Crouch texture cannot be null");
        }
        this.crouchTexture = crouchTexture;
    }
    
    @Override
    public Texture getCurrentFrame() {
        return crouchTexture;
    }
    
    @Override
    public void update(float delta) {
        // No animation update needed for static air state
    }
    
    @Override
    public boolean shouldAnimate() {
        return false; // Static state
    }
    
    @Override
    public void reset() {
        // Nothing to reset for static state
    }
    
    @Override
    public boolean isComplete() {
        return false; // In-air never auto-completes
    }
}
