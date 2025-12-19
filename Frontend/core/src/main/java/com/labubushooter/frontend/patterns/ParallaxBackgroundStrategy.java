package com.labubushooter.frontend.patterns;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Parallax scrolling background rendering strategy.
 * Creates depth effect by moving background slower than foreground.
 * Renders at native resolution (5400x1080px) - NO SCALING or CROPPING.
 */
public class ParallaxBackgroundStrategy implements IBackgroundRenderStrategy {
    private final float parallaxFactor;

    /**
     * @param parallaxFactor How much the background moves relative to camera
     *                       (0.0-1.0)
     *                       0.0 = background doesn't move (infinite distance)
     *                       0.5 = background moves half speed (default for depth)
     *                       1.0 = background moves same speed as camera (no
     *                       parallax)
     */
    public ParallaxBackgroundStrategy(float parallaxFactor) {
        this.parallaxFactor = parallaxFactor;
    }

    @Override
    public void render(SpriteBatch batch, Texture backgroundTexture, OrthographicCamera camera,
            float viewportWidth, float viewportHeight, float levelWidth) {
        if (backgroundTexture == null)
            return;

        // Get texture dimensions (5400 x 1080)
        float bgWidth = backgroundTexture.getWidth();
        float bgHeight = backgroundTexture.getHeight();

        // Calculate parallax offset based on camera position
        // Background moves slower than camera to create depth illusion
        float parallaxOffsetX = camera.position.x * parallaxFactor;

        // Position background with parallax effect
        // Center the background horizontally around the parallax position
        float bgX = parallaxOffsetX - (bgWidth / 2);
        
        // Calculate vertical position (bottom-aligned)
        float bgY = camera.position.y - (viewportHeight / 2);

        // Draw background at native resolution (5400x1080) - NO SCALING/CROPPING
        batch.draw(backgroundTexture, bgX, bgY, bgWidth, bgHeight);
    }
}
