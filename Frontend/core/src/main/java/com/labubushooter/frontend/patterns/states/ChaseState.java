package com.labubushooter.frontend.patterns.states;

import com.badlogic.gdx.Gdx;
import com.labubushooter.frontend.objects.CommonEnemy;

/**
 * Chase state for enemies.
 * Enemy actively pursues the player.
 * 
 * State Pattern Implementation:
 * - Active pursuit state
 * - Transitions to AttackState when close enough
 * - Transitions to IdleState when player is out of range
 */
public class ChaseState implements EnemyState {
    
    private static final float ATTACK_RANGE = 80f;
    private static final float LOSE_RANGE = 600f;
    
    @Override
    public void enter(CommonEnemy enemy) {
        //Gdx.app.log("EnemyState", "Enemy entered CHASE state");
    }
    
    @Override
    public void update(CommonEnemy enemy, float delta) {
        if (enemy.target == null) {
            enemy.setState(new IdleState());
            return;
        }
        
        float distanceToPlayer = Math.abs(enemy.bounds.x - enemy.target.bounds.x);
        
        // Check state transitions
        if (distanceToPlayer > LOSE_RANGE) {
            // Lost sight of player
            enemy.setState(new IdleState());
        } else if (distanceToPlayer < ATTACK_RANGE) {
            // Close enough to attack
            enemy.setState(new AttackState());
        } else {
            // Continue chasing - movement handled by enemy's moveTowardsPlayer()
            enemy.moveTowardsPlayer(delta);
        }
    }
    
    @Override
    public void exit(CommonEnemy enemy) {
        Gdx.app.log("EnemyState", "Enemy exiting CHASE state");
    }
    
    @Override
    public String getName() {
        return "CHASE";
    }
}
