package com.labubushooter.frontend.animation;

import com.badlogic.gdx.graphics.Texture;
import com.labubushooter.frontend.animation.states.PlayerIdleState;
import com.labubushooter.frontend.animation.states.PlayerInAirState;
import com.labubushooter.frontend.animation.states.PlayerWalkingState;

/**
 * Animation strategy specifically for Player character.
 * Handles Idle, Walking, and In-Air states.
 * 
 * Design Pattern: Strategy Pattern + State Pattern
 * - Implements AnimationStrategy for player behavior
 * - Uses State Pattern internally for different animation states
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles player animation logic
 * - Open/Closed: New states can be added without modifying existing code
 * - Liskov Substitution: Can be used anywhere AnimationStrategy is expected
 */
public class PlayerAnimationStrategy implements AnimationStrategy {
    
    /**
     * Enumeration of player animation states.
     */
    public enum PlayerStateType {
        IDLE,
        WALKING,
        IN_AIR
    }
    
    // Animation states (State Pattern)
    private final PlayerIdleState idleState;
    private final PlayerWalkingState walkingState;
    private final PlayerInAirState inAirState;
    
    // Current state reference
    private PlayerAnimationState currentState;
    private PlayerStateType currentStateType;
    private PlayerStateType previousStateType;
    
    // Direction
    private boolean facingLeft;
    private boolean isGrounded;
    
    // Sprite dimensions for alignment
    private float currentSpriteWidth;
    private float currentSpriteHeight;
    
    /**
     * Creates a new player animation strategy with all required textures.
     * 
     * @param idleTexture The idle/standing texture (player.png)
     * @param walkFrame1 First walking frame (player_walk_frame1.png)
     * @param walkFrame2 Second walking frame (player_walk_frame2.png)
     * @param crouchTexture Crouch texture for in-air state (player_crouch.png)
     */
    public PlayerAnimationStrategy(Texture idleTexture, Texture walkFrame1, 
                                   Texture walkFrame2, Texture crouchTexture) {
        // Validate inputs
        if (idleTexture == null || walkFrame1 == null || 
            walkFrame2 == null || crouchTexture == null) {
            throw new IllegalArgumentException("All textures must be provided (cannot be null)");
        }
        
        // Initialize states
        this.idleState = new PlayerIdleState(idleTexture);
        this.walkingState = new PlayerWalkingState(walkFrame1, walkFrame2);
        this.inAirState = new PlayerInAirState(crouchTexture); // Use crouch for in-air
        
        // Initialize to idle
        this.currentState = idleState;
        this.currentStateType = PlayerStateType.IDLE;
        this.previousStateType = PlayerStateType.IDLE;
        
        // Initialize flags
        this.facingLeft = false;
        this.isGrounded = true;
        
        updateSpriteDimensions();
    }
    
    /**
     * Updates the animation state based on player movement.
     * Call this every frame with current velocity.
     * 
     * @param velocityX Horizontal velocity
     * @param velocityY Vertical velocity (not currently used)
     * @param grounded Whether player is on ground
     */
    public void updateMovementState(float velocityX, float velocityY, boolean grounded) {
        this.isGrounded = grounded;
        
        // Update facing direction based on horizontal movement
        if (velocityX > 0.1f) {
            facingLeft = false;
        } else if (velocityX < -0.1f) {
            facingLeft = true;
        }
        
        // Determine state based on grounded status and movement
        if (!grounded) {
            // In air - use crouch texture
            setState(PlayerStateType.IN_AIR);
        } else if (Math.abs(velocityX) > 0.1f) {
            // Moving on ground - walking
            setState(PlayerStateType.WALKING);
        } else {
            // Standing still on ground - idle
            setState(PlayerStateType.IDLE);
        }
    }
    
    /**
     * Sets the current animation state.
     * Handles state transitions and resets.
     * 
     * @param newStateType The new state to transition to
     */
    private void setState(PlayerStateType newStateType) {
        if (currentStateType != newStateType) {
            previousStateType = currentStateType;
            currentStateType = newStateType;
            
            // Update state reference
            switch (newStateType) {
                case IDLE:
                    currentState = idleState;
                    idleState.reset();
                    break;
                case WALKING:
                    currentState = walkingState;
                    walkingState.reset();
                    break;
                case IN_AIR:
                    currentState = inAirState;
                    inAirState.reset();
                    break;
            }
            
            updateSpriteDimensions();
        }
    }
    
    /**
     * Updates cached sprite dimensions based on current frame.
     */
    private void updateSpriteDimensions() {
        Texture current = getCurrentFrame();
        if (current != null) {
            currentSpriteWidth = current.getWidth();
            currentSpriteHeight = current.getHeight();
        }
    }
    
    @Override
    public void update(float delta) {
        // Update current state
        currentState.update(delta);
        
        // Update sprite dimensions in case frame changed
        updateSpriteDimensions();
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
        boolean wasAirborne = !this.isGrounded;
        this.isGrounded = grounded;
        
        // Transition from air to ground
        if (grounded && wasAirborne && currentStateType == PlayerStateType.IN_AIR) {
            setState(PlayerStateType.IDLE);
        }
    }
    
    @Override
    public void reset() {
        currentState = idleState;
        currentStateType = PlayerStateType.IDLE;
        previousStateType = PlayerStateType.IDLE;
        facingLeft = false;
        isGrounded = true;
        
        // Reset all states
        idleState.reset();
        walkingState.reset();
        inAirState.reset();
        
        updateSpriteDimensions();
    }
    
    /**
     * Gets the current state type for external checks.
     * 
     * @return Current player animation state type
     */
    public PlayerStateType getCurrentStateType() {
        return currentStateType;
    }
    
    /**
     * Gets the previous state type.
     * 
     * @return Previous player animation state type
     */
    public PlayerStateType getPreviousStateType() {
        return previousStateType;
    }
    
    /**
     * Gets the current sprite width for alignment calculations.
     * 
     * @return Width of current sprite texture
     */
    public float getCurrentSpriteWidth() {
        return currentSpriteWidth;
    }
    
    /**
     * Gets the current sprite height for alignment calculations.
     * 
     * @return Height of current sprite texture
     */
    public float getCurrentSpriteHeight() {
        return currentSpriteHeight;
    }
    
    /**
     * Checks if the player is currently grounded.
     * 
     * @return true if grounded
     */
    public boolean isGrounded() {
        return isGrounded;
    }
}
