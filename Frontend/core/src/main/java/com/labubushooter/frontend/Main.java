package com.labubushooter.frontend;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
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
    OrthographicCamera orthographicCamera;

    static final float VIEWPORT_WIDTH = 800f;
    static final float VIEWPORT_HEIGHT = 600f;

    private final float LEVEL_EXIT_THRESHOLD = 100f;

    private float currentLevelWidth;
    private int currentLevel = 1;
    private Map<Integer, LevelStrategy> levelStrategy;

    Texture playerTex, platformTex, bulletTex, exitTex;
    Texture pistolTex, mac10Tex;

    Player player;
    Array<Platform> platforms;

    Pool<Bullet> bulletPool;
    Array<Bullet> activeBullets;

    PistolStrategy pistolStrategy;
    Mac10Strategy mac10Strategy;

    @Override
    public void create() {
        batch = new SpriteBatch();
        orthographicCamera = new OrthographicCamera();
        orthographicCamera.setToOrtho(false, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);

        // Setup Camera
        orthographicCamera = new OrthographicCamera();
        orthographicCamera.setToOrtho(false, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        orthographicCamera.update();

        // Buat Texture Dummy (Kotak Warna)
        playerTex = createColorTexture(40, 60, Color.ORANGE);
        platformTex = createColorTexture(100, 20, Color.FOREST);
        bulletTex = createColorTexture(10, 5, Color.YELLOW);
        pistolTex = createColorTexture(20, 10, Color.GRAY);
        mac10Tex = createColorTexture(30, 15, Color.LIME);
        exitTex = createColorTexture(30, 100, Color.FOREST);

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

        // Reset Player Position using Strategy
        player.bounds.setPosition(strategy.getPlayerStartX(), strategy.getPlayerStartY());
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

        if (player.bounds.x + player.bounds.width >= currentLevelWidth - LEVEL_EXIT_THRESHOLD) {
            currentLevel++;
            loadLevel(currentLevel + 1);
        }

        // Update Camera mengikuti Player
        float targetCameraX = player.bounds.x + player.bounds.width / 2;
        // Clamp camera agar tidak keluar dari level boundaries
        orthographicCamera.position.x = MathUtils.clamp(targetCameraX, VIEWPORT_WIDTH / 2,
                currentLevelWidth - VIEWPORT_WIDTH / 2);
        orthographicCamera.update();

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

        batch.setProjectionMatrix(orthographicCamera.combined);
        batch.begin();
        for (Platform p : platforms)
            p.draw(batch);
        batch.draw(exitTex, currentLevelWidth - 80, 50);
        for (Bullet b : activeBullets)
            batch.draw(bulletTex, b.bounds.x, b.bounds.y);
        player.draw(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        playerTex.dispose();
        platformTex.dispose();
        bulletTex.dispose();
    }
}
