package com.labubushooter.frontend.patterns;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Static background rendering strategy with configurable scaling and alignment.
 * Anchors background to world origin, creating natural scrolling effect.
 * Supports multiple scaling modes and vertical alignment strategies.
 * 
 * SOLID Principles Applied:
 * - Single Responsibility: Handles static background positioning and scaling
 * - Open/Closed: Extensible via ScalingMode and VerticalAlignment enums
 * - Dependency Inversion: Depends on enum abstractions
 * 
 * Algorithm:
 * - Background left edge anchored at world x=0
 * - Scale applied based on ScalingMode (NONE, FIT_HEIGHT, FIT_WIDTH, FILL)
 * - Vertical position determined by VerticalAlignment (BOTTOM, CENTER, TOP)
 * - Viewport acts as a "window" sliding across the background
 */
public class StaticBackgroundStrategy implements IBackgroundRenderStrategy {
    private ScalingMode scalingMode;
    private VerticalAlignment verticalAlignment;

    /**
     * Constructs strategy with specified scaling mode and vertical alignment.
     * 
     * @param scalingMode Scaling strategy (NONE, FIT_HEIGHT, FIT_WIDTH, FILL)
     * @param verticalAlignment Vertical positioning (BOTTOM, CENTER, TOP)
     */
    public StaticBackgroundStrategy(ScalingMode scalingMode, VerticalAlignment verticalAlignment) {
        this.scalingMode = scalingMode;
        this.verticalAlignment = verticalAlignment;
    }

    /**
     * Constructs strategy with FIT_HEIGHT scaling and CENTER alignment.
     * Ensures full vertical content is visible and centered in viewport.
     */
    public StaticBackgroundStrategy() {
        this(ScalingMode.FIT_HEIGHT, VerticalAlignment.CENTER);
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
        
        // Calculate vertical position based on alignment strategy
        float bgY = calculateVerticalPosition(camera, bgHeight, viewportHeight);

        // Draw background with calculated dimensions
        // Camera's viewport naturally shows only the visible portion
        // As camera moves, different parts become visible (horizontal scrolling)
        batch.draw(backgroundTexture, bgX, bgY, bgWidth, bgHeight);
    }

    /**
     * Calculates vertical position based on alignment strategy.
     * Follows Strategy Pattern - behavior determined by VerticalAlignment enum.
     * 
     * @param camera Camera for position reference
     * @param bgHeight Scaled background height
     * @param viewportHeight Viewport height
     * @return Y position for background rendering
     */
    private float calculateVerticalPosition(OrthographicCamera camera, float bgHeight, float viewportHeight) {
        float cameraBottom = camera.position.y - (viewportHeight / 2);
        
        switch (verticalAlignment) {
            case BOTTOM:
                // Align to bottom of viewport - shows ground/horizon
                return cameraBottom;
                
            case CENTER:
                // Center background in viewport - ensures full height visible
                return cameraBottom - (bgHeight - viewportHeight) / 2;
                
            case TOP:
                // Align to top of viewport - shows sky
                return cameraBottom - (bgHeight - viewportHeight);
                
            default:
                return cameraBottom;
        }
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
     * Updates vertical alignment at runtime.
     * 
     * @param verticalAlignment New vertical alignment
     */
    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }
    
    /**
     * Gets current scaling mode.
     * 
     * @return Current scaling mode
     */
    public ScalingMode getScalingMode() {
        return scalingMode;
    }
    
    /**
     * Gets current vertical alignment.
     * 
     * @return Current vertical alignment
     */
    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }
}
