package com.labubushooter.frontend.collision;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.labubushooter.frontend.events.CollisionEvent;
import com.labubushooter.frontend.events.EnemyBulletPlayerCollisionEvent;
import com.labubushooter.frontend.objects.EnemyBullet;

/**
 * Handles collisions between enemy bullets and the player.
 * Applies damage to player and manages enemy bullet pooling.
 */
public class EnemyBulletPlayerCollisionHandler implements CollisionHandler {
    private final Pool<EnemyBullet> enemyBulletPool;
    private final Array<EnemyBullet> activeEnemyBullets;

    public EnemyBulletPlayerCollisionHandler(Pool<EnemyBullet> enemyBulletPool,
            Array<EnemyBullet> activeEnemyBullets) {
        this.enemyBulletPool = enemyBulletPool;
        this.activeEnemyBullets = activeEnemyBullets;
    }

    @Override
    public void onCollision(CollisionEvent event) {
        if (event instanceof EnemyBulletPlayerCollisionEvent) {
            handle(event);
        }
    }

    @Override
    public void handle(CollisionEvent event) {
        EnemyBulletPlayerCollisionEvent collisionEvent = (EnemyBulletPlayerCollisionEvent) event;
        EnemyBullet bullet = collisionEvent.getBullet();

        if (!bullet.active)
            return; // Bullet already processed

        // Apply damage to player
        collisionEvent.getPlayer().takeDamage(collisionEvent.getDamage());
        Gdx.app.log("Combat", "Player hit by enemy bullet for " + collisionEvent.getDamage() + " damage");

        // Remove enemy bullet
        activeEnemyBullets.removeValue(bullet, true);
        enemyBulletPool.free(bullet);
    }

    @Override
    public boolean canHandle(Class<? extends CollisionEvent> eventType) {
        return eventType == EnemyBulletPlayerCollisionEvent.class;
    }
}
