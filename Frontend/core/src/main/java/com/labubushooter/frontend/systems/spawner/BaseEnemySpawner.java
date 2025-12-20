package com.labubushooter.frontend.systems.spawner;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.labubushooter.frontend.objects.CommonEnemy;
import com.labubushooter.frontend.objects.Player;

/**
 * Abstract base class for enemy spawners.
 * Provides common spawning logic with Template Method pattern.
 * 
 * Design Patterns:
 * - Template Method: Defines spawning algorithm skeleton, subclasses customize
 * - Strategy: Concrete spawners customize spawn behavior per level
 * 
 * SOLID Principles:
 * - Single Responsibility: Handles enemy spawning only
 * - Open/Closed: Extend for new level spawners without modifying base
 * - Liskov Substitution: All spawners interchangeable via IEnemySpawner
 */
public abstract class BaseEnemySpawner implements IEnemySpawner {
    
    // ==================== SPAWN CONSTANTS ====================
    /** Buffer distance from camera edge for enemy spawning */
    protected static final float SPAWN_BUFFER = 200f;
    
    /** Minimum safe distance from player for enemy spawning */
    protected static final float PLAYER_SAFETY_ZONE = 300f;
    
    /** Maximum spawn attempts before fallback */
    protected static final int MAX_SPAWN_ATTEMPTS = 20;
    
    /** Default spawn Y position (above ground) */
    protected static final float SPAWN_Y = 150f;
    
    // ==================== SPAWN STATE ====================
    protected final Random random;
    protected long lastSpawnTime;
    protected long nextSpawnDelay;
    
    public BaseEnemySpawner() {
        this.random = new Random();
        this.lastSpawnTime = TimeUtils.nanoTime();
        this.nextSpawnDelay = calculateNextSpawnDelay();
    }
    
    // ==================== TEMPLATE METHOD IMPLEMENTATION ====================
    
    @Override
    public void spawnInitialEnemies(Pool<CommonEnemy> enemyPool, Array<CommonEnemy> activeEnemies,
                                    Player player, int level) {
        if (!isActive()) return;
        
        float[] positions = getInitialSpawnPositions();
        for (float spawnX : positions) {
            CommonEnemy enemy = enemyPool.obtain();
            enemy.init(spawnX, player, level);
            activeEnemies.add(enemy);
            Gdx.app.log("EnemySpawner", "Initial spawn at X: " + spawnX);
        }
    }
    
    @Override
    public boolean trySpawnEnemy(Pool<CommonEnemy> enemyPool, Array<CommonEnemy> activeEnemies,
                                  Player player, int level, float levelWidth,
                                  float cameraX, float viewportWidth) {
        if (!isActive()) return false;
        if (activeEnemies.size >= getMaxEnemies()) return false;
        
        // Check spawn timing
        long currentTime = TimeUtils.nanoTime();
        if (currentTime - lastSpawnTime < nextSpawnDelay) return false;
        
        // Calculate spawn position
        float cameraLeft = cameraX - viewportWidth / 2;
        float cameraRight = cameraX + viewportWidth / 2;
        float spawnX = calculateSpawnPosition(player, levelWidth, cameraLeft, cameraRight);
        
        if (spawnX < 0) return false; // Invalid spawn position
        
        // Spawn enemy
        CommonEnemy enemy = enemyPool.obtain();
        enemy.init(spawnX, player, level);
        activeEnemies.add(enemy);
        
        // Reset spawn timer
        lastSpawnTime = currentTime;
        nextSpawnDelay = calculateNextSpawnDelay();
        
        Gdx.app.log("EnemySpawner", "Spawned at X: " + spawnX + 
                   " | Active: " + activeEnemies.size + "/" + getMaxEnemies());
        
        return true;
    }
    
    @Override
    public void resetSpawnTimer() {
        lastSpawnTime = TimeUtils.nanoTime();
        nextSpawnDelay = calculateNextSpawnDelay();
    }
    
    // ==================== SPAWN POSITION CALCULATION ====================
    
    /**
     * Calculates a valid spawn position for an enemy.
     * Spawns enemy outside camera view and away from player.
     * 
     * Template Method hook - can be overridden by subclasses.
     * 
     * @param player Target player
     * @param levelWidth Level width boundary
     * @param cameraLeft Left edge of camera view
     * @param cameraRight Right edge of camera view
     * @return X position for spawn, or -1 if invalid
     */
    protected float calculateSpawnPosition(Player player, float levelWidth,
                                           float cameraLeft, float cameraRight) {
        float spawnX;
        int attempts = 0;
        
        do {
            boolean spawnLeft = random.nextBoolean();
            
            if (spawnLeft) {
                spawnX = cameraLeft - SPAWN_BUFFER - random.nextFloat() * 100f;
                if (spawnX < 0) {
                    spawnX = cameraRight + SPAWN_BUFFER + random.nextFloat() * 100f;
                }
            } else {
                spawnX = cameraRight + SPAWN_BUFFER + random.nextFloat() * 100f;
                if (spawnX > levelWidth - 100f) {
                    spawnX = cameraLeft - SPAWN_BUFFER - random.nextFloat() * 100f;
                }
            }
            
            attempts++;
            
            if (attempts >= MAX_SPAWN_ATTEMPTS) {
                // Fallback: spawn at opposite end of level from player
                if (player.bounds.x < levelWidth / 2) {
                    spawnX = levelWidth - 200f;
                } else {
                    spawnX = 100f;
                }
                break;
            }
        } while (Math.abs(spawnX - player.bounds.x) < PLAYER_SAFETY_ZONE ||
                 (spawnX >= cameraLeft && spawnX <= cameraRight));
        
        // Final boundary check
        if (spawnX < 50f) spawnX = 50f;
        if (spawnX > levelWidth - 100f) spawnX = levelWidth - 100f;
        
        return spawnX;
    }
    
    /**
     * Calculate next spawn delay with randomization.
     * 
     * @return Delay in nanoseconds
     */
    protected long calculateNextSpawnDelay() {
        long minDelay = getMinSpawnDelay();
        long maxDelay = getMaxSpawnDelay();
        return minDelay + (long)(random.nextFloat() * (maxDelay - minDelay));
    }
}
