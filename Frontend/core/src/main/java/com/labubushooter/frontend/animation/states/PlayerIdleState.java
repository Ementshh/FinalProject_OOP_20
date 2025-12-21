package com.labubushooter.frontend.animation.states;

import com.badlogic.gdx.graphics.Texture;
import com.labubushooter.frontend.animation.PlayerAnimationState;

/**
 * Idle animation state - displays a single static frame.
 * Used when player is standing still on the ground.
 * 
 * Design Pattern: State Pattern
 * - Implements PlayerAnimationState for idle behavior
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles idle animation (static frame)
 * - Open/Closed: Can be extended without modifying other states
 */
public class PlayerIdleState implements PlayerAnimationState {
    
    private final Texture idleTexture;
    
    /**
     * Creates a new idle animation state.
     * 
     * @param idleTexture The texture to display when idle
     */
    public PlayerIdleState(Texture idleTexture) {
        if (idleTexture == null) {
            throw new IllegalArgumentException("Idle texture cannot be null");
        }
        this.idleTexture = idleTexture;
    }
    
    @Override
    public Texture getCurrentFrame() {
        return idleTexture;
    }
    
    @Override
    public void update(float delta) {
        // No animation update needed for static idle state
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
        return false; // Idle never auto-completes
    }
}
