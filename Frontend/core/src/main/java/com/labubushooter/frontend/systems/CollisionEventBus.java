package com.labubushooter.frontend.systems;

import com.badlogic.gdx.utils.Array;
import com.labubushooter.frontend.events.CollisionEvent;
import com.labubushooter.frontend.events.CollisionEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Event bus for managing collision event subscriptions and publications.
 * Uses Observer pattern similar to the callback pattern in PlayerApiService.
 */
public class CollisionEventBus {
    private final Map<Class<? extends CollisionEvent>, Array<CollisionEventListener>> listeners;

    public CollisionEventBus() {
        this.listeners = new HashMap<>();
    }

    /**
     * Subscribe a listener to a specific event type.
     * 
     * @param eventType The class of the event to listen for
     * @param listener  The listener to notify when the event occurs
     */
    public void subscribe(Class<? extends CollisionEvent> eventType, CollisionEventListener listener) {
        listeners.putIfAbsent(eventType, new Array<>());
        listeners.get(eventType).add(listener);
    }

    /**
     * Publish an event to all registered listeners.
     * 
     * @param event The collision event to publish
     */
    public void publish(CollisionEvent event) {
        Array<CollisionEventListener> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (CollisionEventListener listener : eventListeners) {
                listener.onCollision(event);
            }
        }
    }

    /**
     * Unsubscribe a listener from a specific event type.
     * 
     * @param eventType The class of the event
     * @param listener  The listener to remove
     */
    public void unsubscribe(Class<? extends CollisionEvent> eventType, CollisionEventListener listener) {
        Array<CollisionEventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.removeValue(listener, true);
        }
    }

    /**
     * Clear all listeners for a specific event type.
     * 
     * @param eventType The class of the event
     */
    public void clearListeners(Class<? extends CollisionEvent> eventType) {
        listeners.remove(eventType);
    }

    /**
     * Clear all listeners for all event types.
     */
    public void clearAllListeners() {
        listeners.clear();
    }
}
