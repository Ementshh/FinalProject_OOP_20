package com.labubushooter.frontend;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.objects.Platform;
import com.labubushooter.frontend.objects.Player;
import com.labubushooter.frontend.patterns.ShootingStrategy;

public class Main extends ApplicationAdapter {
    SpriteBatch batch;

    // Assets Placeholder
    Texture playerTex, platformTex, bulletTex;

    // Game Objects
    Player player;
    Array<Platform> platforms;

    // Object Pool Pattern
    Pool<Bullet> bulletPool;
    Array<Bullet> activeBullets;

    @Override
    public void create () {
        batch = new SpriteBatch();

        // 1. Buat Texture Dummy (Kotak Warna)
        playerTex = createColorTexture(40, 60, Color.ORANGE);
        platformTex = createColorTexture(100, 20, Color.FOREST);
        bulletTex = createColorTexture(10, 5, Color.YELLOW);

        // 2. Setup Object Pool
        bulletPool = new Pool<Bullet>() {
            @Override
            protected Bullet newObject() {
                return new Bullet();
            }
        };
        activeBullets = new Array<>();

        // 3. Setup Level
        platforms = new Array<>();
        platforms.add(new Platform(0, 50, 800, 50, platformTex));   // Lantai
        platforms.add(new Platform(300, 200, 200, 20, platformTex)); // Platform Floating

        // 4. Setup Player dengan Strategy Default (Anonymous Class untuk simplifikasi demo)
        ShootingStrategy defaultStrategy = new ShootingStrategy() {
            @Override
            public void shoot(float x, float y, boolean facingRight, Array<Bullet> activeBullets, Pool<Bullet> bulletPool) {
                Bullet b = bulletPool.obtain();
                b.init(x, y, facingRight ? 400 : -400);
                activeBullets.add(b);
            }
        };

        player = new Player(playerTex, defaultStrategy);
    }

    @Override
    public void render () {
        float delta = Gdx.graphics.getDeltaTime();

        // --- UPDATE ---

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) player.jump();
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) player.shoot(activeBullets, bulletPool);

        player.update(delta, platforms);

        // Update Peluru
        for (int i = activeBullets.size - 1; i >= 0; i--) {
            Bullet b = activeBullets.get(i);
            b.update(delta);
            // Hapus jika keluar layar
            if (b.bounds.x < 0 || b.bounds.x > Gdx.graphics.getWidth()) {
                activeBullets.removeIndex(i);
                bulletPool.free(b);
            }
        }

        // --- DRAW ---
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        for (Platform p : platforms) p.draw(batch);
        for (Bullet b : activeBullets) batch.draw(bulletTex, b.bounds.x, b.bounds.y);
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
    public void dispose () {
        batch.dispose();
        playerTex.dispose();
        platformTex.dispose();
        bulletTex.dispose();
    }
}
