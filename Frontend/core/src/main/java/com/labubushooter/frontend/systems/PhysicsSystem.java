package com.labubushooter.frontend.systems;

import com.badlogic.gdx.utils.Array;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.objects.EnemyBullet;
import com.labubushooter.frontend.objects.Platform;

/**
 * Centralized physics system for handling movement and physics calculations.
 * 
 * SOLID Principles Applied:
 * - Single Responsibility: Only handles physics-related calculations
 * - Open/Closed: New physics behaviors can be added without modifying existing code
 * - Dependency Inversion: Depends on abstractions (interfaces) not concrete classes
 * 
 * Design Pattern: System Pattern (ECS-inspired)
 * - Separates physics logic from entity logic
 * - Provides centralized physics constants and calculations
 */
public class PhysicsSystem {
    
    // ==================== PHYSICS CONSTANTS ====================
    /** Standard gravity acceleration (pixels/second²) */
    public static final float GRAVITY = -900f;
    
    /** Terminal velocity - maximum falling speed */
    public static final float TERMINAL_VELOCITY = -1500f;
    
    /** Ground friction coefficient */
    public static final float GROUND_FRICTION = 0.8f;
    
    /** Air resistance coefficient */
    public static final float AIR_RESISTANCE = 0.99f;
    
    // ==================== SINGLETON INSTANCE ====================
    private static PhysicsSystem instance;
    
    private PhysicsSystem() {
        // Private constructor for singleton
    }
    
    /**
     * Gets the singleton instance of PhysicsSystem.
     * Implements Singleton Pattern for centralized physics management.
     * 
     * @return The PhysicsSystem instance
     */
    public static PhysicsSystem getInstance() {
        if (instance == null) {
            instance = new PhysicsSystem();
        }
        return instance;
    }
    
    // ==================== PHYSICS CALCULATIONS ====================
    
    /**
     * Applies gravity to a velocity value.
     * 
     * @param currentVelocityY Current vertical velocity
     * @param delta Time delta in seconds
     * @return Updated vertical velocity with gravity applied
     */
    public float applyGravity(float currentVelocityY, float delta) {
        float newVelocity = currentVelocityY + GRAVITY * delta;
        return Math.max(newVelocity, TERMINAL_VELOCITY);
    }
    
    /**
     * Checks if an entity is grounded (on a platform or ground).
     * 
     * @param entityY Entity's Y position (bottom)
     * @param entityHeight Entity's height
     * @param groundY Ground Y level
     * @return True if entity is on ground level
     */
    public boolean isGrounded(float entityY, float entityHeight, float groundY) {
        return entityY <= groundY;
    }
    
    /**
     * Clamps a position within level bounds.
     * 
     * @param position Current position
     * @param minBound Minimum bound
     * @param maxBound Maximum bound
     * @return Clamped position
     */
    public float clampPosition(float position, float minBound, float maxBound) {
        return Math.max(minBound, Math.min(position, maxBound));
    }
    
    // ==================== BULLET PHYSICS ====================
    
    /**
     * Updates bullet physics and removes out-of-bounds bullets.
     * 
     * @param bullets Array of active bullets
     * @param platforms Array of platforms for collision
     * @param bulletPool Pool to return freed bullets
     * @param viewportHeight Current viewport height
     * @param delta Time delta
     */
    public void updateBullets(Array<Bullet> bullets, Array<Platform> platforms, 
                              com.badlogic.gdx.utils.Pool<Bullet> bulletPool,
                              float viewportHeight, float delta) {
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);
            
            boolean shouldRemove = false;
            
            // Check collision with platforms
            for (Platform p : platforms) {
                if (b.bounds.overlaps(p.bounds)) {
                    shouldRemove = true;
                    break;
                }
            }
            
            // Check if bullet traveled too far vertically
            if (!shouldRemove && b.isOutOfVerticalBounds(viewportHeight)) {
                shouldRemove = true;
            }
            
            if (shouldRemove) {
                bullets.removeIndex(i);
                bulletPool.free(b);
            }
        }
    }
    
    /**
     * Updates enemy bullet physics and removes out-of-bounds bullets.
     * 
     * @param enemyBullets Array of active enemy bullets
     * @param enemyBulletPool Pool to return freed bullets
     * @param levelWidth Current level width
     * @param viewportHeight Current viewport height
     * @param delta Time delta
     */
    public void updateEnemyBullets(Array<EnemyBullet> enemyBullets,
                                   com.badlogic.gdx.utils.Pool<EnemyBullet> enemyBulletPool,
                                   float levelWidth, float viewportHeight, float delta) {
        for (int i = enemyBullets.size - 1; i >= 0; i--) {
            EnemyBullet eb = enemyBullets.get(i);
            eb.update(delta);
            
            if (eb.isOutOfBounds(levelWidth, viewportHeight)) {
                enemyBullets.removeIndex(i);
                enemyBulletPool.free(eb);
            }
        }
    }
}
