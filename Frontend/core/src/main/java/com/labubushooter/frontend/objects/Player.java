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
        
        // Calculate weapon angle (pure 360° rotation)
        float dx = mouseWorldPos.x - playerCenterX;
        float dy = mouseWorldPos.y - playerCenterY;
        weaponAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
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

        // 2. Draw Weapon with smooth 360° rotation and Y-axis mirroring
        if (shootingStrategy != null) {
            Texture currentWeaponTex = null;
            float w = 0, h = 0;
            String strategyName = shootingStrategy.getClass().getSimpleName();

            // Pistol: 25x8 horizontal rectangle (seperti gambar 1)
            if (strategyName.contains("Pistol")) {
                currentWeaponTex = pistolTex;
                w = 25; 
                h = 8;
            } 
            // SMG/Mac10: 30x10 horizontal rectangle (seperti gambar 2)
            else if (strategyName.contains("Mac10")) {
                currentWeaponTex = mac10Tex;
                w = 30; 
                h = 10;
            }

            if (currentWeaponTex != null) {
                float playerCenterX = bounds.x + bounds.width / 2;
                float playerCenterY = bounds.y + bounds.height / 2;
                
                // Weapon offset dari player center
                float weaponOffsetX = 10f; // Jarak horizontal dari center
                float weaponOffsetY = 5f;  // Sedikit ke atas dari center
                
                // Position weapon di tangan player
                float weaponX = playerCenterX + weaponOffsetX;
                float weaponY = playerCenterY + weaponOffsetY;
                
                // Origin untuk rotation (di grip/handle, pojok kiri tengah)
                float originX = 5f;     // Grip position dari kiri
                float originY = h / 2;  // Center vertikal
                
                // Mirror weapon dengan Y-axis flip jika facing left
                float scaleY = facingRight ? 1 : -1;
                
                // Draw weapon dengan rotation 360° smooth
                batch.draw(
                    currentWeaponTex,
                    weaponX - originX,       // x position
                    weaponY - originY,       // y position
                    originX,                 // origin x (rotation pivot)
                    originY,                 // origin y (rotation pivot)
                    w,                       // width
                    h,                       // height
                    1,                       // scale x
                    scaleY,                  // scale y (flip if facing left)
                    weaponAngle,             // rotation angle (360°)
                    0, 0,                    // source x, y
                    (int)w, (int)h,          // source width, height
                    false, false             // flip x, y
                );
            }
        }
    }
}