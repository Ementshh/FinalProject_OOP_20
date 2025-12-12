package com.labubushooter.frontend.events;

import com.badlogic.gdx.utils.TimeUtils;

/**
 * Base class for all collision events in the game.
 * Contains common properties like timestamp and the colliding entities.
 */
public abstract class CollisionEvent {
    private final long timestamp;

    public CollisionEvent() {
        this.timestamp = TimeUtils.nanoTime();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
