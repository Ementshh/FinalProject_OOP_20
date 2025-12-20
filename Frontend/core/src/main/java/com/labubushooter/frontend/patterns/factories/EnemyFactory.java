package com.labubushooter.frontend.patterns.factories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Pool;
import com.labubushooter.frontend.objects.CommonEnemy;
import com.labubushooter.frontend.objects.FinalBoss;
import com.labubushooter.frontend.objects.MiniBossEnemy;
import com.labubushooter.frontend.objects.Player;

/**
 * Factory for creating enemy instances.
 * 
 * Design Pattern: Factory Method Pattern
 * - Encapsulates enemy creation logic
 * - Provides type-safe enemy instantiation
 * - Supports Object Pool integration
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles enemy creation
 * - Open/Closed: New enemy types can be added via EnemyType enum
 * - Dependency Inversion: Depends on abstractions (EnemyType)
 */
public class EnemyFactory {
    
    // ==================== SINGLETON ====================
    private static EnemyFactory instance;
    
    private EnemyFactory() {
        // Private constructor for singleton
    }
    
    /**
     * Gets the singleton instance of EnemyFactory.
     * 
     * @return The EnemyFactory instance
     */
    public static EnemyFactory getInstance() {
        if (instance == null) {
            instance = new EnemyFactory();
        }
        return instance;
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Creates a common enemy from pool with specified type and level scaling.
     * Factory Method Pattern - encapsulates creation and configuration.
     * 
     * @param pool The enemy pool to obtain from
     * @param type The type of enemy to create
     * @param spawnX X position to spawn at
     * @param target Player target for AI
     * @param level Current game level for scaling
     * @return Configured CommonEnemy instance
     */
    public CommonEnemy createCommonEnemy(Pool<CommonEnemy> pool, EnemyType type, 
                                         float spawnX, Player target, int level) {
        CommonEnemy enemy = pool.obtain();
        
        // Initialize with type-specific stats
        enemy.init(spawnX, target, level);
        
        // Apply type-specific modifications
        switch (type) {
            case FAST:
                enemy.setSpeedMultiplier(1.8f);
                enemy.setHealthMultiplier(0.6f);
                break;
            case TANK:
                enemy.setSpeedMultiplier(0.6f);
                enemy.setHealthMultiplier(2.0f);
                break;
            default:
                // COMMON type uses default stats
                break;
        }
        
        Gdx.app.log("EnemyFactory", "Created " + type.getDisplayName() + " at X: " + spawnX);
        return enemy;
    }
    
    /**
     * Creates a common enemy with default type.
     * Convenience method for backward compatibility.
     * 
     * @param pool The enemy pool to obtain from
     * @param spawnX X position to spawn at
     * @param target Player target for AI
     * @param level Current game level for scaling
     * @return Configured CommonEnemy instance
     */
    public CommonEnemy createCommonEnemy(Pool<CommonEnemy> pool, float spawnX, 
                                         Player target, int level) {
        return createCommonEnemy(pool, EnemyType.COMMON, spawnX, target, level);
    }
    
    /**
     * Creates a Mini Boss enemy.
     * Direct instantiation as bosses are unique, not pooled.
     * 
     * @param walkFrame1 First walking frame texture
     * @param walkFrame2 Second walking frame texture
     * @param crouchTex Crouch texture (for super jump prep)
     * @param dashPrepTex Dash preparation texture
     * @param dashTex Dash texture
     * @param dashFlashTex Dash flash effect texture
     * @param superJumpFlashTex Super jump flash effect texture
     * @param spawnX X position to spawn at
     * @param spawnY Y position to spawn at
     * @return New MiniBossEnemy instance
     */
    public MiniBossEnemy createMiniBoss(Texture walkFrame1, Texture walkFrame2, 
                                        Texture crouchTex, Texture dashPrepTex, Texture dashTex,
                                        Texture dashFlashTex, Texture superJumpFlashTex,
                                        float spawnX, float spawnY) {
        MiniBossEnemy boss = new MiniBossEnemy(walkFrame1, walkFrame2, crouchTex, 
                                               dashPrepTex, dashTex, dashFlashTex, superJumpFlashTex);
        boss.init(spawnX, spawnY);
        
        Gdx.app.log("EnemyFactory", "Created Mini Boss at X: " + spawnX + ", Y: " + spawnY);
        return boss;
    }
    
    /**
     * Creates the Final Boss enemy.
     * Direct instantiation as bosses are unique, not pooled.
     * 
     * @param texture Boss texture
     * @param flashTex Flash effect texture
     * @param bulletTexture Texture for boss projectiles
     * @param upwardShotFlashTex Upward shot flash texture
     * @param spawnX X position to spawn at
     * @param levelWidth Level width for boundary
     * @return New FinalBoss instance
     */
    public FinalBoss createFinalBoss(Texture texture, Texture flashTex, 
                                     Texture bulletTexture, Texture upwardShotFlashTex,
                                     float spawnX, float levelWidth) {
        FinalBoss boss = new FinalBoss(texture, flashTex, bulletTexture, upwardShotFlashTex);
        boss.init(spawnX, levelWidth);
        
        Gdx.app.log("EnemyFactory", "Created Final Boss at X: " + spawnX);
        return boss;
    }
    
    /**
     * Creates a random enemy type based on level.
     * Higher levels have chance to spawn stronger enemies.
     * 
     * @param pool The enemy pool to obtain from
     * @param spawnX X position to spawn at
     * @param target Player target for AI
     * @param level Current game level
     * @return Configured CommonEnemy instance with random type
     */
    public CommonEnemy createRandomEnemy(Pool<CommonEnemy> pool, float spawnX, 
                                         Player target, int level) {
        EnemyType type = getRandomTypeForLevel(level);
        return createCommonEnemy(pool, type, spawnX, target, level);
    }
    
    /**
     * Gets a random enemy type appropriate for the level.
     * Higher levels have better chances for stronger enemies.
     * 
     * @param level Current game level
     * @return Appropriate EnemyType for the level
     */
    private EnemyType getRandomTypeForLevel(int level) {
        double random = Math.random();
        
        if (level >= 4) {
            // Level 4+: 30% Tank, 30% Fast, 40% Common
            if (random < 0.30) return EnemyType.TANK;
            if (random < 0.60) return EnemyType.FAST;
            return EnemyType.COMMON;
        } else if (level >= 2) {
            // Level 2-3: 20% Tank, 20% Fast, 60% Common
            if (random < 0.20) return EnemyType.TANK;
            if (random < 0.40) return EnemyType.FAST;
            return EnemyType.COMMON;
        } else {
            // Level 1: 10% Fast, 90% Common (no tanks)
            if (random < 0.10) return EnemyType.FAST;
            return EnemyType.COMMON;
        }
    }
}
