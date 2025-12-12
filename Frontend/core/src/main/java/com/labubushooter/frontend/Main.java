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

    Texture playerTex, platformTex, bulletTex, exitTex;
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
        bulletTex = createColorTexture(10, 5, Color.YELLOW);
        pistolTex = createColorTexture(20, 10, Color.GRAY);
        mac10Tex = createColorTexture(30, 15, Color.LIME);
        exitTex = createColorTexture(30, 100, Color.FOREST);
        debugTex = createColorTexture(10, 600, Color.RED); // Debug marker
        levelIndicatorTex = createColorTexture(30, 30, Color.YELLOW); // Level indicator
        enemyTex = createColorTexture(40, 60, Color.RED);

        // Boss textures
        miniBossTex = createColorTexture(60, 90, Color.PURPLE);
        bossTex = createColorTexture(60, 100, Color.MAROON);
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

        loadLevel(currentLevel);
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
        isGameOver = false;
        isVictory = false;
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

        // FORCE-ADD: Base ground that spans the ENTIRE level width
        // Use extended width for ultra-wide screens (prevents visual gaps)
        float safeGroundWidth = Math.max(currentLevelWidth, 3000f);
        Platform baseGround = new Platform(0, 0, safeGroundWidth, 100, platformTex);
        platforms.insert(0, baseGround);

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

        // Check for Victory screen
        if (isVictory) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                restartGame();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                Gdx.app.exit();
            }

            // Render Victory Screen
            renderVictory();
            return;
        }

        // Check for Game Over restart
        if (isGameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                restartGame();
            }

            // Render Game Over Screen
            renderGameOver();
            return;
        }

        // Check if player is dead
        if (player.isDead()) {
            isGameOver = true;
            Gdx.app.log("Game", "GAME OVER");
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.W))
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
        player.update(delta, platforms);

        // Update bosses
        if (currentLevel == 3 && miniBoss != null && !miniBoss.isDead()) {
            miniBoss.update(delta, platforms, player);
        }

        if (currentLevel == 5 && boss != null && !boss.isDead()) {
            boss.update(delta, platforms, player, activeEnemyBullets, enemyBulletPool);
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
            enemy.update(delta, platforms);

            if (!enemy.isActive()) {
                activeEnemies.removeIndex(i);
                enemyPool.free(enemy);
            }
        }

        // Check bullet-enemy collisions
        for (int i = activeEnemies.size - 1; i >= 0; i--) {
            CommonEnemy enemy = activeEnemies.get(i);

            for (int j = activeBullets.size - 1; j >= 0; j--) {
                Bullet bullet = activeBullets.get(j);

                if (enemy.collider.overlaps(bullet.bounds)) {
                    enemy.takeDamage(bullet.damage);
                    activeBullets.removeIndex(j);
                    bulletPool.free(bullet);

                    Gdx.app.log("Combat", "Enemy hit! Health: " + enemy.health);
                    break;
                }
            }
        }

        // Check bullet-miniboss collisions
        if (miniBoss != null && !miniBoss.isDead()) {
            for (int j = activeBullets.size - 1; j >= 0; j--) {
                Bullet bullet = activeBullets.get(j);

                if (bullet.bounds.overlaps(miniBoss.collider)) {
                    miniBoss.takeDamage(bullet.damage);
                    activeBullets.removeIndex(j);
                    bulletPool.free(bullet);
                    Gdx.app.log("Combat", "Mini Boss hit! Health: " + miniBoss.health);
                    break;
                }
            }
        }

        // Check bullet-boss collisions
        if (boss != null && !boss.isDead()) {
            for (int j = activeBullets.size - 1; j >= 0; j--) {
                Bullet bullet = activeBullets.get(j);

                if (bullet.bounds.overlaps(boss.collider)) {
                    boss.takeDamage(bullet.damage);
                    activeBullets.removeIndex(j);
                    bulletPool.free(bullet);
                    Gdx.app.log("Combat", "Boss hit! Health: " + boss.health);
                    break;
                }
            }
        }

        // Check enemy bullet-player collisions
        for (int i = activeEnemyBullets.size - 1; i >= 0; i--) {
            EnemyBullet eb = activeEnemyBullets.get(i);

            if (eb.bounds.overlaps(player.bounds)) {
                player.takeDamage(eb.damage);
                activeEnemyBullets.removeIndex(i);
                enemyBulletPool.free(eb);
                Gdx.app.log("Combat", "Player hit by enemy bullet!");
            }
        }

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
                isVictory = true;
                Gdx.app.log("Game", "VICTORY! Game Completed!");
            } else {
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

        // Update Coins - HANYA HAPUS SAAT DIKUMPULKAN
        for (int i = activeCoins.size - 1; i >= 0; i--) {
            Coin coin = activeCoins.get(i);
            coin.update(delta);

            // Check coin collection - GUNAKAN isColliding
            if (coin.isColliding(player.bounds)) {
                coin.active = false;
                coinScore++;
                activeCoins.removeIndex(i);
                coinPool.free(coin);
                Gdx.app.log("Coin", "Collected! Total: " + coinScore);
            }
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
            batch.draw(exitTex, currentLevelWidth - 80, 50);
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
    }
}