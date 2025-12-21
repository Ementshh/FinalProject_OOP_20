package com.labubushooter.frontend.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.labubushooter.frontend.animation.FinalBossAnimationStrategy;
import com.labubushooter.frontend.patterns.bullets.BulletRenderStrategyFactory.BulletType;
import com.labubushooter.frontend.services.AssetManager;

public class FinalBoss extends BossEnemy {
    private static final float LEVEL_WIDTH = 2400f; // Level 4 width
    private static final float JUMP_POWER = 450f;
    
    // Visual scaling - boss appears 1.5x larger but collider remains original size
    private static final float VISUAL_SCALE_FACTOR = 1.5f;
    private final float originalBoundsWidth;
    private final float originalBoundsHeight;
    
    // Pre-calculated render offsets (optimization: calculated once in constructor)
    private final float renderOffsetX;
    private final float renderOffsetY;
    private final float scaledWidth;
    private final float scaledHeight;

    // Animation system
    private FinalBossAnimationStrategy animation;
    private boolean facingLeft;

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
    // Removed: shouldUpwardShotFlash, upwardShotFlashTimer, upwardShotFlashTexture (replaced by animation system)
    private float upwardShotWarningTimer;
    private float upwardShotStunTimer;
    private boolean isUpwardShotStunned;
    private static final float MIN_UPWARD_SHOT_TIME = 2.0f;
    private static final float MAX_UPWARD_SHOT_TIME = 5.0f;
    private static final float HEIGHT_THRESHOLD = 50f;
    // Removed: UPWARD_SHOT_FLASH_INTERVAL (no longer needed, animation system handles visuals)
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

    public FinalBoss(Texture tex, Texture flashTex, Texture bulletTex) {
        super(0, 0, 60, 100, 400, 8.0f, tex, flashTex);
        // Store original bounds dimensions for collider/physics (keep at original size)
        this.originalBoundsWidth = 60f;
        this.originalBoundsHeight = 100f;
        this.enemyBulletTex = bulletTex;
        // Removed: upwardShotFlashTexture parameter (animation system handles big attack visuals)
        this.currentSpeed = PHASE1_SPEED;
        this.shootCooldown = PHASE1_COOLDOWN;
        this.shootTimer = shootCooldown;
        
        // Pre-calculate render offsets and scaled dimensions (optimization)
        // These values never change, so calculate once instead of every frame
        this.scaledWidth = originalBoundsWidth * VISUAL_SCALE_FACTOR;   // 90
        this.scaledHeight = originalBoundsHeight * VISUAL_SCALE_FACTOR; // 150
        this.renderOffsetX = (originalBoundsWidth - scaledWidth) / 2f;  // -15 (centers horizontally)
        this.renderOffsetY = 0f;  // Bottom-align (sprite bottom = bounds bottom, prevents ground sinking)
        
        // Initialize animation system with textures from AssetManager
        AssetManager assetManager = AssetManager.getInstance();
        
        // Load Phase 1 textures with fallback to boss.png
        Texture phase1Walk1 = assetManager.getTexture(AssetManager.BOSS_PHASE1_WALK1);
        if (phase1Walk1 == null) {
            Gdx.app.error("FinalBoss", "Failed to load BOSS_PHASE1_WALK1, using fallback");
            phase1Walk1 = tex;
        }
        
        Texture phase1Walk2 = assetManager.getTexture(AssetManager.BOSS_PHASE1_WALK2);
        if (phase1Walk2 == null) {
            Gdx.app.error("FinalBoss", "Failed to load BOSS_PHASE1_WALK2, using fallback");
            phase1Walk2 = tex;
        }
        
        Texture phase1Jump = assetManager.getTexture(AssetManager.BOSS_PHASE1_JUMP);
        if (phase1Jump == null) {
            Gdx.app.error("FinalBoss", "Failed to load BOSS_PHASE1_JUMP, using fallback");
            phase1Jump = tex;
        }
        
        Texture phase1BigAttack = assetManager.getTexture(AssetManager.BOSS_PHASE1_BIGATTACK);
        if (phase1BigAttack == null) {
            Gdx.app.error("FinalBoss", "Failed to load BOSS_PHASE1_BIGATTACK, using fallback");
            phase1BigAttack = tex;
        }
        
        // Load Phase 2 textures with fallback to boss.png
        Texture phase2Walk1 = assetManager.getTexture(AssetManager.BOSS_PHASE2_WALK1);
        if (phase2Walk1 == null) {
            Gdx.app.error("FinalBoss", "Failed to load BOSS_PHASE2_WALK1, using fallback");
            phase2Walk1 = tex;
        }
        
        Texture phase2Walk2 = assetManager.getTexture(AssetManager.BOSS_PHASE2_WALK2);
        if (phase2Walk2 == null) {
            Gdx.app.error("FinalBoss", "Failed to load BOSS_PHASE2_WALK2, using fallback");
            phase2Walk2 = tex;
        }
        
        Texture phase2Jump = assetManager.getTexture(AssetManager.BOSS_PHASE2_JUMP);
        if (phase2Jump == null) {
            Gdx.app.error("FinalBoss", "Failed to load BOSS_PHASE2_JUMP, using fallback");
            phase2Jump = tex;
        }
        
        Texture phase2BigAttack = assetManager.getTexture(AssetManager.BOSS_PHASE2_BIGATTACK);
        if (phase2BigAttack == null) {
            Gdx.app.error("FinalBoss", "Failed to load BOSS_PHASE2_BIGATTACK, using fallback");
            phase2BigAttack = tex;
        }
        
        // Load Phase 3 textures with fallback to boss.png
        Texture phase3Walk1 = assetManager.getTexture(AssetManager.BOSS_PHASE3_WALK1);
        if (phase3Walk1 == null) {
            Gdx.app.error("FinalBoss", "Failed to load BOSS_PHASE3_WALK1, using fallback");
            phase3Walk1 = tex;
        }
        
        Texture phase3Walk2 = assetManager.getTexture(AssetManager.BOSS_PHASE3_WALK2);
        if (phase3Walk2 == null) {
            Gdx.app.error("FinalBoss", "Failed to load BOSS_PHASE3_WALK2, using fallback");
            phase3Walk2 = tex;
        }
        
        Texture phase3Jump = assetManager.getTexture(AssetManager.BOSS_PHASE3_JUMP);
        if (phase3Jump == null) {
            Gdx.app.error("FinalBoss", "Failed to load BOSS_PHASE3_JUMP, using fallback");
            phase3Jump = tex;
        }
        
        Texture phase3BigAttack = assetManager.getTexture(AssetManager.BOSS_PHASE3_BIGATTACK);
        if (phase3BigAttack == null) {
            Gdx.app.error("FinalBoss", "Failed to load BOSS_PHASE3_BIGATTACK, using fallback");
            phase3BigAttack = tex;
        }
        
        // Initialize animation strategy with all textures
        this.animation = new FinalBossAnimationStrategy(
            phase1Walk1, phase1Walk2, phase1Jump, phase1BigAttack,
            phase2Walk1, phase2Walk2, phase2Jump, phase2BigAttack,
            phase3Walk1, phase3Walk2, phase3Jump, phase3BigAttack,
            tex // fallback texture
        );
        
        this.facingLeft = false;
        
        // Validate asset dimensions to ensure they match expectations
        validateAssetDimensions(phase1Walk1, phase1Walk2, phase1Jump, phase1BigAttack,
                               phase2Walk1, phase2Walk2, phase2Jump, phase2BigAttack,
                               phase3Walk1, phase3Walk2, phase3Jump, phase3BigAttack);
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
        // Removed: shouldUpwardShotFlash, upwardShotFlashTimer initialization (animation system handles visuals)
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
            // Update animation state even when dead
            animation.setState(currentPhase, isUpwardShotWarning, grounded);
            animation.update(delta);
            return;
        }
        
        // Update animation state early to ensure it's set before any early returns
        // This fixes the bug where big attack frame wasn't visible during warning phase
        animation.setState(currentPhase, isUpwardShotWarning, grounded);
        animation.update(delta);
        
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

        // 2. Warning Phase (Boss stops jumping, animation system shows big attack texture)
        if (isUpwardShotWarning) {
            upwardShotWarningTimer -= delta;

            // Removed: Flash effect logic (animation system now handles big attack visual via bigattack texture)

            // End warning, execute upward shot
            if (upwardShotWarningTimer <= 0) {
                isUpwardShotWarning = false;
                // Removed: shouldUpwardShotFlash = false (no longer needed)
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
            
            // Update facing direction based on movement
            boolean newFacingLeft = directionX < 0;
            if (newFacingLeft != facingLeft) {
                facingLeft = newFacingLeft;
                animation.setFacingLeft(facingLeft);
            }
        }

        // Smart jump logic - enhanced pursuit
        checkAndPerformSmartJump(player, platforms);

        // Apply gravity and platform collision
        applyGravityAndCollision(delta, platforms, grounds);

        // Animation state already updated at the beginning of this method
        // This ensures it's set even when early returns occur (e.g., during warning phase)

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
            animation.setPhase(currentPhase);
            Gdx.app.log("Boss", "PHASE 2 - Spread Attack!");
        }
        // Transition to Phase 3 at 25% health
        else if (healthPercent <= 0.25f && currentPhase == 2) {
            currentPhase = 3;
            currentSpeed = PHASE3_SPEED;
            shootCooldown = PHASE3_COOLDOWN;
            shootTimer = shootCooldown;
            phaseTransitionTimer = PHASE_TRANSITION_DURATION;
            animation.setPhase(currentPhase);
            Gdx.app.log("Boss", "PHASE 3 - Fan Barrage!");
        }
    }

    private void shootPattern(Array<EnemyBullet> bullets, Pool<EnemyBullet> bulletPool, Player player) {
        // Calculate base angle toward player
        float dx = player.bounds.x + player.bounds.width / 2 - (bounds.x + bounds.width / 2);
        float dy = player.bounds.y + player.bounds.height / 2 - (bounds.y + bounds.height / 2);
        float baseAngle = (float) Math.toDegrees(Math.atan2(dy, dx));

        if (currentPhase == 1) {
            // Phase 1: Single directional bullet aimed at player (32x32)
            spawnBulletWithType(bullets, bulletPool, baseAngle, BulletType.PHASE1_SINGLE, 32, 32);
            Gdx.app.log("Boss", "Single shot!");
        } else if (currentPhase == 2) {
            // Phase 2: 3-bullet spread with slow spin (20x20) (-20°, 0°, +20°)
            for (int i = 0; i < PHASE2_BULLET_COUNT; i++) {
                float angleOffset = (i - 1) * PHASE2_SPREAD_ANGLE; // -20, 0, 20
                spawnBulletWithType(bullets, bulletPool, baseAngle + angleOffset, BulletType.PHASE23_MULTI, 20, 20);
            }
            Gdx.app.log("Boss", "Spread attack!");
        } else if (currentPhase == 3) {
            // Phase 3: 5-bullet fan with slow spin (20x20) (-30°, -15°, 0°, +15°, +30°)
            for (int i = 0; i < PHASE3_BULLET_COUNT; i++) {
                float angleOffset = (i - 2) * 15f; // -30, -15, 0, 15, 30
                spawnBulletWithType(bullets, bulletPool, baseAngle + angleOffset, BulletType.PHASE23_MULTI, 20, 20);
            }
            Gdx.app.log("Boss", "Fan barrage!");
        }
    }

    /**
     * Spawns a bullet with a specific rendering strategy type.
     * 
     * @param bullets Active bullets array
     * @param bulletPool Bullet pool for object reuse
     * @param angle Firing angle in degrees
     * @param type Bullet type (determines visual rendering)
     * @param width Bullet width
     * @param height Bullet height
     */
    private void spawnBulletWithType(Array<EnemyBullet> bullets, Pool<EnemyBullet> bulletPool, 
                                     float angle, BulletType type, float width, float height) {
        EnemyBullet bullet = bulletPool.obtain();
        float spawnX = bounds.x + bounds.width / 2 - width / 2; // Center of boss
        float spawnY = bounds.y + bounds.height / 2 - height / 2;
        bullet.init(spawnX, spawnY, angle, BULLET_SPEED, BULLET_DAMAGE, type, width, height);
        bullets.add(bullet);
    }

    /**
     * Legacy spawn method for backward compatibility (not used by FinalBoss anymore).
     */
    private void spawnBullet(Array<EnemyBullet> bullets, Pool<EnemyBullet> bulletPool, float angle) {
        EnemyBullet bullet = bulletPool.obtain();
        float spawnX = bounds.x + bounds.width / 2 - 4; // Center of boss
        float spawnY = bounds.y + bounds.height / 2 - 4;
        bullet.init(spawnX, spawnY, angle, BULLET_SPEED, BULLET_DAMAGE, enemyBulletTex);
        bullets.add(bullet);
    }

    private void shootBigUpwardBullet(Array<EnemyBullet> bullets, Pool<EnemyBullet> bulletPool) {
        // Fire ONE massive bullet straight up with fast spin
        EnemyBullet bullet = bulletPool.obtain();
        float spawnX = bounds.x + bounds.width / 2 - BIG_BULLET_WIDTH / 2; // Center of boss
        float spawnY = bounds.y + bounds.height / 2 - BIG_BULLET_HEIGHT / 2;
        bullet.init(spawnX, spawnY, UPWARD_SHOT_BASE_ANGLE, BIG_BULLET_SPEED, BIG_BULLET_DAMAGE, 
                    BulletType.BIG_ATTACK, BIG_BULLET_WIDTH, BIG_BULLET_HEIGHT);
        bullets.add(bullet);
        Gdx.app.log("Boss", "Fired BIG bullet upward with " + BIG_BULLET_DAMAGE + " damage!");
    }

    @Override
    protected Texture getCurrentTexture() {
        return animation.getCurrentFrame();
    }

    @Override
    protected boolean isFacingLeft() {
        return facingLeft;
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Draw sprite with visual scaling (1.5x larger)
        // Collider remains at original size for gameplay balance
        Texture currentTexture = getCurrentTexture();
        boolean flipX = isFacingLeft();
        
        // Use pre-calculated offsets and dimensions (optimized - no per-frame calculation)
        // renderOffsetX: -15 (centers sprite horizontally on bounds)
        // renderOffsetY: 0 (bottom-aligns sprite with bounds to prevent ground sinking)
        
        // Draw with mirroring based on facing direction and visual scaling
        batch.draw(currentTexture, 
                  bounds.x + renderOffsetX, bounds.y + renderOffsetY,  // Position: centered horizontally, bottom-aligned vertically
                  0f, 0f,  // originX, originY (0,0 = flip around left-bottom corner)
                  scaledWidth, scaledHeight,  // Use pre-calculated scaled dimensions
                  1f, 1f, 0f,  // scaleX, scaleY, rotation
                  0, 0,  // srcX, srcY
                  currentTexture.getWidth(), currentTexture.getHeight(),
                  flipX, false);  // flipX, flipY

        // Draw health bar above scaled sprite
        drawScaledHealthBar(batch, scaledWidth);
    }
    
    /**
     * Draws the health bar scaled appropriately for the visual boss size.
     * Health bar width matches the scaled visual width.
     * 
     * @param batch Sprite batch for drawing
     * @param scaledWidth The scaled width of the boss visual
     */
    private void drawScaledHealthBar(SpriteBatch batch, float scaledWidth) {
        float barWidth = scaledWidth;
        float barHeight = 5f;
        // Position health bar above the scaled visual sprite
        float barX = bounds.x + (originalBoundsWidth - scaledWidth) / 2f;
        float barY = bounds.y + originalBoundsHeight + 10f;

        float healthPercent = health / maxHealth;

        // Draw background (black with transparency)
        batch.setColor(0f, 0f, 0f, 0.7f);
        batch.draw(BossEnemy.getHealthBarBg(), barX - 1, barY - 1, barWidth + 2, barHeight + 2);

        // Draw border (white)
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(BossEnemy.getHealthBarBorder(), barX - 1, barY - 1, barWidth + 2, 1); // Bottom
        batch.draw(BossEnemy.getHealthBarBorder(), barX - 1, barY + barHeight, barWidth + 2, 1); // Top
        batch.draw(BossEnemy.getHealthBarBorder(), barX - 1, barY, 1, barHeight); // Left
        batch.draw(BossEnemy.getHealthBarBorder(), barX + barWidth, barY, 1, barHeight); // Right

        // Draw health bar (green to red gradient based on health)
        float red = 1f - healthPercent;
        float green = healthPercent;
        batch.setColor(red, green, 0f, 1f);
        batch.draw(BossEnemy.getHealthBarFill(), barX, barY, barWidth * healthPercent, barHeight);

        // Reset color
        batch.setColor(1f, 1f, 1f, 1f);
    }
    
    /**
     * Validates that all boss asset dimensions match expected values.
     * Logs warnings if dimensions don't match, which could indicate:
     * - Assets need to be resized
     * - VISUAL_SCALE_FACTOR needs adjustment
     * - Transparent padding in assets affecting positioning
     * 
     * Expected dimensions are based on originalBounds * VISUAL_SCALE_FACTOR.
     * 
     * @param phase1Walk1 Phase 1 walk frame 1 texture
     * @param phase1Walk2 Phase 1 walk frame 2 texture
     * @param phase1Jump Phase 1 jump texture
     * @param phase1BigAttack Phase 1 big attack texture
     * @param phase2Walk1 Phase 2 walk frame 1 texture
     * @param phase2Walk2 Phase 2 walk frame 2 texture
     * @param phase2Jump Phase 2 jump texture
     * @param phase2BigAttack Phase 2 big attack texture
     * @param phase3Walk1 Phase 3 walk frame 1 texture
     * @param phase3Walk2 Phase 3 walk frame 2 texture
     * @param phase3Jump Phase 3 jump texture
     * @param phase3BigAttack Phase 3 big attack texture
     */
    private void validateAssetDimensions(Texture phase1Walk1, Texture phase1Walk2, Texture phase1Jump, Texture phase1BigAttack,
                                        Texture phase2Walk1, Texture phase2Walk2, Texture phase2Jump, Texture phase2BigAttack,
                                        Texture phase3Walk1, Texture phase3Walk2, Texture phase3Jump, Texture phase3BigAttack) {
        // Expected dimensions based on visual scale
        int expectedWidth = (int) scaledWidth;   // 90
        int expectedHeight = (int) scaledHeight; // 150
        
        Gdx.app.log("FinalBoss", "=== Asset Dimension Validation ===");
        Gdx.app.log("FinalBoss", "Expected dimensions: " + expectedWidth + "x" + expectedHeight);
        Gdx.app.log("FinalBoss", "Original bounds: " + (int)originalBoundsWidth + "x" + (int)originalBoundsHeight);
        Gdx.app.log("FinalBoss", "Visual scale factor: " + VISUAL_SCALE_FACTOR + "x");
        
        // Validate Phase 1 assets
        validateTextureDimension("Phase1Walk1", phase1Walk1, expectedWidth, expectedHeight);
        validateTextureDimension("Phase1Walk2", phase1Walk2, expectedWidth, expectedHeight);
        validateTextureDimension("Phase1Jump", phase1Jump, expectedWidth, expectedHeight);
        validateTextureDimension("Phase1BigAttack", phase1BigAttack, expectedWidth, expectedHeight);
        
        // Validate Phase 2 assets
        validateTextureDimension("Phase2Walk1", phase2Walk1, expectedWidth, expectedHeight);
        validateTextureDimension("Phase2Walk2", phase2Walk2, expectedWidth, expectedHeight);
        validateTextureDimension("Phase2Jump", phase2Jump, expectedWidth, expectedHeight);
        validateTextureDimension("Phase2BigAttack", phase2BigAttack, expectedWidth, expectedHeight);
        
        // Validate Phase 3 assets
        validateTextureDimension("Phase3Walk1", phase3Walk1, expectedWidth, expectedHeight);
        validateTextureDimension("Phase3Walk2", phase3Walk2, expectedWidth, expectedHeight);
        validateTextureDimension("Phase3Jump", phase3Jump, expectedWidth, expectedHeight);
        validateTextureDimension("Phase3BigAttack", phase3BigAttack, expectedWidth, expectedHeight);
        
        Gdx.app.log("FinalBoss", "=== Validation Complete ===");
    }
    
    /**
     * Validates a single texture's dimensions against expected values.
     * Logs info if dimensions match, warning if they don't.
     * 
     * @param name Asset name for logging
     * @param texture Texture to validate
     * @param expectedWidth Expected width in pixels
     * @param expectedHeight Expected height in pixels
     */
    private void validateTextureDimension(String name, Texture texture, int expectedWidth, int expectedHeight) {
        if (texture == null) {
            Gdx.app.log("FinalBoss", name + ": null (using fallback)");
            return;
        }
        
        int actualWidth = texture.getWidth();
        int actualHeight = texture.getHeight();
        
        if (actualWidth == expectedWidth && actualHeight == expectedHeight) {
            Gdx.app.log("FinalBoss", name + ": " + actualWidth + "x" + actualHeight + " ✓");
        } else {
            Gdx.app.log("FinalBoss", "WARNING: " + name + " dimensions mismatch!");
            Gdx.app.log("FinalBoss", "  Actual: " + actualWidth + "x" + actualHeight);
            Gdx.app.log("FinalBoss", "  Expected: " + expectedWidth + "x" + expectedHeight);
            Gdx.app.log("FinalBoss", "  Difference: " + (actualWidth - expectedWidth) + "x" + (actualHeight - expectedHeight));
            
            // Provide guidance based on the mismatch
            if (actualWidth < expectedWidth || actualHeight < expectedHeight) {
                Gdx.app.log("FinalBoss", "  → Asset is smaller than expected. Consider resizing asset or adjusting VISUAL_SCALE_FACTOR.");
            } else {
                Gdx.app.log("FinalBoss", "  → Asset is larger than expected. May have transparent padding or need resizing.");
            }
        }
    }
}
