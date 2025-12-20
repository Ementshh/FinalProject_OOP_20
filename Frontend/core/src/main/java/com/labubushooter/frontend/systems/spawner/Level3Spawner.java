package com.labubushooter.frontend.systems.spawner;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.labubushooter.frontend.objects.CommonEnemy;
import com.labubushooter.frontend.objects.Player;

/**
 * Enemy spawner for Level 3 (Mini Boss level).
 * No regular enemy spawning - boss fight only.
 * 
 * Design Pattern: 
 * - Strategy Pattern (concrete strategy)
 * - Null Object Pattern (inactive spawner that does nothing)
 * 
 * SOLID Principles:
 * - Single Responsibility: Level 3 (no-spawn) behavior only
 * - Liskov Substitution: Safe to use where IEnemySpawner expected
 */
public class Level3Spawner extends BaseEnemySpawner {
    
    @Override
    public int getMaxEnemies() {
        return 0; // No regular enemies in boss level
    }
    
    @Override
    public long getMinSpawnDelay() {
        return Long.MAX_VALUE; // Never spawn
    }
    
    @Override
    public long getMaxSpawnDelay() {
        return Long.MAX_VALUE;
    }
    
    @Override
    public float[] getInitialSpawnPositions() {
        return new float[]{}; // No initial enemies
    }
    
    @Override
    public boolean isActive() {
        return false; // Boss level - no regular enemy spawning
    }
    
    @Override
    public void spawnInitialEnemies(Pool<CommonEnemy> enemyPool, Array<CommonEnemy> activeEnemies,
                                    Player player, int level) {
        // Null Object Pattern - do nothing for boss level
    }
    
    @Override
    public boolean trySpawnEnemy(Pool<CommonEnemy> enemyPool, Array<CommonEnemy> activeEnemies,
                                  Player player, int level, float levelWidth,
                                  float cameraX, float viewportWidth) {
        return false; // Never spawn in boss level
    }
}
