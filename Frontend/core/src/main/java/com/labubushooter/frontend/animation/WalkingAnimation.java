package com.labubushooter.frontend.animation;

import com.badlogic.gdx.graphics.Texture;

/**
 * Walking animation implementation using two frames.
 * Supports grounded state to lock animation when airborne.
 * 
 * Design Pattern: Strategy Pattern + State Pattern
 * - Implements AnimationStrategy for walking behavior
 * - Uses grounded state to control animation behavior
 * - Can be easily replaced with other animation strategies
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles walking animation logic with grounded state
 * - Open/Closed: Can be extended without modifying CommonEnemy
 * - Dependency Inversion: CommonEnemy depends on AnimationStrategy interface
 */
public class WalkingAnimation implements AnimationStrategy {
    
    private final Texture frame1;
    private final Texture frame2;
    private float animationTimer;
    private boolean facingLeft;
    private boolean isFrame1;
    private boolean isGrounded;
    
    // Animation speed: frame duration in seconds
    private static final float FRAME_DURATION = 0.2f;
    
    /**
     * Creates a new walking animation with two frames.
     * 
     * @param frame1 First frame texture (default facing right)
     * @param frame2 Second frame texture (default facing right)
     */
    public WalkingAnimation(Texture frame1, Texture frame2) {
        if (frame1 == null || frame2 == null) {
            throw new IllegalArgumentException("Frame textures cannot be null");
        }
        this.frame1 = frame1;
        this.frame2 = frame2;
        this.animationTimer = 0f;
        this.facingLeft = false;
        this.isFrame1 = true;
        this.isGrounded = true;
    }
    
    @Override
    public void update(float delta) {
        // Only update animation when grounded
        if (!isGrounded) {
            return;
        }
        
        animationTimer += delta;
        
        // Switch frame when duration exceeded
        if (animationTimer >= FRAME_DURATION) {
            isFrame1 = !isFrame1;
            animationTimer = 0f;
        }
    }
    
    @Override
    public Texture getCurrentFrame() {
        // When airborne, always return frame1 (static frame)
        if (!isGrounded) {
            return frame1;
        }
        // When grounded, return current animation frame
        return isFrame1 ? frame1 : frame2;
    }
    
    @Override
    public boolean isFacingLeft() {
        return facingLeft;
    }
    
    @Override
    public void setFacingLeft(boolean facingLeft) {
        this.facingLeft = facingLeft;
    }
    
    @Override
    public void setGrounded(boolean grounded) {
        this.isGrounded = grounded;
    }
    
    @Override
    public void reset() {
        animationTimer = 0f;
        isFrame1 = true;
        facingLeft = false;
        isGrounded = true;
    }
}

