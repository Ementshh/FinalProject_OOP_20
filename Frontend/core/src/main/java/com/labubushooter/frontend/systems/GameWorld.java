package com.labubushooter.frontend.systems;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import com.labubushooter.frontend.core.GameContext;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.objects.Coin;
import com.labubushooter.frontend.objects.CommonEnemy;

/**
 * GameWorld manages entity lifecycle, spawning, and world state.
 * 
 * SOLID Principles Applied:
 * - Single Responsibility: Only handles entity management and spawning
 * - Open/Closed: New entity types can be added without modifying core logic
 * - Dependency Inversion: Depends on abstractions (pools, arrays) not concrete implementations
 * 
 * Design Patterns:
 * - Factory Method: Provides entity creation through pools
 * - Object Pool: Uses LibGDX Pool for efficient memory management
 * - Facade: Provides simplified interface for entity management
 */
public class GameWorld {
    
    // ==================== SPAWN CONSTANTS ====================
    /** Buffer distance from camera edge for enemy spawning */
    private static final float SPAWN_BUFFER = 200f;
    
    /** Minimum safe distance from player for enemy spawning */
    private static final float PLAYER_SAFETY_ZONE = 300f;
    
    /** Maximum spawn attempts before fallback */
    private static final int MAX_SPAWN_ATTEMPTS = 20;
    
    // ==================== DEPENDENCIES ====================
    private final GameContext context;
    private final PhysicsSystem physicsSystem;
    private final CollisionSystem collisionSystem;
    private final Random random;
    
    // ==================== SPAWN TIMING ====================
    private long lastEnemySpawnTime;
    private long nextEnemySpawnDelay;
    
    /**
     * Constructs a GameWorld with required dependencies.
     * 
     * @param context The game context containing shared resources
     */
    public GameWorld(GameContext context) {
        this.context = context;
        this.physicsSystem = PhysicsSystem.getInstance();
        this.collisionSystem = CollisionSystem.getInstance();
        this.random = new Random();
        this.lastEnemySpawnTime = TimeUtils.nanoTime();
        resetEnemySpawnTimer();
    }
    
    // ==================== MAIN UPDATE ====================
    
    /**
     * Updates all world entities for one frame.
     * Orchestrates physics, spawning, and entity lifecycle.
     * 
     * @param delta Time since last frame in seconds
     */
    public void update(float delta) {
        // Update player physics
        context.player.update(delta, context.platforms, context.grounds);
        
        // Update bosses
        updateBosses(delta);
        
        // Update enemy bullets (physics)
        physicsSystem.updateEnemyBullets(
            context.activeEnemyBullets,
            context.enemyBulletPool,
            context.currentLevelWidth,
            context.viewport.getWorldHeight(),
            delta
        );
        
        // Handle enemy spawning (only for non-boss levels)
        if (context.currentLevel != 3 && context.currentLevel != 5) {
            handleEnemySpawning();
        }
        
        // Update enemies
        updateEnemies(delta);
        
        // Update and check collisions
        checkAllCollisions();
        
        // Update coins
        updateCoins(delta);
        
        // Update bullets (physics)
        physicsSystem.updateBullets(
            context.activeBullets,
            context.platforms,
            context.bulletPool,
            context.viewport.getWorldHeight(),
            delta
        );
    }
    
    // ==================== BOSS UPDATES ====================
    
    /**
     * Updates boss entities based on current level.
     * 
     * @param delta Time since last frame
     */
    private void updateBosses(float delta) {
        // Mini boss (Level 3)
        if (context.currentLevel == 3 && context.miniBoss != null && !context.miniBoss.isDead()) {
            context.miniBoss.update(delta, context.platforms, context.grounds, context.player);
        }
        
        // Final boss (Level 5)
        if (context.currentLevel == 5 && context.boss != null && !context.boss.isDead()) {
            context.boss.update(delta, context.platforms, context.grounds, context.player,
                               context.activeEnemyBullets, context.enemyBulletPool);
        }
    }
    
    // ==================== ENEMY MANAGEMENT ====================
    
    /**
     * Handles enemy spawning based on timer and level limits.
     */
    private void handleEnemySpawning() {
        if (TimeUtils.nanoTime() - lastEnemySpawnTime > nextEnemySpawnDelay) {
            int maxEnemies = context.getMaxEnemiesForLevel(context.currentLevel);
            
            if (context.activeEnemies.size < maxEnemies) {
                spawnEnemy();
                Gdx.app.log("EnemySpawn", "Active enemies: " + context.activeEnemies.size + "/" + maxEnemies);
            }
            
            resetEnemySpawnTimer();
            lastEnemySpawnTime = TimeUtils.nanoTime();
        }
    }
    
    /**
     * Spawns an enemy at a valid location outside the camera view.
     * Uses intelligent positioning to avoid spawning too close to player.
     */
    public void spawnEnemy() {
        float cameraLeft = context.camera.position.x - context.viewport.getWorldWidth() / 2;
        float cameraRight = context.camera.position.x + context.viewport.getWorldWidth() / 2;
        
        float spawnX = calculateSpawnPosition(cameraLeft, cameraRight);
        
        CommonEnemy enemy = context.enemyPool.obtain();
        enemy.init(spawnX, context.player, context.currentLevel);
        context.activeEnemies.add(enemy);
        
        Gdx.app.log("EnemySpawn", "Spawned at X: " + spawnX);
    }
    
    /**
     * Calculates a valid spawn position for an enemy.
     * 
     * @param cameraLeft Left edge of camera view
     * @param cameraRight Right edge of camera view
     * @return X position for enemy spawn
     */
    private float calculateSpawnPosition(float cameraLeft, float cameraRight) {
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
                if (spawnX > context.currentLevelWidth - 100f) {
                    spawnX = cameraLeft - SPAWN_BUFFER - random.nextFloat() * 100f;
                }
            }
            
            attempts++;
            
            if (attempts >= MAX_SPAWN_ATTEMPTS) {
                // Fallback: spawn at opposite end of level from player
                if (context.player.bounds.x < context.currentLevelWidth / 2) {
                    spawnX = context.currentLevelWidth - 200f;
                } else {
                    spawnX = 100f;
                }
                break;
            }
        } while (Math.abs(spawnX - context.player.bounds.x) < PLAYER_SAFETY_ZONE ||
                 (spawnX >= cameraLeft && spawnX <= cameraRight));
        
        return spawnX;
    }
    
    /**
     * Updates all active enemies and removes inactive ones.
     * 
     * @param delta Time since last frame
     */
    private void updateEnemies(float delta) {
        for (int i = context.activeEnemies.size - 1; i >= 0; i--) {
            CommonEnemy enemy = context.activeEnemies.get(i);
            enemy.update(delta, context.platforms, context.grounds);
            
            if (!enemy.isActive()) {
                context.activeEnemies.removeIndex(i);
                context.enemyPool.free(enemy);
            }
        }
    }
    
    /**
     * Resets enemy spawn timer with level-appropriate delays.
     */
    public void resetEnemySpawnTimer() {
        long minSpawn, maxSpawn;
        switch (context.currentLevel) {
            case 2:
                minSpawn = GameContext.LEVEL2_MIN_SPAWN;
                maxSpawn = GameContext.LEVEL2_MAX_SPAWN;
                break;
            case 4:
                minSpawn = GameContext.LEVEL4_MIN_SPAWN;
                maxSpawn = GameContext.LEVEL4_MAX_SPAWN;
                break;
            default:
                minSpawn = GameContext.LEVEL1_MIN_SPAWN;
                maxSpawn = GameContext.LEVEL1_MAX_SPAWN;
                break;
        }
        nextEnemySpawnDelay = minSpawn + (long)(random.nextFloat() * (maxSpawn - minSpawn));
    }
    
    // ==================== COIN MANAGEMENT ====================
    
    /**
     * Updates all active coins and handles collection.
     * 
     * @param delta Time since last frame
     */
    private void updateCoins(float delta) {
        for (int i = context.activeCoins.size - 1; i >= 0; i--) {
            Coin coin = context.activeCoins.get(i);
            coin.update(delta);
        }
        
        // Check coin collection via collision system
        int collected = collisionSystem.checkCoinPlayerCollisions(
            context.player,
            context.activeCoins,
            context.coinPool
        );
        
        if (collected > 0) {
            context.coinScore += collected;
            context.coinsCollectedThisSession += collected;
            Gdx.app.log("Coin", "Total: " + context.coinScore);
        }
    }
    
    // ==================== COLLISION HANDLING ====================
    
    /**
     * Performs all collision checks for the current frame.
     */
    private void checkAllCollisions() {
        // Bullet-enemy collisions
        collisionSystem.checkBulletEnemyCollisions(
            context.activeEnemies,
            context.activeBullets,
            context.bulletPool
        );
        
        // Bullet-boss collisions
        collisionSystem.checkBulletBossCollisions(
            context.miniBoss,
            context.boss,
            context.activeBullets,
            context.bulletPool
        );
        
        // Enemy bullet-player collisions
        collisionSystem.checkEnemyBulletPlayerCollisions(
            context.player,
            context.activeEnemyBullets,
            context.enemyBulletPool
        );
    }
    
    // ==================== LEVEL EXIT CHECK ====================
    
    /**
     * Checks if player has reached the level exit.
     * 
     * @return True if player is at level exit and boss is defeated (if applicable)
     */
    public boolean isAtLevelExit() {
        boolean bossDefeated = true;
        if (context.currentLevel == 3 && context.miniBoss != null) {
            bossDefeated = context.miniBoss.isDead();
        }
        if (context.currentLevel == 5 && context.boss != null) {
            bossDefeated = context.boss.isDead();
        }
        
        return bossDefeated && context.player.bounds.x + context.player.bounds.width >= 
               context.currentLevelWidth - GameContext.LEVEL_EXIT_THRESHOLD;
    }
    
    /**
     * Checks if current level is the final level.
     * 
     * @return True if current level is level 5
     */
    public boolean isFinalLevel() {
        return context.currentLevel == 5;
    }
    
    // ==================== ENTITY CLEANUP ====================
    
    /**
     * Clears all active entities from the world.
     * Called when loading a new level or restarting.
     */
    public void clearAllEntities() {
        // Clear enemies
        for (CommonEnemy enemy : context.activeEnemies) {
            context.enemyPool.free(enemy);
        }
        context.activeEnemies.clear();
        
        // Clear bullets
        for (Bullet bullet : context.activeBullets) {
            context.bulletPool.free(bullet);
        }
        context.activeBullets.clear();
        
        // Clear enemy bullets
        for (com.labubushooter.frontend.objects.EnemyBullet eb : context.activeEnemyBullets) {
            context.enemyBulletPool.free(eb);
        }
        context.activeEnemyBullets.clear();
        
        // Clear coins
        for (Coin coin : context.activeCoins) {
            context.coinPool.free(coin);
        }
        context.activeCoins.clear();
        
        // Reset spawn timer
        lastEnemySpawnTime = TimeUtils.nanoTime();
        resetEnemySpawnTimer();
        
        Gdx.app.log("GameWorld", "All entities cleared");
    }
    
    // ==================== GETTERS ====================
    
    public PhysicsSystem getPhysicsSystem() {
        return physicsSystem;
    }
    
    public CollisionSystem getCollisionSystem() {
        return collisionSystem;
    }
}
