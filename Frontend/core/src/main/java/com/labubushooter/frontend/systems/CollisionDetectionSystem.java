package com.labubushooter.frontend.systems;

import com.badlogic.gdx.utils.Array;
import com.labubushooter.frontend.events.*;
import com.labubushooter.frontend.objects.*;

/**
 * System responsible for detecting collisions between game objects.
 * Uses Rectangle.overlaps() for AABB collision detection.
 * Publishes collision events through the event bus instead of directly applying
 * effects.
 * 
 * This separates collision detection (what collided) from collision response
 * (what happens).
 */
public class CollisionDetectionSystem {
    private final CollisionEventBus eventBus;

    public CollisionDetectionSystem(CollisionEventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Detect all collisions for this frame and publish events.
     * Should be called once per frame during PLAYING state.
     */
    public void detectCollisions(
            Array<Bullet> activeBullets,
            Array<CommonEnemy> activeEnemies,
            Array<EnemyBullet> activeEnemyBullets,
            Array<Coin> activeCoins,
            Player player,
            MiniBossEnemy miniBoss,
            FinalBoss boss,
            int currentLevel) {
        // Player bullets vs enemies
        detectBulletEnemyCollisions(activeBullets, activeEnemies, miniBoss, boss, currentLevel);

        // Enemy bullets vs player
        detectEnemyBulletPlayerCollisions(activeEnemyBullets, player);

        // Player vs enemies (melee damage)
        detectPlayerEnemyCollisions(player, activeEnemies, miniBoss, boss, currentLevel);

        // Player vs coins
        detectCoinCollisions(activeCoins, player);
    }

    /**
     * Check collisions between player bullets and enemies.
     */
    private void detectBulletEnemyCollisions(
            Array<Bullet> activeBullets,
            Array<CommonEnemy> activeEnemies,
            MiniBossEnemy miniBoss,
            FinalBoss boss,
            int currentLevel) {
        for (int i = activeBullets.size - 1; i >= 0; i--) {
            Bullet bullet = activeBullets.get(i);
            if (!bullet.active)
                continue;

            // Check vs common enemies
            for (int j = activeEnemies.size - 1; j >= 0; j--) {
                CommonEnemy enemy = activeEnemies.get(j);
                if (enemy.spawned && enemy.collider.overlaps(bullet.bounds)) {
                    eventBus.publish(new BulletEnemyCollisionEvent(bullet, enemy));
                    break; // Bullet can only hit one enemy
                }
            }

            // Check vs mini-boss (level 3)
            if (currentLevel == 3 && miniBoss != null && miniBoss.active) {
                if (bullet.bounds.overlaps(miniBoss.collider)) {
                    eventBus.publish(new BulletEnemyCollisionEvent(bullet, miniBoss));
                }
            }

            // Check vs final boss (level 5)
            if (currentLevel == 5 && boss != null && boss.active) {
                if (bullet.bounds.overlaps(boss.collider)) {
                    eventBus.publish(new BulletEnemyCollisionEvent(bullet, boss));
                }
            }
        }
    }

    /**
     * Check collisions between enemy bullets and player.
     */
    private void detectEnemyBulletPlayerCollisions(Array<EnemyBullet> activeEnemyBullets, Player player) {
        for (int i = activeEnemyBullets.size - 1; i >= 0; i--) {
            EnemyBullet eb = activeEnemyBullets.get(i);
            if (eb.active && eb.bounds.overlaps(player.bounds)) {
                eventBus.publish(new EnemyBulletPlayerCollisionEvent(eb, player));
            }
        }
    }

    /**
     * Check collisions between player and enemies (melee damage).
     */
    private void detectPlayerEnemyCollisions(
            Player player,
            Array<CommonEnemy> activeEnemies,
            MiniBossEnemy miniBoss,
            FinalBoss boss,
            int currentLevel) {
        // Check vs common enemies
        for (CommonEnemy enemy : activeEnemies) {
            if (enemy.spawned && enemy.collider.overlaps(player.bounds)) {
                eventBus.publish(new PlayerEnemyCollisionEvent(player, enemy));
            }
        }

        // Check vs mini-boss (level 3)
        if (currentLevel == 3 && miniBoss != null && miniBoss.active) {
            if (miniBoss.collider.overlaps(player.bounds)) {
                eventBus.publish(new PlayerEnemyCollisionEvent(player, miniBoss));
            }
        }

        // Check vs final boss (level 5)
        if (currentLevel == 5 && boss != null && boss.active) {
            if (boss.collider.overlaps(player.bounds)) {
                eventBus.publish(new PlayerEnemyCollisionEvent(player, boss));
            }
        }
    }

    /**
     * Check collisions between player and coins.
     */
    private void detectCoinCollisions(Array<Coin> activeCoins, Player player) {
        for (int i = activeCoins.size - 1; i >= 0; i--) {
            Coin coin = activeCoins.get(i);
            if (coin.active && coin.isColliding(player.bounds)) {
                eventBus.publish(new PlayerCoinCollisionEvent(player, coin));
            }
        }
    }
}
