package com.labubushooter.frontend.animation.states;

import com.badlogic.gdx.graphics.Texture;
import com.labubushooter.frontend.animation.BossAnimationState;

/**
 * Phase 1 big attack preparation animation state for Final Boss (static).
 * Displays a single texture when boss is preparing for big attack.
 * Replaces the yellow flashing box warning.
 * 
 * Design Pattern: State Pattern
 * - Implements BossAnimationState for Phase 1 big attack preparation behavior
 * - Static state (no animation)
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles Phase 1 big attack preparation display
 * - Open/Closed: Can be extended without modifying other states
 */
public class Phase1BigAttackState implements BossAnimationState {
    
    private final Texture bigAttackTexture;
    
    /**
     * Creates a new Phase 1 big attack preparation animation state.
     * 
     * @param bigAttackTexture The big attack preparation texture (boss_phase1_bigattack.png)
     */
    public Phase1BigAttackState(Texture bigAttackTexture) {
        if (bigAttackTexture == null) {
            throw new IllegalArgumentException("Big attack texture cannot be null");
        }
        this.bigAttackTexture = bigAttackTexture;
    }
    
    @Override
    public Texture getCurrentFrame() {
        return bigAttackTexture;
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
