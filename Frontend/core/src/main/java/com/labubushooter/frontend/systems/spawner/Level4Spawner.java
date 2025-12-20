package com.labubushooter.frontend.systems.spawner;

/**
 * Enemy spawner for Level 4.
 * High difficulty with fast spawns and more enemies.
 * 
 * Design Pattern: Strategy Pattern (concrete strategy)
 * 
 * SOLID Principles:
 * - Single Responsibility: Level 4 spawning configuration only
 * - Liskov Substitution: Fully interchangeable with other IEnemySpawner implementations
 */
public class Level4Spawner extends BaseEnemySpawner {
    
    // Level 4 spawn configuration - hardest regular level
    private static final int MAX_ENEMIES = 8;
    private static final long MIN_SPAWN_DELAY = 1_000_000_000L; // 1 second
    private static final long MAX_SPAWN_DELAY = 3_000_000_000L; // 3 seconds
    
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
        return new float[]{1400f};
    }
    
    @Override
    public boolean isActive() {
        return true;
    }
}
