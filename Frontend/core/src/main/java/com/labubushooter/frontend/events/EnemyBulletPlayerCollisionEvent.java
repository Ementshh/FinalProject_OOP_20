package com.labubushooter.frontend.events;

import com.labubushooter.frontend.objects.EnemyBullet;
import com.labubushooter.frontend.objects.Player;

/**
 * Event fired when an enemy bullet collides with the player.
 */
public class EnemyBulletPlayerCollisionEvent extends CollisionEvent {
    private final EnemyBullet bullet;
    private final Player player;

    public EnemyBulletPlayerCollisionEvent(EnemyBullet bullet, Player player) {
        super();
        this.bullet = bullet;
        this.player = player;
    }

    public EnemyBullet getBullet() {
        return bullet;
    }

    public Player getPlayer() {
        return player;
    }

    public float getDamage() {
        return bullet.damage;
    }
}
