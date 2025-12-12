package com.labubushooter.frontend.collision;

import com.labubushooter.frontend.events.CollisionEvent;
import com.labubushooter.frontend.events.CollisionEventListener;

/**
 * Base interface for collision response handlers.
 * Handlers implement the Strategy pattern for different collision response
 * behaviors.
 */
public interface CollisionHandler extends CollisionEventListener {
    /**
     * Handle the collision event and apply appropriate responses.
     * 
     * @param event The collision event to handle
     */
    void handle(CollisionEvent event);
}
