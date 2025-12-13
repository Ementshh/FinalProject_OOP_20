package com.labubushooter.frontend.events;

/**
 * Interface for handling collision events.
 * Similar to callback pattern used in PlayerApiService.
 */
public interface CollisionEventListener {
    /**
     * Called when a collision event occurs.
     * @param event The collision event that occurred
     */
    void onCollision(CollisionEvent event);

    /**
     * Checks if this listener can handle a specific event type.
     * @param eventType The class of the event type
     * @return true if this listener can handle the event type
     */
    boolean canHandle(Class<? extends CollisionEvent> eventType);
}
