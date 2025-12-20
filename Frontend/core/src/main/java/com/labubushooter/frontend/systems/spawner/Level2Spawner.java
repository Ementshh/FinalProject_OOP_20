package com.labubushooter.frontend.systems.spawner;

/**
 * Enemy spawner for Level 2.
 * Moderate difficulty with faster spawns and more enemies.
 * 
 * Design Pattern: Strategy Pattern (concrete strategy)
 * 
 * SOLID Principles:
 * - Single Responsibility: Level 2 spawning configuration only
 * - Liskov Substitution: Fully interchangeable with other IEnemySpawner implementations
 */
public class Level2Spawner extends BaseEnemySpawner {
    
    // Level 2 spawn configuration - harder than level 1
    private static final int MAX_ENEMIES = 7;
    private static final long MIN_SPAWN_DELAY = 2_000_000_000L; // 2 seconds
    private static final long MAX_SPAWN_DELAY = 4_000_000_000L; // 4 seconds
    
    @Override
    public int getMaxEnemies() {
        return MAX_ENEMIES;
    }
    
    @Override
    public long getMinSpawnDelay() {
        return MIN_SPAWN_DELAY;
    }
    
    @Override
    public long getMaxSpawnDelay() {
        return MAX_SPAWN_DELAY;
    }
    
    @Override
    public float[] getInitialSpawnPositions() {
        return new float[]{1000f};
    }
    
    @Override
    public boolean isActive() {
        return true;
    }
}
