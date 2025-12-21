package com.labubushooter.frontend.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public abstract class BossEnemy {
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

    protected void applyGravityAndCollision(float delta, Array<Platform> platforms, Array<Ground> grounds) {
        // Apply gravity
        velY += GRAVITY * delta;
        bounds.y += velY * delta;

        // Update collider position
        updateCollider();

        // Platform collision - bottom collision only (boss lands when bottom touches platform top)
        grounded = false;
        for (Platform p : platforms) {
            if (bounds.overlaps(p.bounds)) {
                // Get collision points
                float bossBottom = getCollisionBottomY();
                float platformTop = p.bounds.y + p.bounds.height;
                
                // Boss is falling AND boss has penetrated the platform
                // Check if boss bottom is below platform top (penetration)
                if (velY < 0 && bossBottom < platformTop) {
                    // Position boss so its bottom sits exactly on platform top
                    bounds.y = platformTop;
                    velY = 0;
                    grounded = true;
                    updateCollider();
                }
            }
        }

        // Ground collision - same logic as platforms
        for (Ground g : grounds) {
            if (bounds.overlaps(g.bounds)) {
                // Get collision points
                float bossBottom = getCollisionBottomY();
                float groundTop = g.bounds.y + g.bounds.height;
                
                // Boss is falling AND boss has penetrated the ground
                // Check if boss bottom is below ground top (penetration)
                if (velY < 0 && bossBottom < groundTop) {
                    // Position boss so its bottom sits exactly on ground top
                    bounds.y = groundTop;
                    velY = 0;
                    grounded = true;
                    updateCollider();
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
    
    /**
     * Gets the Y coordinate of the bottom of the boss for collision detection.
     * Override this method in subclasses if custom collision behavior is needed.
     * 
     * Default implementation returns the bottom of the bounds rectangle.
     * This is the standard behavior for most boss types.
     * 
     * @return Y coordinate of the collision bottom point
     */
    protected float getCollisionBottomY() {
        return bounds.y;
    }
    
    /**
     * Gets the visual offset from the bottom of bounds to the bottom of the rendered sprite.
     * Used for debugging and visual verification.
     * 
     * Default implementation returns 0 (no offset - sprite bottom aligns with bounds bottom).
     * Override in subclasses that use visual scaling or offset rendering.
     * 
     * @return Visual bottom offset in pixels (negative means sprite extends below bounds)
     */
    protected float getVisualBottomOffset() {
        return 0f;
    }

    public void draw(SpriteBatch batch) {
        // Draw sprite with flipping support
        Texture currentTexture = getCurrentTexture();
        boolean flipX = isFacingLeft();
        
        // Draw with mirroring based on facing direction
        // Using originX = 0 means flip happens around left-bottom corner
        batch.draw(currentTexture, 
                  bounds.x, bounds.y, 
                  0f, 0f,  // originX, originY (0,0 = flip around left-bottom corner)
                  bounds.width, bounds.height,
                  1f, 1f, 0f,  // scaleX, scaleY, rotation
                  0, 0,  // srcX, srcY
                  currentTexture.getWidth(), currentTexture.getHeight(),
                  flipX, false);  // flipX, flipY

        // Draw health bar above sprite
        drawHealthBar(batch);
    }
    
    /**
     * Checks if the boss is facing left.
     * Override in subclasses to provide facing direction.
     * 
     * @return true if facing left, false if facing right
     */
    protected boolean isFacingLeft() {
        return false; // Default: face right
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
    
    /**
     * Gets the health bar background texture.
     * Protected access for subclasses that need custom health bar rendering.
     * 
     * @return The health bar background texture
     */
    protected static Texture getHealthBarBg() {
        return healthBarBg;
    }
    
    /**
     * Gets the health bar border texture.
     * Protected access for subclasses that need custom health bar rendering.
     * 
     * @return The health bar border texture
     */
    protected static Texture getHealthBarBorder() {
        return healthBarBorder;
    }
    
    /**
     * Gets the health bar fill texture.
     * Protected access for subclasses that need custom health bar rendering.
     * 
     * @return The health bar fill texture
     */
    protected static Texture getHealthBarFill() {
        return healthBarFill;
    }

    public abstract void update(float delta, Array<Platform> platforms, Array<Ground> grounds);
}
