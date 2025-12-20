package com.labubushooter.frontend.patterns.states;

import com.badlogic.gdx.Gdx;
import com.labubushooter.frontend.objects.CommonEnemy;

/**
 * Stunned state for enemies.
 * Enemy is temporarily incapacitated after taking damage.
 * 
 * State Pattern Implementation:
 * - Triggered when enemy takes significant damage
 * - Enemy cannot move or attack while stunned
 * - Automatically transitions back to ChaseState after duration
 */
public class StunnedState implements EnemyState {
    
    private static final float STUN_DURATION = 0.5f;
    
    private float stunTimer = 0f;
    
    @Override
    public void enter(CommonEnemy enemy) {
        stunTimer = 0f;
        //Gdx.app.log("EnemyState", "Enemy entered STUNNED state");
    }
    
    @Override
    public void update(CommonEnemy enemy, float delta) {
        stunTimer += delta;
        
        // Visual feedback - enemy flashes or shakes
        // (Can be implemented in enemy's draw method)
        
        if (stunTimer >= STUN_DURATION) {
            // Recover from stun
            enemy.setState(new ChaseState());
        }
    }
    
    @Override
    public void exit(CommonEnemy enemy) {
        Gdx.app.log("EnemyState", "Enemy recovered from STUNNED state");
    }
    
    @Override
    public String getName() {
        return "STUNNED";
    }
}
