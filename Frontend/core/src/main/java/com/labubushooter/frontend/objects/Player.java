package com.labubushooter.frontend.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.labubushooter.frontend.patterns.ShootingStrategy;

public class Player extends GameObject {
    public float velY = 0;
    public boolean grounded = false;
    public boolean facingRight = true;

<<<<<<< Updated upstream
    // Health System
    public float health;
    public static final float MAX_HEALTH = 20f;
    
    // Health Regeneration System
    private long lastDamageTime;
    private long lastRegenTime;
    private static final long REGEN_DELAY = 5000000000L; // 5 seconds in nanoseconds
    private static final long REGEN_INTERVAL = 2000000000L; // 2 seconds in nanoseconds
=======
    // Health System - BARU
    public float health;
    public static final float MAX_HEALTH = 20f;
    
    // Health Regeneration System - BARU
    private long lastDamageTime;
    private long lastRegenTime;
    private static final long REGEN_DELAY = 5000000000L; // 5 seconds
    private static final long REGEN_INTERVAL = 2000000000L; // 2 seconds
>>>>>>> Stashed changes
    private static final float REGEN_AMOUNT = 1f;

    // Strategy Pattern: The behavior of shooting is encapsulated in this interface
    private ShootingStrategy shootingStrategy;

    final float GRAVITY = -900f;
    final float JUMP_POWER = 500f;
    final float SPEED = 250f;
    public static float LEVEL_WIDTH = 2400f;
    
    public Texture pistolTex;
    public Texture mac10Tex;

    public Player(Texture tex) {
        super(100, 300, 40, 60, tex);
        this.shootingStrategy = null;
<<<<<<< Updated upstream
        this.health = MAX_HEALTH;
        this.lastDamageTime = TimeUtils.nanoTime();
        this.lastRegenTime = TimeUtils.nanoTime();
=======
        this.health = MAX_HEALTH; // BARU
        this.lastDamageTime = TimeUtils.nanoTime(); // BARU
        this.lastRegenTime = TimeUtils.nanoTime(); // BARU
>>>>>>> Stashed changes
    }

    public void setWeapon(ShootingStrategy strategy) {
        this.shootingStrategy = strategy;
    }

    public ShootingStrategy getWeapon() {
        return this.shootingStrategy;
    }

<<<<<<< Updated upstream
    public void takeDamage(float damage) {
        health -= damage;
        if (health < 0) health = 0;
        lastDamageTime = TimeUtils.nanoTime(); // Reset damage timer
=======
    // BARU - Health Methods
    public void takeDamage(float damage) {
        health -= damage;
        if (health < 0) health = 0;
        lastDamageTime = TimeUtils.nanoTime();
>>>>>>> Stashed changes
        Gdx.app.log("Player", "Health: " + health);
    }

    public boolean isDead() {
        return health <= 0;
    }

    public void reset() {
        this.health = MAX_HEALTH;
        this.velY = 0;
        this.grounded = false;
        this.facingRight = true;
        this.bounds.setPosition(100, 300);
        this.lastDamageTime = TimeUtils.nanoTime();
        this.lastRegenTime = TimeUtils.nanoTime();
    }

    public void update(float delta, Array<Platform> platforms) {
<<<<<<< Updated upstream
        // Health Regeneration Logic
=======
        // BARU - Health Regeneration Logic
>>>>>>> Stashed changes
        long currentTime = TimeUtils.nanoTime();
        if (health < MAX_HEALTH && currentTime - lastDamageTime > REGEN_DELAY) {
            if (currentTime - lastRegenTime > REGEN_INTERVAL) {
                health += REGEN_AMOUNT;
                if (health > MAX_HEALTH) health = MAX_HEALTH;
                lastRegenTime = currentTime;
                Gdx.app.log("Player", "Health regenerated: " + health);
            }
        }

        // Movement Input
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bounds.x -= SPEED * delta;
            facingRight = false;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bounds.x += SPEED * delta;
            facingRight = true;
        }

        // Boundary check
        if (bounds.x < 0)
            bounds.x = 0;
        if (bounds.x + bounds.width > LEVEL_WIDTH)
            bounds.x = LEVEL_WIDTH - bounds.width;

        // Gravitasi
        velY += GRAVITY * delta;
        bounds.y += velY * delta;

        // Platform Collisions
        grounded = false;
        for (Platform p : platforms) {
            if (bounds.overlaps(p.bounds)) {
                if (velY < 0 && bounds.y + bounds.height / 2 > p.bounds.y + p.bounds.height) {
                    bounds.y = p.bounds.y + p.bounds.height;
                    velY = 0;
                    grounded = true;
                }
            }
        }

        // Reset position
        if (bounds.y < 0) {
            bounds.setPosition(100, 400);
            velY = 0;
        }
    }

    public void jump() {
        if (grounded) {
            velY = JUMP_POWER;
            grounded = false;
        }
    }

    public void shoot(Array<Bullet> activeBullets, Pool<Bullet> pool) {
        if (shootingStrategy == null) return;

        float startX = facingRight ? bounds.x + bounds.width + 10 : bounds.x - 10;
        float startY = bounds.y + (bounds.height / 2) - 5.5f;

        shootingStrategy.shoot(startX, startY, facingRight, activeBullets, pool);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Draw Player Body
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);

        // Draw Weapon
        if (shootingStrategy != null) {
            Texture currentWeaponTex = null;
            float w = 0, h = 0;
            String strategyName = shootingStrategy.getClass().getSimpleName();

            if (strategyName.contains("Pistol")) {
                currentWeaponTex = pistolTex;
                w = 20; h = 10;
            } else if (strategyName.contains("Mac10")) {
                currentWeaponTex = mac10Tex;
                w = 30; h = 15;
            }

            if (currentWeaponTex != null) {
                float wx = facingRight ? bounds.x + bounds.width - 10 : bounds.x - w + 10;
                float wy = bounds.y + bounds.height / 2 - h / 2 - 5;

                batch.draw(currentWeaponTex, wx, wy, w, h);
            }
        }
    }
}