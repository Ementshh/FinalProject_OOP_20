package com.labubushooter.frontend.services;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.labubushooter.frontend.patterns.IBackgroundRenderStrategy;

/**
 * BackgroundRenderer - Manages background rendering using Strategy Pattern.
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles background rendering
 * - Open/Closed: Can extend with new strategies without modifying this class
 * - Dependency Inversion: Depends on IBackgroundRenderStrategy abstraction
 * 
 * Design Patterns:
 * - Strategy Pattern: Delegates rendering to interchangeable strategies
 * - Facade: Simplifies background rendering interface for clients
 */
public class BackgroundRenderer {
    private IBackgroundRenderStrategy renderStrategy;
    private Texture backgroundTexture;

    /**
     * Constructs a BackgroundRenderer with specified texture and strategy.
     * 
     * @param backgroundTexture The background texture (5400x1080px)
     * @param renderStrategy    The rendering strategy to use (e.g., StaticBackgroundStrategy)
     */
    public BackgroundRenderer(Texture backgroundTexture, IBackgroundRenderStrategy renderStrategy) {
        this.backgroundTexture = backgroundTexture;
        this.renderStrategy = renderStrategy;
    }

    /**
     * Changes the rendering strategy at runtime.
     * Allows dynamic switching between static, parallax, tiled, etc.
     * 
     * @param strategy New rendering strategy
     */
    public void setRenderStrategy(IBackgroundRenderStrategy strategy) {
        this.renderStrategy = strategy;
    }

    /**
     * Updates the background texture.
     * Useful for level transitions or dynamic backgrounds.
     * 
     * @param texture New background texture
     */
    public void setBackgroundTexture(Texture texture) {
        this.backgroundTexture = texture;
    }

    /**
     * Renders the background using the current strategy.
     * 
     * @param batch          SpriteBatch for rendering
     * @param camera         Camera for positioning
     * @param viewportWidth  Viewport width
     * @param viewportHeight Viewport height
     * @param levelWidth     Current level width (for mapping background to world)
     */
    public void render(SpriteBatch batch, OrthographicCamera camera,
            float viewportWidth, float viewportHeight, float levelWidth) {
        if (renderStrategy != null) {
            renderStrategy.render(batch, backgroundTexture, camera, viewportWidth, viewportHeight, levelWidth);
        }
    }
}
