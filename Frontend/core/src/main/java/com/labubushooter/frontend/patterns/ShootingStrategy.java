package com.labubushooter.frontend.patterns;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.patterns.weapons.WeaponRenderer;

public interface ShootingStrategy {
    void shoot(float x, float y, Vector2 direction, Array<Bullet> activeBullets, Pool<Bullet> bulletPool, Texture bulletTexture);
    boolean isAutomatic();

    // Ammo & Reloading
    void reload();
    void addAmmo(int amount);
    int getCurrentMag();
    int getMaxMag();
    int getTotalAmmo();
    boolean isReloading();

    // Update loop for timers (reload, cooldowns)
    void update(float delta);

    // Identification
    String getName();

    // Rendering
    WeaponRenderer getRenderer();
}
