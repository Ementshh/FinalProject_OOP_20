package com.labubushooter.frontend.patterns.states;

import com.labubushooter.frontend.objects.CommonEnemy;

/**
 * Interface for Enemy State Pattern.
 * Defines behavior for different enemy states.
 * 
 * Design Pattern: State Pattern
 * - Allows enemy to change behavior based on internal state
 * - Encapsulates state-specific behavior in separate classes
 * - Enables clean state transitions without complex conditionals
 * 
 * SOLID Principles:
 * - Single Responsibility: Each state handles one behavior
 * - Open/Closed: New states can be added without modifying existing code
 * - Liskov Substitution: All states are interchangeable
 */
public interface EnemyState {
    
    /**
     * Called when entering this state.
     * Initialize state-specific variables.
     * 
     * @param enemy The enemy entering this state
     */
    void enter(CommonEnemy enemy);
    
    /**
     * Called every frame while in this state.
     * Update enemy behavior based on current state.
     * 
     * @param enemy The enemy in this state
     * @param delta Time since last frame
     */
    void update(CommonEnemy enemy, float delta);
    
    /**
     * Called when exiting this state.
     * Clean up state-specific resources.
     * 
     * @param enemy The enemy leaving this state
     */
    void exit(CommonEnemy enemy);
    
    /**
     * Gets the name of this state for debugging.
     * 
     * @return State name
     */
    String getName();
}
