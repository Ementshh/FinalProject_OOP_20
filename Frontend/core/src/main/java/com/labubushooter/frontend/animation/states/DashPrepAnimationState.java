package com.labubushooter.frontend.animation.states;

import com.badlogic.gdx.graphics.Texture;
import com.labubushooter.frontend.animation.MiniBossAnimationState;

/**
 * Dash preparation animation state (static).
 * Displays the dash preparation texture.
 * 
 * Design Pattern: State Pattern
 * - Implements MiniBossAnimationState for dash prep behavior
 * - Static state (no animation)
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles dash prep display
 * - Open/Closed: Can be extended without modifying other states
 */
public class DashPrepAnimationState implements MiniBossAnimationState {
    
    private final Texture texture;
    
    /**
     * Creates a new dash prep animation state.
     * 
     * @param texture The dash prep texture
     */
    public DashPrepAnimationState(Texture texture) {
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

