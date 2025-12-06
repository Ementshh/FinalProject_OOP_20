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
import com.labubushooter.frontend.patterns.Mac10Strategy;
import com.labubushooter.frontend.patterns.PistolStrategy;
import com.labubushooter.frontend.patterns.ShootingStrategy;

public class Main extends ApplicationAdapter {
    SpriteBatch batch;
    OrthographicCamera orthographicCamera;

    static final float LEVEL_WIDTH = 2400f;
    static final float VIEWPORT_WIDTH = 800f;
    static final float VIEWPORT_HEIGHT = 600f;

    // Assets Placeholder
    Texture playerTex, platformTex, bulletTex;
    Texture pistolTex, mac10Tex;

    // Game Objects
    Player player;
    Array<Platform> platforms;

    // Object Pool Pattern
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

        // 1. Buat Texture Dummy (Kotak Warna)
        playerTex = createColorTexture(40, 60, Color.ORANGE);
        platformTex = createColorTexture(100, 20, Color.FOREST);
        bulletTex = createColorTexture(10, 5, Color.YELLOW);
        pistolTex = createColorTexture(20, 10, Color.GRAY);
        mac10Tex = createColorTexture(30, 15, Color.LIME);

        // 2. Setup Object Pool
        bulletPool = new Pool<Bullet>() {
            @Override
            protected Bullet newObject() {
                return new Bullet();
            }
        };
        activeBullets = new Array<>();

        // 3. Setup Level (Extended untuk scrolling)
        platforms = new Array<>();
        platforms.add(new Platform(0, 50, LEVEL_WIDTH, 50, platformTex));
        platforms.add(new Platform(300, 200, 200, 20, platformTex));
        platforms.add(new Platform(900, 180, 150, 20, platformTex));
        platforms.add(new Platform(1200, 280, 180, 20, platformTex));
        platforms.add(new Platform(1450, 350, 120, 20, platformTex));
        platforms.add(new Platform(1700, 200, 200, 20, platformTex));
        platforms.add(new Platform(2000, 300, 150, 20, platformTex));
        platforms.add(new Platform(2200, 250, 180, 20, platformTex));

        pistolStrategy = new PistolStrategy();
        mac10Strategy = new Mac10Strategy();

        // Initialize Player without a weapon initially (or pick one)
        player = new Player(playerTex);
        player.pistolTex = pistolTex;
        player.mac10Tex = mac10Tex;
        player.setWeapon(null);
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) player.jump();

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

        // Update Camera mengikuti Player
        float targetCameraX = player.bounds.x + player.bounds.width / 2;
        // Clamp camera agar tidak keluar dari level boundaries
        orthographicCamera.position.x = MathUtils.clamp(targetCameraX, VIEWPORT_WIDTH / 2, LEVEL_WIDTH - VIEWPORT_WIDTH / 2);
        orthographicCamera.update();

        // Update Peluru
        for (int i = activeBullets.size - 1; i >= 0; i--) {
            Bullet b = activeBullets.get(i);
            b.update(delta);
            // Hapus jika keluar layar
            if (b.bounds.x < 0 || b.bounds.x > LEVEL_WIDTH) {
                activeBullets.removeIndex(i);
                bulletPool.free(b);
            }
        }

        // Camera Update
        orthographicCamera.position.x = MathUtils.clamp(targetCameraX, VIEWPORT_WIDTH / 2, LEVEL_WIDTH - VIEWPORT_WIDTH / 2);
        orthographicCamera.update();

        // --- DRAW ---
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.setProjectionMatrix(orthographicCamera.combined);
        batch.begin();
        for (Platform p : platforms)
            p.draw(batch);
        for (Bullet b : activeBullets)
            batch.draw(bulletTex, b.bounds.x, b.bounds.y);
        player.draw(batch);
        batch.end();
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
    public void dispose() {
        batch.dispose();
        playerTex.dispose();
        platformTex.dispose();
        bulletTex.dispose();
    }
}
