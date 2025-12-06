package com.labubushooter.frontend.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.labubushooter.frontend.patterns.ShootingStrategy;

public class Player extends GameObject {
    public float velY = 0;
    public boolean grounded = false;
    public boolean facingRight = true;

    // Strategy Pattern: The behavior of shooting is encapsulated in this interface
    private ShootingStrategy shootingStrategy;

    final float GRAVITY = -900f;
    final float JUMP_POWER = 450f;
    final float SPEED = 250f;
    public static float LEVEL_WIDTH = 2400f;
    
    public Texture pistolTex;
    public Texture mac10Tex;

    public Player(Texture tex) {
        super(100, 300, 40, 60, tex);
        this.shootingStrategy = null; // Default to no weapon
    }

    public void setWeapon(ShootingStrategy strategy) {
        this.shootingStrategy = strategy;
    }

    public ShootingStrategy getWeapon() {
        return this.shootingStrategy;
    }

    public void update(float delta, Array<Platform> platforms) {
        // Movement Input
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bounds.x -= SPEED * delta;
            facingRight = false;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bounds.x += SPEED * delta;
            facingRight = true;
        }

        // Boundary check untuk level yang lebih lebar
        if (bounds.x < 0)
            bounds.x = 0;
        if (bounds.x + bounds.width > LEVEL_WIDTH)
            bounds.x = LEVEL_WIDTH - bounds.width;

        // 2. Gravitasi
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
        if (shootingStrategy == null) return; // Cannot shoot if holding nothing

        float startX = facingRight ? bounds.x + bounds.width + 10 : bounds.x - 10;
        float startY = bounds.y + (bounds.height / 2) - 5.5f;

        shootingStrategy.shoot(startX, startY, facingRight, activeBullets, pool);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // 1. Draw Player Body
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);

        // 2. Draw Weapon Hitbox (if holding one)
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
                float wy = bounds.y + bounds.height / 2 - h / 2 - 5; // Slightly lower than center

                batch.draw(currentWeaponTex, wx, wy, w, h);
            }
        }
    }
}