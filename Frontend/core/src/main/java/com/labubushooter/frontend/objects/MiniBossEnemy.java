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
    private static final float JUMP_POWER = 500f;

    // Dash system timers
    private float dashCooldown;
    private float dashWarningTimer;
    private float dashDuration;
    private float dashStunTimer;
    private float flashTimer;

    // State flags
    private boolean isWarning;
    private boolean isDashing;
    private boolean isStunned;
    private boolean shouldFlash;

    // Dash direction
    private float dashDirectionX;

    // Player damage cooldown
    private long lastDamageTime;
    private static final long DAMAGE_COOLDOWN = 1000000000L; // 1 second in nanoseconds

    public MiniBossEnemy(Texture tex, Texture flashTex) {
        super(0, 0, 60, 90, 120, 5.0f, tex, flashTex);
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
    }

    @Override
    public void update(float delta, Array<Platform> platforms) {
        // This method is called from parent, but we need Player reference
        // Will be overridden with proper signature
    }

    public void update(float delta, Array<Platform> platforms, Player player) {
        if (isDead()) {
            active = false;
            return;
        }

        // Update dash cooldown
        if (!isWarning && !isDashing && !isStunned) {
            dashCooldown -= delta;
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

        // 4. Normal Movement Phase
        if (!isWarning && !isDashing && !isStunned) {
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

            // Jump if player is above
            if (grounded && player.bounds.y > bounds.y + 50 && Math.abs(directionX) < 100) {
                velY = JUMP_POWER;
                grounded = false;
            }
        }

        // Apply gravity and platform collision
        applyGravityAndCollision(delta, platforms);

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

    @Override
    protected Texture getCurrentTexture() {
        // Show flash texture during warning phase
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
