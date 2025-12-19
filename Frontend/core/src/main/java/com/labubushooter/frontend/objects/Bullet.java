package com.labubushooter.frontend.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class Bullet implements Pool.Poolable {
    public Rectangle bounds;
    public Vector2 velocity;
    public boolean active;
    public float damage;
    private Texture texture;

    private float rotation;
    private float startY;

    public Bullet() {
        this.bounds = new Rectangle(0, 0, 8, 5);
        this.velocity = new Vector2(0, 0);
        this.active = false;
        this.damage = 0;
        this.startY = 0;
        this.rotation = 0;
        this.texture = null;
    }

    public void init(float x, float y, Vector2 direction, float speed, float damage, Texture texture) {
        this.bounds.setPosition(x, y);
        this.velocity.set(direction).scl(speed);
        this.damage = damage;
        this.active = true;
        this.startY = y;
        this.texture = texture;

        // Calculate rotation from velocity direction (Strategy Pattern)
        // atan2 returns angle in radians, convert to degrees
        this.rotation = (float) Math.toDegrees(Math.atan2(direction.y, direction.x));
    }

    public void update(float delta) {
        bounds.x += velocity.x * delta;
        bounds.y += velocity.y * delta;
    }

    public void draw(SpriteBatch batch, Texture defaultTexture){
        if(!active){
            return;
        }

        Texture tex = (texture != null) ? texture : defaultTexture;
        if(tex == null) return;

        // Origin bullet
        float originX = bounds.width / 2;
        float originY = bounds.height / 2;

        batch.draw(
                tex,
                bounds.x, bounds.y,
                originX, originY,
                bounds.width, bounds.height,
                1, 1,
                rotation,
                0, 0,
                (int)bounds.width, (int)bounds.height,
                false, false
        );
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
        this.rotation = 0;
        this.texture = null;
    }
}
