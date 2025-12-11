package com.labubushooter.frontend.objects;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class Bullet implements Pool.Poolable {
    public Rectangle bounds;
    public Vector2 velocity;
    public boolean active;
    public float damage;
    
    private float startY;

    public Bullet() {
        this.bounds = new Rectangle(0, 0, 10, 5);
        this.velocity = new Vector2(0, 0);
        this.active = false;
        this.damage = 0;
        this.startY = 0;
    }

    public void init(float x, float y, Vector2 direction, float speed, float damage) {
        this.bounds.setPosition(x, y);
        this.velocity.set(direction).scl(speed);
        this.damage = damage;
        this.active = true;
        this.startY = y;
    }

    public void update(float delta) {
        bounds.x += velocity.x * delta;
        bounds.y += velocity.y * delta;
    }
    
    public boolean isOutOfVerticalBounds(float screenHeight) {
        return Math.abs(bounds.y - startY) > screenHeight;
    }

    @Override
    public void reset() {
        this.bounds.setPosition(0, 0);
        this.velocity.set(0, 0);
        this.damage = 0;
        this.active = false;
        this.startY = 0;
    }
}
