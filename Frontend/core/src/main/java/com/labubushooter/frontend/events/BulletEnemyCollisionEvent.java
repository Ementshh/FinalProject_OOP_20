package com.labubushooter.frontend.events;

import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.objects.CommonEnemy;
import com.labubushooter.frontend.objects.BossEnemy;

/**
 * Event fired when a player bullet collides with an enemy.
 * Can handle both CommonEnemy and BossEnemy types.
 */
public class BulletEnemyCollisionEvent extends CollisionEvent {
    private final Bullet bullet;
    private final Object enemy; // Can be CommonEnemy or BossEnemy
    private final boolean isBossEnemy;

    public BulletEnemyCollisionEvent(Bullet bullet, CommonEnemy enemy) {
        super();
        this.bullet = bullet;
        this.enemy = enemy;
        this.isBossEnemy = false;
    }

    public BulletEnemyCollisionEvent(Bullet bullet, BossEnemy enemy) {
        super();
        this.bullet = bullet;
        this.enemy = enemy;
        this.isBossEnemy = true;
    }

    public Bullet getBullet() {
        return bullet;
    }

    public Object getEnemy() {
        return enemy;
    }

    public boolean isBossEnemy() {
        return isBossEnemy;
    }

    public CommonEnemy getCommonEnemy() {
        return isBossEnemy ? null : (CommonEnemy) enemy;
    }

    public BossEnemy getBossEnemy() {
        return isBossEnemy ? (BossEnemy) enemy : null;
    }
}
