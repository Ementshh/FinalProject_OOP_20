package com.labubushooter.frontend.patterns.weapons;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.patterns.ShootingStrategy;

public class Mac10Strategy implements ShootingStrategy {
    private long lastShotTime;
    private final long FIRE_RATE_DELAY = 150000000L;
    private final float DAMAGE = 2f;
    private final float BULLET_SPEED = 800f;

    @Override
    public boolean isAutomatic() {
        return true;
    }

    @Override
    public void shoot(float x, float y, Vector2 direction, Array<Bullet> activeBullets, Pool<Bullet> bulletPool) {
        if (TimeUtils.nanoTime() - lastShotTime > FIRE_RATE_DELAY) {
            Bullet b = bulletPool.obtain();
            b.init(x, y, direction, BULLET_SPEED, DAMAGE);
            activeBullets.add(b);
            lastShotTime = TimeUtils.nanoTime();
        }
    }
}