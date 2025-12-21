package com.labubushooter.frontend.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
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
    
    // Animation fields (similar to Coin)
    private float bobOffset;
    private float bobSpeed;
    private static final float BOB_AMPLITUDE = 5f;
    private float baseY; // Store the base Y position for animation

    public Pickup() {
        this.bounds = new Rectangle(0, 0, 32, 32);
        this.active = false;
        this.bobSpeed = MathUtils.random(2f, 4f);
        this.bobOffset = MathUtils.random(0f, MathUtils.PI2);
    }

    public void init(float x, float y, Type type, Texture texture) {
        this.bounds.setPosition(x, y);
        this.baseY = y;
        this.type = type;
        this.texture = texture;
        this.active = true;
        this.bobOffset = MathUtils.random(0f, MathUtils.PI2);
    }
    
    public void update(float delta) {
        if (!active) return;
        bobOffset += bobSpeed * delta;
        // Update Y position with bob animation
        float animatedY = baseY + (float)(Math.sin(bobOffset) * BOB_AMPLITUDE);
        bounds.setPosition(bounds.x, animatedY);
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
        baseY = 0;
        type = null;
        texture = null;
        bobOffset = MathUtils.random(0f, MathUtils.PI2);
    }
}
