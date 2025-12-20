package com.labubushooter.frontend.patterns.states;

import com.labubushooter.frontend.objects.CommonEnemy;

/**
 * Stunned state for enemies.
 * Enemy is temporarily incapacitated after taking damage.
 * 
 * State Pattern Implementation:
 * - Triggered when enemy takes damage but survives
 * - Enemy cannot move or attack while stunned
 * - Automatically transitions back to ChaseState after duration
 * - Checks if enemy died while stunned (from rapid fire weapons)
 */
public class StunnedState implements EnemyState {
    
    private static final float STUN_DURATION = 0.3f; // Reduced for better gameplay
    
    private float stunTimer = 0f;
    
    @Override
    public void enter(CommonEnemy enemy) {
        stunTimer = 0f;
    }
    
    @Override
    public void update(CommonEnemy enemy, float delta) {
        // Check if enemy was killed while stunned (e.g., from continued fire)
        if (enemy.health <= 0 || !enemy.isActive()) {
            enemy.setState(new DeadState());
            return;
        }
        
        stunTimer += delta;
        
        if (stunTimer >= STUN_DURATION) {
            // Recover from stun - back to chasing
            enemy.setState(new ChaseState());
        }
    }
    
    @Override
    public void exit(CommonEnemy enemy) {
        // Recovery from stun
    }
    
    @Override
    public String getName() {
        return "STUNNED";
    }
}
