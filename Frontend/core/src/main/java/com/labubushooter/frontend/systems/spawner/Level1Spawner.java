package com.labubushooter.frontend.systems.spawner;

/**
 * Enemy spawner for Level 1.
 * Tutorial-like experience with slower spawn rate and fewer max enemies.
 * 
 * Design Pattern: Strategy Pattern (concrete strategy)
 * 
 * SOLID Principles:
 * - Single Responsibility: Level 1 spawning configuration only
 * - Liskov Substitution: Fully interchangeable with other IEnemySpawner implementations
 */
public class Level1Spawner extends BaseEnemySpawner {
    
    // Level 1 spawn configuration
    private static final int MAX_ENEMIES = 6;
    private static final long MIN_SPAWN_DELAY = 3_000_000_000L; // 3 seconds (nanoseconds)
    private static final long MAX_SPAWN_DELAY = 4_000_000_000L; // 4 seconds (nanoseconds)
    
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
        // Spawn one enemy at start of level
        return new float[]{1200f};
    }
    
    @Override
    public boolean isActive() {
        return true; // Regular enemy spawning enabled
    }
}
