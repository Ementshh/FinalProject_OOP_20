package com.labubushooter.frontend;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.objects.Platform;
import com.labubushooter.frontend.objects.Player;
import com.labubushooter.frontend.patterns.LevelStrategy;
import com.labubushooter.frontend.patterns.levels.*;
import com.labubushooter.frontend.patterns.weapons.Mac10Strategy;
import com.labubushooter.frontend.patterns.weapons.PistolStrategy;
import com.labubushooter.frontend.patterns.ShootingStrategy;

import java.util.HashMap;
import java.util.Map;

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
    Texture debugTex; // Debug marker untuk level boundaries
    Texture levelIndicatorTex; // Level indicator marker

    Player player;
    Array<Platform> platforms;

    Pool<Bullet> bulletPool;
    Array<Bullet> activeBullets;

    PistolStrategy pistolStrategy;
    Mac10Strategy mac10Strategy;

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

        // Buat Texture Dummy (Kotak Warna)
        playerTex = createColorTexture(40, 60, Color.ORANGE);
        platformTex = createColorTexture(100, 20, Color.FOREST);
        bulletTex = createColorTexture(10, 5, Color.YELLOW);
        pistolTex = createColorTexture(20, 10, Color.GRAY);
        mac10Tex = createColorTexture(30, 15, Color.LIME);
        exitTex = createColorTexture(30, 100, Color.FOREST);
        debugTex = createColorTexture(10, 600, Color.RED); // Debug marker
        levelIndicatorTex = createColorTexture(30, 30, Color.YELLOW); // Level indicator

        // Setup Object Pool
        bulletPool = new Pool<Bullet>() {
            @Override
            protected Bullet newObject() {
                return new Bullet();
            }
        };
        activeBullets = new Array<>();

        pistolStrategy = new PistolStrategy();
        mac10Strategy = new Mac10Strategy();

        // Initialize Player without a weapon initially (or pick one)
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

    private void loadLevel(int level) {
        LevelStrategy strategy = levelStrategy.get(level);

        if (strategy == null) {
            // Fallback or game complete logic
            System.out.println("Level " + level + " not found!");
            return;
        }

        // Update current level state (FIX: prevents infinite Level 2 loop)
        this.currentLevel = level;

        // Hapus semua objek dari level-level sebelumnya
        if (platforms == null) {
            platforms = new Array<Platform>();
        } else {
            platforms.clear();
        }
        activeBullets.clear();

        // Set Level Parameters using Strategy
        currentLevelWidth = strategy.getLevelWidth();

        // Load Platforms using Strategy
        strategy.loadPlatforms(platforms, platformTex);

        // FORCE-ADD: Base ground that spans the ENTIRE level width
        // This ensures player can walk from 0 to currentLevelWidth regardless of
        // Strategy design
        Platform baseGround = new Platform(0, 0, currentLevelWidth, 50, platformTex);
        platforms.insert(0, baseGround); // Insert at index 0 so it's drawn first (behind other platforms)

        // Reset Player Position using Strategy
        player.bounds.setPosition(strategy.getPlayerStartX(), strategy.getPlayerStartY());

        // Update Player's level width for boundary checking
        Player.LEVEL_WIDTH = currentLevelWidth;

        // Reset camera position
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

        // Check Level Exit
        if (player.bounds.x + player.bounds.width >= currentLevelWidth - LEVEL_EXIT_THRESHOLD) {
            loadLevel(currentLevel + 1);
        }

        // --- CAMERA FOLLOW LOGIC ---
        // Camera follows player's center position
        float targetCameraX = player.bounds.x + player.bounds.width / 2;

        // Clamp camera to stay within level boundaries
        camera.position.x = MathUtils.clamp(
                targetCameraX,
                viewport.getWorldWidth() / 2,
                currentLevelWidth - viewport.getWorldWidth() / 2);

        camera.update();

        // Update Peluru
        for (int i = activeBullets.size - 1; i >= 0; i--) {
            Bullet b = activeBullets.get(i);
            b.update(delta);
            // Hapus jika keluar layar
            if (b.bounds.x < 0 || b.bounds.x > currentLevelWidth) {
                activeBullets.removeIndex(i);
                bulletPool.free(b);
            }
        }

        // --- DRAW ---
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Apply camera projection
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw game objects (bottom layer to top layer)
        for (Platform p : platforms)
            p.draw(batch);
        batch.draw(exitTex, currentLevelWidth - 80, 50);
        for (Bullet b : activeBullets)
            batch.draw(bulletTex, b.bounds.x, b.bounds.y);
        player.draw(batch);

        // Debug markers drawn LAST (on top of everything for visibility)
        batch.draw(debugTex, 0, 0); // Start marker (red strip at x=0)
        batch.draw(debugTex, currentLevelWidth - 10, 0); // End marker (red strip at level end)

        // Level indicator di kiri atas layar (fixed position relative to camera)
        float levelIndicatorStartX = camera.position.x - viewport.getWorldWidth() / 2 + 20;
        float levelIndicatorY = camera.position.y + viewport.getWorldHeight() / 2 - 50;
        for (int i = 0; i < currentLevel; i++) {
            batch.draw(levelIndicatorTex, levelIndicatorStartX + (i * 35), levelIndicatorY);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Update viewport when window is resized or fullscreen toggled
        // The 'true' parameter centers the camera
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
    }
}
