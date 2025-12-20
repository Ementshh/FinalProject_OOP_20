package com.labubushooter.frontend.patterns.states;

import com.badlogic.gdx.Gdx;
import com.labubushooter.frontend.objects.CommonEnemy;

/**
 * Attack state for enemies.
 * Enemy deals damage to player on contact.
 * 
 * State Pattern Implementation:
 * - Combat state when in melee range
 * - Deals contact damage to player
 * - Transitions to ChaseState when player moves away
 */
public class AttackState implements EnemyState {
    
    private static final float ATTACK_COOLDOWN = 1.0f;
    private static final float CHASE_DISTANCE = 100f;
    
    private float attackTimer = 0f;
    
    @Override
    public void enter(CommonEnemy enemy) {
        attackTimer = 0f;
        //Gdx.app.log("EnemyState", "Enemy entered ATTACK state");
    }
    
    @Override
    public void update(CommonEnemy enemy, float delta) {
        if (enemy.target == null) {
            enemy.setState(new IdleState());
            return;
        }
        
        float distanceToPlayer = Math.abs(enemy.bounds.x - enemy.target.bounds.x);
        
        // Check if player moved away
        if (distanceToPlayer > CHASE_DISTANCE) {
            enemy.setState(new ChaseState());
            return;
        }
        
        // Attack on cooldown
        attackTimer += delta;
        if (attackTimer >= ATTACK_COOLDOWN) {
            // Deal contact damage (handled by collision system)
            enemy.performAttack();
            attackTimer = 0f;
        }
    }
    
    @Override
    public void exit(CommonEnemy enemy) {
        Gdx.app.log("EnemyState", "Enemy exiting ATTACK state");
    }
    
    @Override
    public String getName() {
        return "ATTACK";
    }
}
