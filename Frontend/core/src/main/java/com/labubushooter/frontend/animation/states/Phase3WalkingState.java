package com.labubushooter.frontend.animation.states;

import com.badlogic.gdx.graphics.Texture;
import com.labubushooter.frontend.animation.BossAnimationState;

/**
 * Phase 3 walking animation state for Final Boss.
 * Alternates between two walking frames to create walking animation.
 * 
 * Design Pattern: State Pattern
 * - Implements BossAnimationState for Phase 3 walking behavior
 * - Handles frame switching logic internally
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles Phase 3 walking animation logic
 * - Open/Closed: Can be extended without modifying other states
 * 
 * Requirements: 3.1, 4.1, 4.2, 6.5
 */
public class Phase3WalkingState implements BossAnimationState {
    
    private final Texture frame1;
    private final Texture frame2;
    private float animationTimer;
    private boolean isFrame1;
    
    // Animation speed: frame duration in seconds
    private static final float FRAME_DURATION = 0.2f;
    
    /**
     * Creates a new Phase 3 walking animation state with two frames.
     * 
     * @param frame1 First walking frame texture (boss_phase3_walk1.png)
     * @param frame2 Second walking frame texture (boss_phase3_walk2.png)
     */
    public Phase3WalkingState(Texture frame1, Texture frame2) {
        if (frame1 == null || frame2 == null) {
            throw new IllegalArgumentException("Frame textures cannot be null");
        }
        this.frame1 = frame1;
        this.frame2 = frame2;
        this.animationTimer = 0f;
        this.isFrame1 = true;
    }
    
    @Override
    public Texture getCurrentFrame() {
        return isFrame1 ? frame1 : frame2;
    }
    
    @Override
    public void update(float delta) {
        animationTimer += delta;
        
        // Switch frame when duration exceeded
        if (animationTimer >= FRAME_DURATION) {
            isFrame1 = !isFrame1;
            animationTimer = 0f;
        }
    }
    
    @Override
    public boolean shouldAnimate() {
        return true;
    }
    
    @Override
    public void reset() {
        animationTimer = 0f;
        isFrame1 = true;
    }
}
