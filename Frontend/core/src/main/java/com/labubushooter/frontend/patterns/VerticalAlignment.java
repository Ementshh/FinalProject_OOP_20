package com.labubushooter.frontend.patterns;

/**
 * Enum defining vertical alignment strategies for background rendering.
 * 
 * SOLID Principles Applied:
 * - Open/Closed Principle: New alignment types can be added without modifying existing code
 * - Single Responsibility: Each enum value represents one vertical positioning strategy
 * 
 * Used by background rendering strategies to determine how to position the background
 * vertically when the background height differs from the viewport height.
 * 
 * For a 5400×1080px background with 600px viewport height:
 * - BOTTOM: Shows pixels 0-600 (ground/horizon - ideal for platformers)
 * - CENTER: Shows pixels 240-840 (middle slice)
 * - TOP: Shows pixels 480-1080 (sky portion)
 */
public enum VerticalAlignment {
    /**
     * Align background to bottom of viewport (bgY = 0).
     * Best for platformer games where ground is at y=0.
     * Shows the bottom portion of tall backgrounds.
     */
    BOTTOM,
    
    /**
     * Center background vertically in viewport.
     * Shows the middle portion of tall backgrounds.
     */
    CENTER,
    
    /**
     * Align background to top of viewport.
     * Shows the top portion of tall backgrounds.
     */
    TOP
}
