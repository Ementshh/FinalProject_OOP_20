package com.labubushooter.frontend.patterns.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.patterns.ShootingStrategy;

public class PistolStrategy implements ShootingStrategy {
    private long lastShotTime;
    private final long FIRE_RATE_DELAY = 400000000L;
    private final float DAMAGE = 4f;
    private final float BULLET_SPEED = 750f;

    private final WeaponRenderer renderer;

    public PistolStrategy(Texture pistolTexture){
        this.renderer = new PistolRenderer(pistolTexture);
    }

    @Override
    public boolean isAutomatic() {
        return false;
    }

    @Override
    public void shoot(float x, float y, Vector2 direction, Array<Bullet> activeBullets, Pool<Bullet> bulletPool, Texture bulletTexture) {
        if (TimeUtils.nanoTime() - lastShotTime > FIRE_RATE_DELAY) {
            Bullet b = bulletPool.obtain();
            b.init(x, y, direction, BULLET_SPEED, DAMAGE, bulletTexture);
            activeBullets.add(b);
            lastShotTime = TimeUtils.nanoTime();
        }
    }

    public WeaponRenderer getRenderer() {
        return renderer;
    }
}
