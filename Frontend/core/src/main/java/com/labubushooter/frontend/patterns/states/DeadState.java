package com.labubushooter.frontend.patterns.states;

import com.badlogic.gdx.Gdx;
import com.labubushooter.frontend.objects.CommonEnemy;

/**
 * Dead state for enemies.
 * Final state before enemy is returned to pool.
 * 
 * State Pattern Implementation:
 * - Terminal state when health reaches 0
 * - Immediately marks enemy for removal (no animation delay)
 * - Prevents issues with rapid fire weapons like Mac10
 */
public class DeadState implements EnemyState {
    
    @Override
    public void enter(CommonEnemy enemy) {
        // Immediately mark for removal when entering dead state
        enemy.markForRemoval();
        Gdx.app.log("EnemyState", "Enemy entered DEAD state - marked for removal");
    }
    
    @Override
    public void update(CommonEnemy enemy, float delta) {
        // No update logic needed - enemy is already marked for removal
        // The GameWorld will handle pool return on next update cycle
    }
    
    @Override
    public void exit(CommonEnemy enemy) {
        // This shouldn't be called normally as Dead is terminal state
        Gdx.app.log("EnemyState", "Enemy exiting DEAD state (unusual)");
    }
    
    @Override
    public String getName() {
        return "DEAD";
    }
}
