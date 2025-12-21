package com.labubushooter.frontend.patterns.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.patterns.ShootingStrategy;

public class PistolStrategy implements ShootingStrategy {
    private long lastShotTime;
    private final long FIRE_RATE_DELAY = 400000000L; // 0.4s
    private final float DAMAGE = 4f;
    private final float BULLET_SPEED = 750f;

    // Ammo System
    private int currentMag;
    private int totalAmmo;
    private final int MAX_MAG = 15;

    // Reloading
    private boolean isReloading = false;
    private float reloadTimer = 0f;
    private final float RELOAD_TIME = 1.5f; // 1.5 seconds reload

    private final WeaponRenderer renderer;

    public PistolStrategy(Texture pistolTexture){
        this.renderer = new PistolRenderer(pistolTexture);
        this.currentMag = MAX_MAG;
        this.totalAmmo = 90; // Starter ammo
    }

    @Override
    public boolean isAutomatic() {
        return false;
    }

    @Override
    public void shoot(float x, float y, Vector2 direction, Array<Bullet> activeBullets, Pool<Bullet> bulletPool, Texture bulletTexture) {
        if (isReloading) return;

        if (currentMag <= 0) {
            reload();
            return;
        }

        if (TimeUtils.nanoTime() - lastShotTime > FIRE_RATE_DELAY) {
            Bullet b = bulletPool.obtain();
            b.init(x, y, direction, BULLET_SPEED, DAMAGE, bulletTexture);
            activeBullets.add(b);

            currentMag--;
            lastShotTime = TimeUtils.nanoTime();
        }
    }

    @Override
    public void reload() {
        if (isReloading || currentMag == MAX_MAG || totalAmmo <= 0) return;

        isReloading = true;
        reloadTimer = 0f;
        Gdx.app.log("Pistol", "Reloading...");
    }

    @Override
    public void addAmmo(int amount) {
        totalAmmo += amount;
        Gdx.app.log("Pistol", "Added " + amount + " ammo. Total: " + totalAmmo);
    }

    @Override
    public int getCurrentMag() {
        return currentMag;
    }

    @Override
    public int getMaxMag() {
        return MAX_MAG;
    }

    @Override
    public int getTotalAmmo() {
        return totalAmmo;
    }

    @Override
    public boolean isReloading() {
        return isReloading;
    }

    @Override
    public void update(float delta) {
        if (isReloading) {
            reloadTimer += delta;
            if (reloadTimer >= RELOAD_TIME) {
                finishReload();
            }
        }
    }

    private void finishReload() {
        isReloading = false;
        int needed = MAX_MAG - currentMag;
        int toAdd = Math.min(needed, totalAmmo);

        currentMag += toAdd;
        totalAmmo -= toAdd;
        Gdx.app.log("Pistol", "Reload complete. Mag: " + currentMag + ", Total: " + totalAmmo);
    }

    @Override
    public String getName() {
        return "Pistol";
    }

    public WeaponRenderer getRenderer() {
        return renderer;
    }
}
