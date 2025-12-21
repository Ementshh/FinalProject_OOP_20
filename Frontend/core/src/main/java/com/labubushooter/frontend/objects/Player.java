package com.labubushooter.frontend.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.labubushooter.frontend.animation.PlayerAnimationStrategy;
import com.labubushooter.frontend.animation.SpriteAligner;
import com.labubushooter.frontend.patterns.ShootingStrategy;
import com.labubushooter.frontend.patterns.weapons.Mac10Strategy;
import com.labubushooter.frontend.patterns.weapons.PistolStrategy;
import com.labubushooter.frontend.patterns.weapons.WeaponRenderer;

/**
 * Player entity with animation controller and proper sprite alignment.
 * 
 * Design Patterns:
 * - Strategy Pattern: Uses PlayerAnimationStrategy for animations
 * - Strategy Pattern: Uses ShootingStrategy for weapons
 * 
 * SOLID Principles:
 * - Single Responsibility: Handles player entity logic
 * - Open/Closed: Animation behavior can be extended via strategy
 * - Dependency Inversion: Depends on strategy abstractions
 */
public class Player extends GameObject {
    public float velY = 0;
    public boolean grounded = false;
    public boolean facingRight = true;
    
    // Current horizontal velocity for animation
    private float currentVelocityX = 0f;

    // Health System
    public float health;
    public static final float MAX_HEALTH = 20f;

    // Health Regeneration System
    private long lastDamageTime;
    private long lastRegenTime;
    private static final long REGEN_DELAY = 5000000000L; // 5 seconds in nanoseconds
    private static final long REGEN_INTERVAL = 2000000000L; // 2 seconds in nanoseconds
    private static final float REGEN_AMOUNT = 1f;

    // Strategy Pattern: The behavior of shooting is encapsulated in this interface
    private ShootingStrategy shootingStrategy;
    
    // Animation Strategy
    private PlayerAnimationStrategy animationStrategy;

    final float GRAVITY = -900f;
    final float JUMP_POWER = 500f;
    final float SPEED = 250f;
    public static float LEVEL_WIDTH = 2400f;

    //public Texture pistolTex;
    //public Texture mac10Tex;

    // Mouse aiming
    private Vector2 mouseWorldPos;
    private float weaponAngle;
    public OrthographicCamera camera;

    public Player(Texture tex) {
        super(100, 300, 50, 75, tex); // Increased from 40x60 to 50x75 (1.25x larger)
        this.shootingStrategy = null;
        this.animationStrategy = null;
        this.health = MAX_HEALTH;
        this.lastDamageTime = TimeUtils.nanoTime();
        this.lastRegenTime = TimeUtils.nanoTime();
        this.mouseWorldPos = new Vector2();
        this.weaponAngle = 0f;
    }
    
    /**
     * Sets the animation strategy for the player.
     * Enables full animation with idle, walking, and jump anticipation.
     * 
     * @param strategy The animation strategy to use
     */
    public void setAnimationStrategy(PlayerAnimationStrategy strategy) {
        this.animationStrategy = strategy;
    }
    
    /**
     * Gets the current animation strategy.
     * 
     * @return The animation strategy or null if not set
     */
    public PlayerAnimationStrategy getAnimationStrategy() {
        return animationStrategy;
    }

    public void setWeapon(ShootingStrategy strategy) {
        this.shootingStrategy = strategy;
    }

    public ShootingStrategy getWeapon() {
        return this.shootingStrategy;
    }

    public void takeDamage(float damage) {
        health -= damage;
        if (health < 0)
            health = 0;
        lastDamageTime = TimeUtils.nanoTime(); // Reset damage timer
        Gdx.app.log("Player", "Health: " + health);
    }

    public boolean isDead() {
        return health <= 0;
    }

    public void reset() {
        this.health = MAX_HEALTH;
        this.velY = 0;
        this.grounded = false;
        this.facingRight = true;
        this.bounds.setPosition(100, 300);
        this.lastDamageTime = TimeUtils.nanoTime();
        this.lastRegenTime = TimeUtils.nanoTime();
        this.weaponAngle = 0f;
        this.currentVelocityX = 0f;
        
        // Reset animation strategy
        if (animationStrategy != null) {
            animationStrategy.reset();
        }
    }

    public void update(float delta, Array<Platform> platforms, Array<Ground> grounds) {
        // Health Regeneration Logic
        long currentTime = TimeUtils.nanoTime();
        if (health < MAX_HEALTH && currentTime - lastDamageTime > REGEN_DELAY) {
            if (currentTime - lastRegenTime > REGEN_INTERVAL) {
                health += REGEN_AMOUNT;
                if (health > MAX_HEALTH)
                    health = MAX_HEALTH;
                lastRegenTime = currentTime;
                Gdx.app.log("Player", "Health regenerated: " + health);
            }
        }

        // Calculate horizontal velocity for animation
        currentVelocityX = 0f;
        
        // WASD Movement Input
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            bounds.x -= SPEED * delta;
            currentVelocityX = -SPEED;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            bounds.x += SPEED * delta;
            currentVelocityX = SPEED;
        }

        // Boundary check untuk level yang lebih lebar
        if (bounds.x < 0)
            bounds.x = 0;
        if (bounds.x + bounds.width > LEVEL_WIDTH)
            bounds.x = LEVEL_WIDTH - bounds.width;

        // 2. Gravitasi
        velY += GRAVITY * delta;
        bounds.y += velY * delta;

        // Platform Collisions
        grounded = false;
        for (Platform p : platforms) {
            if (bounds.overlaps(p.bounds)) {
                if (velY < 0 && bounds.y + bounds.height / 2 > p.bounds.y + p.bounds.height) {
                    bounds.y = p.bounds.y + p.bounds.height;
                    velY = 0;
                    grounded = true;
                }
            }
        }

        // Ground Collisions
        for (Ground g : grounds) {
            if (bounds.overlaps(g.bounds)) {
                if (velY < 0 && bounds.y + bounds.height / 2 > g.bounds.y + g.bounds.height) {
                    bounds.y = g.bounds.y + g.bounds.height;
                    velY = 0;
                    grounded = true;
                }
            }
        }

        // Reset position
        if (bounds.y < 0) {
            bounds.setPosition(100, 400);
            velY = 0;
        }

        // Update animation state
        if (animationStrategy != null) {
            animationStrategy.updateMovementState(currentVelocityX, velY, grounded);
            animationStrategy.update(delta);
            
            // Sync facing direction with animation (unless mouse aiming overrides)
            if (camera == null) {
                facingRight = !animationStrategy.isFacingLeft();
            }
        }

        // Update mouse world position and weapon angle
        if (camera != null) {
            updateMouseAiming();
        }
    }

    private void updateMouseAiming() {
        // Convert screen coordinates to world coordinates
        Vector3 mousePos3D = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos3D);
        mouseWorldPos.set(mousePos3D.x, mousePos3D.y);

        // Calculate player center
        float playerCenterX = bounds.x + bounds.width / 2;
        float playerCenterY = bounds.y + bounds.height / 2;

        // Calculate direction to mouse
        float dx = mouseWorldPos.x - playerCenterX;
        float dy = mouseWorldPos.y - playerCenterY;

        // Calculate weapon angle (smooth 360° rotation)
        weaponAngle = (float) Math.toDegrees(Math.atan2(dy, dx));

        // Determine facing direction based on mouse X position
        if (mouseWorldPos.x >= playerCenterX) {
            facingRight = true;
        } else {
            facingRight = false;
        }
        
        // Sync animation strategy facing direction with mouse aiming
        if (animationStrategy != null) {
            animationStrategy.setFacingLeft(!facingRight);
        }
    }

    /**
     * Performs a jump if grounded.
     * Jump is immediate - no anticipation delay.
     */
    public void jump() {
        if (grounded) {
            velY = JUMP_POWER;
            grounded = false;
        }
    }

    public Vector2 getShootDirection() {
        float playerCenterX = bounds.x + bounds.width / 2;
        float playerCenterY = bounds.y + bounds.height / 2;

        float dx = mouseWorldPos.x - playerCenterX;
        float dy = mouseWorldPos.y - playerCenterY;

        // Normalize
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0) {
            dx /= length;
            dy /= length;
        }

        return new Vector2(dx, dy);
    }

    public Vector2 getShootStartPosition() {
        float playerCenterX = bounds.x + bounds.width / 2;
        float playerCenterY = bounds.y + bounds.height / 2;

        Vector2 dir = getShootDirection();
        float offsetDistance = 25f;

        return new Vector2(
                playerCenterX + dir.x * offsetDistance,
                playerCenterY + dir.y * offsetDistance);
    }

    public void shoot(Array<Bullet> activeBullets, Pool<Bullet> pool, Texture bulletTexture) {
        if (shootingStrategy == null)
            return;

        Vector2 startPos = getShootStartPosition();
        Vector2 direction = getShootDirection();

        shootingStrategy.shoot(startPos.x, startPos.y, direction, activeBullets, pool, bulletTexture);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // 1. Draw Player Body with animation and proper alignment
        Texture currentTexture;
        float drawWidth, drawHeight;
        float drawX, drawY;
        
        if (animationStrategy != null) {
            currentTexture = animationStrategy.getCurrentFrame();
            
            // Calculate scaled dimensions maintaining aspect ratio
            float[] scaledDims = SpriteAligner.getScaledDimensions(
                bounds, 
                currentTexture.getWidth(), 
                currentTexture.getHeight(), 
                1.0f
            );
            drawWidth = scaledDims[0];
            drawHeight = scaledDims[1];
            
            // Get foot-aligned position (feet at bottom, centered horizontally)
            float[] alignedPos = SpriteAligner.getFootAlignedPosition(bounds, drawWidth, drawHeight);
            drawX = alignedPos[0];
            drawY = alignedPos[1];
            
            // Determine if we need to flip for facing direction
            boolean flipX = !facingRight;
            
            // Draw with proper alignment and flipping
            batch.draw(currentTexture,
                       flipX ? drawX + drawWidth : drawX,  // Adjust X for flip
                       drawY,
                       flipX ? -drawWidth : drawWidth,     // Negative width for flip
                       drawHeight);
        } else {
            // Fallback: draw texture directly without animation
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }

        // 2. Draw Weapon using WeaponRenderer (Strategy Pattern)
        if (shootingStrategy != null) {
            WeaponRenderer renderer = null;

            // Get renderer from strategy (Dependency Inversion Principle)
            if (shootingStrategy instanceof PistolStrategy) {
                renderer = ((PistolStrategy) shootingStrategy).getRenderer();
            } else if (shootingStrategy instanceof Mac10Strategy) {
                renderer = ((Mac10Strategy) shootingStrategy).getRenderer();
            }

            if (renderer != null) {
                float playerCenterX = bounds.x + bounds.width / 2;
                float playerCenterY = bounds.y + bounds.height / 2;

                // Delegate rendering to strategy (Strategy Pattern)
                renderer.render(batch, playerCenterX, playerCenterY, weaponAngle, facingRight);
            }
        }
    }
}