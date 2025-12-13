package com.labubushooter.frontend.collision;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import com.labubushooter.frontend.events.CollisionEvent;
import com.labubushooter.frontend.events.PlayerEnemyCollisionEvent;

/**
 * Handles collisions between player and enemies (melee damage).
 * Centralizes cooldown logic that was previously scattered across enemy
 * classes.
 */
public class PlayerEnemyCollisionHandler implements CollisionHandler {
    private static final long DAMAGE_COOLDOWN = 1000000000L; // 1 second in nanoseconds
    private long lastDamageTime = 0;

    @Override
    public void onCollision(CollisionEvent event) {
        if (event instanceof PlayerEnemyCollisionEvent) {
            handle(event);
        }
    }

    @Override
    public void handle(CollisionEvent event) {
        PlayerEnemyCollisionEvent collisionEvent = (PlayerEnemyCollisionEvent) event;

        long currentTime = TimeUtils.nanoTime();
        if (currentTime - lastDamageTime > DAMAGE_COOLDOWN) {
            collisionEvent.getPlayer().takeDamage(collisionEvent.getDamage());
            lastDamageTime = currentTime;

            Gdx.app.log(collisionEvent.getEnemyType(),
                    "Hit player for " + collisionEvent.getDamage() + " damage");
        }
    }

    @Override
    public boolean canHandle(Class<? extends CollisionEvent> eventType) {
        return eventType == PlayerEnemyCollisionEvent.class;
    }
}
