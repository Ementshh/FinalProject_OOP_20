package com.labubushooter.finpro.patterns;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.labubushooter.finpro.objects.Bullet;

public interface ShootingStrategy {
    void shoot(float x, float y, boolean facingRight, Array<Bullet> activeBullets, Pool<Bullet> bulletPool);
}
