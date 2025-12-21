package com.labubushooter.frontend.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Pool;
import com.labubushooter.frontend.patterns.bullets.BulletRenderStrategy;
import com.labubushooter.frontend.patterns.bullets.BulletRenderStrategyFactory;
import com.labubushooter.frontend.patterns.bullets.BulletRenderStrategyFactory.BulletType;

public class EnemyBullet implements Pool.Poolable {
    public Rectangle bounds;
    public float velocityX;
    public float velocityY;
    public float damage;
    public boolean active;
    private Texture texture;
    private BulletRenderStrategy renderStrategy;

    public EnemyBullet() {
        this.bounds = new Rectangle(0, 0, 8, 8);
        this.active = false;
        this.renderStrategy = null;
    }

    /**
     * Initialize bullet with texture (legacy method for backward compatibility).
     * Used by MiniBoss and other enemies.
     * 
     * @param x Starting X position
     * @param y Starting Y position
     * @param angleInDegrees Firing angle in degrees
     * @param speed Bullet speed
     * @param damage Bullet damage
     * @param texture Bullet texture
     */
    public void init(float x, float y, float angleInDegrees, float speed, float damage, Texture texture) {
        init(x, y, angleInDegrees, speed, damage, texture, 8, 8);
    }

    /**
     * Initialize bullet with texture and custom size (legacy method for backward compatibility).
     * Used by MiniBoss and other enemies.
     * 
     * @param x Starting X position
     * @param y Starting Y position
     * @param angleInDegrees Firing angle in degrees
     * @param speed Bullet speed
     * @param damage Bullet damage
     * @param texture Bullet texture
     * @param width Bullet width
     * @param height Bullet height
     */
    public void init(float x, float y, float angleInDegrees, float speed, float damage, Texture texture, float width,
            float height) {
        this.bounds.setPosition(x, y);
        this.bounds.setSize(width, height);
        this.damage = damage;
        this.texture = texture;
        this.active = true;
        this.renderStrategy = null; // No strategy for legacy bullets

        // Convert angle to radians and calculate velocity components
        float angleInRadians = (float) Math.toRadians(angleInDegrees);
        this.velocityX = (float) Math.cos(angleInRadians) * speed;
        this.velocityY = (float) Math.sin(angleInRadians) * speed;
    }

    /**
     * Initialize bullet with rendering strategy (new method for boss bullets).
     * Used by FinalBoss for Phase 1, Phase 2/3, and Big Attack bullets.
     * 
     * @param x Starting X position
     * @param y Starting Y position
     * @param angleInDegrees Firing angle in degrees
     * @param speed Bullet speed
     * @param damage Bullet damage
     * @param bulletType Type of bullet (determines rendering strategy)
     * @param width Bullet width
     * @param height Bullet height
     */
    public void init(float x, float y, float angleInDegrees, float speed, float damage, 
                     BulletType bulletType, float width, float height) {
        // Set up basic properties
        this.bounds.setPosition(x, y);
        this.bounds.setSize(width, height);
        this.damage = damage;
        this.active = true;
        
        // Calculate velocity
        float angleInRadians = (float) Math.toRadians(angleInDegrees);
        this.velocityX = (float) Math.cos(angleInRadians) * speed;
        this.velocityY = (float) Math.sin(angleInRadians) * speed;
        
        // Create rendering strategy
        this.renderStrategy = BulletRenderStrategyFactory.getInstance()
            .createStrategy(bulletType, velocityX, velocityY);
        this.texture = renderStrategy.getTexture();
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

    /**
     * Draw the bullet using either rendering strategy or legacy texture rendering.
     * 
     * @param batch SpriteBatch for rendering
     * @param delta Time since last frame (for rotation animations)
     */
    public void draw(SpriteBatch batch, float delta) {
        if (!active) return;
        
        if (renderStrategy != null) {
            // Use strategy for boss bullets (with rotation/animation)
            renderStrategy.render(batch, bounds, delta);
        } else if (texture != null) {
            // Legacy rendering for non-boss bullets (simple texture draw)
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    /**
     * Legacy draw method for backward compatibility.
     * Calls the new draw method with delta = 0.
     * 
     * @param batch SpriteBatch for rendering
     */
    public void draw(SpriteBatch batch) {
        draw(batch, 0f);
    }

    @Override
    public void reset() {
        active = false;
        velocityX = 0;
        velocityY = 0;
        damage = 0;
        texture = null;
        if (renderStrategy != null) {
            renderStrategy.reset();
            renderStrategy = null;
        }
    }
}
