package com.labubushooter.frontend.events;

import com.labubushooter.frontend.objects.Player;
import com.labubushooter.frontend.objects.CommonEnemy;
import com.labubushooter.frontend.objects.BossEnemy;

/**
 * Event fired when player collides with an enemy (melee damage).
 * Can handle both CommonEnemy and BossEnemy types.
 */
public class PlayerEnemyCollisionEvent extends CollisionEvent {
    private final Player player;
    private final Object enemy; // Can be CommonEnemy or BossEnemy
    private final float damage;

    public PlayerEnemyCollisionEvent(Player player, CommonEnemy enemy) {
        super();
        this.player = player;
        this.enemy = enemy;
        this.damage = enemy.damageAmount;
    }

    public PlayerEnemyCollisionEvent(Player player, BossEnemy enemy) {
        super();
        this.player = player;
        this.enemy = enemy;
        this.damage = enemy.damage;
    }

    public Player getPlayer() {
        return player;
    }

    public Object getEnemy() {
        return enemy;
    }

    public float getDamage() {
        return damage;
    }

    public String getEnemyType() {
        if (enemy instanceof CommonEnemy) {
            return "CommonEnemy";
        } else if (enemy instanceof BossEnemy) {
            return enemy.getClass().getSimpleName();
        }
        return "Unknown";
    }
}
