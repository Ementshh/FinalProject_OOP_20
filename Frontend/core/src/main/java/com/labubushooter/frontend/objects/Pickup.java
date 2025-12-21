package com.labubushooter.frontend.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Pool;

public class Pickup implements Pool.Poolable {
    public enum Type {
        AMMO_9MM,
        AMMO_45CAL,
        HEALTH_POTION
    }

    public Rectangle bounds;
    public Type type;
    public boolean active;
    private Texture texture;

    public Pickup() {
        this.bounds = new Rectangle(0, 0, 32, 32);
        this.active = false;
    }

    public void init(float x, float y, Type type, Texture texture) {
        this.bounds.setPosition(x, y);
        this.type = type;
        this.texture = texture;
        this.active = true;
    }

    public void draw(SpriteBatch batch) {
        if (active && texture != null) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    @Override
    public void reset() {
        active = false;
        bounds.setPosition(0, 0);
        type = null;
        texture = null;
    }
}
