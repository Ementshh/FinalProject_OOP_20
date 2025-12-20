package com.labubushooter.frontend.systems.spawner;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.labubushooter.frontend.objects.CommonEnemy;
import com.labubushooter.frontend.objects.Player;

/**
 * Interface for enemy spawning strategies.
 * 
 * Design Pattern: Strategy Pattern
 * - Encapsulates spawning algorithm per level
 * - Allows runtime spawner switching
 * - Eliminates switch statements in spawning logic
 * 
 * SOLID Principles:
 * - Interface Segregation: Focused spawning contract
 * - Open/Closed: New spawners without modifying existing code
 * - Dependency Inversion: Depend on abstraction, not concrete spawners
 */
public interface IEnemySpawner {
    
    /**
     * Spawn initial enemies for the level.
     * Called once when level starts.
     * 
     * @param enemyPool Pool to obtain enemies from
     * @param activeEnemies Array to add spawned enemies to
     * @param player Target player for enemies
     * @param level Current level number
     */
    void spawnInitialEnemies(Pool<CommonEnemy> enemyPool, Array<CommonEnemy> activeEnemies, 
                             Player player, int level);
    
    /**
     * Handle continuous enemy spawning during gameplay.
     * Called every frame to check if spawn is needed.
     * 
     * @param enemyPool Pool to obtain enemies from
     * @param activeEnemies Array of currently active enemies
     * @param player Target player for enemies
     * @param level Current level number
     * @param levelWidth Width of current level
     * @param cameraX Current camera X position
     * @param viewportWidth Current viewport width
     * @return true if enemy was spawned
     */
    boolean trySpawnEnemy(Pool<CommonEnemy> enemyPool, Array<CommonEnemy> activeEnemies,
                          Player player, int level, float levelWidth,
                          float cameraX, float viewportWidth);
    
    /**
     * Get maximum enemies allowed for this spawner.
     * 
     * @return Maximum enemy count
     */
    int getMaxEnemies();
    
    /**
     * Get minimum spawn delay in nanoseconds.
     * 
     * @return Minimum delay between spawns
     */
    long getMinSpawnDelay();
    
    /**
     * Get maximum spawn delay in nanoseconds.
     * 
     * @return Maximum delay between spawns
     */
    long getMaxSpawnDelay();
    
    /**
     * Get initial spawn positions for this level.
     * 
     * @return Array of X positions for initial enemy spawns
     */
    float[] getInitialSpawnPositions();
    
    /**
     * Check if this spawner is active (should spawn enemies).
     * Boss levels may return false.
     * 
     * @return true if spawning is enabled
     */
    boolean isActive();
    
    /**
     * Reset spawn timer. Called when loading new level.
     */
    void resetSpawnTimer();
}
