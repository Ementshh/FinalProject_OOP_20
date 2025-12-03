package com.labubushooter.frontend.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.labubushooter.frontend.patterns.ShootingStrategy;

public class Player extends GameObject {
    public float velY = 0;
    public boolean grounded = false;
    public boolean facingRight = true;

    private ShootingStrategy shootingStrategy;

    // Konstanta Fisika
    final float GRAVITY = -900f;
    final float JUMP_POWER = 450f;
    final float SPEED = 250f;

    public Player(Texture tex, ShootingStrategy strategy) {
        super(100, 300, 40, 60, tex);
        this.shootingStrategy = strategy;
    }

    public void update(float delta, Array<Platform> platforms) {
        // 1. Input Gerak
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bounds.x -= SPEED * delta;
            facingRight = false;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bounds.x += SPEED * delta;
            facingRight = true;
        }

        // 2. Gravitasi
        velY += GRAVITY * delta;
        bounds.y += velY * delta;

        // 3. Tabrakan Platform
        grounded = false;
        for (Platform p : platforms) {
            if (bounds.overlaps(p.bounds)) {
                if (velY < 0 && bounds.y + bounds.height/2 > p.bounds.y + p.bounds.height) {
                    bounds.y = p.bounds.y + p.bounds.height;
                    velY = 0;
                    grounded = true;
                }
            }
        }

        // Reset posisi jika jatuh
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
        float startX = facingRight ? bounds.x + bounds.width : bounds.x - 10;
        float startY = bounds.y + bounds.height/2;

        shootingStrategy.shoot(startX, startY, facingRight, activeBullets, pool);
    }
}
