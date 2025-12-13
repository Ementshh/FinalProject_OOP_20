package com.labubushooter.frontend.collision;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.labubushooter.frontend.events.BulletEnemyCollisionEvent;
import com.labubushooter.frontend.events.CollisionEvent;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.objects.BossEnemy;
import com.labubushooter.frontend.objects.CommonEnemy;

/**
 * Handles collisions between player bullets and enemies.
 * Applies damage to enemies and manages bullet pooling.
 */
public class BulletEnemyCollisionHandler implements CollisionHandler {
    private final Pool<Bullet> bulletPool;
    private final Array<Bullet> activeBullets;

    public BulletEnemyCollisionHandler(Pool<Bullet> bulletPool, Array<Bullet> activeBullets) {
        this.bulletPool = bulletPool;
        this.activeBullets = activeBullets;
    }

    @Override
    public void onCollision(CollisionEvent event) {
        if (event instanceof BulletEnemyCollisionEvent) {
            handle(event);
        }
    }

    @Override
    public void handle(CollisionEvent event) {
        BulletEnemyCollisionEvent collisionEvent = (BulletEnemyCollisionEvent) event;
        Bullet bullet = collisionEvent.getBullet();
        
        if (!bullet.active) return; // Bullet already processed
        
        // Apply damage to enemy
        if (collisionEvent.isBossEnemy()) {
            BossEnemy boss = collisionEvent.getBossEnemy();
            boss.takeDamage(bullet.damage);
            Gdx.app.log("Combat", "Bullet hit " + boss.getClass().getSimpleName() + 
                " for " + bullet.damage + " damage");
        } else {
            CommonEnemy enemy = collisionEvent.getCommonEnemy();
            enemy.takeDamage(bullet.damage);
            Gdx.app.log("Combat", "Bullet hit CommonEnemy for " + bullet.damage + " damage");
        }
        
        // Remove bullet
        activeBullets.removeValue(bullet, true);
        bulletPool.free(bullet);
    }

    @Override
    public boolean canHandle(Class<? extends CollisionEvent> eventType) {
        return eventType == BulletEnemyCollisionEvent.class;
    }
}
