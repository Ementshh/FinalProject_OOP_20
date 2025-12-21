package com.labubushooter.frontend.animation.states;

import com.badlogic.gdx.graphics.Texture;
import com.labubushooter.frontend.animation.BossAnimationState;

/**
 * Phase 3 jumping animation state (static).
 * Displays the Phase 3 jumping texture.
 * 
 * Design Pattern: State Pattern
 * - Implements BossAnimationState for Phase 3 jumping behavior
 * - Static state (no animation)
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles Phase 3 jumping display
 * - Open/Closed: Can be extended without modifying other states
 * 
 * Requirements: 3.2, 4.1, 4.2
 */
public class Phase3JumpingState implements BossAnimationState {
    
    private final Texture jumpTexture;
    
    /**
     * Creates a new Phase 3 jumping animation state.
     * 
     * @param jumpTexture The jumping texture (boss_phase3_jump.png)
     */
    public Phase3JumpingState(Texture jumpTexture) {
        if (jumpTexture == null) {
            throw new IllegalArgumentException("Jump texture cannot be null");
        }
        this.jumpTexture = jumpTexture;
    }
    
    @Override
    public Texture getCurrentFrame() {
        return jumpTexture;
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
