package com.labubushooter.frontend.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.labubushooter.frontend.patterns.states.ChaseState;
import com.labubushooter.frontend.patterns.states.DeadState;
import com.labubushooter.frontend.patterns.states.EnemyState;
import com.labubushooter.frontend.patterns.states.IdleState;
import com.labubushooter.frontend.patterns.states.StunnedState;

/**
 * Common enemy entity with State Pattern support.
 * 
 * Design Patterns:
 * - State Pattern: Behavior changes based on current EnemyState
 * - Object Pool: Implements Pool.Poolable for efficient memory management
 * 
 * SOLID Principles:
 * - Single Responsibility: Handles enemy entity logic
 * - Open/Closed: Extensible via EnemyState implementations
 */
public class CommonEnemy implements Pool.Poolable {
    public Rectangle collider;
    public Rectangle bounds; // Alias for collider for consistency
    public boolean spawned;
    public float health;
    public float maxHealth;
    public boolean grounded = false;
    
    // State Pattern
    private EnemyState currentState;

    // Target reference (public for state access)
    public Player target;
    private float velocityX;
    private float velocityY = 0;
    private float speed;
    private Texture texture;
    private float damageAmount;
    
    // Factory multipliers
    private float speedMultiplier = 1.0f;
    private float healthMultiplier = 1.0f;

    // Damage System
    private long lastDamageTime;
    private static final long DAMAGE_COOLDOWN = 1000000000L; // 1 second in nanoseconds

    // Jump System
    private static final float JUMP_POWER = 500f;
    private static final float JUMP_THRESHOLD = 10f;
    private static final float JUMP_COOLDOWN = 0.8f; // Cooldown between jumps
    private static final float PLATFORM_DETECTION_RANGE = 150f; // Range to detect if player is on platform above
    private float jumpCooldownTimer = 0f;

    private static final float GRAVITY = -900f;
    private static final float WIDTH = 60f;
    private static final float HEIGHT = 90f;
    private static final float SPAWN_Y = 300f;

    // Base speed for multiplier
    private static final float BASE_SPEED = 120f;

    public CommonEnemy(Texture texture) {
        this.texture = texture;
        this.collider = new Rectangle(0, SPAWN_Y, WIDTH, HEIGHT);
        this.bounds = this.collider; // Alias
        this.spawned = false;
        this.health = 10f;
        this.maxHealth = 10f;
        this.damageAmount = 1f;
        this.speed = BASE_SPEED;
        this.velocityX = 0;
        this.velocityY = 0;
        this.lastDamageTime = 0;
        this.currentState = new IdleState();
    }

    public void init(float x, Player target, int level) {
        this.collider.setPosition(x, SPAWN_Y);
        this.target = target;
        this.jumpCooldownTimer = 0f; // Reset jump cooldown

        // Set health, damage, and speed based on level
        switch (level) {
            case 1:
                this.maxHealth = 10f;
                this.damageAmount = 1f;
                this.speed = BASE_SPEED * 1.2f; // 1.2x lebih cepat
                break;
            case 2:
                this.maxHealth = 15f;
                this.damageAmount = 1.5f;
                this.speed = BASE_SPEED * 1.4f; // 1.4x lebih cepat
                break;
            case 4:
                this.maxHealth = 20f;
                this.damageAmount = 1.5f;
                this.speed = BASE_SPEED * 1.6f; // 1.6x lebih cepat
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
        
        // Initialize state to Chase (active pursuit)
        setState(new ChaseState());

        Gdx.app.log("Enemy", "Spawned at level " + level + " - HP: " + health +
                ", Damage: " + damageAmount + ", Speed: " + speed);
    }

    public void update(float delta, Array<Platform> platforms, Array<Ground> grounds) {
        if (!spawned || target == null)
            return;
        
        // Update jump cooldown
        if (jumpCooldownTimer > 0) {
            jumpCooldownTimer -= delta;
        }
        
        // Update current state (State Pattern)
        if (currentState != null) {
            currentState.update(this, delta);
        }
        
        // Smart jump logic - check if should jump to reach player
        checkAndPerformSmartJump(platforms);

        // Apply horizontal movement
        collider.x += velocityX * delta;

        // Boundary check (X-axis)
        if (collider.x < 0)
            collider.x = 0;
        if (collider.x + collider.width > Player.LEVEL_WIDTH) {
            collider.x = Player.LEVEL_WIDTH - collider.width;
        }

        // Apply gravity (Y-axis)
        velocityY += GRAVITY * delta;
        collider.y += velocityY * delta;

        // Platform collision detection
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

        // Ground collision detection
        for (Ground g : grounds) {
            if (collider.overlaps(g.bounds)) {
                if (velocityY < 0 && collider.y + collider.height / 2 > g.bounds.y + g.bounds.height) {
                    collider.y = g.bounds.y + g.bounds.height;
                    velocityY = 0;
                    grounded = true;
                }
            }
        }

        // Reset position jika jatuh ke bawah
        if (collider.y < 0) {
            spawned = false;
        }

        // Damage player if touching (damage per second based on level)
        if (collider.overlaps(target.bounds)) {
            if (TimeUtils.nanoTime() - lastDamageTime > DAMAGE_COOLDOWN) {
                target.takeDamage(damageAmount);
                lastDamageTime = TimeUtils.nanoTime();
            }
        }
    }
    
    /**
     * Moves enemy towards player. Called by ChaseState.
     * Extracted method for State Pattern usage.
     */
    public void moveTowardsPlayer(float delta) {
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
        // Note: Jump logic is now handled in checkAndPerformSmartJump()
    }
    
    /**
     * Smart jump logic to reach player on platforms or above.
     * Checks multiple conditions:
     * 1. Player is above enemy (Y-axis difference)
     * 2. Player is horizontally aligned (X-axis overlap)
     * 3. Enemy is grounded and jump is off cooldown
     * 4. There's a platform above that player might be on
     */
    private void checkAndPerformSmartJump(Array<Platform> platforms) {
        if (target == null || !grounded || jumpCooldownTimer > 0) {
            return;
        }

        float enemyCenterX = collider.x + collider.width / 2f;
        float playerCenterX = target.bounds.x + target.bounds.width / 2f;
        float horizontalDistance = Math.abs(enemyCenterX - playerCenterX);
        float verticalDifference = target.bounds.y - collider.y;

        // Condition 1: Player is above enemy
        boolean playerIsAbove = verticalDifference > JUMP_THRESHOLD;
        
        // Condition 2: Horizontally aligned (within jump threshold range)
        boolean horizontallyAligned = horizontalDistance < collider.width * 2;
        
        // Condition 3: Player is significantly above (on platform)
        boolean playerOnPlatformAbove = verticalDifference > 50f && verticalDifference < 300f;
        
        // Condition 4: X-axis overlap but no Y collision (player directly above)
        boolean xAxisOverlap = (collider.x < target.bounds.x + target.bounds.width) && 
                               (collider.x + collider.width > target.bounds.x);
        boolean noYCollision = !collider.overlaps(target.bounds);
        
        // Condition 5: Check if there's a reachable platform above
        boolean platformAbove = isPlatformAbove(platforms);

        // Jump if:
        // - Player is above AND horizontally close (trying to reach player directly)
        // - OR X-axis overlap but no collision (player directly above)
        // - OR player is on a platform above and we're close enough horizontally
        if (playerIsAbove && grounded) {
            if (xAxisOverlap && noYCollision && playerOnPlatformAbove) {
                // Player directly above - priority jump
                performJump();
            } else if (horizontallyAligned && playerOnPlatformAbove) {
                // Player nearby and above
                performJump();
            } else if (platformAbove && horizontalDistance < PLATFORM_DETECTION_RANGE) {
                // Platform above, player might be there
                performJump();
            }
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
     * Performs a jump with cooldown reset.
     */
    private void performJump() {
        velocityY = JUMP_POWER;
        grounded = false;
        jumpCooldownTimer = JUMP_COOLDOWN;
    }
    
    /**
     * Performs attack action. Called by AttackState.
     */
    public void performAttack() {
        // Attack handled by collision in update()
        Gdx.app.log("EnemyAttack", "Enemy performing attack");
    }

    public void takeDamage(float damage) {
        // Ignore damage if already dead or not spawned
        if (!spawned || health <= 0) {
            return;
        }
        
        health -= damage;
        
        if (health <= 0) {
            // Immediately mark for removal - don't wait for animation
            health = 0;
            spawned = false; // This triggers removal in GameWorld.updateEnemies()
            
            // Set DeadState for any cleanup logic
            setState(new DeadState());
            
            Gdx.app.log("Enemy", "Enemy killed! Marked for immediate removal.");
        } else {
            // Only stun if not already stunned (prevent state spam from rapid fire)
            if (!(currentState instanceof StunnedState)) {
                setState(new StunnedState());
            }
        }
    }
    
    /**
     * Marks enemy for removal. Called by DeadState.
     */
    public void markForRemoval() {
        spawned = false;
    }

    public void draw(SpriteBatch batch) {
        if (!spawned)
            return;

        batch.draw(texture, collider.x, collider.y, collider.width, collider.height);
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
        this.currentState = new IdleState();
    }

    public boolean isActive() {
        return spawned;
    }

    public void setActive(boolean spawned) {
        this.spawned = spawned;
    }
    
    // ==================== STATE PATTERN METHODS ====================
    
    /**
     * Changes the current state.
     * State Pattern: Allows dynamic behavior changes.
     * 
     * @param newState The new state to transition to
     */
    public void setState(EnemyState newState) {
        if (currentState != null) {
            currentState.exit(this);
        }
        currentState = newState;
        if (currentState != null) {
            currentState.enter(this);
        }
    }
    
    /**
     * Gets the current state.
     * 
     * @return Current EnemyState
     */
    public EnemyState getState() {
        return currentState;
    }
    
    /**
     * Gets the current state name for debugging.
     * 
     * @return State name string
     */
    public String getStateName() {
        return currentState != null ? currentState.getName() : "NULL";
    }
    
    // ==================== FACTORY MULTIPLIER METHODS ====================
    
    /**
     * Sets speed multiplier. Used by EnemyFactory for enemy variants.
     * 
     * @param multiplier Speed multiplier (1.0 = normal)
     */
    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
    }
    
    /**
     * Sets health multiplier. Used by EnemyFactory for enemy variants.
     * 
     * @param multiplier Health multiplier (1.0 = normal)
     */
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