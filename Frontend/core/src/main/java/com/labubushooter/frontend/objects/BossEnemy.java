package com.labubushooter.frontend.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public abstract class   BossEnemy {
    public Rectangle bounds;
    public Rectangle collider; // Slightly smaller for fairness
    public float health;
    public float maxHealth;
    public float damage;
    public boolean active;
    public float velY = 0;
    public boolean grounded = false;

    protected Texture texture;
    protected Texture flashTexture;

    // Health bar textures
    private static Texture healthBarBg;
    private static Texture healthBarBorder;
    private static Texture healthBarFill;

    static {
        // Create 1x1 pixel textures for health bar
        Pixmap bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(0f, 0f, 0f, 0.7f);
        bgPixmap.fill();
        healthBarBg = new Texture(bgPixmap);
        bgPixmap.dispose();

        Pixmap borderPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        borderPixmap.setColor(Color.WHITE);
        borderPixmap.fill();
        healthBarBorder = new Texture(borderPixmap);
        borderPixmap.dispose();

        Pixmap fillPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        fillPixmap.setColor(Color.GREEN);
        fillPixmap.fill();
        healthBarFill = new Texture(fillPixmap);
        fillPixmap.dispose();
    }

    protected static final float GRAVITY = -900f;

    public BossEnemy(float x, float y, float width, float height, float maxHealth, float damage, Texture texture,
            Texture flashTexture) {
        this.bounds = new Rectangle(x, y, width, height);
        // Collider is 80% of bounds for fairer hitbox
        float colliderWidth = width * 0.8f;
        float colliderHeight = height * 0.8f;
        float offsetX = (width - colliderWidth) / 2;
        float offsetY = (height - colliderHeight) / 2;
        this.collider = new Rectangle(x + offsetX, y + offsetY, colliderWidth, colliderHeight);

        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.damage = damage;
        this.texture = texture;
        this.flashTexture = flashTexture;
        this.active = true;
    }

    public void takeDamage(float amount) {
        health -= amount;
        if (health < 0)
            health = 0;
    }

    public boolean isDead() {
        return health <= 0;
    }

    protected void applyGravityAndCollision(float delta, Array<Platform> platforms) {
        // Apply gravity
        velY += GRAVITY * delta;
        bounds.y += velY * delta;

        // Update collider position
        updateCollider();

        // Platform collision
        grounded = false;
        for (Platform p : platforms) {
            if (bounds.overlaps(p.bounds)) {
                // Calculate overlap amounts
                float overlapBottom = (bounds.y + bounds.height) - p.bounds.y;
                float overlapTop = (p.bounds.y + p.bounds.height) - bounds.y;
                float overlapLeft = (bounds.x + bounds.width) - p.bounds.x;
                float overlapRight = (p.bounds.x + p.bounds.width) - bounds.x;

                // Determine smallest overlap to find collision side
                float minOverlap = Math.min(Math.min(overlapBottom, overlapTop), Math.min(overlapLeft, overlapRight));

                // Landing on top (falling down)
                if (minOverlap == overlapTop && velY <= 0 && overlapTop > 0 && overlapTop < bounds.height) {
                    bounds.y = p.bounds.y + p.bounds.height;
                    velY = 0;
                    grounded = true;
                }
                // Hitting ceiling (jumping up)
                else if (minOverlap == overlapBottom && velY > 0 && overlapBottom > 0
                        && overlapBottom < bounds.height) {
                    bounds.y = p.bounds.y - bounds.height;
                    velY = 0;
                }
                // Hitting from left
                else if (minOverlap == overlapLeft && overlapLeft > 0 && overlapLeft < bounds.width) {
                    bounds.x = p.bounds.x - bounds.width;
                }
                // Hitting from right
                else if (minOverlap == overlapRight && overlapRight > 0 && overlapRight < bounds.width) {
                    bounds.x = p.bounds.x + p.bounds.width;
                }
            }
        }

        updateCollider();
    }

    protected void updateCollider() {
        float offsetX = (bounds.width - collider.width) / 2;
        float offsetY = (bounds.height - collider.height) / 2;
        collider.setPosition(bounds.x + offsetX, bounds.y + offsetY);
    }

    public void draw(SpriteBatch batch) {
        // Draw sprite
        batch.draw(getCurrentTexture(), bounds.x, bounds.y, bounds.width, bounds.height);

        // Draw health bar above sprite
        drawHealthBar(batch);
    }

    protected Texture getCurrentTexture() {
        return texture;
    }

    private void drawHealthBar(SpriteBatch batch) {
        float barWidth = bounds.width;
        float barHeight = 5f;
        float barX = bounds.x;
        float barY = bounds.y + bounds.height + 10f;

        float healthPercent = health / maxHealth;

        // Draw background (black with transparency)
        batch.setColor(0f, 0f, 0f, 0.7f);
        batch.draw(healthBarBg, barX - 1, barY - 1, barWidth + 2, barHeight + 2);

        // Draw border (white)
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(healthBarBorder, barX - 1, barY - 1, barWidth + 2, 1); // Bottom
        batch.draw(healthBarBorder, barX - 1, barY + barHeight, barWidth + 2, 1); // Top
        batch.draw(healthBarBorder, barX - 1, barY, 1, barHeight); // Left
        batch.draw(healthBarBorder, barX + barWidth, barY, 1, barHeight); // Right

        // Draw health bar (green to red gradient based on health)
        float red = 1f - healthPercent;
        float green = healthPercent;
        batch.setColor(red, green, 0f, 1f);
        batch.draw(healthBarFill, barX, barY, barWidth * healthPercent, barHeight);

        // Reset color
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public abstract void update(float delta, Array<Platform> platforms);
}
