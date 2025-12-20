package com.labubushooter.frontend.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class MiniBossEnemy extends BossEnemy {
    private static final float LEVEL_WIDTH = 2400f; // Level 3 width
    private static final float NORMAL_SPEED = 120f;
    private static final float DASH_SPEED = 800f;
    private static final float DASH_COOLDOWN_DURATION = 4.0f;
    private static final float DASH_WARNING_DURATION = 0.3f;
    private static final float DASH_DURATION = 0.5f;
    private static final float DASH_STUN_DURATION = 0.3f;
    private static final float FLASH_INTERVAL = 0.1f;
    private static final float SUPER_JUMP_FLASH_INTERVAL = 0.15f;
    private static final float SUPER_JUMP_WARNING_DURATION = 0.5f;
    private static final float JUMP_POWER = 500f;
    private static final float SUPER_JUMP_POWER = 750f;
    private static final float SUPER_JUMP_COOLDOWN_DURATION = 3.0f;
    
    // Smart jump system
    private static final float SMART_JUMP_COOLDOWN = 1.0f;
    private static final float PLATFORM_REACH_RANGE = 200f;
    private float smartJumpCooldown = 0f;

    // Dash system timers
    private float dashCooldown;
    private float dashWarningTimer;
    private float dashDuration;
    private float dashStunTimer;
    private float flashTimer;
    private float superJumpFlashTimer;
    private float superJumpWarningTimer;

    // State flags
    private boolean isWarning;
    private boolean isDashing;
    private boolean isStunned;
    private boolean shouldFlash;
    private boolean isSuperJumpWarning;
    private boolean shouldSuperJumpFlash;

    // Dash direction
    private float dashDirectionX;

    // Player damage cooldown
    private long lastDamageTime;
    private static final long DAMAGE_COOLDOWN = 1000000000L; // 1 second in nanoseconds

    // Superjump system
    private float superJumpTimer;
    private float superJumpThreshold;
    private float superJumpCooldown;
    private boolean playerWasAbove;
    private Texture superJumpFlashTexture;
    private static final float MIN_SUPER_JUMP_TIME = 2.0f;
    private static final float MAX_SUPER_JUMP_TIME = 5.0f;
    private static final float HEIGHT_THRESHOLD = 50f; // Player must be at least 50 units above boss

    public MiniBossEnemy(Texture tex, Texture dashFlashTex, Texture superJumpFlashTex) {
        super(0, 0, 60, 90, 120, 5.0f, tex, dashFlashTex);
        this.superJumpFlashTexture = superJumpFlashTex;
        this.dashCooldown = DASH_COOLDOWN_DURATION;
        resetState();
    }

    public void init(float x, float y) {
        bounds.setPosition(x, y);
        updateCollider();
        health = maxHealth;
        velY = 0;
        grounded = false;
        resetState();
        lastDamageTime = TimeUtils.nanoTime();
    }

    private void resetState() {
        isWarning = false;
        isDashing = false;
        isStunned = false;
        shouldFlash = false;
        dashWarningTimer = 0;
        dashDuration = 0;
        dashStunTimer = 0;
        flashTimer = 0;
        dashDirectionX = 0;
        smartJumpCooldown = 0f;

        // Superjump initialization
        superJumpTimer = 0;
        superJumpThreshold = MathUtils.random(MIN_SUPER_JUMP_TIME, MAX_SUPER_JUMP_TIME);
        superJumpCooldown = 0;
        playerWasAbove = false;
        isSuperJumpWarning = false;
        superJumpFlashTimer = 0;
        superJumpWarningTimer = 0;
        shouldSuperJumpFlash = false;
    }

    @Override
    public void update(float delta, Array<Platform> platforms, Array<Ground> grounds) {
        // This method is called from parent, but we need Player reference
        // Will be overridden with proper signature
    }

    public void update(float delta, Array<Platform> platforms, Array<Ground> grounds, Player player) {
        if (isDead()) {
            active = false;
            return;
        }
        
        // Update smart jump cooldown
        if (smartJumpCooldown > 0) {
            smartJumpCooldown -= delta;
        }

        // Update dash cooldown
        if (!isWarning && !isDashing && !isStunned) {
            dashCooldown -= delta;
        }

        // Update superjump cooldown
        if (superJumpCooldown > 0) {
            superJumpCooldown -= delta;
        }

        // --- DASH STATE MACHINE ---

        // 1. Warning Phase
        if (dashCooldown <= 0 && !isWarning && !isDashing && !isStunned) {
            isWarning = true;
            dashWarningTimer = DASH_WARNING_DURATION;
            Gdx.app.log("MiniBoss", "Warning! Preparing dash...");
        }

        if (isWarning) {
            dashWarningTimer -= delta;

            // Flash effect
            flashTimer -= delta;
            if (flashTimer <= 0) {
                shouldFlash = !shouldFlash;
                flashTimer = FLASH_INTERVAL;
            }

            // End warning, start dash
            if (dashWarningTimer <= 0) {
                isWarning = false;
                isDashing = true;
                dashDuration = DASH_DURATION;
                shouldFlash = false;

                // Calculate dash direction toward player
                float dirX = player.bounds.x - bounds.x;
                dashDirectionX = dirX > 0 ? 1f : -1f;

                Gdx.app.log("MiniBoss", "DASH!");
            }
        }

        // 2. Dash Phase
        if (isDashing) {
            dashDuration -= delta;

            // Move at dash speed
            bounds.x += dashDirectionX * DASH_SPEED * delta;

            // Boundary check to prevent dashing out of level
            if (bounds.x < 0)
                bounds.x = 0;
            if (bounds.x + bounds.width > LEVEL_WIDTH)
                bounds.x = LEVEL_WIDTH - bounds.width;

            updateCollider();

            // End dash, start stun
            if (dashDuration <= 0) {
                isDashing = false;
                isStunned = true;
                dashStunTimer = DASH_STUN_DURATION;
                Gdx.app.log("MiniBoss", "Stunned after dash");
            }
        }

        // 3. Stun Phase
        if (isStunned) {
            dashStunTimer -= delta;

            // End stun, reset cooldown
            if (dashStunTimer <= 0) {
                isStunned = false;
                dashCooldown = DASH_COOLDOWN_DURATION;
            }
        }

        // --- SUPERJUMP STATE MACHINE ---

        // 1. Superjump Tracking Phase
        if (!isWarning && !isDashing && !isStunned && !isSuperJumpWarning) {
            boolean playerIsAbove = (player.bounds.y - bounds.y) > HEIGHT_THRESHOLD;

            if (playerIsAbove && superJumpCooldown <= 0 && grounded) {
                if (!playerWasAbove) {
                    playerWasAbove = true;
                    superJumpTimer = 0;
                    superJumpThreshold = MathUtils.random(MIN_SUPER_JUMP_TIME, MAX_SUPER_JUMP_TIME);
                    Gdx.app.log("MiniBoss",
                            "Player above! Superjump countdown: " + String.format("%.1f", superJumpThreshold) + "s");
                }

                superJumpTimer += delta;

                // Trigger superjump warning when threshold reached
                if (superJumpTimer >= superJumpThreshold) {
                    isSuperJumpWarning = true;
                    superJumpWarningTimer = SUPER_JUMP_WARNING_DURATION;
                    superJumpFlashTimer = 0;
                    Gdx.app.log("MiniBoss", "Superjump Warning! Charging up...");
                }
            } else {
                if (playerWasAbove) {
                    playerWasAbove = false;
                    superJumpTimer = 0;
                    Gdx.app.log("MiniBoss", "Player moved away, superjump cancelled");
                }
            }
        }

        // 2. Superjump Warning Phase (Charge up - boss stops moving and flashes yellow)
        if (isSuperJumpWarning) {
            superJumpWarningTimer -= delta;

            // Flash effect during warning
            superJumpFlashTimer -= delta;
            if (superJumpFlashTimer <= 0) {
                shouldSuperJumpFlash = !shouldSuperJumpFlash;
                superJumpFlashTimer = SUPER_JUMP_FLASH_INTERVAL;
            }

            // End warning, execute superjump
            if (superJumpWarningTimer <= 0) {
                isSuperJumpWarning = false;
                shouldSuperJumpFlash = false;
                playerWasAbove = false;
                superJumpTimer = 0;
                velY = SUPER_JUMP_POWER;
                grounded = false;
                superJumpCooldown = SUPER_JUMP_COOLDOWN_DURATION;
                Gdx.app.log("MiniBoss", "SUPER JUMP EXECUTED!");
            }
        }

        // 4. Normal Movement Phase
        if (!isWarning && !isDashing && !isStunned && !isSuperJumpWarning) {
            // Homing AI toward player
            float directionX = player.bounds.x - bounds.x;

            if (Math.abs(directionX) > 10) {
                float moveX = Math.signum(directionX) * NORMAL_SPEED * delta;
                bounds.x += moveX;

                // Boundary check for normal movement
                if (bounds.x < 0)
                    bounds.x = 0;
                if (bounds.x + bounds.width > LEVEL_WIDTH)
                    bounds.x = LEVEL_WIDTH - bounds.width;

                updateCollider();
            }

            // Smart jump logic - replaces the old simple jump
            checkAndPerformSmartJump(player, platforms);
        }

        // Apply gravity and platform collision
        applyGravityAndCollision(delta, platforms, grounds);

        // Collision with player (melee damage)
        if (collider.overlaps(player.bounds)) {
            long currentTime = TimeUtils.nanoTime();
            if (currentTime - lastDamageTime > DAMAGE_COOLDOWN) {
                player.takeDamage(damage);
                lastDamageTime = currentTime;
                Gdx.app.log("MiniBoss", "Hit player for " + damage + " damage");
            }
        }
    }
    
    /**
     * Smart jump logic for MiniBoss to reach player on platforms.
     * More aggressive than common enemy.
     */
    private void checkAndPerformSmartJump(Player player, Array<Platform> platforms) {
        if (!grounded || smartJumpCooldown > 0 || playerWasAbove) {
            return; // Don't interfere with superjump
        }

        float bossCenterX = bounds.x + bounds.width / 2f;
        float playerCenterX = player.bounds.x + player.bounds.width / 2f;
        float horizontalDistance = Math.abs(bossCenterX - playerCenterX);
        float verticalDifference = player.bounds.y - bounds.y;

        // Condition 1: Player is above boss
        boolean playerIsAbove = verticalDifference > HEIGHT_THRESHOLD;
        
        // Condition 2: Horizontally close enough
        boolean horizontallyClose = horizontalDistance < bounds.width * 3;
        
        // Condition 3: Player is on a reachable height
        boolean reachableHeight = verticalDifference > 30f && verticalDifference < 350f;
        
        // Condition 4: X-axis overlap but no collision
        boolean xAxisOverlap = (bounds.x < player.bounds.x + player.bounds.width) && 
                               (bounds.x + bounds.width > player.bounds.x);
        boolean noDirectCollision = !collider.overlaps(player.bounds);
        
        // Condition 5: Player on platform above
        boolean playerOnPlatform = isPlayerOnPlatformAbove(player, platforms);

        if (grounded && playerIsAbove && reachableHeight) {
            boolean shouldJump = false;
            
            if (xAxisOverlap && noDirectCollision) {
                shouldJump = true;
            } else if (horizontallyClose) {
                shouldJump = true;
            } else if (playerOnPlatform && horizontalDistance < PLATFORM_REACH_RANGE) {
                shouldJump = true;
            }
            
            if (shouldJump) {
                velY = JUMP_POWER;
                grounded = false;
                smartJumpCooldown = SMART_JUMP_COOLDOWN;
            }
        }
    }

    /**
     * Checks if player is standing on a platform above the boss.
     */
    private boolean isPlayerOnPlatformAbove(Player player, Array<Platform> platforms) {
        float bossTop = bounds.y + bounds.height;
        
        for (Platform p : platforms) {
            boolean platformAboveBoss = p.bounds.y > bossTop;
            boolean reachable = p.bounds.y - bossTop < 250f;
            boolean playerOnPlatform = Math.abs(player.bounds.y - (p.bounds.y + p.bounds.height)) < 20f;
            boolean playerOverlapsPlatform = player.bounds.x < p.bounds.x + p.bounds.width &&
                                             player.bounds.x + player.bounds.width > p.bounds.x;
            
            if (platformAboveBoss && reachable && playerOnPlatform && playerOverlapsPlatform) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Texture getCurrentTexture() {
        // Priority: Superjump warning (yellow) > Dash warning (white) > Normal
        if (isSuperJumpWarning && shouldSuperJumpFlash) {
            return superJumpFlashTexture;
        }
        if (isWarning && shouldFlash) {
            return flashTexture;
        }
        return texture;
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
    }
}
