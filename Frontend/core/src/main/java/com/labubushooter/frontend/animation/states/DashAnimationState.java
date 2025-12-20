package com.labubushooter.frontend.animation.states;

import com.badlogic.gdx.graphics.Texture;
import com.labubushooter.frontend.animation.MiniBossAnimationState;

/**
 * Dash animation state (static).
 * Displays the dash texture.
 * 
 * Design Pattern: State Pattern
 * - Implements MiniBossAnimationState for dash behavior
 * - Static state (no animation)
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles dash display
 * - Open/Closed: Can be extended without modifying other states
 */
public class DashAnimationState implements MiniBossAnimationState {
    
    private final Texture texture;
    
    /**
     * Creates a new dash animation state.
     * 
     * @param texture The dash texture
     */
    public DashAnimationState(Texture texture) {
        if (texture == null) {
            throw new IllegalArgumentException("Texture cannot be null");
        }
        this.texture = texture;
    }
    
    @Override
    public Texture getCurrentFrame() {
        return texture;
    }
    
    @Override
    public void update(float delta) {
        // Static state, no update needed
    }
    
    @Override
    public boolean shouldAnimate() {
        return false;
    }
    
    @Override
    public void reset() {
        // Static state, no reset needed
    }
}

