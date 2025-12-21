package com.labubushooter.frontend.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.labubushooter.frontend.core.GameContext;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.objects.Coin;
import com.labubushooter.frontend.objects.CommonEnemy;
import com.labubushooter.frontend.objects.EnemyBullet;
import com.labubushooter.frontend.objects.FinalBoss;
import com.labubushooter.frontend.objects.Ground;
import com.labubushooter.frontend.objects.MiniBossEnemy;
import com.labubushooter.frontend.objects.Pickup;
import com.labubushooter.frontend.objects.Platform;
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

    // Pickup Spawning
    private long lastPickupSpawnTime;
    private long nextPickupSpawnDelay;
    private static final long MIN_PICKUP_DELAY = 5000000000L; // 5 seconds
    private static final long MAX_PICKUP_DELAY = 15000000000L; // 15 seconds

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

        // Initialize pickup pools if not already done (Safety check)
        if (context.pickupPool == null) {
            context.pickupPool = new Pool<Pickup>() {
                @Override
                protected Pickup newObject() {
                    return new Pickup();
                }
            };
            context.activePickups = new Array<>();
        }

        resetPickupTimer();

        // Initialize spawner for current level
        updateSpawner();
    }

    private void resetPickupTimer() {
        lastPickupSpawnTime = TimeUtils.nanoTime();
        nextPickupSpawnDelay = MIN_PICKUP_DELAY + (long)(MathUtils.random() * (MAX_PICKUP_DELAY - MIN_PICKUP_DELAY));
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
        // Handle Weapon Switching
        handleWeaponSwitching();

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

        // Handle pickup spawning
        handlePickupSpawning();

        updatePickups(delta);

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

        // Update Mac10 Unlock Message Timer
        if (context.mac10UnlockMessageTimer > 0) {
            context.mac10UnlockMessageTimer -= delta;
            if (context.mac10UnlockMessageTimer <= 0) {
                context.showMac10UnlockMessage = true;
                context.mac10UnlockMessageDuration = 4f; // Show for 4 seconds
                context.mac10UnlockMessageTimer = -1f;
            }
        }

        if (context.showMac10UnlockMessage) {
            context.mac10UnlockMessageDuration -= delta;
            if (context.mac10UnlockMessageDuration <= 0) {
                context.showMac10UnlockMessage = false;
            }
        }
    }

    private void handleWeaponSwitching() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            context.player.setWeapon(context.pistolStrategy);
            Gdx.app.log("Weapon", "Switched to Pistol");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            if (context.currentLevel >= 4) {
                context.player.setWeapon(context.mac10Strategy);
                Gdx.app.log("Weapon", "Switched to Mac10");
            } else {
                Gdx.app.log("Weapon", "Mac10 locked! Must be acquired.");
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            context.player.setWeapon(context.unarmedStrategy);
            Gdx.app.log("Weapon", "Switched to Unarmed");
        }
    }

    // ==================== BOSS UPDATES ====================

    /**
     * Updates boss entities based on current level.
     *
     * @param delta Time since last frame
     */
    private void updateBosses(float delta) {
        // Mini boss (Level 3)
        if (context.currentLevel == 3 && context.miniBoss != null) {
            // Check if boss is dead but message hasn't been triggered yet
            if (context.miniBoss.isDead() && !context.mac10MessageTriggered) {
                // Start 2 second timer for message
                context.mac10UnlockMessageTimer = 2.0f;
                context.mac10MessageTriggered = true; // Mark as triggered so we don't reset timer
                Gdx.app.log("GameWorld", "Mini Boss defeated! Message timer started.");
            }

            // Only update if alive
            if (!context.miniBoss.isDead()) {
                context.miniBoss.update(delta, context.platforms, context.grounds, context.player);
            }
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
     * Note: Level 3 and 5 spawners return false for isActive(), so no enemies spawn.
     */
    private void handleEnemySpawning() {
        // Explicitly skip spawning for boss levels
        if (context.currentLevel == 3 || context.currentLevel == 5) {
            return;
        }

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

    // ==================== PICKUP MANAGEMENT ====================

    private void handlePickupSpawning() {
        if (TimeUtils.nanoTime() - lastPickupSpawnTime > nextPickupSpawnDelay) {
            spawnRandomPickup();
            resetPickupTimer();
        }
    }

    private void spawnRandomPickup() {
        if (context.activePickups.size >= 5) return; // Limit active pickups

        float spawnX, spawnY;

        // Randomly choose between platform or ground
        if (context.platforms.size > 0 && MathUtils.randomBoolean()) {
            Platform p = context.platforms.random();
            spawnX = p.bounds.x + MathUtils.random(0, p.bounds.width - 32);
            spawnY = p.bounds.y + p.bounds.height + 5;
        } else if (context.grounds.size > 0) {
            Ground g = context.grounds.random();
            spawnX = g.bounds.x + MathUtils.random(0, g.bounds.width - 32);
            spawnY = g.bounds.y + g.bounds.height + 5;
        } else {
            return; // No valid spawn location
        }

        // Ensure spawn is within level bounds
        if (spawnX < 0 || spawnX > context.currentLevelWidth) return;

        Pickup p = context.pickupPool.obtain();
        Pickup.Type type;
        com.badlogic.gdx.graphics.Texture tex;

        int rand = MathUtils.random(100);

        // Logic for ammo spawning based on level
        if (context.currentLevel < 4) {
            // Levels 1-3: No Mac10 ammo
            if (rand < 40) {
                type = Pickup.Type.AMMO_9MM;
                tex = context.ammo9mmTex != null ? context.ammo9mmTex : context.debugTex;
            } else if (rand < 70) {
                type = Pickup.Type.HEALTH_POTION;
                tex = context.healthPotionTex != null ? context.healthPotionTex : context.debugTex;
            } else {
                // 30% chance to spawn nothing or maybe just more 9mm/health to fill the gap
                // Let's just default to 9mm for the remaining 30% to keep gameplay active
                type = Pickup.Type.AMMO_9MM;
                tex = context.ammo9mmTex != null ? context.ammo9mmTex : context.debugTex;
            }
        } else {
            // Levels 4-5: Mac10 ammo enabled
            if (rand < 35) {
                type = Pickup.Type.AMMO_9MM;
                tex = context.ammo9mmTex != null ? context.ammo9mmTex : context.debugTex;
            } else if (rand < 70) {
                type = Pickup.Type.AMMO_45CAL;
                tex = context.ammo45CalTex != null ? context.ammo45CalTex : context.debugTex;
            } else {
                type = Pickup.Type.HEALTH_POTION;
                tex = context.healthPotionTex != null ? context.healthPotionTex : context.debugTex;
            }
        }

        // Use debug texture as fallback if specific textures are null
        if (tex == null) tex = context.debugTex;

        p.init(spawnX, spawnY, type, tex);
        context.activePickups.add(p);
        Gdx.app.log("Pickup", "Spawned " + type + " at " + spawnX + ", " + spawnY);
    }

    /**
     * Spawn initial enemies for current level.
     * Called when loading a new level.
     * Note: Level 3 and 5 (boss levels) do not spawn common enemies.
     */
    public void spawnInitialEnemies() {
        // Explicitly skip spawning for boss levels
        if (context.currentLevel == 3 || context.currentLevel == 5) {
            Gdx.app.log("GameWorld", "Level " + context.currentLevel + " is boss level - no common enemies");
            return;
        }

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

    // ==================== PICKUP MANAGEMENT ====================

    /**
     * Updates all active pickups with animation.
     *
     * @param delta Time since last frame
     */
    private void updatePickups(float delta) {
        if (context.activePickups != null) {
            for (int i = context.activePickups.size - 1; i >= 0; i--) {
                Pickup pickup = context.activePickups.get(i);
                pickup.update(delta);
            }
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

        // Pickup-player collisions
        collisionSystem.checkPickupPlayerCollisions(
            context.player,
            context.activePickups,
            context.pickupPool,
            context.pistolStrategy,
            context.mac10Strategy
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

        // Clear pickups
        if (context.activePickups != null) {
            for (Pickup p : context.activePickups) {
                context.pickupPool.free(p);
            }
            context.activePickups.clear();
        }

        // Update spawner for new level and reset timer
        updateSpawner();
        if (currentSpawner != null) {
            currentSpawner.resetSpawnTimer();
        }

        resetPickupTimer();

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
