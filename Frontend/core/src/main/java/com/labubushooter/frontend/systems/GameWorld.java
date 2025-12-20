package com.labubushooter.frontend.systems;

import com.badlogic.gdx.Gdx;
import com.labubushooter.frontend.core.GameContext;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.objects.Coin;
import com.labubushooter.frontend.objects.CommonEnemy;
import com.labubushooter.frontend.systems.spawner.EnemySpawnerFactory;
import com.labubushooter.frontend.systems.spawner.IEnemySpawner;

/**
 * GameWorld manages entity lifecycle, spawning, and world state.
 * 
 * SOLID Principles Applied:
 * - Single Responsibility: Only handles entity management and spawning
 * - Open/Closed: New entity types can be added without modifying core logic
 * - Dependency Inversion: Depends on abstractions (pools, arrays, IEnemySpawner)
 * 
 * Design Patterns:
 * - Factory Method: Provides entity creation through pools
 * - Object Pool: Uses LibGDX Pool for efficient memory management
 * - Facade: Provides simplified interface for entity management
 * - Strategy Pattern: Uses IEnemySpawner for level-specific spawning
 */
public class GameWorld {
    
    // ==================== DEPENDENCIES ====================
    private final GameContext context;
    private final PhysicsSystem physicsSystem;
    private final CollisionSystem collisionSystem;
    private final EnemySpawnerFactory spawnerFactory;
    
    /** Current level's enemy spawner (Strategy Pattern) */
    private IEnemySpawner currentSpawner;
    
    /**
     * Constructs a GameWorld with required dependencies.
     * 
     * @param context The game context containing shared resources
     */
    public GameWorld(GameContext context) {
        this.context = context;
        this.physicsSystem = PhysicsSystem.getInstance();
        this.collisionSystem = CollisionSystem.getInstance();
        this.spawnerFactory = EnemySpawnerFactory.getInstance();
        
        // Initialize spawner for current level
        updateSpawner();
    }
    
    /**
     * Update the enemy spawner when level changes.
     * Called by loadLevel() or when switching levels.
     */
    public void updateSpawner() {
        currentSpawner = spawnerFactory.getSpawner(context.currentLevel);
        Gdx.app.log("GameWorld", "Spawner updated for level " + context.currentLevel + 
                   " | Active: " + currentSpawner.isActive());
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
        
        // Handle enemy spawning via Strategy Pattern
        handleEnemySpawning();
        
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
    
    // ==================== ENEMY MANAGEMENT (Strategy Pattern) ====================
    
    /**
     * Handles enemy spawning using current level's spawner strategy.
     * Delegates to IEnemySpawner implementation.
     */
    private void handleEnemySpawning() {
        if (currentSpawner == null) {
            updateSpawner();
        }
        
        // Delegate spawning to current level's spawner (Strategy Pattern)
        currentSpawner.trySpawnEnemy(
            context.enemyPool,
            context.activeEnemies,
            context.player,
            context.currentLevel,
            context.currentLevelWidth,
            context.camera.position.x,
            context.viewport.getWorldWidth()
        );
    }
    
    /**
     * Spawn initial enemies for current level.
     * Called when loading a new level.
     */
    public void spawnInitialEnemies() {
        if (currentSpawner == null) {
            updateSpawner();
        }
        
        currentSpawner.spawnInitialEnemies(
            context.enemyPool,
            context.activeEnemies,
            context.player,
            context.currentLevel
        );
        
        Gdx.app.log("GameWorld", "Initial enemies spawned for level " + context.currentLevel);
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
        
        // Update spawner for new level and reset timer
        updateSpawner();
        if (currentSpawner != null) {
            currentSpawner.resetSpawnTimer();
        }
        
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
