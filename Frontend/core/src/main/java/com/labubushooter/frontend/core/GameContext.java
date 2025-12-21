package com.labubushooter.frontend.core;

import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.labubushooter.frontend.DebugManager;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.objects.Coin;
import com.labubushooter.frontend.objects.CommonEnemy;
import com.labubushooter.frontend.objects.EnemyBullet;
import com.labubushooter.frontend.objects.FinalBoss;
import com.labubushooter.frontend.objects.Ground;
import com.labubushooter.frontend.objects.MiniBossEnemy;
import com.labubushooter.frontend.objects.Pickup;
import com.labubushooter.frontend.objects.Platform;
import com.labubushooter.frontend.objects.Player;
import com.labubushooter.frontend.patterns.CoinPattern;
import com.labubushooter.frontend.patterns.LevelStrategy;
import com.labubushooter.frontend.patterns.weapons.Mac10Strategy;
import com.labubushooter.frontend.patterns.weapons.PistolStrategy;
import com.labubushooter.frontend.patterns.weapons.UnarmedStrategy;
import com.labubushooter.frontend.services.PlayerApiService;
import com.labubushooter.frontend.services.PlayerApiService.PlayerData;

/**
 * Shared game context containing all resources and state.
 * Acts as a central repository for game data accessible by all screens.
 * Implements Facade Pattern for resource access.
 */
public class GameContext {

    /**
     * Callback interface for game actions that need to be handled by Main.
     * This allows screens to trigger level loading, save, restart without
     * direct dependency on Main.java.
     */
    public interface GameCallback {
        void loadLevel(int level);
        void saveProgress();
        void restartGame();
        void restartToUsernameInput();
    }

    // Callback reference
    public GameCallback callback;

    // Constants
    public static final float VIEWPORT_WIDTH = 1066f;
    public static final float VIEWPORT_HEIGHT = 600f;
    public static final float LEVEL_EXIT_THRESHOLD = 100f;
    public static final int MAX_USERNAME_LENGTH = 20;

    // Rendering
    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public OrthographicCamera camera;
    public Viewport viewport;

    // Fonts
    public BitmapFont font;
    public BitmapFont smallFont;
    public GlyphLayout layout;

    // Textures - Game Objects
    public Texture playerTex;
    public Texture platformTex;
    public Texture groundTex;
    public Texture bulletTex;
    public Texture exitTex;
    public Texture pistolTex;
    public Texture mac10Tex;
    public Texture debugTex;
    public Texture levelIndicatorTex;
    public Texture enemyTex;
    public Texture backgroundTex;
    
    // Level indicator textures (per level with colored background and number)
    public Texture levelIndicator1Tex;
    public Texture levelIndicator2Tex;
    public Texture levelIndicator3Tex;
    public Texture levelIndicator4Tex;
    public Texture levelIndicator5Tex;

    // Textures - Pickups
    public Texture ammo9mmTex;
    public Texture ammo45CalTex;
    public Texture healthPotionTex;

    // Textures - Boss
    public Texture miniBossTex;
    public Texture bossTex;
    public Texture enemyBulletTex;
    public Texture whiteFlashTex;
    public Texture redFlashTex;
    public Texture yellowFlashTex;

    // Textures - UI
    public Texture buttonTex;
    public Texture buttonHoverTex;

    // Rendering Services
    public com.labubushooter.frontend.services.BackgroundRenderer backgroundRenderer;

    // Game Systems
    public com.labubushooter.frontend.systems.GameWorld gameWorld;

    // Game Objects
    public Player player;
    public Array<Platform> platforms;
    public Array<Ground> grounds;
    public MiniBossEnemy miniBoss;
    public FinalBoss boss;

    // Object Pools
    public Pool<Bullet> bulletPool;
    public Array<Bullet> activeBullets;
    public Pool<CommonEnemy> enemyPool;
    public Array<CommonEnemy> activeEnemies;
    public Pool<EnemyBullet> enemyBulletPool;
    public Array<EnemyBullet> activeEnemyBullets;
    public Pool<Coin> coinPool;
    public Array<Coin> activeCoins;
    public Pool<Pickup> pickupPool;
    public Array<Pickup> activePickups;

    // Patterns
    public CoinPattern coinPattern;
    public PistolStrategy pistolStrategy;
    public Mac10Strategy mac10Strategy;
    public UnarmedStrategy unarmedStrategy;
    public Map<Integer, LevelStrategy> levelStrategies;

    // Game State
    public int currentLevel = 1;
    public float currentLevelWidth;
    public int coinScore = 0;
    public Array<float[]> coinSpawnLocations;

    // Enemy Spawning
    public long lastEnemySpawnTime;
    public long nextEnemySpawnDelay;
    public Random random;

    // Player State
    public String username = "";
    public StringBuilder usernameInput;
    public PlayerData currentPlayerData;
    public int coinsCollectedThisSession = 0;
    public boolean isNewPlayer = false;

    // Mac10 Unlock Message State
    public float mac10UnlockMessageTimer = -1f;
    public boolean showMac10UnlockMessage = false;
    public float mac10UnlockMessageDuration = 0f;
    public boolean mac10MessageTriggered = false; // Preventing re-triggering

    // Services
    public PlayerApiService playerApi;
    public DebugManager debugManager;

    // Enemy spawn delays per level (in nanoseconds)
    public static final long LEVEL1_MIN_SPAWN = 3000000000L;
    public static final long LEVEL1_MAX_SPAWN = 4000000000L;
    public static final long LEVEL2_MIN_SPAWN = 2000000000L;
    public static final long LEVEL2_MAX_SPAWN = 4000000000L;
    public static final long LEVEL4_MIN_SPAWN = 1000000000L;
    public static final long LEVEL4_MAX_SPAWN = 3000000000L;

    // Max enemies per level
    public static final int MAX_ENEMIES_LEVEL1 = 6;
    public static final int MAX_ENEMIES_LEVEL2 = 7;
    public static final int MAX_ENEMIES_LEVEL4 = 8;

    public GameContext() {
        this.usernameInput = new StringBuilder();
        this.random = new Random();
    }

    /**
     * Set the callback for game actions.
     */
    public void setCallback(GameCallback callback) {
        this.callback = callback;
    }

    /**
     * Get max enemies for a specific level.
     */
    public int getMaxEnemiesForLevel(int level) {
        switch (level) {
            case 1: return MAX_ENEMIES_LEVEL1;
            case 2: return MAX_ENEMIES_LEVEL2;
            case 4: return MAX_ENEMIES_LEVEL4;
            default: return MAX_ENEMIES_LEVEL1;
        }
    }

    /**
     * Reset enemy spawn timer based on current level.
     */
    public void resetEnemySpawnTimer() {
        lastEnemySpawnTime = TimeUtils.nanoTime();
        long minSpawn, maxSpawn;
        switch (currentLevel) {
            case 2:
                minSpawn = LEVEL2_MIN_SPAWN;
                maxSpawn = LEVEL2_MAX_SPAWN;
                break;
            case 4:
                minSpawn = LEVEL4_MIN_SPAWN;
                maxSpawn = LEVEL4_MAX_SPAWN;
                break;
            default:
                minSpawn = LEVEL1_MIN_SPAWN;
                maxSpawn = LEVEL1_MAX_SPAWN;
                break;
        }
        nextEnemySpawnDelay = minSpawn + (long)(random.nextFloat() * (maxSpawn - minSpawn));
    }

    /**
     * Clear all game objects (for restart/cleanup).
     */
    public void clearGameObjects() {
        // Clear enemies
        if (activeEnemies != null) {
            for (CommonEnemy enemy : activeEnemies) {
                enemyPool.free(enemy);
            }
            activeEnemies.clear();
        }

        // Clear bullets
        if (activeBullets != null) {
            activeBullets.clear();
        }

        // Clear enemy bullets
        if (activeEnemyBullets != null) {
            for (EnemyBullet eb : activeEnemyBullets) {
                enemyBulletPool.free(eb);
            }
            activeEnemyBullets.clear();
        }

        // Clear coins
        if (activeCoins != null) {
            for (Coin coin : activeCoins) {
                coinPool.free(coin);
            }
            activeCoins.clear();
        }

        // Clear pickups
        if (activePickups != null) {
            for (Pickup p : activePickups) {
                pickupPool.free(p);
            }
            activePickups.clear();
        }

        coinScore = 0;
        miniBoss = null;
        boss = null;

        // Reset message state
        mac10UnlockMessageTimer = -1f;
        showMac10UnlockMessage = false;
        mac10UnlockMessageDuration = 0f;
        mac10MessageTriggered = false;
    }
    
    /**
     * Gets the appropriate level indicator texture for the specified level.
     * Each level has a unique colored circle with the level number inside.
     *
     * @param level The level number (1-5)
     * @return The corresponding level indicator texture
     */
    public Texture getLevelIndicatorTexture(int level) {
        switch (level) {
            case 1: return levelIndicator1Tex;
            case 2: return levelIndicator2Tex;
            case 3: return levelIndicator3Tex;
            case 4: return levelIndicator4Tex;
            case 5: return levelIndicator5Tex;
            default: return levelIndicator1Tex;
        }
    }

    /**
     * Reset player state.
     */
    public void resetPlayer() {
        if (player != null) {
            player.reset();
            player.setWeapon(pistolStrategy); // Default to pistol
        }
    }

    /**
     * Dispose all resources.
     */
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        if (smallFont != null) smallFont.dispose();

        // Dispose textures
        disposeTexture(playerTex);
        disposeTexture(platformTex);
        disposeTexture(groundTex);
        disposeTexture(bulletTex);
        disposeTexture(exitTex);
        disposeTexture(pistolTex);
        disposeTexture(mac10Tex);
        disposeTexture(debugTex);
        disposeTexture(levelIndicatorTex);
        disposeTexture(levelIndicator1Tex);
        disposeTexture(levelIndicator2Tex);
        disposeTexture(levelIndicator3Tex);
        disposeTexture(levelIndicator4Tex);
        disposeTexture(levelIndicator5Tex);
        disposeTexture(enemyTex);
        disposeTexture(backgroundTex);
        disposeTexture(miniBossTex);
        disposeTexture(bossTex);
        disposeTexture(enemyBulletTex);
        disposeTexture(whiteFlashTex);
        disposeTexture(redFlashTex);
        disposeTexture(yellowFlashTex);
        disposeTexture(buttonTex);
        disposeTexture(buttonHoverTex);

        disposeTexture(ammo9mmTex);
        disposeTexture(ammo45CalTex);
        disposeTexture(healthPotionTex);
    }

    private void disposeTexture(Texture texture) {
        if (texture != null) texture.dispose();
    }
}
