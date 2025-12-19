package com.labubushooter.frontend.patterns;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Static background rendering strategy with configurable scaling.
 * Anchors background to world origin, creating natural scrolling effect.
 * Supports multiple scaling modes via ScalingMode enum.
 * 
 * SOLID Principles Applied:
 * - Single Responsibility: Handles static background positioning and scaling
 * - Open/Closed: Scaling modes can be extended via ScalingMode enum without modifying this class
 * - Dependency Inversion: Depends on ScalingMode abstraction
 * 
 * Algorithm:
 * - Background left edge anchored at world x=0
 * - Scale applied based on ScalingMode (NONE, FIT_HEIGHT, FIT_WIDTH, FILL)
 * - Viewport acts as a "window" sliding across the background
 */
public class StaticBackgroundStrategy implements IBackgroundRenderStrategy {
    private ScalingMode scalingMode;

    /**
     * Constructs strategy with specified scaling mode.
     * 
     * @param scalingMode Scaling strategy (NONE, FIT_HEIGHT, FIT_WIDTH, FILL)
     */
    public StaticBackgroundStrategy(ScalingMode scalingMode) {
        this.scalingMode = scalingMode;
    }

    /**
     * Constructs strategy with NO SCALING (default/legacy behavior).
     * Background renders at native 5400×1080px resolution.
     */
    public StaticBackgroundStrategy() {
        this(ScalingMode.NONE);
    }

    @Override
    public void render(SpriteBatch batch, Texture backgroundTexture, OrthographicCamera camera,
            float viewportWidth, float viewportHeight, float levelWidth) {
        if (backgroundTexture == null)
            return;

        // Get native texture dimensions (5400 × 1080)
        float nativeWidth = backgroundTexture.getWidth();
        float nativeHeight = backgroundTexture.getHeight();

        // Calculate scale factor based on scaling mode
        float scaleFactor = calculateScaleFactor(nativeWidth, nativeHeight, viewportWidth, viewportHeight);

        // Apply scaling to dimensions
        float bgWidth = nativeWidth * scaleFactor;
        float bgHeight = nativeHeight * scaleFactor;

        // Anchor background to world origin (x=0) - creates natural scrolling
        // The leftmost part of background.png aligns with the leftmost part of level
        float bgX = 0;
        
        // Calculate vertical position (bottom-aligned to show ground/horizon)
        // With FIT_HEIGHT scaling, bgHeight equals viewportHeight, so this always shows full height
        float bgY = camera.position.y - (viewportHeight / 2);

        // Draw background with calculated dimensions
        // Camera's viewport naturally shows only the visible portion
        // As camera moves, different parts become visible (horizontal scrolling)
        batch.draw(backgroundTexture, bgX, bgY, bgWidth, bgHeight);
    }

    /**
     * Calculates scale factor based on scaling mode.
     * Follows Open/Closed Principle - new modes added via enum without modifying this method.
     * 
     * @param nativeWidth    Native texture width (5400px)
     * @param nativeHeight   Native texture height (1080px)
     * @param viewportWidth  Viewport width (typically 1066px)
     * @param viewportHeight Viewport height (600px)
     * @return Scale factor (1.0 = no scaling, 0.556 for FIT_HEIGHT with 1080→600)
     */
    private float calculateScaleFactor(float nativeWidth, float nativeHeight,
            float viewportWidth, float viewportHeight) {
        switch (scalingMode) {
            case FIT_HEIGHT:
                // Scale so entire height fits in viewport
                // Example: 1080px → 600px = 0.556 scale factor
                // Result: 5400×1080 → 3000×600
                return viewportHeight / nativeHeight;

            case FIT_WIDTH:
                // Scale so entire width fits in viewport
                return viewportWidth / nativeWidth;

            case FILL:
                // Scale to fill viewport completely (larger scale to avoid gaps)
                // Uses max of width/height scales to ensure no empty space
                float scaleX = viewportWidth / nativeWidth;
                float scaleY = viewportHeight / nativeHeight;
                return Math.max(scaleX, scaleY);

            case NONE:
            default:
                // No scaling - render at native resolution (5400×1080)
                return 1.0f;
        }
    }
    
    /**
     * Updates scaling mode at runtime.
     * Allows dynamic switching between scaling strategies.
     * 
     * @param scalingMode New scaling mode
     */
    public void setScalingMode(ScalingMode scalingMode) {
        this.scalingMode = scalingMode;
    }
    
    /**
     * Gets current scaling mode.
     * 
     * @return Current scaling mode
     */
    public ScalingMode getScalingMode() {
        return scalingMode;
    }
}
