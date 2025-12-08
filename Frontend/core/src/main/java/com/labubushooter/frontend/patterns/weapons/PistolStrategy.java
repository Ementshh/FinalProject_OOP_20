package com.labubushooter.frontend.patterns.weapons;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.patterns.ShootingStrategy;

public class PistolStrategy implements ShootingStrategy {
    private long lastShotTime;
    private final long FIRE_RATE_DELAY = 400000000L;
    private final float DAMAGE = 4f;

    @Override
    public boolean isAutomatic() {
        return false;
    }

    @Override
    public void shoot(float x, float y, boolean facingRight, Array<Bullet> activeBullets, Pool<Bullet> bulletPool) {
        if (TimeUtils.nanoTime() - lastShotTime > FIRE_RATE_DELAY) {
            Bullet b = bulletPool.obtain();
            b.init(x, y, facingRight ? 750 : -750, DAMAGE);
            activeBullets.add(b);
            lastShotTime = TimeUtils.nanoTime();
        }
    }
}
