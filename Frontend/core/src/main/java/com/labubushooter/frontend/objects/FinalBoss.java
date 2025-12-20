package com.labubushooter.frontend.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;

public class FinalBoss extends BossEnemy {
    private static final float LEVEL_WIDTH = 2400f; // Level 4 width
    private static final float JUMP_POWER = 500f;

    // Phase system
    private int currentPhase = 1;
    private float phaseTransitionTimer = 0f;
    private static final float PHASE_TRANSITION_DURATION = 0.3f;
    private static final float FLASH_INTERVAL = 0.1f;
    private float flashTimer = 0f;
    private boolean shouldFlash = false;

    // Upward shot system
    private float upwardShotTimer;
    private float upwardShotThreshold;
    private float upwardShotCooldown;
    private boolean playerWasAbove;
    private boolean isUpwardShotWarning;
    private boolean shouldUpwardShotFlash;
    private float upwardShotFlashTimer;
    private float upwardShotWarningTimer;
    private float upwardShotStunTimer;
    private boolean isUpwardShotStunned;
    private Texture upwardShotFlashTexture;
    private static final float MIN_UPWARD_SHOT_TIME = 2.0f;
    private static final float MAX_UPWARD_SHOT_TIME = 5.0f;
    private static final float HEIGHT_THRESHOLD = 50f;
    private static final float UPWARD_SHOT_FLASH_INTERVAL = 0.15f;
    private static final float UPWARD_SHOT_WARNING_DURATION = 0.5f;
    private static final float UPWARD_SHOT_STUN_DURATION = 0.3f;
    private static final float UPWARD_SHOT_COOLDOWN_DURATION = 4.0f;
    private static final float UPWARD_SHOT_BASE_ANGLE = 90f; // Straight up
    private static final float BIG_BULLET_SPEED = 600f;
    private static final float BIG_BULLET_DAMAGE = 15.0f;
    private static final float BIG_BULLET_WIDTH = 80f;
    private static final float BIG_BULLET_HEIGHT = 80f;
    
    // Smart jump system
    private static final float SMART_JUMP_COOLDOWN = 0.8f;
    private static final float PLATFORM_REACH_RANGE = 250f;
    private static final float AGGRESSIVE_JUMP_RANGE = 180f;
    private float smartJumpCooldown = 0f;

    // Phase configurations
    private float currentSpeed;
    private float shootCooldown;
    private float shootTimer;

    // Phase 1: Single bullet attack
    private static final float PHASE1_SPEED = 100f;
    private static final float PHASE1_COOLDOWN = 4.0f;

    // Phase 2: Spread attack
    private static final float PHASE2_SPEED = 120f;
    private static final float PHASE2_COOLDOWN = 3.0f;
    private static final int PHASE2_BULLET_COUNT = 3;
    private static final float PHASE2_SPREAD_ANGLE = 20f;

    // Phase 3: Fan attack
    private static final float PHASE3_SPEED = 160f;
    private static final float PHASE3_COOLDOWN = 2.0f;
    private static final int PHASE3_BULLET_COUNT = 5;
    private static final float PHASE3_SPREAD_ANGLE = 30f;

    // Bullet properties
    private static final float BULLET_SPEED = 300f;
    private static final float BULLET_DAMAGE = 5.0f;

    // Player damage cooldown
    private long lastDamageTime;
    private static final long DAMAGE_COOLDOWN = 1000000000L; // 1 second

    private Texture enemyBulletTex;

    public FinalBoss(Texture tex, Texture flashTex, Texture bulletTex, Texture upwardShotFlashTex) {
        super(0, 0, 60, 100, 400, 8.0f, tex, flashTex);
        this.enemyBulletTex = bulletTex;
        this.upwardShotFlashTexture = upwardShotFlashTex;
        this.currentSpeed = PHASE1_SPEED;
        this.shootCooldown = PHASE1_COOLDOWN;
        this.shootTimer = shootCooldown;
    }

    public void init(float x, float y) {
        bounds.setPosition(x, y);
        updateCollider();
        health = maxHealth;
        velY = 0;
        grounded = false;
        currentPhase = 1;
        currentSpeed = PHASE1_SPEED;
        shootCooldown = PHASE1_COOLDOWN;
        shootTimer = shootCooldown;
        phaseTransitionTimer = 0f;
        shouldFlash = false;
        lastDamageTime = TimeUtils.nanoTime();
        smartJumpCooldown = 0f;

        // Upward shot initialization
        upwardShotTimer = 0;
        upwardShotThreshold = MIN_UPWARD_SHOT_TIME
                + (float) Math.random() * (MAX_UPWARD_SHOT_TIME - MIN_UPWARD_SHOT_TIME);
        upwardShotCooldown = 0;
        playerWasAbove = false;
        isUpwardShotWarning = false;
        shouldUpwardShotFlash = false;
        upwardShotFlashTimer = 0;
        upwardShotWarningTimer = 0;
        upwardShotStunTimer = 0;
        isUpwardShotStunned = false;
    }

    @Override
    public void update(float delta, Array<Platform> platforms, Array<Ground> grounds) {
        // This method is called from parent, but we need additional parameters
        // Will be overridden with proper signature
    }

    public void update(float delta, Array<Platform> platforms, Array<Ground> grounds, Player player,
            Array<EnemyBullet> bullets, Pool<EnemyBullet> bulletPool) {
        if (isDead()) {
            active = false;
            return;
        }
        
        // Update smart jump cooldown
        if (smartJumpCooldown > 0) {
            smartJumpCooldown -= delta;
        }

        // Update phase transition
        if (phaseTransitionTimer > 0) {
            phaseTransitionTimer -= delta;

            // Flash effect during transition
            flashTimer -= delta;
            if (flashTimer <= 0) {
                shouldFlash = !shouldFlash;
                flashTimer = FLASH_INTERVAL;
            }

            if (phaseTransitionTimer <= 0) {
                shouldFlash = false;
            }

            // Don't move during phase transition
            applyGravityAndCollision(delta, platforms, grounds);
            return;
        }

        // Update upward shot cooldown
        if (upwardShotCooldown > 0) {
            upwardShotCooldown -= delta;
        }

        // --- UPWARD SHOT STATE MACHINE ---

        // 1. Tracking Phase
        if (!isUpwardShotWarning && !isUpwardShotStunned && phaseTransitionTimer <= 0) {
            boolean playerIsAbove = (player.bounds.y - bounds.y) > HEIGHT_THRESHOLD;

            if (playerIsAbove && upwardShotCooldown <= 0 && grounded) {
                if (!playerWasAbove) {
                    playerWasAbove = true;
                    upwardShotTimer = 0;
                    upwardShotThreshold = MIN_UPWARD_SHOT_TIME
                            + (float) Math.random() * (MAX_UPWARD_SHOT_TIME - MIN_UPWARD_SHOT_TIME);
                    Gdx.app.log("Boss", "Player above! Upward shot countdown: "
                            + String.format("%.1f", upwardShotThreshold) + "s");
                }

                upwardShotTimer += delta;

                // Trigger upward shot warning when threshold reached
                if (upwardShotTimer >= upwardShotThreshold) {
                    isUpwardShotWarning = true;
                    upwardShotWarningTimer = UPWARD_SHOT_WARNING_DURATION;
                    upwardShotFlashTimer = 0;
                    Gdx.app.log("Boss", "Upward Shot Warning! Charging...");
                }
            } else {
                if (playerWasAbove) {
                    playerWasAbove = false;
                    upwardShotTimer = 0;
                    Gdx.app.log("Boss", "Player moved away, upward shot cancelled");
                }
            }
        }

        // 2. Warning Phase (Boss stops jumping and flashes)
        if (isUpwardShotWarning) {
            upwardShotWarningTimer -= delta;

            // Flash effect during warning
            upwardShotFlashTimer -= delta;
            if (upwardShotFlashTimer <= 0) {
                shouldUpwardShotFlash = !shouldUpwardShotFlash;
                upwardShotFlashTimer = UPWARD_SHOT_FLASH_INTERVAL;
            }

            // End warning, execute upward shot
            if (upwardShotWarningTimer <= 0) {
                isUpwardShotWarning = false;
                shouldUpwardShotFlash = false;
                playerWasAbove = false;
                upwardShotTimer = 0;
                upwardShotCooldown = UPWARD_SHOT_COOLDOWN_DURATION;

                // Fire ONE BIG upward bullet
                shootBigUpwardBullet(bullets, bulletPool);

                // Enter stun phase (exhausted after big shot)
                isUpwardShotStunned = true;
                upwardShotStunTimer = UPWARD_SHOT_STUN_DURATION;

                Gdx.app.log("Boss", "BIG UPWARD SHOT EXECUTED!");
            }

            // Don't move or shoot during warning
            applyGravityAndCollision(delta, platforms, grounds);
            return;
        }

        // 3. Stun Phase (Boss exhausted after shooting big bullet)
        if (isUpwardShotStunned) {
            upwardShotStunTimer -= delta;

            if (upwardShotStunTimer <= 0) {
                isUpwardShotStunned = false;
                Gdx.app.log("Boss", "Recovered from upward shot exhaustion");
            }

            // Don't move or shoot during stun
            applyGravityAndCollision(delta, platforms, grounds);
            return;
        }

        // Check for phase changes
        updatePhase();

        // Shooting (all phases)
        shootTimer -= delta;
        if (shootTimer <= 0) {
            shootPattern(bullets, bulletPool, player);
            shootTimer = shootCooldown;
        }

        // Movement AI
        float directionX = player.bounds.x - bounds.x;

        // Horizontal homing
        if (Math.abs(directionX) > 10) {
            float moveX = Math.signum(directionX) * currentSpeed * delta;
            bounds.x += moveX;

            // Boundary check to prevent moving out of level
            if (bounds.x < 0)
                bounds.x = 0;
            if (bounds.x + bounds.width > LEVEL_WIDTH)
                bounds.x = LEVEL_WIDTH - bounds.width;

            updateCollider();
        }

        // Smart jump logic - enhanced pursuit
        checkAndPerformSmartJump(player, platforms);

        // Apply gravity and platform collision
        applyGravityAndCollision(delta, platforms, grounds);

        // Collision with player (melee damage)
        if (collider.overlaps(player.bounds)) {
            long currentTime = TimeUtils.nanoTime();
            if (currentTime - lastDamageTime > DAMAGE_COOLDOWN) {
                player.takeDamage(damage);
                lastDamageTime = currentTime;
                Gdx.app.log("Boss", "Hit player for " + damage + " damage");
            }
        }
    }
    
    /**
     * Smart jump logic for Final Boss.
     * Most aggressive jumping behavior - adapts based on phase.
     */
    private void checkAndPerformSmartJump(Player player, Array<Platform> platforms) {
        // Don't jump during upward shot sequence
        if (!grounded || smartJumpCooldown > 0 || playerWasAbove || isUpwardShotWarning || isUpwardShotStunned) {
            return;
        }

        float bossCenterX = bounds.x + bounds.width / 2f;
        float playerCenterX = player.bounds.x + player.bounds.width / 2f;
        float horizontalDistance = Math.abs(bossCenterX - playerCenterX);
        float verticalDifference = player.bounds.y - bounds.y;

        // Phase-based aggression multiplier
        float aggressionMultiplier = 1.0f + (currentPhase - 1) * 0.3f;
        float effectiveRange = AGGRESSIVE_JUMP_RANGE * aggressionMultiplier;

        // Condition 1: Player is above boss
        boolean playerIsAbove = verticalDifference > HEIGHT_THRESHOLD;
        
        // Condition 2: Horizontally within effective range
        boolean horizontallyClose = horizontalDistance < effectiveRange;
        
        // Condition 3: Player is on a reachable height
        boolean reachableHeight = verticalDifference > 30f && verticalDifference < 400f;
        
        // Condition 4: X-axis overlap detection
        boolean xAxisOverlap = (bounds.x < player.bounds.x + player.bounds.width) && 
                               (bounds.x + bounds.width > player.bounds.x);
        boolean noDirectCollision = !collider.overlaps(player.bounds);
        
        // Condition 5: Player on platform above
        boolean playerOnPlatform = isPlayerOnPlatformAbove(player, platforms);

        if (grounded && playerIsAbove && reachableHeight) {
            boolean shouldJump = false;
            float jumpPower = JUMP_POWER;
            
            // Priority 1: Player directly above (X overlap, no Y collision)
            if (xAxisOverlap && noDirectCollision) {
                shouldJump = true;
                jumpPower = JUMP_POWER * 1.1f;
            }
            // Priority 2: Player on platform above and within range
            else if (playerOnPlatform && horizontalDistance < PLATFORM_REACH_RANGE) {
                shouldJump = true;
            }
            // Priority 3: Player nearby and elevated
            else if (horizontallyClose && verticalDifference > 100f) {
                shouldJump = true;
            }
            
            if (shouldJump) {
                velY = jumpPower;
                grounded = false;
                smartJumpCooldown = SMART_JUMP_COOLDOWN / aggressionMultiplier;
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
            boolean reachable = p.bounds.y - bossTop < 300f;
            boolean playerOnPlatform = Math.abs(player.bounds.y - (p.bounds.y + p.bounds.height)) < 25f;
            boolean playerOverlapsPlatform = player.bounds.x < p.bounds.x + p.bounds.width &&
                                             player.bounds.x + player.bounds.width > p.bounds.x;
            
            if (platformAboveBoss && reachable && playerOnPlatform && playerOverlapsPlatform) {
                return true;
            }
        }
        return false;
    }

    private void updatePhase() {
        float healthPercent = health / maxHealth;

        // Transition to Phase 2 at 50% health
        if (healthPercent <= 0.5f && currentPhase == 1) {
            currentPhase = 2;
            currentSpeed = PHASE2_SPEED;
            shootCooldown = PHASE2_COOLDOWN;
            shootTimer = shootCooldown;
            phaseTransitionTimer = PHASE_TRANSITION_DURATION;
            Gdx.app.log("Boss", "PHASE 2 - Spread Attack!");
        }
        // Transition to Phase 3 at 25% health
        else if (healthPercent <= 0.25f && currentPhase == 2) {
            currentPhase = 3;
            currentSpeed = PHASE3_SPEED;
            shootCooldown = PHASE3_COOLDOWN;
            shootTimer = shootCooldown;
            phaseTransitionTimer = PHASE_TRANSITION_DURATION;
            Gdx.app.log("Boss", "PHASE 3 - Fan Barrage!");
        }
    }

    private void shootPattern(Array<EnemyBullet> bullets, Pool<EnemyBullet> bulletPool, Player player) {
        // Calculate base angle toward player
        float dx = player.bounds.x + player.bounds.width / 2 - (bounds.x + bounds.width / 2);
        float dy = player.bounds.y + player.bounds.height / 2 - (bounds.y + bounds.height / 2);
        float baseAngle = (float) Math.toDegrees(Math.atan2(dy, dx));

        if (currentPhase == 1) {
            // Phase 1: Single bullet aimed at player
            spawnBullet(bullets, bulletPool, baseAngle);
            Gdx.app.log("Boss", "Single shot!");
        } else if (currentPhase == 2) {
            // Phase 2: 3-bullet spread (-20°, 0°, +20°)
            for (int i = 0; i < PHASE2_BULLET_COUNT; i++) {
                float angleOffset = (i - 1) * PHASE2_SPREAD_ANGLE; // -20, 0, 20
                spawnBullet(bullets, bulletPool, baseAngle + angleOffset);
            }
            Gdx.app.log("Boss", "Spread attack!");
        } else if (currentPhase == 3) {
            // Phase 3: 5-bullet fan (-30°, -15°, 0°, +15°, +30°)
            for (int i = 0; i < PHASE3_BULLET_COUNT; i++) {
                float angleOffset = (i - 2) * 15f; // -30, -15, 0, 15, 30
                spawnBullet(bullets, bulletPool, baseAngle + angleOffset);
            }
            Gdx.app.log("Boss", "Fan barrage!");
        }
    }

    private void spawnBullet(Array<EnemyBullet> bullets, Pool<EnemyBullet> bulletPool, float angle) {
        EnemyBullet bullet = bulletPool.obtain();
        float spawnX = bounds.x + bounds.width / 2 - 4; // Center of boss
        float spawnY = bounds.y + bounds.height / 2 - 4;
        bullet.init(spawnX, spawnY, angle, BULLET_SPEED, BULLET_DAMAGE, enemyBulletTex);
        bullets.add(bullet);
    }

    private void shootBigUpwardBullet(Array<EnemyBullet> bullets, Pool<EnemyBullet> bulletPool) {
        // Fire ONE massive bullet straight up
        EnemyBullet bullet = bulletPool.obtain();
        float spawnX = bounds.x + bounds.width / 2 - BIG_BULLET_WIDTH / 2; // Center of boss
        float spawnY = bounds.y + bounds.height / 2 - BIG_BULLET_HEIGHT / 2;
        bullet.init(spawnX, spawnY, UPWARD_SHOT_BASE_ANGLE, BIG_BULLET_SPEED, BIG_BULLET_DAMAGE, enemyBulletTex,
                BIG_BULLET_WIDTH, BIG_BULLET_HEIGHT);
        bullets.add(bullet);
        Gdx.app.log("Boss", "Fired BIG bullet upward with " + BIG_BULLET_DAMAGE + " damage!");
    }

    @Override
    protected Texture getCurrentTexture() {
        // Priority: Upward shot warning > Phase transition flash > Normal
        if (isUpwardShotWarning && shouldUpwardShotFlash) {
            return upwardShotFlashTexture;
        }
        if (phaseTransitionTimer > 0 && shouldFlash) {
            return flashTexture;
        }
        return texture;
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
    }
}
