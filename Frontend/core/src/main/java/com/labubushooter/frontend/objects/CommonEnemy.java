package com.labubushooter.frontend.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;

public class CommonEnemy implements Pool.Poolable {
    public Rectangle collider;
    public boolean spawned;
    public float health;
    public float maxHealth;
    public boolean grounded = false;

    private Player target;
    private float velocityX;
    private float velocityY = 0;
    private float speed; // Speed akan di-set per level
    private Texture texture;
    private Texture coloredTexture; // Texture dengan warna random
    private float damageAmount;

    // Damage System
    private long lastDamageTime;
    private static final long DAMAGE_COOLDOWN = 1000000000L; // 1 second in nanoseconds

    // Jump System
    private static final float JUMP_POWER = 500f; 
    private static final float JUMP_THRESHOLD = 10f; // Threshold untuk X-axis alignment

    private static final float GRAVITY = -900f;
    private static final float WIDTH = 40f;
    private static final float HEIGHT = 60f;
    private static final float SPAWN_Y = 300f; // Ground + 1f
    
    // Base speed untuk multiplier
    private static final float BASE_SPEED = 120f;
    
    // Array warna untuk enemy (hindari ORANGE yang merupakan warna player)
    private static final Color[] ENEMY_COLORS = {
        Color.RED,
        Color.SCARLET,
        Color.MAROON,
        Color.PURPLE,
        Color.VIOLET,
        Color.MAGENTA,
        Color.PINK,
        Color.CORAL,
        Color.FIREBRICK,
        Color.BROWN
    };

    public CommonEnemy(Texture texture) {
        this.texture = texture;
        this.collider = new Rectangle(0, SPAWN_Y, WIDTH, HEIGHT);
        this.spawned = false;
        this.health = 10f;
        this.maxHealth = 10f;
        this.damageAmount = 1f;
        this.speed = BASE_SPEED;
        this.velocityX = 0;
        this.velocityY = 0;
        this.lastDamageTime = 0;
    }

    public void init(float x, Player target, int level) {
        this.collider.setPosition(x, SPAWN_Y);
        this.target = target;
        
        // Set health, damage, and speed based on level
        switch (level) {
            case 1:
                this.maxHealth = 10f;
                this.damageAmount = 1f;
                this.speed = BASE_SPEED * 1.2f; // 1.2x lebih cepat
                break;
            case 2:
                this.maxHealth = 15f;
                this.damageAmount = 1.5f;
                this.speed = BASE_SPEED * 1.5f; // 1.5x lebih cepat
                break;
            case 4:
                this.maxHealth = 20f;
                this.damageAmount = 1.5f;
                this.speed = BASE_SPEED * 1.8f; // 1.8x lebih cepat
                break;
            default:
                this.maxHealth = 10f;
                this.damageAmount = 1f;
                this.speed = BASE_SPEED;
                break;
        }
        
        this.health = this.maxHealth;
        this.spawned = true;
        this.velocityX = 0;
        this.velocityY = 0;
        this.grounded = false;
        this.lastDamageTime = TimeUtils.nanoTime();
        
        // Generate random colored texture
        generateRandomColorTexture();
        
        Gdx.app.log("Enemy", "Spawned at level " + level + " - HP: " + maxHealth + 
                    ", Damage: " + damageAmount + ", Speed: " + speed);
    }
    
    private void generateRandomColorTexture() {
        // Pilih warna random dari array
        Color randomColor = ENEMY_COLORS[MathUtils.random(0, ENEMY_COLORS.length - 1)];
        
        // Dispose texture lama jika ada
        if (coloredTexture != null) {
            coloredTexture.dispose();
        }
        
        // Buat texture baru dengan warna random
        Pixmap pixmap = new Pixmap((int)WIDTH, (int)HEIGHT, Pixmap.Format.RGBA8888);
        pixmap.setColor(randomColor);
        pixmap.fill();
        coloredTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void update(float delta, Array<Platform> platforms) {
        if (!spawned || target == null) return;

        // Homing movement (X-axis only)
        float targetCenterX = target.bounds.x + target.bounds.width / 2f;
        float enemyCenterX = collider.x + collider.width / 2f;

        float directionX = targetCenterX - enemyCenterX;

        if (Math.abs(directionX) > 5f) {
            float normalizedDirX = Math.signum(directionX);
            velocityX = normalizedDirX * speed; // Menggunakan speed yang sudah di-set per level
        } else {
            velocityX = 0;
        }

        // Jump Logic: Jump jika X-axis sudah sejajar tapi belum overlap dengan player
        if (Math.abs(directionX) <= JUMP_THRESHOLD && grounded) {
            // Check jika player ada di atas enemy (Y player > Y enemy)
            if (target.bounds.y > collider.y + collider.height) {
                // Jump untuk mencapai player
                velocityY = JUMP_POWER;
                grounded = false;
            }
        }

        // Apply horizontal movement
        collider.x += velocityX * delta;

        // Boundary check (X-axis)
        if (collider.x < 0) collider.x = 0;
        if (collider.x + collider.width > Player.LEVEL_WIDTH) {
            collider.x = Player.LEVEL_WIDTH - collider.width;
        }

        // Apply gravity (Y-axis)
        velocityY += GRAVITY * delta;
        collider.y += velocityY * delta;

        // Platform collision detection
        grounded = false;
        for (Platform p : platforms) {
            if (collider.overlaps(p.bounds)) {
                if (velocityY < 0 && collider.y + collider.height / 2 > p.bounds.y + p.bounds.height) {
                    collider.y = p.bounds.y + p.bounds.height;
                    velocityY = 0;
                    grounded = true;
                }
            }
        }

        // Reset position jika jatuh ke bawah
        if (collider.y < 0) {
            spawned = false;
        }

        // Damage player if touching (damage per second based on level)
        if (collider.overlaps(target.bounds)) {
            if (TimeUtils.nanoTime() - lastDamageTime > DAMAGE_COOLDOWN) {
                target.takeDamage(damageAmount);
                lastDamageTime = TimeUtils.nanoTime();
            }
        }
    }

    public void takeDamage(float damage) {
        health -= damage;
        if (health <= 0) {
            spawned = false;
        }
    }

    public void draw(SpriteBatch batch) {
        if (!spawned) return;

        // Gunakan texture berwarna random
        Texture texToDraw = (coloredTexture != null) ? coloredTexture : texture;
        batch.draw(texToDraw, collider.x, collider.y, collider.width, collider.height);
    }

    @Override
    public void reset() {
        this.collider.setPosition(0, SPAWN_Y);
        this.spawned = false;
        this.health = 10f;
        this.maxHealth = 10f;
        this.damageAmount = 1f;
        this.speed = BASE_SPEED;
        this.target = null;
        this.velocityX = 0;
        this.velocityY = 0;
        this.grounded = false;
        this.lastDamageTime = 0;
        
        // Dispose colored texture
        if (coloredTexture != null) {
            coloredTexture.dispose();
            coloredTexture = null;
        }
    }

    public boolean isActive() {
        return spawned;
    }

    public void setActive(boolean spawned) {
        this.spawned = spawned;
    }
}