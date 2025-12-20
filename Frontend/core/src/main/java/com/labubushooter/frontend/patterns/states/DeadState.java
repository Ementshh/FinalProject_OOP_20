package com.labubushooter.frontend.patterns.states;

import com.badlogic.gdx.Gdx;
import com.labubushooter.frontend.objects.CommonEnemy;

/**
 * Dead state for enemies.
 * Final state before enemy is returned to pool.
 * 
 * State Pattern Implementation:
 * - Terminal state when health reaches 0
 * - Handles death animation/effects
 * - Marks enemy as inactive for pool return
 */
public class DeadState implements EnemyState {
    
    private static final float DEATH_ANIMATION_DURATION = 0.3f;
    
    private float deathTimer = 0f;
    
    @Override
    public void enter(CommonEnemy enemy) {
        deathTimer = 0f;
        //Gdx.app.log("EnemyState", "Enemy entered DEAD state");
    }
    
    @Override
    public void update(CommonEnemy enemy, float delta) {
        deathTimer += delta;
        
        // Death animation/fade out effect
        // (Can be implemented in enemy's draw method)
        
        if (deathTimer >= DEATH_ANIMATION_DURATION) {
            // Mark as inactive for pool return
            enemy.markForRemoval();
        }
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
