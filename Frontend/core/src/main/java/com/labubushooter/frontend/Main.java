package com.labubushooter.frontend;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.labubushooter.frontend.core.GameContext;
import com.labubushooter.frontend.objects.*;
import com.labubushooter.frontend.patterns.CoinPattern;
import com.labubushooter.frontend.patterns.IBackgroundRenderStrategy;
import com.labubushooter.frontend.patterns.LevelStrategy;
import com.labubushooter.frontend.patterns.ScalingMode;
import com.labubushooter.frontend.patterns.StaticBackgroundStrategy;
import com.labubushooter.frontend.patterns.VerticalAlignment;
import com.labubushooter.frontend.patterns.coins.LinePattern;
import com.labubushooter.frontend.patterns.levels.*;
import com.labubushooter.frontend.patterns.weapons.Mac10Strategy;
import com.labubushooter.frontend.patterns.weapons.PistolStrategy;
import com.labubushooter.frontend.patterns.weapons.UnarmedStrategy;
import com.labubushooter.frontend.screens.*;
import com.labubushooter.frontend.services.AssetManager;
import com.labubushooter.frontend.services.BackgroundRenderer;
import com.labubushooter.frontend.services.BackgroundStrategyResolver;
import com.labubushooter.frontend.services.BackgroundTextureResolver;
import com.labubushooter.frontend.services.PlayerApiService;
import com.labubushooter.frontend.systems.GameWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Main game class - handles initialization and lifecycle.
 * Delegates all rendering and game logic to Screen classes via ScreenManager.
 * Implements GameContext.GameCallback to handle level loading and save/restart operations.
 *
 * Design Patterns Used:
 * - Facade Pattern: GameContext provides unified access to game resources
 * - State Pattern: ScreenManager manages game state transitions
 * - Strategy Pattern: LevelStrategy for different level configurations
 * - Template Method: BaseScreen provides common screen behavior
 * - Singleton: AssetManager for centralized resource management
 */
public class Main extends ApplicationAdapter implements GameContext.GameCallback {

    // ==================== CORE SYSTEMS ====================
    private GameContext gameContext;
    private ScreenManager screenManager;
    private AssetManager assetManager;

    // ==================== RESOURCES ====================
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;

    // Fonts
    private BitmapFont font;
    private BitmapFont smallFont;
    private GlyphLayout layout;

    // Textures
    private Texture playerTex, platformTex, groundTex, bulletTex, exitTex;
    private Texture pistolTex, mac10Tex, debugTex, levelIndicatorTex, enemyTex;
    private Texture enemyFrame1Tex, enemyFrame2Tex;
    private Texture miniBossTex, bossTex, enemyBulletTex;
    private Texture miniBossWalkFrame1Tex, miniBossWalkFrame2Tex;
    private Texture miniBossCrouchTex, miniBossDashPrepTex, miniBossDashTex;
    private Texture whiteFlashTex, redFlashTex, yellowFlashTex;
    private Texture backgroundTex, buttonTex, buttonHoverTex;

    // Pickup Textures
    private Texture ammo9mmTex, ammo45CalTex, healthPotionTex;

    // Game Objects
    private Player player;
    private Array<Platform> platforms;
    private Array<Ground> grounds;

    // Object Pools
    private Pool<Bullet> bulletPool;
    private Array<Bullet> activeBullets;
    private Pool<CommonEnemy> enemyPool;
    private Array<CommonEnemy> activeEnemies;
    private Pool<EnemyBullet> enemyBulletPool;
    private Array<EnemyBullet> activeEnemyBullets;
    private Pool<Coin> coinPool;
    private Array<Coin> activeCoins;

    // Patterns & Strategies
    private CoinPattern coinPattern;
    private PistolStrategy pistolStrategy;
    private Mac10Strategy mac10Strategy;
    private UnarmedStrategy unarmedStrategy;
    private Map<Integer, LevelStrategy> levelStrategies;

    // Services
    private PlayerApiService playerApi;
    private DebugManager debugManager;
    private Random random;

    // ==================== CONSTANTS ====================
    private static final float VIEWPORT_WIDTH = 1066f;
    private static final float VIEWPORT_HEIGHT = 600f;

    @Override
    public void create() {
        // 1. Initialize AssetManager first (loads all textures and fonts)
        assetManager = AssetManager.getInstance();
        assetManager.initialize();

        // 2. Create all resources (pools, strategies, etc.)
        createResources();

        // 3. Initialize GameContext with all resources
        initializeGameContext();

        // 4. Set callback on context
        gameContext.setCallback(this);

        // 5. Initialize Screen Manager with all screens
        initializeScreenManager();

        Gdx.app.log("Main", "Game initialized successfully");
    }

    // ==================== INITIALIZATION ====================

    private void createResources() {
        // Core rendering
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIEWPORT_WIDTH / 2, VIEWPORT_HEIGHT / 2, 0);
        camera.update();

        // Fonts from AssetManager
        font = assetManager.getDefaultFont();
        smallFont = assetManager.getSmallFont();
        layout = new GlyphLayout();

        // Textures from AssetManager
        playerTex = assetManager.getTexture(AssetManager.PLAYER);
        platformTex = assetManager.getTexture(AssetManager.PLATFORM);
        groundTex = assetManager.getTexture(AssetManager.GROUND_BASE);
        exitTex = assetManager.getTexture(AssetManager.EXIT_DOOR);
        enemyTex = assetManager.getTexture(AssetManager.ENEMY);
        enemyFrame1Tex = assetManager.getTexture(AssetManager.ENEMY_FRAME1);
        enemyFrame2Tex = assetManager.getTexture(AssetManager.ENEMY_FRAME2);
        // Initialize with level 1 background as default
        backgroundTex = assetManager.getTexture(AssetManager.BACKGROUND_LEVEL1);

        // Boss textures from AssetManager
        miniBossTex = assetManager.getTexture(AssetManager.MINI_BOSS);
        miniBossWalkFrame1Tex = assetManager.getTexture(AssetManager.MINI_BOSS_WALK_FRAME1);
        miniBossWalkFrame2Tex = assetManager.getTexture(AssetManager.MINI_BOSS_WALK_FRAME2);
        miniBossCrouchTex = assetManager.getTexture(AssetManager.MINI_BOSS_CROUCH);
        miniBossDashPrepTex = assetManager.getTexture(AssetManager.MINI_BOSS_DASHPREP);
        miniBossDashTex = assetManager.getTexture(AssetManager.MINI_BOSS_DASH);
        bossTex = assetManager.getTexture(AssetManager.BOSS);

        // Weapon textures from AssetManager
        pistolTex = assetManager.getTexture(AssetManager.PISTOL);
        mac10Tex = assetManager.getTexture(AssetManager.MAC10);

        // Bullet texture from AssetManager
        bulletTex = assetManager.getTexture(AssetManager.BULLET);

        // Generated textures from AssetManager
        debugTex = assetManager.getTexture(AssetManager.DEBUG_LINE);
        levelIndicatorTex = assetManager.getTexture(AssetManager.LEVEL_INDICATOR);
        enemyBulletTex = assetManager.getTexture(AssetManager.ENEMY_BULLET);
        whiteFlashTex = assetManager.getTexture(AssetManager.FLASH_WHITE);
        redFlashTex = assetManager.getTexture(AssetManager.FLASH_RED);
        yellowFlashTex = assetManager.getTexture(AssetManager.FLASH_YELLOW);
        buttonTex = assetManager.getTexture(AssetManager.BUTTON);
        buttonHoverTex = assetManager.getTexture(AssetManager.BUTTON_HOVER);

        // Pickup Textures
        ammo9mmTex = assetManager.getTexture(AssetManager.AMMO_9MM);
        ammo45CalTex = assetManager.getTexture(AssetManager.AMMO_45CAL);
        healthPotionTex = assetManager.getTexture(AssetManager.HEALTH_POTION);

        // Object pools
        bulletPool = new Pool<Bullet>() {
            @Override
            protected Bullet newObject() {
                return new Bullet();
            }
        };
        activeBullets = new Array<>();

        enemyPool = new Pool<CommonEnemy>() {
            @Override
            protected CommonEnemy newObject() {
                return new CommonEnemy(enemyFrame1Tex, enemyFrame2Tex);
            }
        };
        activeEnemies = new Array<>();

        enemyBulletPool = new Pool<EnemyBullet>() {
            @Override
            protected EnemyBullet newObject() {
                return new EnemyBullet();
            }
        };
        activeEnemyBullets = new Array<>();

        coinPool = new Pool<Coin>() {
            @Override
            protected Coin newObject() {
                return new Coin();
            }
        };
        activeCoins = new Array<>();

        // Patterns & strategies
        coinPattern = new LinePattern();
        pistolStrategy = new PistolStrategy(pistolTex);
        mac10Strategy = new Mac10Strategy(mac10Tex);
        unarmedStrategy = new UnarmedStrategy();

        levelStrategies = new HashMap<>();
        levelStrategies.put(1, new Level1Strategy());
        levelStrategies.put(2, new Level2Strategy());
        levelStrategies.put(3, new Level3Strategy());
        levelStrategies.put(4, new Level4Strategy());
        levelStrategies.put(5, new Level5Strategy());

        // Player
        player = new Player(playerTex);
        player.camera = camera;
        player.setWeapon(pistolStrategy); // Default to pistol

        // Services
        playerApi = new PlayerApiService();
        debugManager = new DebugManager();
        random = new Random();

        // Initialize arrays
        platforms = new Array<>();
        grounds = new Array<>();
    }

    private void initializeGameContext() {
        gameContext = new GameContext();

        // Core rendering
        gameContext.batch = batch;
        gameContext.shapeRenderer = shapeRenderer;
        gameContext.camera = camera;
        gameContext.viewport = viewport;

        // Fonts
        gameContext.font = font;
        gameContext.smallFont = smallFont;
        gameContext.layout = layout;

        // Textures
        gameContext.playerTex = playerTex;
        gameContext.platformTex = platformTex;
        gameContext.groundTex = groundTex;
        gameContext.bulletTex = bulletTex;
        gameContext.exitTex = exitTex;
        gameContext.pistolTex = pistolTex;
        gameContext.mac10Tex = mac10Tex;
        gameContext.debugTex = debugTex;
        gameContext.levelIndicatorTex = levelIndicatorTex;
        gameContext.enemyTex = enemyTex;
        gameContext.backgroundTex = backgroundTex;

        // Initialize BackgroundRenderer with FIT_HEIGHT strategy and CENTER alignment
        // Ensures full vertical height visible at any resolution, width naturally cropped
        gameContext.backgroundRenderer = new BackgroundRenderer(
            backgroundTex,
            new StaticBackgroundStrategy(ScalingMode.FIT_HEIGHT, VerticalAlignment.CENTER)
        );
        gameContext.miniBossTex = miniBossTex;
        gameContext.bossTex = bossTex;
        gameContext.enemyBulletTex = enemyBulletTex;
        gameContext.whiteFlashTex = whiteFlashTex;
        gameContext.redFlashTex = redFlashTex;
        gameContext.yellowFlashTex = yellowFlashTex;
        gameContext.buttonTex = buttonTex;
        gameContext.buttonHoverTex = buttonHoverTex;

        // Pickup Textures
        gameContext.ammo9mmTex = ammo9mmTex;
        gameContext.ammo45CalTex = ammo45CalTex;
        gameContext.healthPotionTex = healthPotionTex;

        // Game objects
        gameContext.player = player;
        gameContext.platforms = platforms;
        gameContext.grounds = grounds;

        // Pools
        gameContext.bulletPool = bulletPool;
        gameContext.activeBullets = activeBullets;
        gameContext.enemyPool = enemyPool;
        gameContext.activeEnemies = activeEnemies;
        gameContext.enemyBulletPool = enemyBulletPool;
        gameContext.activeEnemyBullets = activeEnemyBullets;
        gameContext.coinPool = coinPool;
        gameContext.activeCoins = activeCoins;

        // Patterns
        gameContext.coinPattern = coinPattern;
        gameContext.pistolStrategy = pistolStrategy;
        gameContext.mac10Strategy = mac10Strategy;
        gameContext.unarmedStrategy = unarmedStrategy;
        gameContext.levelStrategies = levelStrategies;

        // Services
        gameContext.playerApi = playerApi;
        gameContext.debugManager = debugManager;
        gameContext.random = random;

        // Initialize GameWorld system for entity management
        // GameWorld handles physics, collision, and entity lifecycle
        gameContext.gameWorld = new GameWorld(gameContext);
    }

    private void initializeScreenManager() {
        screenManager = new ScreenManager(gameContext);

        // Create GamePlayScreen
        GamePlayScreen gamePlayScreen = new GamePlayScreen(gameContext);
        gamePlayScreen.setCallback(new GamePlayScreen.GamePlayCallback() {
            @Override
            public void loadLevel(int level) {
                Main.this.loadLevel(level);
            }

            @Override
            public void saveProgress() {
                Main.this.saveProgress();
            }

            @Override
            public void restartGame() {
                Main.this.restartGame();
            }
        });

        // Create screens with GamePlayScreen reference
        UsernameInputScreen usernameScreen = new UsernameInputScreen(gameContext);
        usernameScreen.setGamePlayScreen(gamePlayScreen);

        ContinueOrNewScreen continueOrNewScreen = new ContinueOrNewScreen(gameContext);
        continueOrNewScreen.setGamePlayScreen(gamePlayScreen);

        PauseScreen pauseScreen = new PauseScreen(gameContext);
        pauseScreen.setGameRenderer(() -> gamePlayScreen.renderGameOnly());

        RestartConfirmScreen restartScreen = new RestartConfirmScreen(gameContext);
        restartScreen.setGameRenderer(() -> gamePlayScreen.renderGameOnly());

        // Register all screens
        screenManager.registerScreen(GameState.USERNAME_INPUT, usernameScreen);
        screenManager.registerScreen(GameState.LOADING_PLAYER_DATA, new LoadingScreen(gameContext));
        screenManager.registerScreen(GameState.CONTINUE_OR_NEW, continueOrNewScreen);
        screenManager.registerScreen(GameState.PLAYING, gamePlayScreen);
        screenManager.registerScreen(GameState.PAUSED, pauseScreen);
        screenManager.registerScreen(GameState.RESTART_CONFIRM, restartScreen);
        screenManager.registerScreen(GameState.GAME_OVER, new GameOverScreen(gameContext));
        screenManager.registerScreen(GameState.VICTORY, new VictoryScreen(gameContext));

        screenManager.setGamePlayScreen(gamePlayScreen);

        // Set initial screen
        screenManager.setScreen(GameState.USERNAME_INPUT);
    }

    // ==================== GameCallback IMPLEMENTATION ====================

    @Override
    public void loadLevel(int level) {
        LevelStrategy strategy = levelStrategies.get(level);
        if (strategy == null) {
            Gdx.app.error("Main", "Level " + level + " not found!");
            return;
        }

        gameContext.currentLevel = level;

        // Get appropriate background texture for this level
        Texture levelBackgroundTex = BackgroundTextureResolver.getTexture(level, assetManager);
        if (levelBackgroundTex != null) {
            gameContext.backgroundRenderer.setBackgroundTexture(levelBackgroundTex);
            gameContext.backgroundTex = levelBackgroundTex;
        }

        // Get and set appropriate background rendering strategy for this level
        IBackgroundRenderStrategy backgroundStrategy = BackgroundStrategyResolver.getStrategy(level);
        gameContext.backgroundRenderer.setRenderStrategy(backgroundStrategy);

        // Clear existing objects
        platforms.clear();
        grounds.clear();
        activeBullets.clear();

        for (CommonEnemy enemy : activeEnemies) {
            enemyPool.free(enemy);
        }
        activeEnemies.clear();

        for (EnemyBullet eb : activeEnemyBullets) {
            enemyBulletPool.free(eb);
        }
        activeEnemyBullets.clear();

        for (Coin coin : activeCoins) {
            coinPool.free(coin);
        }
        activeCoins.clear();

        // Spawn boss for levels 3 and 5
        if (level == 3) {
            gameContext.miniBoss = new MiniBossEnemy(
                miniBossWalkFrame1Tex, miniBossWalkFrame2Tex,
                miniBossCrouchTex, miniBossDashPrepTex, miniBossDashTex,
                whiteFlashTex, yellowFlashTex);
            gameContext.miniBoss.init(strategy.getBossSpawnX(), strategy.getBossSpawnY());
            gameContext.boss = null;
            Gdx.app.log("Level3", "Mini Boss spawned!");
        } else if (level == 5) {
            gameContext.boss = new FinalBoss(bossTex, redFlashTex, enemyBulletTex, yellowFlashTex);
            gameContext.boss.init(strategy.getBossSpawnX(), strategy.getBossSpawnY());
            gameContext.miniBoss = null;
            Gdx.app.log("Level5", "Final Boss spawned!");
        } else {
            gameContext.miniBoss = null;
            gameContext.boss = null;
        }

        // Set level width
        gameContext.currentLevelWidth = strategy.getLevelWidth();
        if (level == 5) {
            gameContext.currentLevelWidth = Math.max(gameContext.currentLevelWidth, viewport.getWorldWidth());
        }

        // Load platforms and ground
        strategy.loadPlatforms(platforms, platformTex);
        strategy.loadGround(grounds, groundTex);

        // Position player
        player.bounds.setPosition(strategy.getPlayerStartX(), strategy.getPlayerStartY());
        Player.LEVEL_WIDTH = gameContext.currentLevelWidth;

        // Reset camera
        camera.position.x = VIEWPORT_WIDTH / 2;
        camera.update();

        // Spawn coins
        setupCoinSpawnLocations(level);
        spawnCoinsForLevel();

        // Spawn initial enemies via GameWorld (uses Strategy Pattern)
        if (gameContext.gameWorld != null) {
            gameContext.gameWorld.spawnInitialEnemies();
        }

        Gdx.app.log("Main", "Loaded Level " + level + " | Width: " + gameContext.currentLevelWidth);
    }

    @Override
    public void saveProgress() {
        if (debugManager.isDebugModeActive()) {
            debugManager.logSkippedAction("Save to backend");
            Gdx.app.log("Main", "Game saved (debug mode - skipped backend)");
            return;
        }

        if (gameContext.currentPlayerData == null) {
            Gdx.app.error("Main", "Cannot save - no player data");
            return;
        }

        playerApi.saveProgress(
            gameContext.currentPlayerData.playerId,
            gameContext.currentLevel,
            gameContext.coinsCollectedThisSession,
            new PlayerApiService.SaveCallback() {
                @Override
                public void onSuccess() {
                    Gdx.app.log("Main", "Progress saved successfully");
                    gameContext.currentPlayerData.lastStage = gameContext.currentLevel;
                    gameContext.currentPlayerData.totalCoins += gameContext.coinsCollectedThisSession;
                    gameContext.coinsCollectedThisSession = 0;
                }

                @Override
                public void onFailure(String error) {
                    Gdx.app.error("Main", "Failed to save: " + error);
                }
            });
    }

    @Override
    public void restartGame() {
        gameContext.clearGameObjects();
        gameContext.resetPlayer();
        gameContext.currentLevel = 1;
        gameContext.coinScore = 0;
        loadLevel(1);
        screenManager.setScreen(GameState.PLAYING);
        Gdx.app.log("Main", "Game restarted");
    }

    @Override
    public void restartToUsernameInput() {
        gameContext.clearGameObjects();
        gameContext.resetPlayer();
        gameContext.usernameInput.setLength(0);
        gameContext.username = "";
        gameContext.currentLevel = 1;
        gameContext.coinScore = 0;
        gameContext.currentPlayerData = null;
        screenManager.setScreen(GameState.USERNAME_INPUT);
        Gdx.app.log("Main", "Returned to username input");
    }

    // ==================== LEVEL HELPER METHODS ====================

    private void setupCoinSpawnLocations(int level) {
        gameContext.coinSpawnLocations = new Array<>();

        final float PLATFORM_OFFSET = 30f;
        final float GROUND_Y = 50f;
        final float GROUND_HEIGHT = 50f;
        final float PLATFORM_HEIGHT = 20f;
        final float JUMP_OFFSET = 500f / 2.5f - 50f;

        switch (level) {
            case 1:
                gameContext.coinSpawnLocations.add(new float[]{600f, 200f + PLATFORM_HEIGHT + PLATFORM_OFFSET});
                gameContext.coinSpawnLocations.add(new float[]{900f, GROUND_Y + GROUND_HEIGHT + PLATFORM_OFFSET});
                gameContext.coinSpawnLocations.add(new float[]{1300f, 300f + PLATFORM_HEIGHT + PLATFORM_OFFSET});
                gameContext.coinSpawnLocations.add(new float[]{1700f, GROUND_Y + GROUND_HEIGHT + JUMP_OFFSET});
                break;
            case 2:
                gameContext.coinSpawnLocations.add(new float[]{650f, 150f + PLATFORM_HEIGHT + PLATFORM_OFFSET});
                gameContext.coinSpawnLocations.add(new float[]{950f, 250f + PLATFORM_HEIGHT + PLATFORM_OFFSET});
                gameContext.coinSpawnLocations.add(new float[]{1300f, 350f + PLATFORM_HEIGHT + PLATFORM_OFFSET});
                gameContext.coinSpawnLocations.add(new float[]{1650f, GROUND_Y + GROUND_HEIGHT + PLATFORM_OFFSET});
                gameContext.coinSpawnLocations.add(new float[]{1900f, 200f + PLATFORM_HEIGHT + PLATFORM_OFFSET});
                break;
            case 3:
                gameContext.coinSpawnLocations.add(new float[]{550f, 200f + PLATFORM_HEIGHT + PLATFORM_OFFSET});
                gameContext.coinSpawnLocations.add(new float[]{1150f, 200f + PLATFORM_HEIGHT + PLATFORM_OFFSET});
                break;
            case 4:
                gameContext.coinSpawnLocations.add(new float[]{750f, 300f + PLATFORM_HEIGHT + PLATFORM_OFFSET});
                gameContext.coinSpawnLocations.add(new float[]{1050f, 200f + PLATFORM_HEIGHT + PLATFORM_OFFSET});
                gameContext.coinSpawnLocations.add(new float[]{1400f, 350f + PLATFORM_HEIGHT + PLATFORM_OFFSET});
                gameContext.coinSpawnLocations.add(new float[]{1650f, 250f + PLATFORM_HEIGHT + PLATFORM_OFFSET});
                gameContext.coinSpawnLocations.add(new float[]{2000f, 180f + PLATFORM_HEIGHT + PLATFORM_OFFSET});
                break;
            case 5:
                gameContext.coinSpawnLocations.add(new float[]{300f, 200f + PLATFORM_HEIGHT + PLATFORM_OFFSET});
                gameContext.coinSpawnLocations.add(new float[]{800f, 200f + PLATFORM_HEIGHT + PLATFORM_OFFSET});
                gameContext.coinSpawnLocations.add(new float[]{550f, 330f + PLATFORM_HEIGHT + PLATFORM_OFFSET});
                break;
        }
    }

    private void spawnCoinsForLevel() {
        for (float[] location : gameContext.coinSpawnLocations) {
            Array<Coin> spawnedCoins = coinPattern.spawn(coinPool, location[0], location[1]);
            activeCoins.addAll(spawnedCoins);
        }
        Gdx.app.log("Coins", "Spawned " + activeCoins.size + " coins");
    }

    // ==================== LIFECYCLE ====================

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // Debug mode activation check
        if (debugManager.checkDebugActivation()) {
            gameContext.username = debugManager.getDebugUsername();
            gameContext.currentPlayerData = debugManager.createDebugPlayerData();
            gameContext.coinsCollectedThisSession = 0;
            gameContext.isNewPlayer = true;
            loadLevel(debugManager.getDebugStartingLevel());
            screenManager.setScreen(GameState.PLAYING);
            return;
        }

        // Render via ScreenManager
        screenManager.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        screenManager.resize(width, height);
    }

    @Override
    public void dispose() {
        // Dispose batch and shapeRenderer
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();

        // Dispose screen manager
        if (screenManager != null) screenManager.dispose();

        // Dispose AssetManager (handles all textures and fonts)
        if (assetManager != null) assetManager.dispose();

        Gdx.app.log("Main", "Game disposed successfully");
    }
}
