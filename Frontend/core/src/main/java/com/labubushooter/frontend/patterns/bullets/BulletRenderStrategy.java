package com.labubushooter.frontend.patterns.bullets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * Strategy interface for rendering different types of bullets with various visual effects.
 * 
 * Design Pattern: Strategy Pattern
 * - Encapsulates different bullet rendering algorithms
 * - Allows runtime selection of rendering behavior
 * - Enables adding new bullet types without modifying existing code
 * 
 * SOLID Principles:
 * - Single Responsibility: Each implementation handles one rendering style
 * - Open/Closed: New strategies can be added without modifying this interface
 * - Interface Segregation: Minimal, focused interface with only essential methods
 * - Dependency Inversion: Clients depend on this abstraction, not concrete implementations
 */
public interface BulletRenderStrategy {
    
    /**
     * Renders the bullet with appropriate visual effects.
     * 
     * @param batch SpriteBatch for rendering (must be between begin() and end())
     * @param bounds Bullet bounds containing position (x, y) and size (width, height)
     * @param delta Time elapsed since last frame in seconds (for rotation updates)
     */
    void render(SpriteBatch batch, Rectangle bounds, float delta);
    
    /**
     * Gets the texture used by this rendering strategy.
     * 
     * @return The bullet texture, or null if no texture is used
     */
    Texture getTexture();
    
    /**
     * Resets the strategy state to initial values.
     * Called when bullet is returned to pool or reinitialized.
     * Implementations should reset rotation angles, timers, etc.
     */
    void reset();
}
