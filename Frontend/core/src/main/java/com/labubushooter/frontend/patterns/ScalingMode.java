package com.labubushooter.frontend.patterns;

/**
 * Defines scaling strategies for background rendering.
 * 
 * SOLID Principles Applied:
 * - Open/Closed Principle: New scaling modes can be added without modifying existing code
 * - Single Responsibility: Each mode represents one scaling strategy
 * 
 * Used by background rendering strategies to determine how to scale backgrounds
 * when dimensions differ from viewport dimensions.
 */
public enum ScalingMode {
    /**
     * No scaling - render at native resolution (5400×1080px).
     * Viewport acts as sliding window across background.
     * Use when you want pixel-perfect rendering without any image manipulation.
     */
    NONE,
    
    /**
     * Fit to viewport height - scale background so entire height is visible.
     * Width scales proportionally (may be cropped or insufficient for wide levels).
     * 
     * Example: 5400×1080px background → 3000×600px (for 600px viewport height)
     * Scale factor = viewport height / background height = 600/1080 = 0.556
     * 
     * Best for: Ensuring entire vertical content is visible (platformers with important sky/ground)
     */
    FIT_HEIGHT,
    
    /**
     * Fit to viewport width - scale background so entire width is visible.
     * Height scales proportionally (may be cropped or insufficient).
     * 
     * Best for: Wide panoramic backgrounds
     */
    FIT_WIDTH,
    
    /**
     * Fill viewport - scale background to cover entire viewport.
     * Maintains aspect ratio, crops overflow on one dimension.
     * 
     * Best for: Ensuring no gaps while maintaining aspect ratio
     */
    FILL
}
