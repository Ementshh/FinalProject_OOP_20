package com.labubushooter.frontend.animation.states;

import com.badlogic.gdx.graphics.Texture;
import com.labubushooter.frontend.animation.MiniBossAnimationState;

/**
 * In-air animation state (static).
 * Displays the default in-air texture (walk_frame2).
 * 
 * Design Pattern: State Pattern
 * - Implements MiniBossAnimationState for in-air behavior
 * - Static state (no animation)
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles in-air display
 * - Open/Closed: Can be extended without modifying other states
 */
public class InAirAnimationState implements MiniBossAnimationState {
    
    private final Texture texture;
    
    /**
     * Creates a new in-air animation state.
     * 
     * @param texture The in-air texture (default: walk_frame2)
     */
    public InAirAnimationState(Texture texture) {
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

