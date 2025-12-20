package com.labubushooter.frontend.animation;

import com.badlogic.gdx.graphics.Texture;
import com.labubushooter.frontend.animation.states.DashAnimationState;
import com.labubushooter.frontend.animation.states.DashPrepAnimationState;
import com.labubushooter.frontend.animation.states.InAirAnimationState;
import com.labubushooter.frontend.animation.states.SuperJumpPrepAnimationState;
import com.labubushooter.frontend.animation.states.WalkingAnimationState;

/**
 * Animation strategy for MiniBoss using State Pattern.
 * Manages different animation states based on boss behavior.
 * 
 * Design Patterns:
 * - Strategy Pattern: Implements AnimationStrategy interface
 * - State Pattern: Uses MiniBossAnimationState to manage different animation behaviors
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles MiniBoss animation logic
 * - Open/Closed: New states can be added without modifying this class
 * - Dependency Inversion: Depends on AnimationStrategy and MiniBossAnimationState abstractions
 */
public class MiniBossAnimationStrategy implements AnimationStrategy {
    
    // Animation states
    private final WalkingAnimationState walkingState;
    private final DashPrepAnimationState dashPrepState;
    private final DashAnimationState dashState;
    private final SuperJumpPrepAnimationState superJumpPrepState;
    private final InAirAnimationState inAirState;
    
    // Current state
    private MiniBossAnimationState currentState;
    
    // Facing direction
    private boolean facingLeft;
    
    // Grounded state
    private boolean isGrounded;
    
    /**
     * Creates a new MiniBoss animation strategy with all required textures.
     * 
     * @param walkFrame1 First walking frame texture
     * @param walkFrame2 Second walking frame texture
     * @param crouchTexture Crouch texture (for super jump prep)
     * @param dashPrepTexture Dash prep texture
     * @param dashTexture Dash texture
     */
    public MiniBossAnimationStrategy(Texture walkFrame1, Texture walkFrame2,
                                     Texture crouchTexture, Texture dashPrepTexture,
                                     Texture dashTexture) {
        this.walkingState = new WalkingAnimationState(walkFrame1, walkFrame2);
        this.dashPrepState = new DashPrepAnimationState(dashPrepTexture);
        this.dashState = new DashAnimationState(dashTexture);
        this.superJumpPrepState = new SuperJumpPrepAnimationState(crouchTexture);
        this.inAirState = new InAirAnimationState(walkFrame2); // Default to walk_frame2 in air
        
        this.currentState = walkingState;
        this.facingLeft = false;
        this.isGrounded = true;
    }
    
    /**
     * Sets the animation state based on boss behavior.
     * Priority: SuperJumpPrep > DashPrep > Dash > InAir > Walking
     * 
     * @param isSuperJumpWarning true if super jump warning is active
     * @param isDashWarning true if dash warning is active
     * @param isDashing true if currently dashing
     * @param isGrounded true if boss is on the ground
     */
    public void setState(boolean isSuperJumpWarning, boolean isDashWarning,
                        boolean isDashing, boolean isGrounded) {
        this.isGrounded = isGrounded;
        
        // Priority order: SuperJumpPrep > DashPrep > Dash > InAir > Walking
        if (isSuperJumpWarning) {
            currentState = superJumpPrepState;
        } else if (isDashWarning) {
            currentState = dashPrepState;
        } else if (isDashing) {
            currentState = dashState;
        } else if (!isGrounded) {
            currentState = inAirState;
        } else {
            currentState = walkingState;
        }
    }
    
    @Override
    public void update(float delta) {
        // Only update if current state should animate
        if (currentState.shouldAnimate()) {
            currentState.update(delta);
        }
    }
    
    @Override
    public Texture getCurrentFrame() {
        return currentState.getCurrentFrame();
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
        // State will be updated via setState() call
    }
    
    @Override
    public void reset() {
        walkingState.reset();
        dashPrepState.reset();
        dashState.reset();
        superJumpPrepState.reset();
        inAirState.reset();
        currentState = walkingState;
        facingLeft = false;
        isGrounded = true;
    }
}

