package com.labubushooter.frontend;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.objects.CommonEnemy;
import com.labubushooter.frontend.objects.EnemyBullet;
import com.labubushooter.frontend.objects.FinalBoss;
import com.labubushooter.frontend.objects.MiniBossEnemy;
import com.labubushooter.frontend.objects.Platform;
import com.labubushooter.frontend.objects.Player;
import com.labubushooter.frontend.patterns.LevelStrategy;
import com.labubushooter.frontend.patterns.ShootingStrategy;
import com.labubushooter.frontend.patterns.levels.*;
import com.labubushooter.frontend.patterns.weapons.Mac10Strategy;
import com.labubushooter.frontend.patterns.weapons.PistolStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main extends ApplicationAdapter {
    SpriteBatch batch;
    OrthographicCamera camera;
    Viewport viewport;

    static final float VIEWPORT_WIDTH = 800f;
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
    Texture whiteFlashTex, redFlashTex;

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

    // Enemy spawn delays per level (in nanoseconds)
    // Level 1: 7-10 seconds
    private static final long LEVEL1_MIN_SPAWN = 6000000000L;
    private static final long LEVEL1_MAX_SPAWN = 8000000000L;

    // Level 2: 6-8 seconds
    private static final long LEVEL2_MIN_SPAWN = 5000000000L;
    private static final long LEVEL2_MAX_SPAWN = 8000000000L;

    // Level 4: 4-7 seconds
    private static final long LEVEL4_MIN_SPAWN = 4000000000L;
    private static final long LEVEL4_MAX_SPAWN = 7000000000L;

    PistolStrategy pistolStrategy;
    Mac10Strategy mac10Strategy;

    // Game Over System
    private boolean isGameOver = false;
    private BitmapFont font;
    private BitmapFont smallFont;
    private GlyphLayout layout;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Setup Camera with ExtendViewport for fullscreen without black bars
        // ExtendViewport shows MORE of the level horizontally on wider screens
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
        playerTex = createColorTexture(40, 60, Color.ORANGE);
        platformTex = createColorTexture(100, 20, Color.FOREST);
        bulletTex = createColorTexture(10, 5, Color.YELLOW);
        pistolTex = createColorTexture(20, 10, Color.GRAY);
        mac10Tex = createColorTexture(30, 15, Color.LIME);
        exitTex = createColorTexture(30, 100, Color.FOREST);
        debugTex = createColorTexture(10, 600, Color.RED); // Debug marker
        levelIndicatorTex = createColorTexture(30, 30, Color.YELLOW); // Level indicator
        enemyTex = createColorTexture(40, 60, Color.RED);

        // Boss textures
        miniBossTex = createColorTexture(60, 90, Color.PURPLE);
        bossTex = createColorTexture(80, 120, Color.MAROON);
        enemyBulletTex = createColorTexture(8, 8, Color.ORANGE);
        whiteFlashTex = createColorTexture(60, 90, Color.WHITE);
        redFlashTex = createColorTexture(80, 120, Color.RED);

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

        pistolStrategy = new PistolStrategy();
        mac10Strategy = new Mac10Strategy();

        player = new Player(playerTex);
        player.pistolTex = pistolTex;
        player.mac10Tex = mac10Tex;
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

    private void spawnEnemy() {
        float spawnX;
        do {
            spawnX = random.nextFloat() * (currentLevelWidth - 100);
        } while (Math.abs(spawnX - player.bounds.x) < 200);

        CommonEnemy enemy = enemyPool.obtain();
        enemy.init(spawnX, player, currentLevel); // Pass current level
        activeEnemies.add(enemy);

        Gdx.app.log("Enemy", "Spawned at X: " + spawnX);
    }

    private void restartGame() {
        isGameOver = false;
        currentLevel = 1;

        // Clear all enemies
        for (CommonEnemy enemy : activeEnemies) {
            enemyPool.free(enemy);
        }
        activeEnemies.clear();

        // Clear all bullets
        activeBullets.clear();

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

        // Spawn boss for levels 3 and 5
        if (level == 3) {
            miniBoss = new MiniBossEnemy(miniBossTex, whiteFlashTex);
            miniBoss.init(strategy.getBossSpawnX(), strategy.getBossSpawnY());
            boss = null;
            Gdx.app.log("Level3", "Mini Boss spawned!");
        } else if (level == 5) {
            boss = new FinalBoss(bossTex, redFlashTex, enemyBulletTex);
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
        Platform baseGround = new Platform(0, 0, safeGroundWidth, 50, platformTex);
        platforms.insert(0, baseGround);

        player.bounds.setPosition(strategy.getPlayerStartX(), strategy.getPlayerStartY());

        Player.LEVEL_WIDTH = currentLevelWidth;

        camera.position.x = VIEWPORT_WIDTH / 2;
        camera.update();

        Gdx.app.log("Game", "Loaded Level " + level + " | Width: " + currentLevelWidth);
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP))
            player.jump();

        // --- SHOOTING ---
        ShootingStrategy currentWeapon = player.getWeapon();
        if (currentWeapon != null) {
            // Automatic and non-automatic weapon mechanic
            if (currentWeapon.isAutomatic()) {
                if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                    player.shoot(activeBullets, bulletPool);
                }
            } else {
                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
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

            if (eb.isOutOfBounds(currentLevelWidth, VIEWPORT_HEIGHT)) {
                activeEnemyBullets.removeIndex(i);
                enemyBulletPool.free(eb);
            }
        }

        // Spawn enemy based on timer - KECUALI di Level 3 dan 5
        // Level 1: 7-10 seconds
        // Level 2: 6-8 seconds
        // Level 4: 4-7 seconds
        if (currentLevel != 3 && currentLevel != 5) {
            if (TimeUtils.nanoTime() - lastEnemySpawnTime > nextEnemySpawnDelay) {
                spawnEnemy();
                resetEnemySpawnTimer();
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
            loadLevel(currentLevel + 1);
        }

        // --- CAMERA FOLLOW LOGIC ---
        // Robust camera handling for both single-screen and scrolling levels
        float halfViewport = viewport.getWorldWidth() / 2;
        float levelMid = currentLevelWidth / 2;

        if (viewport.getWorldWidth() >= currentLevelWidth) {
            // Case A: Screen is wider than or equal to level (e.g., Level 5)
            // LOCK camera to the exact center of the level. Do not follow player.
            camera.position.x = levelMid;
        } else {
            // Case B: Level is wider than screen (Scrolling Levels)
            // Follow player but clamp within bounds
            float targetX = player.bounds.x + player.bounds.width / 2;
            camera.position.x = MathUtils.clamp(targetX, halfViewport, currentLevelWidth - halfViewport);
        }

        camera.update();

        // Update Bullets
        for (int i = activeBullets.size - 1; i >= 0; i--) {
            Bullet b = activeBullets.get(i);
            b.update(delta);
            if (b.bounds.x < 0 || b.bounds.x > currentLevelWidth) {
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

        // Draw platforms
        for (Platform p : platforms)
            p.draw(batch);
        batch.draw(exitTex, currentLevelWidth - 80, 50);

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

        // Debug markers
        batch.draw(debugTex, 0, 0);
        batch.draw(debugTex, currentLevelWidth - 10, 0);

        // Level indicator
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

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
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
        font.dispose();
        smallFont.dispose();
    }
}