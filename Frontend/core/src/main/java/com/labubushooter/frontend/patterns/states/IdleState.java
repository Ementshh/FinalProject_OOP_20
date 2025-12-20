package com.labubushooter.frontend.patterns.states;

import com.badlogic.gdx.Gdx;
import com.labubushooter.frontend.objects.CommonEnemy;

/**
 * Idle state for enemies.
 * Enemy waits and scans for player.
 * 
 * State Pattern Implementation:
 * - Default state when enemy spawns or loses sight of player
 * - Transitions to ChaseState when player is detected
 */
public class IdleState implements EnemyState {
    
    private static final float DETECTION_RANGE = 400f;
    private float idleTimer = 0f;
    
    @Override
    public void enter(CommonEnemy enemy) {
        idleTimer = 0f;
        //Gdx.app.log("EnemyState", "Enemy entered IDLE state");
    }
    
    @Override
    public void update(CommonEnemy enemy, float delta) {
        idleTimer += delta;
        
        // Check if player is within detection range
        if (enemy.target != null) {
            float distanceToPlayer = Math.abs(enemy.bounds.x - enemy.target.bounds.x);
            
            if (distanceToPlayer < DETECTION_RANGE) {
                // Transition to chase state
                enemy.setState(new ChaseState());
            }
        }
    }
    
    @Override
    public void exit(CommonEnemy enemy) {
        Gdx.app.log("EnemyState", "Enemy exiting IDLE state after " + idleTimer + "s");
    }
    
    @Override
    public String getName() {
        return "IDLE";
    }
}
