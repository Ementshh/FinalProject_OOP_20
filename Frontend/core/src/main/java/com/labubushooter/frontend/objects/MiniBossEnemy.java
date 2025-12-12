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

    // Dash system timers
    private float dashCooldown;
    private float dashWarningTimer;
    private float dashDuration;
    private float dashStunTimer;
    private float flashTimer;
    private float superJumpFlashTimer;
    private float superJumpWarningTimer;
    private long lastDamageTime; // Track last damage time for damage cooldown

    // State flags
    private boolean isWarning;
    private boolean isDashing;
    private boolean isStunned;
    private boolean shouldFlash;
    private boolean isSuperJumpWarning;
    private boolean shouldSuperJumpFlash;

    // Dash direction
    private float dashDirectionX;

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

            // Normal jump if player is above (but not triggering superjump)
            if (grounded && player.bounds.y > bounds.y + 50 && Math.abs(directionX) < 100 && !playerWasAbove) {
                velY = JUMP_POWER;
                grounded = false;
            }
        }

        // Apply gravity and platform collision
        applyGravityAndCollision(delta, platforms, grounds);

        // Melee damage is now handled by PlayerEnemyCollisionHandler in the collision
        // system
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
