package com.labubushooter.frontend.animation.states;

import com.badlogic.gdx.graphics.Texture;
import com.labubushooter.frontend.animation.MiniBossAnimationState;

/**
 * Super jump preparation animation state (static).
 * Displays the crouch texture.
 * 
 * Design Pattern: State Pattern
 * - Implements MiniBossAnimationState for super jump prep behavior
 * - Static state (no animation)
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles super jump prep display
 * - Open/Closed: Can be extended without modifying other states
 */
public class SuperJumpPrepAnimationState implements MiniBossAnimationState {
    
    private final Texture texture;
    
    /**
     * Creates a new super jump prep animation state.
     * 
     * @param texture The crouch texture
     */
    public SuperJumpPrepAnimationState(Texture texture) {
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

