package com.labubushooter.frontend.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Pool;

public class EnemyBullet implements Pool.Poolable {
    public Rectangle bounds;
    public float velocityX;
    public float velocityY;
    public float damage;
    public boolean active;
    private Texture texture;

    public EnemyBullet() {
        this.bounds = new Rectangle(0, 0, 8, 8);
        this.active = false;
    }

    public void init(float x, float y, float angleInDegrees, float speed, float damage, Texture texture) {
        init(x, y, angleInDegrees, speed, damage, texture, 8, 8);
    }

    public void init(float x, float y, float angleInDegrees, float speed, float damage, Texture texture, float width,
            float height) {
        this.bounds.setPosition(x, y);
        this.bounds.setSize(width, height);
        this.damage = damage;
        this.texture = texture;
        this.active = true;

        // Convert angle to radians and calculate velocity components
        float angleInRadians = (float) Math.toRadians(angleInDegrees);
        this.velocityX = (float) Math.cos(angleInRadians) * speed;
        this.velocityY = (float) Math.sin(angleInRadians) * speed;
    }

    public void update(float delta) {
        if (!active)
            return;

        bounds.x += velocityX * delta;
        bounds.y += velocityY * delta;
    }

    public boolean isOutOfBounds(float levelWidth, float levelHeight) {
        return bounds.x < -50 || bounds.x > levelWidth + 50 ||
                bounds.y < -50 || bounds.y > levelHeight + 50;
    }

    public void draw(SpriteBatch batch) {
        if (active && texture != null) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    @Override
    public void reset() {
        active = false;
        velocityX = 0;
        velocityY = 0;
        damage = 0;
        texture = null;
    }
}
