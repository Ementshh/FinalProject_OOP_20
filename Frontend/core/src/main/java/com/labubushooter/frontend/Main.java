package com.labubushooter.frontend;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.objects.Coin;
import com.labubushooter.frontend.objects.CommonEnemy;
import com.labubushooter.frontend.objects.EnemyBullet;
import com.labubushooter.frontend.objects.FinalBoss;
import com.labubushooter.frontend.objects.Ground;
import com.labubushooter.frontend.objects.MiniBossEnemy;
import com.labubushooter.frontend.objects.Platform;
import com.labubushooter.frontend.objects.Player;
import com.labubushooter.frontend.patterns.CoinPattern;
import com.labubushooter.frontend.patterns.LevelStrategy;
import com.labubushooter.frontend.patterns.ShootingStrategy;
import com.labubushooter.frontend.patterns.levels.*;
import com.labubushooter.frontend.patterns.coins.LinePattern;
import com.labubushooter.frontend.patterns.weapons.Mac10Strategy;
import com.labubushooter.frontend.patterns.weapons.PistolStrategy;
import com.labubushooter.frontend.services.PlayerApiService;
import com.labubushooter.frontend.services.PlayerApiService.PlayerData;
import com.labubushooter.frontend.systems.CollisionEventBus;
import com.labubushooter.frontend.systems.CollisionDetectionSystem;
import com.labubushooter.frontend.collision.*;
import com.labubushooter.frontend.events.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main extends ApplicationAdapter {
    SpriteBatch batch;
    ShapeRenderer shapeRenderer;
    OrthographicCamera camera;
    Viewport viewport;

    static final float VIEWPORT_WIDTH = 1066f; // 16:9 aspect ratio
    static final float VIEWPORT_HEIGHT = 600f;

    private final float LEVEL_EXIT_THRESHOLD = 100f;

    private float currentLevelWidth;
    private int currentLevel = 1;
    private Map<Integer, LevelStrategy> levelStrategy;

    Texture playerTex, platformTex, groundTex, bulletTex, exitTex;
    Texture pistolTex, mac10Tex;
    Texture debugTex;
    Texture levelIndicatorTex;
    Texture enemyTex;

    // Boss textures
    Texture miniBossTex, bossTex, enemyBulletTex;
    Texture whiteFlashTex, redFlashTex, yellowFlashTex;
    Texture backgroundTex;

    Player player;
    Array<Platform> platforms;
    Array<Ground> grounds;

    Pool<Bullet> bulletPool;
    Array<Bullet> activeBullets;

    // Enemy Pool System
    Pool<CommonEnemy> enemyPool;
    Array<CommonEnemy> activeEnemies;
    private long lastEnemySpawnTime;
    private long nextEnemySpawnDelay;
    private Random random;

    // Boss System
    MiniBossEnemy miniBoss;
    FinalBoss boss;
    Pool<EnemyBullet> enemyBulletPool;
    Array<EnemyBullet> activeEnemyBullets;

    // Coin System
    Pool<Coin> coinPool;
    Array<Coin> activeCoins;
    CoinPattern coinPattern;
    private int coinScore = 0;
    private Array<float[]> coinSpawnLocations;

    // MAX ENEMY PER LEVEL
    private static final int MAX_ENEMIES_LEVEL1 = 6;
    private static final int MAX_ENEMIES_LEVEL2 = 7;
    private static final int MAX_ENEMIES_LEVEL4 = 8;

    // Enemy spawn delays per level (in nanoseconds)
    // Level 1: 7-10 seconds
    private static final long LEVEL1_MIN_SPAWN = 3000000000L;
    private static final long LEVEL1_MAX_SPAWN = 4000000000L;

    // Level 2: 6-8 seconds
    private static final long LEVEL2_MIN_SPAWN = 2000000000L;
    private static final long LEVEL2_MAX_SPAWN = 4000000000L;

    // Level 4: 4-7 seconds
    private static final long LEVEL4_MIN_SPAWN = 1000000000L;
    private static final long LEVEL4_MAX_SPAWN = 3000000000L;

    PistolStrategy pistolStrategy;
    Mac10Strategy mac10Strategy;

    // Game Over System
    private boolean isGameOver = false;
    private boolean isVictory = false;
    private BitmapFont font;
    private BitmapFont smallFont;
    private GlyphLayout layout;

    // Game State System
    private GameState gameState = GameState.USERNAME_INPUT;
    private String username = "";
    private static final int MAX_USERNAME_LENGTH = 20;

    // UI Textures
    private Texture buttonTex;
    private Texture buttonHoverTex;

    // Pause Menu Buttons
    private Rectangle continueButton;
    private Rectangle saveButton;
    private Rectangle newGameButton;
    private Rectangle quitButton;

    // Restart Confirmation Buttons
    private Rectangle confirmYesButton;
    private Rectangle confirmNoButton;

    // Username Input
    private Rectangle startGameButton;
    private StringBuilder usernameInput;

    // Backend Integration
    private PlayerApiService playerApi;
    private PlayerData currentPlayerData;
    private int coinsCollectedThisSession;
    private boolean isNewPlayer;

    // Continue/New Game buttons
    private Rectangle continueGameButton;
    private Rectangle newGameButtonMenu;

    // Collision System
    private CollisionEventBus collisionEventBus;
    private CollisionDetectionSystem collisionDetectionSystem;
    private PlayerEnemyCollisionHandler playerEnemyCollisionHandler;
    private BulletEnemyCollisionHandler bulletEnemyCollisionHandler;
    private EnemyBulletPlayerCollisionHandler enemyBulletPlayerCollisionHandler;
    private CoinCollectionHandler coinCollectionHandler;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Setup Camera with ExtendViewport for fullscreen scaling
        // ExtendViewport maintains minimum 800x600 world units and extends horizontally
        // on wider screens
        // No black bars - fills entire screen while keeping gameplay area consistent
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIEWPORT_WIDTH / 2, VIEWPORT_HEIGHT / 2, 0);
        camera.update();

        // Create Fonts
        font = new BitmapFont();
        font.getData().setScale(3f);
        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.5f);
        layout = new GlyphLayout();

        // Create Textures
        playerTex = new Texture(Gdx.files.internal("player.png"));
        playerTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        platformTex = new Texture(Gdx.files.internal("ground.png"));
        platformTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        groundTex = new Texture(Gdx.files.internal("ground_base.png"));
        groundTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        bulletTex = createColorTexture(10, 5, Color.YELLOW);
        pistolTex = createColorTexture(20, 10, Color.GRAY);
        mac10Tex = createColorTexture(30, 15, Color.LIME);
        exitTex = new Texture(Gdx.files.internal("door.png"));
        exitTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        debugTex = createColorTexture(10, 600, Color.RED); // Debug marker
        levelIndicatorTex = createColorTexture(30, 30, Color.YELLOW); // Level indicator
        enemyTex = new Texture(Gdx.files.internal("enemy.png"));
        enemyTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Boss textures
        miniBossTex = new Texture(Gdx.files.internal("miniboss.png"));
        miniBossTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        bossTex = new Texture(Gdx.files.internal("boss.png"));
        bossTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        enemyBulletTex = createColorTexture(8, 8, Color.ORANGE);
        whiteFlashTex = createColorTexture(60, 90, Color.WHITE);
        redFlashTex = createColorTexture(60, 100, Color.RED);
        yellowFlashTex = createColorTexture(60, 90, Color.YELLOW);

        // Setup Bullet Pool
        bulletPool = new Pool<Bullet>() {
            @Override
            protected Bullet newObject() {
                return new Bullet();
            }
        };
        activeBullets = new Array<>();

        // Setup Enemy Pool
        random = new Random();

        // Load background image
        backgroundTex = new Texture(Gdx.files.internal("background.png"));
        backgroundTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        enemyPool = new Pool<CommonEnemy>() {
            @Override
            protected CommonEnemy newObject() {
                return new CommonEnemy(enemyTex);
            }
        };
        activeEnemies = new Array<>();
        resetEnemySpawnTimer();

        // Setup Enemy Bullet Pool
        enemyBulletPool = new Pool<EnemyBullet>() {
            @Override
            protected EnemyBullet newObject() {
                return new EnemyBullet();
            }
        };
        activeEnemyBullets = new Array<>();

        // Setup Coin Pool
        coinPool = new Pool<Coin>() {
            @Override
            protected Coin newObject() {
                return new Coin();
            }
        };
        activeCoins = new Array<>();
        coinPattern = new LinePattern();

        pistolStrategy = new PistolStrategy();
        mac10Strategy = new Mac10Strategy();

        player = new Player(playerTex);
        player.pistolTex = pistolTex;
        player.mac10Tex = mac10Tex;
        player.camera = camera;
        player.setWeapon(null);

        // Initialize Level Strategies
        levelStrategy = new HashMap<>();
        levelStrategy.put(1, new Level1Strategy());
        levelStrategy.put(2, new Level2Strategy());
        levelStrategy.put(3, new Level3Strategy());
        levelStrategy.put(4, new Level4Strategy());
        levelStrategy.put(5, new Level5Strategy());

        // Create UI textures
        buttonTex = createColorTexture(500, 80, new Color(0.7f, 0.7f, 0.7f, 1f));
        buttonHoverTex = createColorTexture(500, 80, new Color(0.9f, 0.9f, 0.9f, 1f));

        // Initialize username input
        usernameInput = new StringBuilder();

        // Initialize pause menu buttons (centered on screen)
        float buttonWidth = 500f;
        float buttonHeight = 80f;
        float centerX = VIEWPORT_WIDTH / 2 - buttonWidth / 2;

        continueButton = new Rectangle(centerX, 370, buttonWidth, buttonHeight);
        saveButton = new Rectangle(centerX, 270, buttonWidth, buttonHeight);
        newGameButton = new Rectangle(centerX, 170, buttonWidth, buttonHeight);
        quitButton = new Rectangle(centerX, 70, buttonWidth, buttonHeight);

        // Confirmation buttons (smaller)
        float confirmButtonWidth = 200f;
        confirmYesButton = new Rectangle(VIEWPORT_WIDTH / 2 - confirmButtonWidth - 20, 200, confirmButtonWidth,
                buttonHeight);
        confirmNoButton = new Rectangle(VIEWPORT_WIDTH / 2 + 20, 200, confirmButtonWidth, buttonHeight);

        // Start game button
        startGameButton = new Rectangle(centerX, 200, buttonWidth, buttonHeight);

        // Initialize API service
        playerApi = new PlayerApiService();
        coinsCollectedThisSession = 0;

        // Continue/New Game buttons
        continueGameButton = new Rectangle(centerX, 270, buttonWidth, buttonHeight);
        newGameButtonMenu = new Rectangle(centerX, 170, buttonWidth, buttonHeight);

        // Initialize Collision System
        collisionEventBus = new CollisionEventBus();

        // Create handlers with constructor dependencies
        playerEnemyCollisionHandler = new PlayerEnemyCollisionHandler();
        bulletEnemyCollisionHandler = new BulletEnemyCollisionHandler(bulletPool, activeBullets);
        enemyBulletPlayerCollisionHandler = new EnemyBulletPlayerCollisionHandler(enemyBulletPool, activeEnemyBullets);
        coinCollectionHandler = new CoinCollectionHandler(coinPool, activeCoins);

        // Subscribe handlers to event types
        collisionEventBus.subscribe(PlayerEnemyCollisionEvent.class, playerEnemyCollisionHandler);
        collisionEventBus.subscribe(BulletEnemyCollisionEvent.class, bulletEnemyCollisionHandler);
        collisionEventBus.subscribe(EnemyBulletPlayerCollisionEvent.class, enemyBulletPlayerCollisionHandler);
        collisionEventBus.subscribe(PlayerCoinCollisionEvent.class, coinCollectionHandler);

        // Create detection system
        collisionDetectionSystem = new CollisionDetectionSystem(collisionEventBus);

        // Don't load level yet - wait for username input
        // loadLevel(currentLevel);
    }

    private void resetEnemySpawnTimer() {
        lastEnemySpawnTime = TimeUtils.nanoTime();

        // Set spawn delay based on current level
        long minDelay, maxDelay;

        switch (currentLevel) {
            case 1:
                minDelay = LEVEL1_MIN_SPAWN;
                maxDelay = LEVEL1_MAX_SPAWN;
                break;
            case 2:
                minDelay = LEVEL2_MIN_SPAWN;
                maxDelay = LEVEL2_MAX_SPAWN;
                break;
            case 4:
                minDelay = LEVEL4_MIN_SPAWN;
                maxDelay = LEVEL4_MAX_SPAWN;
                break;
            default:
                // Level 3 and 5 don't spawn enemies, but set default just in case
                minDelay = 10000000000L;
                maxDelay = 15000000000L;
                break;
        }

        nextEnemySpawnDelay = minDelay + (long) (random.nextFloat() * (maxDelay - minDelay));
    }

    // Method to spawn initial enemies at level start
    private void spawnInitialEnemiesForLevel(int level) {
        // Only spawn initial enemy for levels 1, 2, and 4
        if (level != 1 && level != 2 && level != 4) {
            return;
        }

        // Get spawn positions for this level
        float[] spawnPositions = getInitialEnemySpawnPositions(level);

        for (float spawnX : spawnPositions) {
            CommonEnemy enemy = enemyPool.obtain();
            enemy.init(spawnX, player, level);
            activeEnemies.add(enemy);
            Gdx.app.log("InitialEnemy", "Spawned initial enemy at X: " + spawnX + " for level " + level);
        }
    }

    // Get initial enemy spawn positions per level
    private float[] getInitialEnemySpawnPositions(int level) {
        switch (level) {
            case 1:
                // Level 1: Spawn 1 enemy at middle platform (blocking path)
                return new float[] { 1200f };

            case 2:
                // Level 2: Spawn 1 enemy at stairway area
                return new float[] { 1000f };

            case 4:
                // Level 4: Spawn 1 enemy at vertical tower area
                return new float[] { 1400f };

            default:
                return new float[] {}; // No initial spawn
        }
    }

    private void spawnEnemy() {
        float cameraLeft = camera.position.x - viewport.getWorldWidth() / 2;
        float cameraRight = camera.position.x + viewport.getWorldWidth() / 2;

        // Spawn area dengan buffer zone di luar layar
        final float SPAWN_BUFFER = 200f; // Jarak spawn di luar layar
        final float PLAYER_SAFETY_ZONE = 300f; // Jarak minimum dari player

        float spawnX;
        int attempts = 0;
        final int MAX_ATTEMPTS = 20;

        do {
            // Pilih spawn di kiri atau kanan layar secara random
            boolean spawnLeft = random.nextBoolean();

            if (spawnLeft) {
                // Spawn di kiri layar (di luar view)
                spawnX = cameraLeft - SPAWN_BUFFER - random.nextFloat() * 100f;
                // Pastikan tidak keluar dari level bounds
                if (spawnX < 0)
                    spawnX = cameraRight + SPAWN_BUFFER + random.nextFloat() * 100f;
            } else {
                // Spawn di kanan layar (di luar view)
                spawnX = cameraRight + SPAWN_BUFFER + random.nextFloat() * 100f;
                // Pastikan tidak keluar dari level bounds
                if (spawnX > currentLevelWidth - 100f)
                    spawnX = cameraLeft - SPAWN_BUFFER - random.nextFloat() * 100f;
            }

            attempts++;

            // Break jika sudah terlalu banyak percobaan
            if (attempts >= MAX_ATTEMPTS) {
                // Fallback: spawn di ujung level yang jauh dari player
                if (player.bounds.x < currentLevelWidth / 2) {
                    spawnX = currentLevelWidth - 200f; // Spawn di kanan
                } else {
                    spawnX = 100f; // Spawn di kiri
                }
                break;
            }

        } while (Math.abs(spawnX - player.bounds.x) < PLAYER_SAFETY_ZONE ||
                (spawnX >= cameraLeft && spawnX <= cameraRight)); // Pastikan di luar layar

        CommonEnemy enemy = enemyPool.obtain();
        enemy.init(spawnX, player, currentLevel);
        activeEnemies.add(enemy);

        Gdx.app.log("EnemySpawn", "Spawned at X: " + spawnX +
                " (Camera: " + cameraLeft + " - " + cameraRight +
                ", Player: " + player.bounds.x + ")");
    }

    private int getMaxEnemiesForLevel(int level) {
        switch (level) {
            case 1:
                return MAX_ENEMIES_LEVEL1; // 6 enemies
            case 2:
                return MAX_ENEMIES_LEVEL2; // 7 enemies
            case 4:
                return MAX_ENEMIES_LEVEL4; // 8 enemies
            default:
                return 5; // Default
        }
    }

    private void restartGame() {
        gameState = GameState.PLAYING;
        currentLevel = 1;

        // Clear all enemies
        for (CommonEnemy enemy : activeEnemies) {
            enemyPool.free(enemy);
        }
        activeEnemies.clear();

        // Clear all bullets
        activeBullets.clear();

        // Clear all coins
        for (Coin coin : activeCoins) {
            coinPool.free(coin);
        }
        activeCoins.clear();
        coinScore = 0;
        coinCollectionHandler.setCoinScore(0);
        coinCollectionHandler.setCoinsCollectedThisSession(0);

        // Reset player
        player.reset();
        player.setWeapon(null);

        // Reload level 1
        loadLevel(1);

        Gdx.app.log("Game", "Game Restarted");
    }

    private void loadLevel(int level) {
        LevelStrategy strategy = levelStrategy.get(level);

        if (strategy == null) {
            System.out.println("Level " + level + " not found!");
            return;
        }

        this.currentLevel = level;

        if (platforms == null) {
            platforms = new Array<Platform>();
        } else {
            platforms.clear();
        }
        if (grounds == null) {
            grounds = new Array<Ground>();
        } else {
            grounds.clear();
        }
        activeBullets.clear();

        // Clear enemies when loading new level
        for (CommonEnemy enemy : activeEnemies) {
            enemyPool.free(enemy);
        }
        activeEnemies.clear();
        resetEnemySpawnTimer();

        // Clear enemy bullets
        for (EnemyBullet eb : activeEnemyBullets) {
            enemyBulletPool.free(eb);
        }
        activeEnemyBullets.clear();

        // Clear coins
        for (Coin coin : activeCoins) {
            coinPool.free(coin);
        }
        activeCoins.clear();

        // Spawn boss for levels 3 and 5
        if (level == 3) {
            miniBoss = new MiniBossEnemy(miniBossTex, whiteFlashTex, yellowFlashTex);
            miniBoss.init(strategy.getBossSpawnX(), strategy.getBossSpawnY());
            boss = null;
            Gdx.app.log("Level3", "Mini Boss spawned!");
        } else if (level == 5) {
            boss = new FinalBoss(bossTex, redFlashTex, enemyBulletTex, yellowFlashTex);
            boss.init(strategy.getBossSpawnX(), strategy.getBossSpawnY());
            miniBoss = null;
            Gdx.app.log("Level5", "Final Boss spawned!");
        } else {
            miniBoss = null;
            boss = null;
        }

        currentLevelWidth = strategy.getLevelWidth();

        // Special handling for Level 5: Match viewport width for perfect edge-to-edge
        if (level == 5) {
            currentLevelWidth = Math.max(currentLevelWidth, viewport.getWorldWidth());
            Gdx.app.log("Level5",
                    "Dynamic Width: " + currentLevelWidth + " (Viewport: " + viewport.getWorldWidth() + ")");
        }

        strategy.loadPlatforms(platforms, platformTex);
        strategy.loadGround(grounds, groundTex);

        player.bounds.setPosition(strategy.getPlayerStartX(), strategy.getPlayerStartY());

        Player.LEVEL_WIDTH = currentLevelWidth;

        camera.position.x = VIEWPORT_WIDTH / 2;
        camera.update();

        // Setup and spawn coins for this level
        setupCoinSpawnLocations(level);
        spawnCoinsForLevel();

        // Spawn initial enemies for level 1, 2, and 4
        spawnInitialEnemiesForLevel(level);

        Gdx.app.log("Game", "Loaded Level " + level + " | Width: " + currentLevelWidth);
    }

    private void setupCoinSpawnLocations(int level) {
        coinSpawnLocations = new Array<>();

        // Konstanta untuk posisi coin
        final float PLATFORM_OFFSET = 30f; // Jarak di atas platform
        final float GROUND_Y = 50f; // Ground base Y
        final float GROUND_HEIGHT = 50f; // Ground height
        final float PLATFORM_HEIGHT = 20f; // Platform height (dari createColorTexture)
        final float JUMP_OFFSET = 500f / 2.5f - 50f; // (JUMP_POWER / 2.5) - 50f = 150f

        switch (level) {
            case 1:
                // Level 1: Coin di atas platform dan ground
                // Platform di x=500, y=200 -> coin di y=200+20+30 = 250
                coinSpawnLocations.add(new float[] { 600f, 200f + PLATFORM_HEIGHT + PLATFORM_OFFSET });

                // Ground y=50 + 50(tinggi) = 100 -> coin di y=100+30 = 130
                coinSpawnLocations.add(new float[] { 900f, GROUND_Y + GROUND_HEIGHT + PLATFORM_OFFSET });

                // Platform di x=1200, y=300 -> coin di y=300+20+30 = 350
                coinSpawnLocations.add(new float[] { 1300f, 300f + PLATFORM_HEIGHT + PLATFORM_OFFSET });

                // Coin di udara (tinggi lompatan dari ground)
                coinSpawnLocations.add(new float[] { 1700f, GROUND_Y + GROUND_HEIGHT + JUMP_OFFSET });
                break;

            case 2:
                // Level 2: Mengikuti pola stairway
                // Platform x=600, y=150 -> y=150+20+30 = 200
                coinSpawnLocations.add(new float[] { 650f, 150f + PLATFORM_HEIGHT + PLATFORM_OFFSET });

                // Platform x=900, y=250 -> y=250+20+30 = 300
                coinSpawnLocations.add(new float[] { 950f, 250f + PLATFORM_HEIGHT + PLATFORM_OFFSET });

                // Platform x=1200, y=350 -> y=350+20+30 = 400
                coinSpawnLocations.add(new float[] { 1300f, 350f + PLATFORM_HEIGHT + PLATFORM_OFFSET });

                // Ground area
                coinSpawnLocations.add(new float[] { 1650f, GROUND_Y + GROUND_HEIGHT + PLATFORM_OFFSET });

                // Platform x=1800, y=200 -> y=200+20+30 = 250
                coinSpawnLocations.add(new float[] { 1900f, 200f + PLATFORM_HEIGHT + PLATFORM_OFFSET });
                break;

            case 3:
                // Level 3: Boss level - coins di safe spots
                // Platform x=500, y=200 -> y=200+20+30 = 250
                coinSpawnLocations.add(new float[] { 550f, 200f + PLATFORM_HEIGHT + PLATFORM_OFFSET });

                // Platform x=1100, y=200 -> y=200+20+30 = 250
                coinSpawnLocations.add(new float[] { 1150f, 200f + PLATFORM_HEIGHT + PLATFORM_OFFSET });
                break;

            case 4:
                // Level 4: Vertical tower pattern
                // Platform x=700, y=300 -> y=300+20+30 = 350
                coinSpawnLocations.add(new float[] { 750f, 300f + PLATFORM_HEIGHT + PLATFORM_OFFSET });

                // Platform x=1000, y=200 -> y=200+20+30 = 250
                coinSpawnLocations.add(new float[] { 1050f, 200f + PLATFORM_HEIGHT + PLATFORM_OFFSET });

                // Platform x=1300, y=350 -> y=350+20+30 = 400
                coinSpawnLocations.add(new float[] { 1400f, 350f + PLATFORM_HEIGHT + PLATFORM_OFFSET });

                // Platform x=1600, y=250 -> y=250+20+30 = 300
                coinSpawnLocations.add(new float[] { 1650f, 250f + PLATFORM_HEIGHT + PLATFORM_OFFSET });

                // Platform x=1900, y=180 -> y=180+20+30 = 230
                coinSpawnLocations.add(new float[] { 2000f, 180f + PLATFORM_HEIGHT + PLATFORM_OFFSET });
                break;

            case 5:
                // Level 5: Boss arena
                // Platform kiri x=200, y=200, width=200 -> coin at center x=300, y=200+20+30 =
                // 250
                coinSpawnLocations.add(new float[] { 300f, 200f + PLATFORM_HEIGHT + PLATFORM_OFFSET });

                // Platform kanan x=700, y=200, width=200 -> coin at center x=800, y=200+20+30 =
                // 250
                coinSpawnLocations.add(new float[] { 800f, 200f + PLATFORM_HEIGHT + PLATFORM_OFFSET });

                // Platform tengah elevated x=450, y=330, width=200 -> coin at center x=550,
                // y=330+20+30 = 380
                coinSpawnLocations.add(new float[] { 550f, 330f + PLATFORM_HEIGHT + PLATFORM_OFFSET });
                break;
        }

        Gdx.app.log("CoinSpawn", "Setup " + coinSpawnLocations.size + " spawn locations for level " + level);
    }

    private void spawnCoinsForLevel() {
        for (float[] location : coinSpawnLocations) {
            Array<Coin> spawnedCoins = coinPattern.spawn(coinPool, location[0], location[1]);
            activeCoins.addAll(spawnedCoins);
        }

        Gdx.app.log("Coins", "Spawned " + activeCoins.size + " coins for level " + currentLevel);
    }

    private Texture createColorTexture(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // Handle different game states
        switch (gameState) {
            case USERNAME_INPUT:
                handleUsernameInput();
                renderUsernameInput();
                return;

            case LOADING_PLAYER_DATA:
                renderLoading();
                return;

            case CONTINUE_OR_NEW:
                handleContinueOrNew();
                renderContinueOrNew();
                return;

            case PAUSED:
                handlePauseMenu();
                renderPauseMenu();
                return;

            case RESTART_CONFIRM:
                handleRestartConfirm();
                renderRestartConfirm();
                return;

            case VICTORY:
                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    restartToUsernameInput();
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    Gdx.app.exit();
                }
                renderVictory();
                return;

            case GAME_OVER:
                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    restartToUsernameInput();
                }
                renderGameOver();
                return;

            case PLAYING:
                // Game logic here
                break;
        }

        // Check if player is dead
        if (player.isDead()) {
            gameState = GameState.GAME_OVER;
            Gdx.app.log("Game", "GAME OVER");
            return;
        }

        // Check for PAUSE
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            gameState = GameState.PAUSED;
            Gdx.app.log("Game", "PAUSED");
            return;
        }

        // --- SECRET LEVEL SKIP ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            loadLevel(3);
            Gdx.app.log("Debug", "Skipped to Level 3 (Mini Boss)");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            loadLevel(5);
            Gdx.app.log("Debug", "Skipped to Level 5 (Final Boss)");
        }

        // --- DEBUG: INSTA-KILL BOSS ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            if (currentLevel == 3 && miniBoss != null && !miniBoss.isDead()) {
                miniBoss.takeDamage(999999f);
                Gdx.app.log("Debug", "Mini Boss instantly killed!");
            }
            if (currentLevel == 5 && boss != null && !boss.isDead()) {
                boss.takeDamage(999999f);
                Gdx.app.log("Debug", "Final Boss instantly killed!");
            }
        }

        // --- WEAPON SWITCHING ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            player.setWeapon(pistolStrategy);
            Gdx.app.log("WeaponSystem", "Pistol Equipped");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            player.setWeapon(mac10Strategy);
            Gdx.app.log("WeaponSystem", "Mac-10 Equipped");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            player.setWeapon(null);
            Gdx.app.log("WeaponSystem", "Unarmed");
        }

        // --- JUMP ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
            player.jump();

        // --- MOUSE SHOOTING ---
        ShootingStrategy currentWeapon = player.getWeapon();
        if (currentWeapon != null) {
            if (currentWeapon.isAutomatic()) {
                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                    player.shoot(activeBullets, bulletPool);
                }
            } else {
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    player.shoot(activeBullets, bulletPool);
                }
            }
        }

        // --- UPDATE ---
        player.update(delta, platforms, grounds);

        // Update bosses
        if (currentLevel == 3 && miniBoss != null && !miniBoss.isDead()) {
            miniBoss.update(delta, platforms, grounds, player);
        }

        if (currentLevel == 5 && boss != null && !boss.isDead()) {
            boss.update(delta, platforms, grounds, player, activeEnemyBullets, enemyBulletPool);
        }

        // Update enemy bullets
        for (int i = activeEnemyBullets.size - 1; i >= 0; i--) {
            EnemyBullet eb = activeEnemyBullets.get(i);
            eb.update(delta);

            if (eb.isOutOfBounds(currentLevelWidth, viewport.getWorldHeight())) {
                activeEnemyBullets.removeIndex(i);
                enemyBulletPool.free(eb);
            }
        }

        // Spawn enemy based on timer - DENGAN BATASAN JUMLAH
        if (currentLevel != 3 && currentLevel != 5) {
            if (TimeUtils.nanoTime() - lastEnemySpawnTime > nextEnemySpawnDelay) {
                int maxEnemies = getMaxEnemiesForLevel(currentLevel);

                // Hanya spawn jika belum mencapai batas
                if (activeEnemies.size < maxEnemies) {
                    spawnEnemy();
                    resetEnemySpawnTimer();
                    Gdx.app.log("EnemySpawn", "Active enemies: " + activeEnemies.size + "/" + maxEnemies);
                } else {
                    // Reset timer agar tidak terus mengecek
                    resetEnemySpawnTimer();
                }
            }
        }

        // Update enemies dengan platform collision
        for (int i = activeEnemies.size - 1; i >= 0; i--) {
            CommonEnemy enemy = activeEnemies.get(i);
            enemy.update(delta, platforms, grounds);

            if (!enemy.isActive()) {
                activeEnemies.removeIndex(i);
                enemyPool.free(enemy);
            }
        }

        // Collision Detection System - handles all combat collisions and coin
        // collection
        collisionDetectionSystem.detectCollisions(
                activeBullets,
                activeEnemies,
                activeEnemyBullets,
                activeCoins,
                player,
                miniBoss,
                boss,
                currentLevel);

        // Sync coin scores from handler to Main
        coinScore = coinCollectionHandler.getCoinScore();
        coinsCollectedThisSession = coinCollectionHandler.getCoinsCollectedThisSession();

        // Check Level Exit (with boss defeat requirement)
        boolean bossDefeated = true;
        if (currentLevel == 3 && miniBoss != null) {
            bossDefeated = miniBoss.isDead();
        }
        if (currentLevel == 5 && boss != null) {
            bossDefeated = boss.isDead();
        }

        if (bossDefeated && player.bounds.x + player.bounds.width >= currentLevelWidth - LEVEL_EXIT_THRESHOLD) {
            if (currentLevel == 5) {
                // Level 5 completed - trigger victory!
                saveGameProgress(); // Save final progress
                gameState = GameState.VICTORY;
                Gdx.app.log("Game", "VICTORY! Game Completed!");
            } else {
                saveGameProgress(); // Save after each level
                loadLevel(currentLevel + 1);
            }
        }

        // --- CAMERA FOLLOW LOGIC ---
        float halfViewport = viewport.getWorldWidth() / 2;
        float levelMid = currentLevelWidth / 2;

        if (viewport.getWorldWidth() >= currentLevelWidth) {
            camera.position.x = levelMid;
        } else {
            float targetX = player.bounds.x + player.bounds.width / 2;
            camera.position.x = MathUtils.clamp(targetX, halfViewport, currentLevelWidth - halfViewport);
        }

        camera.update();

        // Update Coins - collision detection handled by CollisionDetectionSystem
        for (int i = activeCoins.size - 1; i >= 0; i--) {
            Coin coin = activeCoins.get(i);
            coin.update(delta);
            // Coin collection is now handled by CoinCollectionHandler
            // Coin TIDAK dihapus ketika keluar layar
        }

        // Update Bullets dengan collision check
        for (int i = activeBullets.size - 1; i >= 0; i--) {
            Bullet b = activeBullets.get(i);
            b.update(delta);

            boolean shouldRemove = false;

            // Check collision with platforms (walls)
            for (Platform p : platforms) {
                if (b.bounds.overlaps(p.bounds)) {
                    shouldRemove = true;
                    break;
                }
            }

            // Check if bullet traveled too far vertically
            if (!shouldRemove && b.isOutOfVerticalBounds(viewport.getWorldHeight())) {
                shouldRemove = true;
            }

            if (shouldRemove) {
                activeBullets.removeIndex(i);
                bulletPool.free(b);
            }
        }

        // --- DRAW ---
        renderGame();
    }

    private void renderGame() {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw background at native resolution (crop if doesn't fit)
        if (backgroundTex != null) {
            // Get actual texture dimensions
            float bgWidth = backgroundTex.getWidth();
            float bgHeight = backgroundTex.getHeight();

            // Position background at world origin - scrolls naturally with camera
            float bgX = 0; // Start from world origin
            float bgY = (VIEWPORT_HEIGHT - bgHeight) / 2; // Centered vertically

            // Draw at native size - will crop naturally if larger than viewport
            batch.draw(backgroundTex, bgX, bgY, bgWidth, bgHeight);
        }

        // Draw grounds
        for (Ground g : grounds)
            g.draw(batch);

        // Draw platforms
        for (Platform p : platforms)
            p.draw(batch);

        // Draw exit door (conditional for boss levels)
        boolean bossDefeated = true;
        if (currentLevel == 3 && miniBoss != null) {
            bossDefeated = miniBoss.isDead();
        }
        if (currentLevel == 5 && boss != null) {
            bossDefeated = boss.isDead();
        }

        if (bossDefeated) {
            batch.draw(exitTex, currentLevelWidth - 80, 100, 45, 150);
        }

        // Draw enemies
        for (CommonEnemy enemy : activeEnemies) {
            enemy.draw(batch);
        }

        // Draw bullets
        for (Bullet b : activeBullets)
            batch.draw(bulletTex, b.bounds.x, b.bounds.y);

        player.draw(batch);

        // Draw bosses
        if (miniBoss != null && !miniBoss.isDead()) {
            miniBoss.draw(batch);
        }

        if (boss != null && !boss.isDead()) {
            boss.draw(batch);
        }

        // Draw enemy bullets
        for (EnemyBullet eb : activeEnemyBullets) {
            eb.draw(batch);
        }

        batch.end();

        // RENDER COINS DENGAN SHAPERENDERER (SETELAH BATCH.END)
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Coin coin : activeCoins) {
            coin.renderShape(shapeRenderer);
        }
        shapeRenderer.end();

        // RENDER UI DAN DEBUG (GUNAKAN BATCH LAGI)
        batch.begin();

        // Debug markers
        if (currentLevel == 5) {
            // Level 5: Draw at viewport edges for fullscreen
            float leftEdge = camera.position.x - viewport.getWorldWidth() / 2;
            float rightEdge = camera.position.x + viewport.getWorldWidth() / 2 - 10;
            batch.draw(debugTex, leftEdge, 0);
            batch.draw(debugTex, rightEdge, 0);
        } else {
            // Other levels: Draw at level boundaries
            batch.draw(debugTex, 0, 0);
            batch.draw(debugTex, currentLevelWidth - 10, 0);
        } // Level indicator
        float levelIndicatorStartX = camera.position.x - viewport.getWorldWidth() / 2 + 20;
        float levelIndicatorY = camera.position.y + viewport.getWorldHeight() / 2 - 50;
        for (int i = 0; i < currentLevel; i++) {
            batch.draw(levelIndicatorTex, levelIndicatorStartX + (i * 35), levelIndicatorY);
        }

        // Draw Health Bar
        String healthText = "HP: " + (int) player.health + "/" + (int) Player.MAX_HEALTH;
        float healthX = camera.position.x - viewport.getWorldWidth() / 2 + 20;
        float healthY = camera.position.y + viewport.getWorldHeight() / 2 - 80;
        smallFont.draw(batch, healthText, healthX, healthY);

        // Draw Coin Score
        String coinText = "Coins: " + coinScore;
        float coinX = camera.position.x - viewport.getWorldWidth() / 2 + 20;
        float coinY = healthY - 30; // Below health
        smallFont.draw(batch, coinText, coinX, coinY);

        batch.end();
    }

    private void renderGameOver() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw "GAME OVER" text
        String gameOverText = "GAME OVER";
        layout.setText(font, gameOverText);
        float gameOverX = camera.position.x - layout.width / 2;
        float gameOverY = camera.position.y + 50;
        font.draw(batch, gameOverText, gameOverX, gameOverY);

        // Draw "press space to restart" text
        String restartText = "Press SPACE to restart";
        layout.setText(smallFont, restartText);
        float restartX = camera.position.x - layout.width / 2;
        float restartY = camera.position.y - 20;
        smallFont.draw(batch, restartText, restartX, restartY);

        batch.end();
    }

    private void renderVictory() {
        Gdx.gl.glClearColor(0.1f, 0.3f, 0.1f, 1); // Green tint for victory
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw "VICTORY!" text
        String victoryText = "VICTORY!";
        layout.setText(font, victoryText);
        float victoryX = camera.position.x - layout.width / 2;
        float victoryY = camera.position.y + 100;
        font.draw(batch, victoryText, victoryX, victoryY);

        // Draw "Game Completed!" text
        String completedText = "Game Completed!";
        layout.setText(smallFont, completedText);
        float completedX = camera.position.x - layout.width / 2;
        float completedY = camera.position.y + 50;
        smallFont.draw(batch, completedText, completedX, completedY);

        // Draw coin score
        String coinsText = "Total Coins Collected: " + coinScore;
        layout.setText(smallFont, coinsText);
        float coinsX = camera.position.x - layout.width / 2;
        float coinsY = camera.position.y;
        smallFont.draw(batch, coinsText, coinsX, coinsY);

        // Draw replay instruction
        String replayText = "Press SPACE to Play Again";
        layout.setText(smallFont, replayText);
        float replayX = camera.position.x - layout.width / 2;
        float replayY = camera.position.y - 50;
        smallFont.draw(batch, replayText, replayX, replayY);

        // Draw quit instruction
        String quitText = "Press ESC to Quit";
        layout.setText(smallFont, quitText);
        float quitX = camera.position.x - layout.width / 2;
        float quitY = camera.position.y - 80;
        smallFont.draw(batch, quitText, quitX, quitY);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        playerTex.dispose();
        platformTex.dispose();
        groundTex.dispose();
        bulletTex.dispose();
        debugTex.dispose();
        levelIndicatorTex.dispose();
        enemyTex.dispose();
        miniBossTex.dispose();
        bossTex.dispose();
        enemyBulletTex.dispose();
        whiteFlashTex.dispose();
        redFlashTex.dispose();
        yellowFlashTex.dispose();
        backgroundTex.dispose();
        font.dispose();
        smallFont.dispose();
        if (buttonTex != null)
            buttonTex.dispose();
        if (buttonHoverTex != null)
            buttonHoverTex.dispose();
    }

    // ==================== USERNAME INPUT ====================
    private void handleUsernameInput() {
        // Handle text input
        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                // Debug mode: Right Ctrl + D to skip login
                if (keycode == Input.Keys.D && (Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT) ||
                        Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))) {
                    Gdx.app.log("Debug", "Debug mode activated - skipping login");
                    username = "DEBUG_USER";
                    gameState = GameState.PLAYING;
                    loadLevel(1);
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                if (gameState != GameState.USERNAME_INPUT)
                    return false;

                if (character == '\b' && usernameInput.length() > 0) {
                    // Backspace
                    usernameInput.deleteCharAt(usernameInput.length() - 1);
                } else if (Character.isLetterOrDigit(character) && usernameInput.length() < MAX_USERNAME_LENGTH) {
                    // Add character
                    usernameInput.append(character);
                }
                return true;
            }
        });

        // Check start button click
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            if (startGameButton.contains(touchPos.x, touchPos.y) && usernameInput.length() > 0) {
                username = usernameInput.toString();
                gameState = GameState.LOADING_PLAYER_DATA;

                // Call API to login/create player
                playerApi.login(username, new PlayerApiService.LoginCallback() {
                    @Override
                    public void onSuccess(PlayerData playerData, boolean isNew) {
                        currentPlayerData = playerData;
                        isNewPlayer = isNew;
                        coinsCollectedThisSession = 0;

                        if (isNew || playerData.lastStage == 1) {
                            // New player - start from level 1
                            gameState = GameState.PLAYING;
                            loadLevel(1);
                        } else {
                            // Existing player - show continue/new menu
                            gameState = GameState.CONTINUE_OR_NEW;
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Gdx.app.error("Game", "Login failed: " + error);
                        gameState = GameState.USERNAME_INPUT;
                        // Show error message to user
                    }
                });
            }
        }
    }

    private void renderUsernameInput() {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw title "LABUBOOM"
        String title = "LABUBOOM";
        layout.setText(font, title);
        float titleX = VIEWPORT_WIDTH / 2 - layout.width / 2;
        float titleY = 500;
        font.draw(batch, title, titleX, titleY);

        // Draw "Enter Your Name:"
        String prompt = "Enter Your Name:";
        layout.setText(smallFont, prompt);
        float promptX = VIEWPORT_WIDTH / 2 - layout.width / 2;
        float promptY = 380;
        smallFont.draw(batch, prompt, promptX, promptY);

        // Draw username input box background
        batch.setColor(0.3f, 0.3f, 0.3f, 1f);
        batch.draw(buttonTex, VIEWPORT_WIDTH / 2 - 250, 300, 500, 60);
        batch.setColor(1, 1, 1, 1);

        // Draw username text
        String displayText = usernameInput.length() > 0 ? usernameInput.toString() : "Username...";
        layout.setText(smallFont, displayText);
        float textX = VIEWPORT_WIDTH / 2 - layout.width / 2;
        float textY = 340;

        if (usernameInput.length() > 0) {
            smallFont.draw(batch, displayText, textX, textY);
        } else {
            // Draw placeholder in gray
            smallFont.setColor(0.5f, 0.5f, 0.5f, 1f);
            smallFont.draw(batch, displayText, textX, textY);
            smallFont.setColor(1, 1, 1, 1);
        }

        // Draw Start Game button
        boolean canStart = usernameInput.length() > 0;
        if (canStart) {
            batch.draw(buttonTex, startGameButton.x, startGameButton.y, startGameButton.width, startGameButton.height);
        } else {
            batch.setColor(0.4f, 0.4f, 0.4f, 1f);
            batch.draw(buttonTex, startGameButton.x, startGameButton.y, startGameButton.width, startGameButton.height);
            batch.setColor(1, 1, 1, 1);
        }

        String buttonText = "START GAME";
        layout.setText(font, buttonText);
        float btnTextX = startGameButton.x + startGameButton.width / 2 - layout.width / 2;
        float btnTextY = startGameButton.y + startGameButton.height / 2 + layout.height / 2;
        font.draw(batch, buttonText, btnTextX, btnTextY);

        batch.end();
    }

    // ==================== PAUSE MENU ====================
    private void handlePauseMenu() {
        // Resume with ESC
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            gameState = GameState.PLAYING;
            Gdx.app.log("Game", "RESUMED");
            return;
        }

        // Check button clicks
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            // Continue
            if (continueButton.contains(touchPos.x, touchPos.y)) {
                gameState = GameState.PLAYING;
                Gdx.app.log("Game", "RESUMED");
            }

            // Save Game (placeholder)
            else if (saveButton.contains(touchPos.x, touchPos.y)) {
                saveGameProgress();
                Gdx.app.log("Game", "Game saved manually");
            }

            // New Game - Show confirmation
            else if (newGameButton.contains(touchPos.x, touchPos.y)) {
                gameState = GameState.RESTART_CONFIRM;
                Gdx.app.log("Game", "Showing restart confirmation");
            }

            // Quit with auto-save
            else if (quitButton.contains(touchPos.x, touchPos.y)) {
                saveGameProgress();
                Gdx.app.log("Game", "Quitting game with auto-save");
                // Give time for save request
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500); // Wait for save
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Gdx.app.exit();
                    }
                }).start();
            }
        }
    }

    private void renderPauseMenu() {
        // Draw game in background (dimmed)
        renderGame();

        // Draw overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.6f);
        shapeRenderer.rect(0, 0, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw "Game Paused" title
        String title = "Game Paused";
        layout.setText(font, title);
        float titleX = VIEWPORT_WIDTH / 2 - layout.width / 2;
        float titleY = 500;
        font.draw(batch, title, titleX, titleY);

        // Draw buttons
        drawButton("Continue", continueButton);
        drawButton("Save Game", saveButton);
        drawButton("New Game", newGameButton);
        drawButton("Quit (Auto Save)", quitButton);

        batch.end();
    }

    // ==================== RESTART CONFIRMATION ====================
    private void handleRestartConfirm() {
        // ESC to cancel
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            gameState = GameState.PAUSED;
            Gdx.app.log("Game", "Restart cancelled");
            return;
        }

        // Check button clicks
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            // YES - Restart game with same username
            if (confirmYesButton.contains(touchPos.x, touchPos.y)) {
                // Reset progress on server
                playerApi.resetProgress(currentPlayerData.playerId, new PlayerApiService.SaveCallback() {
                    @Override
                    public void onSuccess() {
                        currentPlayerData.lastStage = 1;
                        coinsCollectedThisSession = 0;
                        restartGameSameUser();
                    }

                    @Override
                    public void onFailure(String error) {
                        Gdx.app.error("Game", "Failed to reset: " + error);
                        restartGameSameUser(); // Continue anyway
                    }
                });
            }

            // NO - Back to pause menu
            else if (confirmNoButton.contains(touchPos.x, touchPos.y)) {
                gameState = GameState.PAUSED;
                Gdx.app.log("Game", "Restart cancelled");
            }
        }
    }

    private void renderRestartConfirm() {
        // Draw game in background (dimmed)
        renderGame();

        // Draw overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.6f);
        shapeRenderer.rect(0, 0, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw "Restart?" title
        String title = "Restart?";
        layout.setText(font, title);
        float titleX = VIEWPORT_WIDTH / 2 - layout.width / 2;
        float titleY = 400;
        font.draw(batch, title, titleX, titleY);

        // Draw YES/NO buttons
        drawButton("Yes", confirmYesButton);
        drawButton("No", confirmNoButton);

        batch.end();
    }

    // ==================== HELPER METHODS ====================
    private void drawButton(String text, Rectangle button) {
        // Check hover
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        boolean hover = button.contains(mousePos.x, mousePos.y);

        // Draw button background
        Texture btnTex = hover ? buttonHoverTex : buttonTex;
        batch.draw(btnTex, button.x, button.y, button.width, button.height);

        // Draw button text
        layout.setText(smallFont, text);
        float textX = button.x + button.width / 2 - layout.width / 2;
        float textY = button.y + button.height / 2 + layout.height / 2;

        smallFont.setColor(0.2f, 0.2f, 0.2f, 1f);
        smallFont.draw(batch, text, textX, textY);
        smallFont.setColor(1, 1, 1, 1);
    }

    private void restartGameSameUser() {
        Gdx.app.log("Game", "Restarting game with username: " + username);

        // Clear game state
        for (CommonEnemy enemy : activeEnemies) {
            enemyPool.free(enemy);
        }
        activeEnemies.clear();
        activeBullets.clear();
        activeEnemyBullets.clear();

        for (Coin coin : activeCoins) {
            coinPool.free(coin);
        }
        activeCoins.clear();
        coinScore = 0;
        coinCollectionHandler.setCoinScore(0);
        coinCollectionHandler.setCoinsCollectedThisSession(0);

        // Reset player
        player.reset();
        player.setWeapon(null);

        // Reset bosses
        miniBoss = null;
        boss = null;

        // Load level 1
        currentLevel = 1;
        loadLevel(1);

        // Resume game
        gameState = GameState.PLAYING;
    }

    private void restartToUsernameInput() {
        // Clear username and go back to input screen
        usernameInput.setLength(0);
        username = "";
        gameState = GameState.USERNAME_INPUT;

        // Clear game state
        for (CommonEnemy enemy : activeEnemies) {
            enemyPool.free(enemy);
        }
        activeEnemies.clear();
        activeBullets.clear();
        activeEnemyBullets.clear();

        for (Coin coin : activeCoins) {
            coinPool.free(coin);
        }
        activeCoins.clear();
        coinScore = 0;
        coinCollectionHandler.setCoinScore(0);
        coinCollectionHandler.setCoinsCollectedThisSession(0);

        player.reset();
        player.setWeapon(null);
        currentLevel = 1;

        miniBoss = null;
        boss = null;

        Gdx.app.log("Game", "Returned to username input");
    }

    // ==================== BACKEND INTEGRATION METHODS ====================
    private void saveGameProgress() {
        if (currentPlayerData != null) {
            playerApi.saveProgress(
                    currentPlayerData.playerId,
                    currentLevel,
                    coinsCollectedThisSession,
                    new PlayerApiService.SaveCallback() {
                        @Override
                        public void onSuccess() {
                            Gdx.app.log("Game", "Progress saved: Stage " + currentLevel +
                                    ", Coins: " + coinsCollectedThisSession);
                            currentPlayerData.lastStage = currentLevel;
                            currentPlayerData.totalCoins += coinsCollectedThisSession;
                            coinsCollectedThisSession = 0; // Reset session counter
                        }

                        @Override
                        public void onFailure(String error) {
                            Gdx.app.error("Game", "Failed to save progress: " + error);
                        }
                    });
        }
    }

    private void handleContinueOrNew() {
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            // Continue from last stage
            if (continueGameButton.contains(touchPos.x, touchPos.y)) {
                gameState = GameState.PLAYING;
                loadLevel(currentPlayerData.lastStage);
                Gdx.app.log("Game", "Continuing from stage " + currentPlayerData.lastStage);
            }

            // Start new game
            else if (newGameButtonMenu.contains(touchPos.x, touchPos.y)) {
                // Reset progress on server
                playerApi.resetProgress(currentPlayerData.playerId, new PlayerApiService.SaveCallback() {
                    @Override
                    public void onSuccess() {
                        currentPlayerData.lastStage = 1;
                        coinsCollectedThisSession = 0;
                        gameState = GameState.PLAYING;
                        loadLevel(1);
                        Gdx.app.log("Game", "Starting new game");
                    }

                    @Override
                    public void onFailure(String error) {
                        Gdx.app.error("Game", "Failed to reset progress: " + error);
                    }
                });
            }
        }
    }

    private void renderLoading() {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        String loadingText = "Loading...";
        layout.setText(font, loadingText);
        float x = VIEWPORT_WIDTH / 2 - layout.width / 2;
        float y = VIEWPORT_HEIGHT / 2;
        font.draw(batch, loadingText, x, y);

        batch.end();
    }

    private void renderContinueOrNew() {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw welcome message
        String welcomeText = "Welcome back, " + username + "!";
        layout.setText(font, welcomeText);
        float welcomeX = VIEWPORT_WIDTH / 2 - layout.width / 2;
        float welcomeY = 450;
        font.draw(batch, welcomeText, welcomeX, welcomeY);

        // Draw last stage info
        String stageText = "Last Stage: " + currentPlayerData.lastStage;
        layout.setText(smallFont, stageText);
        float stageX = VIEWPORT_WIDTH / 2 - layout.width / 2;
        float stageY = 380;
        smallFont.draw(batch, stageText, stageX, stageY);

        // Draw total coins
        String coinsText = "Total Coins: " + currentPlayerData.totalCoins;
        layout.setText(smallFont, coinsText);
        float coinsX = VIEWPORT_WIDTH / 2 - layout.width / 2;
        float coinsY = 350;
        smallFont.draw(batch, coinsText, coinsX, coinsY);

        // Draw Continue button
        drawButton("Continue", continueGameButton);

        // Draw New Game button
        drawButton("New Game", newGameButtonMenu);

        batch.end();
    }
}