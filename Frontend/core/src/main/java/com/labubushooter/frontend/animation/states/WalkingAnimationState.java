package com.labubushooter.frontend.animation.states;

import com.badlogic.gdx.graphics.Texture;
import com.labubushooter.frontend.animation.MiniBossAnimationState;

/**
 * Walking animation state that alternates between two frames.
 * 
 * Design Pattern: State Pattern
 * - Implements MiniBossAnimationState for walking behavior
 * - Handles frame switching logic internally
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles walking animation logic
 * - Open/Closed: Can be extended without modifying other states
 */
public class WalkingAnimationState implements MiniBossAnimationState {
    
    private final Texture frame1;
    private final Texture frame2;
    private float animationTimer;
    private boolean isFrame1;
    
    // Animation speed: frame duration in seconds
    private static final float FRAME_DURATION = 0.2f;
    
    /**
     * Creates a new walking animation state with two frames.
     * 
     * @param frame1 First frame texture
     * @param frame2 Second frame texture
     */
    public WalkingAnimationState(Texture frame1, Texture frame2) {
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

