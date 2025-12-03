package com.clement.finpro.objects;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Pool;

public class Bullet implements Pool.Poolable {
    public Rectangle bounds;
    public float velX;
    public boolean active;

    public Bullet() {
        this.bounds = new Rectangle(0, 0, 10, 5);
        this.active = false;
    }

    public void init(float x, float y, float velX) {
        this.bounds.setPosition(x, y);
        this.velX = velX;
        this.active = true;
    }

    public void update(float delta) {
        bounds.x += velX * delta;
    }

    @Override
    public void reset() {
        this.bounds.setPosition(0, 0);
        this.velX = 0;
        this.active = false;
    }
}
