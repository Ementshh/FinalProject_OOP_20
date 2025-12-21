package com.labubushooter.frontend.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.labubushooter.frontend.animation.states.*;

/**
 * Animation strategy for Final Boss with three phases.
 * Manages animation states across three phases with walking, jumping, and big attack animations.
 * 
 * Design Pattern: Strategy Pattern + State Pattern
 * - Implements AnimationStrategy interface
 * - Uses State Pattern to manage 9 different animation states (3 phases × 3 states)
 * - Composition over inheritance for flexibility
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles Final Boss animation logic
 * - Open/Closed: Can be extended with new phases without modifying existing code
 * - Dependency Inversion: Depends on BossAnimationState abstraction
 * 
 * Requirements: 4.1, 4.3, 5.2, 5.3
 */
public class FinalBossAnimationStrategy implements AnimationStrategy {
    
    // Phase 1 states
    private final Phase1WalkingState phase1Walking;
    private final Phase1JumpingState phase1Jumping;
    private final Phase1BigAttackState phase1BigAttack;
    
    // Phase 2 states
    private final Phase2WalkingState phase2Walking;
    private final Phase2JumpingState phase2Jumping;
    private final Phase2BigAttackState phase2BigAttack;
    
    // Phase 3 states
    private final Phase3WalkingState phase3Walking;
    private final Phase3JumpingState phase3Jumping;
    private final Phase3BigAttackState phase3BigAttack;
    
    // Current state tracking
    private BossAnimationState currentState;
    private int currentPhase;
    private boolean facingLeft;
    private boolean isGrounded;
    
    // Fallback texture for error cases
    private final Texture fallbackTexture;
    
    /**
     * Creates a new Final Boss animation strategy with all phase textures.
     * 
     * @param phase1Walk1 Phase 1 walking frame 1
     * @param phase1Walk2 Phase 1 walking frame 2
     * @param phase1Jump Phase 1 jumping texture
     * @param phase1BigAttack Phase 1 big attack texture
     * @param phase2Walk1 Phase 2 walking frame 1
     * @param phase2Walk2 Phase 2 walking frame 2
     * @param phase2Jump Phase 2 jumping texture
     * @param phase2BigAttack Phase 2 big attack texture
     * @param phase3Walk1 Phase 3 walking frame 1
     * @param phase3Walk2 Phase 3 walking frame 2
     * @param phase3Jump Phase 3 jumping texture
     * @param phase3BigAttack Phase 3 big attack texture
     * @param fallbackTexture Fallback texture (boss.png) for error cases
     */
    public FinalBossAnimationStrategy(
            Texture phase1Walk1, Texture phase1Walk2, Texture phase1Jump, Texture phase1BigAttack,
            Texture phase2Walk1, Texture phase2Walk2, Texture phase2Jump, Texture phase2BigAttack,
            Texture phase3Walk1, Texture phase3Walk2, Texture phase3Jump, Texture phase3BigAttack,
            Texture fallbackTexture) {
        
        // Store fallback texture
        this.fallbackTexture = fallbackTexture;
        
        // Initialize Phase 1 states with null checking
        this.phase1Walking = new Phase1WalkingState(
            phase1Walk1 != null ? phase1Walk1 : fallbackTexture,
            phase1Walk2 != null ? phase1Walk2 : fallbackTexture
        );
        this.phase1Jumping = new Phase1JumpingState(
            phase1Jump != null ? phase1Jump : fallbackTexture
        );
        this.phase1BigAttack = new Phase1BigAttackState(
            phase1BigAttack != null ? phase1BigAttack : fallbackTexture
        );
        
        // Initialize Phase 2 states with null checking
        this.phase2Walking = new Phase2WalkingState(
            phase2Walk1 != null ? phase2Walk1 : fallbackTexture,
            phase2Walk2 != null ? phase2Walk2 : fallbackTexture
        );
        this.phase2Jumping = new Phase2JumpingState(
            phase2Jump != null ? phase2Jump : fallbackTexture
        );
        this.phase2BigAttack = new Phase2BigAttackState(
            phase2BigAttack != null ? phase2BigAttack : fallbackTexture
        );
        
        // Initialize Phase 3 states with null checking
        this.phase3Walking = new Phase3WalkingState(
            phase3Walk1 != null ? phase3Walk1 : fallbackTexture,
            phase3Walk2 != null ? phase3Walk2 : fallbackTexture
        );
        this.phase3Jumping = new Phase3JumpingState(
            phase3Jump != null ? phase3Jump : fallbackTexture
        );
        this.phase3BigAttack = new Phase3BigAttackState(
            phase3BigAttack != null ? phase3BigAttack : fallbackTexture
        );
        
        // Log warnings for any null textures
        if (phase1Walk1 == null && Gdx.app != null) Gdx.app.error("FinalBossAnimation", "Phase 1 walk1 texture is null, using fallback");
        if (phase1Walk2 == null && Gdx.app != null) Gdx.app.error("FinalBossAnimation", "Phase 1 walk2 texture is null, using fallback");
        if (phase1Jump == null && Gdx.app != null) Gdx.app.error("FinalBossAnimation", "Phase 1 jump texture is null, using fallback");
        if (phase1BigAttack == null && Gdx.app != null) Gdx.app.error("FinalBossAnimation", "Phase 1 big attack texture is null, using fallback");
        if (phase2Walk1 == null && Gdx.app != null) Gdx.app.error("FinalBossAnimation", "Phase 2 walk1 texture is null, using fallback");
        if (phase2Walk2 == null && Gdx.app != null) Gdx.app.error("FinalBossAnimation", "Phase 2 walk2 texture is null, using fallback");
        if (phase2Jump == null && Gdx.app != null) Gdx.app.error("FinalBossAnimation", "Phase 2 jump texture is null, using fallback");
        if (phase2BigAttack == null && Gdx.app != null) Gdx.app.error("FinalBossAnimation", "Phase 2 big attack texture is null, using fallback");
        if (phase3Walk1 == null && Gdx.app != null) Gdx.app.error("FinalBossAnimation", "Phase 3 walk1 texture is null, using fallback");
        if (phase3Walk2 == null && Gdx.app != null) Gdx.app.error("FinalBossAnimation", "Phase 3 walk2 texture is null, using fallback");
        if (phase3Jump == null && Gdx.app != null) Gdx.app.error("FinalBossAnimation", "Phase 3 jump texture is null, using fallback");
        if (phase3BigAttack == null && Gdx.app != null) Gdx.app.error("FinalBossAnimation", "Phase 3 big attack texture is null, using fallback");
        
        // Initialize to Phase 1 walking state
        this.currentState = phase1Walking;
        this.currentPhase = 1;
        this.facingLeft = false;
        this.isGrounded = true;
    }
    
    @Override
    public void update(float delta) {
        if (currentState != null && currentState.shouldAnimate()) {
            currentState.update(delta);
        }
    }
    
    @Override
    public Texture getCurrentFrame() {
        if (currentState == null) {
            if (Gdx.app != null) {
                Gdx.app.error("FinalBossAnimation", "Current state is null, using fallback");
            }
            return fallbackTexture;
        }
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
    }
    
    /**
     * Sets the current animation state based on phase and boss conditions.
     * 
     * Priority logic: BigAttack > Jumping > Walking
     * - If upward shot warning is active, use big attack state
     * - Else if boss is not grounded, use jumping state
     * - Else use walking state
     * 
     * @param phase The current phase (1, 2, or 3)
     * @param isUpwardShotWarning Whether the boss is preparing a big attack
     * @param isGrounded Whether the boss is on the ground
     * 
     * Requirements: 4.1, 6.1, 6.2, 6.3, 6.4
     */
    public void setState(int phase, boolean isUpwardShotWarning, boolean isGrounded) {
        // Validate and set current phase
        if (phase < 1 || phase > 3) {
            if (Gdx.app != null) {
                Gdx.app.error("FinalBossAnimation", "Invalid phase: " + phase + ", defaulting to 1");
            }
            phase = 1;
        }
        this.currentPhase = phase;
        
        // Priority logic: BigAttack > Jumping > Walking
        if (isUpwardShotWarning) {
            // Big attack has highest priority
            switch (phase) {
                case 1:
                    currentState = phase1BigAttack;
                    break;
                case 2:
                    currentState = phase2BigAttack;
                    break;
                case 3:
                    currentState = phase3BigAttack;
                    break;
            }
        } else if (!isGrounded) {
            // Jumping has second priority
            switch (phase) {
                case 1:
                    currentState = phase1Jumping;
                    break;
                case 2:
                    currentState = phase2Jumping;
                    break;
                case 3:
                    currentState = phase3Jumping;
                    break;
            }
        } else {
            // Walking is default state
            switch (phase) {
                case 1:
                    currentState = phase1Walking;
                    break;
                case 2:
                    currentState = phase2Walking;
                    break;
                case 3:
                    currentState = phase3Walking;
                    break;
            }
        }
    }
    
    /**
     * Sets the current phase of the boss.
     * Validates phase input and defaults to 1 if invalid.
     * 
     * @param phase The phase to set (1, 2, or 3)
     * 
     * Requirements: 4.3, 6.6, 8.4
     */
    public void setPhase(int phase) {
        if (phase < 1 || phase > 3) {
            if (Gdx.app != null) {
                Gdx.app.error("FinalBossAnimation", "Invalid phase: " + phase + ", defaulting to 1");
            }
            phase = 1;
        }
        
        if (this.currentPhase != phase) {
            if (Gdx.app != null) {
                Gdx.app.log("FinalBossAnimation", "Phase changed from " + this.currentPhase + " to " + phase);
            }
            this.currentPhase = phase;
        }
    }
    
    @Override
    public void reset() {
        // Reset all states
        phase1Walking.reset();
        phase1Jumping.reset();
        phase1BigAttack.reset();
        phase2Walking.reset();
        phase2Jumping.reset();
        phase2BigAttack.reset();
        phase3Walking.reset();
        phase3Jumping.reset();
        phase3BigAttack.reset();
        
        // Reset to initial state
        currentState = phase1Walking;
        currentPhase = 1;
        facingLeft = false;
        isGrounded = true;
    }
}
