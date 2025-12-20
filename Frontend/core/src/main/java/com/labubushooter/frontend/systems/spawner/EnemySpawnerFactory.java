package com.labubushooter.frontend.systems.spawner;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating and managing enemy spawners.
 * 
 * Design Patterns:
 * - Factory Method: Creates appropriate spawner for each level
 * - Singleton: Single point of access for spawner management
 * - Flyweight: Caches and reuses spawner instances
 * 
 * SOLID Principles:
 * - Single Responsibility: Only creates and manages spawners
 * - Open/Closed: New levels = add new spawner class, register in initializeSpawners()
 * - Dependency Inversion: Returns IEnemySpawner abstraction
 */
public class EnemySpawnerFactory {
    
    private static EnemySpawnerFactory instance;
    private final Map<Integer, IEnemySpawner> spawnerCache;
    
    private EnemySpawnerFactory() {
        spawnerCache = new HashMap<>();
        initializeSpawners();
    }
    
    /**
     * Get singleton instance.
     * Thread-safe lazy initialization.
     * 
     * @return The singleton EnemySpawnerFactory instance
     */
    public static synchronized EnemySpawnerFactory getInstance() {
        if (instance == null) {
            instance = new EnemySpawnerFactory();
        }
        return instance;
    }
    
    /**
     * Initialize all level spawners.
     * Add new levels here when extending the game.
     * 
     * Open/Closed Principle: Adding new level only requires:
     * 1. Create new LevelXSpawner class
     * 2. Register here
     */
    private void initializeSpawners() {
        spawnerCache.put(1, new Level1Spawner());
        spawnerCache.put(2, new Level2Spawner());
        spawnerCache.put(3, new Level3Spawner()); // Mini Boss - no spawning
        spawnerCache.put(4, new Level4Spawner());
        spawnerCache.put(5, new Level5Spawner()); // Final Boss - no spawning
    }
    
    /**
     * Get spawner for specific level.
     * 
     * @param level Level number (1-5)
     * @return Appropriate spawner for the level, or Level1Spawner as default
     */
    public IEnemySpawner getSpawner(int level) {
        IEnemySpawner spawner = spawnerCache.get(level);
        if (spawner == null) {
            // Default fallback to Level 1 spawner
            return spawnerCache.get(1);
        }
        return spawner;
    }
    
    /**
     * Reset all spawner timers.
     * Called when restarting game or changing levels.
     */
    public void resetAllSpawners() {
        for (IEnemySpawner spawner : spawnerCache.values()) {
            spawner.resetSpawnTimer();
        }
    }
    
    /**
     * Reset spawner for specific level.
     * 
     * @param level Level number to reset
     */
    public void resetSpawnerForLevel(int level) {
        IEnemySpawner spawner = spawnerCache.get(level);
        if (spawner != null) {
            spawner.resetSpawnTimer();
        }
    }
    
    /**
     * Check if a level has active enemy spawning.
     * 
     * @param level Level number
     * @return true if the level spawns regular enemies
     */
    public boolean isSpawningActive(int level) {
        IEnemySpawner spawner = spawnerCache.get(level);
        return spawner != null && spawner.isActive();
    }
    
    /**
     * Reset singleton instance (for testing).
     */
    public static void reset() {
        instance = null;
    }
}
