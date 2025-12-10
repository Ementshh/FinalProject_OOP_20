package com.labubushooter.frontend.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class Coin implements Pool.Poolable {
    private Vector2 position;
    public Rectangle bounds;
    public boolean active;
    
    private float bobOffset;
    private float bobSpeed;
    private static final float BOB_AMPLITUDE = 5f;
    private static final float RADIUS = 10f;
    
    public static final float WIDTH = RADIUS * 2;
    public static final float HEIGHT = RADIUS * 2;
    
    public Coin() {
        this.position = new Vector2(0, 0);
        this.bounds = new Rectangle(0, 0, WIDTH, HEIGHT);
        this.active = false;
        this.bobSpeed = MathUtils.random(2f, 4f);
        this.bobOffset = MathUtils.random(0f, MathUtils.PI2);
    }
    
    public void init(float x, float y) {
        this.position.set(x + RADIUS, y + RADIUS); // Center position
        this.bounds.setPosition(x, y);
        this.active = true;
        this.bobOffset = MathUtils.random(0f, MathUtils.PI2);
    }
    
    public void update(float delta) {
        if (!active) return;
        bobOffset += bobSpeed * delta;
        
        // Update collider position
        bounds.setPosition(position.x - RADIUS, position.y - RADIUS);
    }
    
    public void renderShape(ShapeRenderer shapeRenderer) {
        if (!active) return;
        
        float drawY = position.y + (float)(Math.sin(bobOffset) * BOB_AMPLITUDE);
        
        // Draw filled circle dengan warna emas
        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.circle(position.x, drawY, RADIUS);
    }
    
    public boolean isColliding(Rectangle playerCollider) {
        return active && bounds.overlaps(playerCollider);
    }
    
    public boolean isOffScreen(float cameraLeft) {
        return (position.x + RADIUS) < cameraLeft;
    }
    
    public Vector2 getPosition() {
        return position;
    }
    
    public void setPosition(float x, float y) {
        this.position.set(x, y);
        this.bounds.setPosition(x - RADIUS, y - RADIUS);
    }
    
    @Override
    public void reset() {
        this.position.set(0, 0);
        this.bounds.setPosition(0, 0);
        this.active = false;
    }
}
