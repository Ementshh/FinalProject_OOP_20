package com.labubushooter.frontend.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.labubushooter.frontend.animation.WalkingAnimation;

/**
 * Common enemy entity with simple homing behavior and walking animation.
 * 
 * Design Patterns:
 * - Object Pool: Implements Pool.Poolable for efficient memory management
 * - Composition: Uses WalkingAnimation for animation behavior
 * 
 * SOLID Principles:
 * - Single Responsibility: Handles enemy entity logic
 * - Dependency Inversion: Depends on AnimationStrategy abstraction
 */
public class CommonEnemy implements Pool.Poolable {
    public Rectangle collider;
    public Rectangle bounds;
    public boolean spawned;
    public float health;
    public float maxHealth;
    public boolean grounded = false;

    // Target reference
    public Player target;
    private float velocityX;
    private float velocityY = 0;
    private float speed;
    private final WalkingAnimation animation;
    private float damageAmount;
    
    // Factory multipliers
    private float speedMultiplier = 1.0f;
    private float healthMultiplier = 1.0f;

    // Damage System
    private long lastDamageTime;
    private static final long DAMAGE_COOLDOWN = 1000000000L; // 1 second in nanoseconds

    // Jump System
    private static final float JUMP_POWER = 450f;
    private static final float JUMP_COOLDOWN = 1.5f;
    private static final float PLATFORM_DETECTION_RANGE = 150f;
    private float jumpCooldownTimer = 0.3f;

    // Constants
    private static final float GRAVITY = -900f;
    private static final float WIDTH = 60f;
    private static final float HEIGHT = 90f;
    private static final float SPAWN_Y = 300f;
    private static final float BASE_SPEED = 120f;

    public CommonEnemy(Texture frame1, Texture frame2) {
        this.animation = new WalkingAnimation(frame1, frame2);
        this.collider = new Rectangle(0, SPAWN_Y, WIDTH, HEIGHT);
        this.bounds = this.collider;
        this.spawned = false;
        this.health = 10f;
        this.maxHealth = 10f;
        this.damageAmount = 1f;
        this.speed = BASE_SPEED;
        this.velocityX = 0;
        this.velocityY = 0;
        this.lastDamageTime = 0;
    }

    public void init(float x, Player target, int level) {
        this.collider.setPosition(x, SPAWN_Y);
        this.target = target;
        this.jumpCooldownTimer = 0f;

        // Set health, damage, and speed based on level
        switch (level) {
            case 1:
                this.maxHealth = 10f;
                this.damageAmount = 1f;
                this.speed = BASE_SPEED * 1.2f;
                break;
            case 2:
                this.maxHealth = 15f;
                this.damageAmount = 1.5f;
                this.speed = BASE_SPEED * 1.4f;
                break;
            case 4:
                this.maxHealth = 20f;
                this.damageAmount = 1.5f;
                this.speed = BASE_SPEED * 1.6f;
                break;
            default:
                this.maxHealth = 10f;
                this.damageAmount = 1f;
                this.speed = BASE_SPEED;
                break;
        }

        this.health = this.maxHealth * healthMultiplier;
        this.spawned = true;
        this.velocityX = 0;
        this.velocityY = 0;
        this.grounded = false;
        this.lastDamageTime = TimeUtils.nanoTime();

        Gdx.app.log("Enemy", "Spawned at level " + level + " - HP: " + health +
                ", Damage: " + damageAmount + ", Speed: " + speed);
    }

    public void update(float delta, Array<Platform> platforms, Array<Ground> grounds) {
        if (!spawned || health <= 0) return;

        // Update animation
        animation.update(delta);
        animation.setFacingLeft(velocityX < 0);

        // Update jump cooldown
        if (jumpCooldownTimer > 0) {
            jumpCooldownTimer -= delta;
        }

        // Always chase player (homing behavior)
        moveTowardsPlayer();
        
        // Smart jump to reach player
        checkAndPerformSmartJump(platforms);

        // Apply gravity
        velocityY += GRAVITY * delta;

        // Apply movement
        collider.x += velocityX * delta;
        collider.y += velocityY * delta;

        // Boundary check (X-axis)
        if (collider.x < 0) collider.x = 0;
        if (collider.x + collider.width > Player.LEVEL_WIDTH) {
            collider.x = Player.LEVEL_WIDTH - collider.width;
        }

        // Platform collision
        grounded = false;
        for (Platform p : platforms) {
            if (collider.overlaps(p.bounds)) {
                if (velocityY < 0 && collider.y + collider.height / 2 > p.bounds.y + p.bounds.height) {
                    collider.y = p.bounds.y + p.bounds.height;
                    velocityY = 0;
                    grounded = true;
                }
            }
        }

        // Ground collision
        for (Ground g : grounds) {
            if (collider.overlaps(g.bounds)) {
                if (velocityY < 0 && collider.y + collider.height / 2 > g.bounds.y + g.bounds.height) {
                    collider.y = g.bounds.y + g.bounds.height;
                    velocityY = 0;
                    grounded = true;
                }
            }
        }

        // Reset if fell below ground
        if (collider.y < 0) {
            spawned = false;
        }

        // Damage player if touching
        if (target != null && collider.overlaps(target.bounds)) {
            long currentTime = TimeUtils.nanoTime();
            if (currentTime - lastDamageTime > DAMAGE_COOLDOWN) {
                target.takeDamage(damageAmount);
                lastDamageTime = currentTime;
            }
        }
    }
    
    /**
     * Moves enemy towards player - always active homing behavior.
     */
    private void moveTowardsPlayer() {
        if (target == null) return;
        
        float targetCenterX = target.bounds.x + target.bounds.width / 2f;
        float enemyCenterX = collider.x + collider.width / 2f;
        float directionX = targetCenterX - enemyCenterX;

        if (Math.abs(directionX) > 5f) {
            float normalizedDirX = Math.signum(directionX);
            velocityX = normalizedDirX * speed * speedMultiplier;
        } else {
            velocityX = 0;
        }
    }
    
    /**
     * Smart jump logic to reach player on platforms.
     */
    private void checkAndPerformSmartJump(Array<Platform> platforms) {
        if (target == null || !grounded || jumpCooldownTimer > 0) return;
        
        float playerCenterY = target.bounds.y + target.bounds.height / 2f;
        float enemyCenterY = collider.y + collider.height / 2f;
        float playerCenterX = target.bounds.x + target.bounds.width / 2f;
        float enemyCenterX = collider.x + collider.width / 2f;
        
        // Check if player is above
        boolean playerAbove = playerCenterY > enemyCenterY + 30f;
        // Check if horizontally close
        boolean horizontallyClose = Math.abs(playerCenterX - enemyCenterX) < PLATFORM_DETECTION_RANGE;
        // Check if platform above
        boolean platformAbove = isPlatformAbove(platforms);
        
        if (playerAbove && horizontallyClose && platformAbove) {
            performJump();
        }
    }

    /**
     * Checks if there's a platform above the enemy that could be jumped to.
     */
    private boolean isPlatformAbove(Array<Platform> platforms) {
        float enemyTop = collider.y + collider.height;
        float enemyCenterX = collider.x + collider.width / 2f;
        
        for (Platform p : platforms) {
            // Platform is above enemy
            boolean platformAbove = p.bounds.y > enemyTop;
            // Platform is reachable (within jump height)
            boolean reachable = p.bounds.y - enemyTop < 200f; // Max jump height ~200 units
            // Enemy is horizontally aligned with platform
            boolean horizontallyAligned = enemyCenterX > p.bounds.x - 50 && 
                                          enemyCenterX < p.bounds.x + p.bounds.width + 50;
            
            if (platformAbove && reachable && horizontallyAligned) {
                return true;
            }
        }
        return false;
    }

    /**
     * Performs a jump.
     */
    private void performJump() {
        velocityY = JUMP_POWER;
        grounded = false;
        jumpCooldownTimer = JUMP_COOLDOWN;
    }

    public void takeDamage(float damage) {
        if (!spawned || health <= 0) return;
        
        health -= damage;
        
        if (health <= 0) {
            health = 0;
            spawned = false;
            Gdx.app.log("Enemy", "Enemy killed!");
        }
    }

    public void draw(SpriteBatch batch) {
        if (!spawned) return;
        Texture currentFrame = animation.getCurrentFrame();
        boolean flipX = animation.isFacingLeft();
        
        // Draw with mirroring based on facing direction
        // Using originX = 0 means flip happens around left edge
        batch.draw(currentFrame, 
                  collider.x, collider.y, 
                  0f, 0f,  // originX, originY (0,0 = flip around left-bottom corner)
                  collider.width, collider.height,
                  1f, 1f, 0f,  // scaleX, scaleY, rotation
                  0, 0,  // srcX, srcY
                  currentFrame.getWidth(), currentFrame.getHeight(),
                  flipX, false);  // flipX, flipY
    }

    @Override
    public void reset() {
        this.collider.setPosition(0, SPAWN_Y);
        this.spawned = false;
        this.health = 10f;
        this.maxHealth = 10f;
        this.damageAmount = 1f;
        this.speed = BASE_SPEED;
        this.speedMultiplier = 1.0f;
        this.healthMultiplier = 1.0f;
        this.target = null;
        this.velocityX = 0;
        this.velocityY = 0;
        this.grounded = false;
        this.lastDamageTime = 0;
        this.jumpCooldownTimer = 0f;
        this.animation.reset();
    }

    public boolean isActive() {
        return spawned;
    }

    public void setActive(boolean spawned) {
        this.spawned = spawned;
    }

    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
    }

    public void setHealthMultiplier(float multiplier) {
        this.healthMultiplier = multiplier;
        this.health = this.maxHealth * multiplier;
    }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    public float getHealthMultiplier() {
        return healthMultiplier;
    }
}