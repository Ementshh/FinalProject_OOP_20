package com.labubushooter.frontend.patterns.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.patterns.ShootingStrategy;
import com.labubushooter.frontend.services.AssetManager;

public class UnarmedStrategy implements ShootingStrategy {
    private long lastShotTime;
    private final long FIRE_RATE_DELAY = 1000000000L; // 1 second
    private final float DAMAGE = 1f;
    private final float PUNCH_RANGE_SPEED = 600f; // Fast but short lived

    private final WeaponRenderer renderer;
    private final Texture transparentTexture;

    public UnarmedStrategy() {
        this.renderer = new UnarmedRenderer();
        // Get transparent texture from AssetManager
        this.transparentTexture = AssetManager.getInstance().getTexture(AssetManager.TRANSPARENT);
    }

    @Override
    public boolean isAutomatic() {
        return false;
    }

    @Override
    public void shoot(float x, float y, Vector2 direction, Array<Bullet> activeBullets, Pool<Bullet> bulletPool, Texture bulletTexture) {
        if (TimeUtils.nanoTime() - lastShotTime > FIRE_RATE_DELAY) {
            Bullet b = bulletPool.obtain();

            // Use transparent texture instead of the passed bulletTexture
            // This makes the "bullet" invisible, simulating a melee punch
            Texture texToUse = (transparentTexture != null) ? transparentTexture : bulletTexture;

            b.init(x, y, direction, PUNCH_RANGE_SPEED, DAMAGE, texToUse);

            // Note: The bullet will fly until it hits something or goes off screen.
            // Since it's invisible and fast, it acts like a projectile punch.
            // Ideally we would limit its range, but Bullet class doesn't support lifespan yet.
            // Given the speed (600) and typical screen width, it will clear quickly.

            activeBullets.add(b);
            lastShotTime = TimeUtils.nanoTime();
        }
    }

    @Override
    public void reload() {
        // No reload for unarmed
    }

    @Override
    public void addAmmo(int amount) {
        // Infinite ammo
    }

    @Override
    public int getCurrentMag() {
        return 1; // Always ready
    }

    @Override
    public int getMaxMag() {
        return 1;
    }

    @Override
    public int getTotalAmmo() {
        return 9999; // Infinite
    }

    @Override
    public boolean isReloading() {
        return false;
    }

    @Override
    public void update(float delta) {
        // Nothing to update
    }

    @Override
    public String getName() {
        return "Unarmed";
    }

    public WeaponRenderer getRenderer() {
        return renderer;
    }
}
