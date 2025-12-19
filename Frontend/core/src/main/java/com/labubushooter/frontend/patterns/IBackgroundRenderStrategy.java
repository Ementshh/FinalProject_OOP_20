package com.labubushooter.frontend.patterns;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Strategy Pattern interface for different background rendering approaches.
 * Follows Open/Closed Principle - open for extension, closed for modification.
 */
public interface IBackgroundRenderStrategy {
    /**
     * Renders the background using a specific strategy.
     * 
     * @param batch             SpriteBatch for rendering
     * @param backgroundTexture The background texture to render (5400x1080px)
     * @param camera            Camera for positioning calculations
     * @param viewportWidth     Viewport width for scaling calculations
     * @param viewportHeight    Viewport height for scaling calculations
     * @param levelWidth        The width of the current level (for mapping
     *                          background to world)
     */
    void render(SpriteBatch batch, Texture backgroundTexture, OrthographicCamera camera,
            float viewportWidth, float viewportHeight, float levelWidth);
}
