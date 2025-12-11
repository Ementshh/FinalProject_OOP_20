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
import com.labubushooter.frontend.patterns.ShootingStrategy;

public class Player extends GameObject {
    public float velY = 0;
    public boolean grounded = false;
    public boolean facingRight = true;

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

    final float GRAVITY = -900f;
    final float JUMP_POWER = 500f;
    final float SPEED = 250f;
    public static float LEVEL_WIDTH = 2400f;
    
    public Texture pistolTex;
    public Texture mac10Tex;
    
    // Mouse aiming
    private Vector2 mouseWorldPos;
    private float weaponAngle;
    public OrthographicCamera camera;

    public Player(Texture tex) {
        super(100, 300, 40, 60, tex);
        this.shootingStrategy = null;
        this.health = MAX_HEALTH;
        this.lastDamageTime = TimeUtils.nanoTime();
        this.lastRegenTime = TimeUtils.nanoTime();
        this.mouseWorldPos = new Vector2();
        this.weaponAngle = 0f;
    }

    public void setWeapon(ShootingStrategy strategy) {
        this.shootingStrategy = strategy;
    }

    public ShootingStrategy getWeapon() {
        return this.shootingStrategy;
    }

    public void takeDamage(float damage) {
        health -= damage;
        if (health < 0) health = 0;
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
    }

    public void update(float delta, Array<Platform> platforms) {
        // Health Regeneration Logic
        long currentTime = TimeUtils.nanoTime();
        if (health < MAX_HEALTH && currentTime - lastDamageTime > REGEN_DELAY) {
            if (currentTime - lastRegenTime > REGEN_INTERVAL) {
                health += REGEN_AMOUNT;
                if (health > MAX_HEALTH) health = MAX_HEALTH;
                lastRegenTime = currentTime;
                Gdx.app.log("Player", "Health regenerated: " + health);
            }
        }

        // WASD Movement Input
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            bounds.x -= SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            bounds.x += SPEED * delta;
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

        // Reset position
        if (bounds.y < 0) {
            bounds.setPosition(100, 400);
            velY = 0;
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
        
        // Determine facing direction based on mouse X position
        if (mouseWorldPos.x > playerCenterX) {
            facingRight = true;
        } else {
            facingRight = false;
        }
        
        // Calculate weapon angle
        float dx = mouseWorldPos.x - playerCenterX;
        float dy = mouseWorldPos.y - playerCenterY;
        weaponAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
        
        // Adjust angle if facing left
        if (!facingRight && weaponAngle > 0) {
            weaponAngle = 180 - weaponAngle;
        } else if (!facingRight && weaponAngle < 0) {
            weaponAngle = -180 - weaponAngle;
        }
    }

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
            playerCenterY + dir.y * offsetDistance
        );
    }

    public void shoot(Array<Bullet> activeBullets, Pool<Bullet> pool) {
        if (shootingStrategy == null) return;

        Vector2 startPos = getShootStartPosition();
        Vector2 direction = getShootDirection();
        
        shootingStrategy.shoot(startPos.x, startPos.y, direction, activeBullets, pool);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // 1. Draw Player Body
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);

        // 2. Draw Weapon Hitbox (if holding one)
        if (shootingStrategy != null) {
            Texture currentWeaponTex = null;
            float w = 0, h = 0;
            String strategyName = shootingStrategy.getClass().getSimpleName();

            if (strategyName.contains("Pistol")) {
                currentWeaponTex = pistolTex;
                w = 20; h = 10;
            } else if (strategyName.contains("Mac10")) {
                currentWeaponTex = mac10Tex;
                w = 30; h = 15;
            }

            if (currentWeaponTex != null) {
                float playerCenterX = bounds.x + bounds.width / 2;
                float playerCenterY = bounds.y + bounds.height / 2;
                
                float weaponDistance = 15f;
                Vector2 dir = getShootDirection();
                float weaponX = playerCenterX + dir.x * weaponDistance;
                float weaponY = playerCenterY + dir.y * weaponDistance;
                
                float originX = w / 2;
                float originY = h / 2;
                
                float scaleX = 1;
                float scaleY = facingRight ? 1 : -1;
                
                batch.draw(
                    currentWeaponTex,
                    weaponX - originX, weaponY - originY,
                    originX, originY,
                    w, h,
                    scaleX, scaleY,
                    weaponAngle,
                    0, 0,
                    (int)w, (int)h,
                    false, false
                );
            }
        }
    }
}