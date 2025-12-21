package com.labubushooter.frontend.animation;

import com.badlogic.gdx.math.Rectangle;

/**
 * Utility class for aligning sprites to physics colliders.
 * Ensures consistent foot placement across different sprite sizes.
 * 
 * Design Pattern: Utility/Helper Class
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles sprite alignment calculations
 */
public final class SpriteAligner {
    
    // Private constructor to prevent instantiation
    private SpriteAligner() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Alignment mode for horizontal positioning.
     */
    public enum HorizontalAlign {
        LEFT,
        CENTER,
        RIGHT
    }
    
    /**
     * Alignment mode for vertical positioning.
     */
    public enum VerticalAlign {
        BOTTOM,  // Feet aligned to collider bottom
        CENTER,
        TOP
    }
    
    /**
     * Calculates the X position for drawing a sprite centered on a collider.
     * 
     * @param collider The physics collider/bounding box
     * @param spriteWidth Width of the sprite to draw
     * @param align Horizontal alignment mode
     * @return X coordinate for drawing
     */
    public static float getAlignedX(Rectangle collider, float spriteWidth, HorizontalAlign align) {
        switch (align) {
            case LEFT:
                return collider.x;
            case RIGHT:
                return collider.x + collider.width - spriteWidth;
            case CENTER:
            default:
                return collider.x + (collider.width - spriteWidth) / 2f;
        }
    }
    
    /**
     * Calculates the Y position for drawing a sprite with feet at collider bottom.
     * 
     * @param collider The physics collider/bounding box
     * @param spriteHeight Height of the sprite to draw
     * @param align Vertical alignment mode
     * @return Y coordinate for drawing
     */
    public static float getAlignedY(Rectangle collider, float spriteHeight, VerticalAlign align) {
        switch (align) {
            case TOP:
                return collider.y + collider.height - spriteHeight;
            case CENTER:
                return collider.y + (collider.height - spriteHeight) / 2f;
            case BOTTOM:
            default:
                // Feet at bottom - most common for platformers
                return collider.y;
        }
    }
    
    /**
     * Calculates draw position with feet alignment and horizontal centering.
     * This is the most common use case for platformer characters.
     * 
     * @param collider The physics collider
     * @param spriteWidth Current sprite width
     * @param spriteHeight Current sprite height
     * @return float array [x, y] for draw position
     */
    public static float[] getFootAlignedPosition(Rectangle collider, 
                                                  float spriteWidth, 
                                                  float spriteHeight) {
        float x = getAlignedX(collider, spriteWidth, HorizontalAlign.CENTER);
        float y = getAlignedY(collider, spriteHeight, VerticalAlign.BOTTOM);
        return new float[] { x, y };
    }
    
    /**
     * Calculates draw position with custom padding/offset.
     * Useful for sprites with transparent borders.
     * 
     * @param collider The physics collider
     * @param spriteWidth Current sprite width
     * @param spriteHeight Current sprite height
     * @param offsetX Horizontal offset (positive = right)
     * @param offsetY Vertical offset (positive = up)
     * @return float array [x, y] for draw position
     */
    public static float[] getAlignedPositionWithOffset(Rectangle collider,
                                                        float spriteWidth,
                                                        float spriteHeight,
                                                        float offsetX,
                                                        float offsetY) {
        float[] basePos = getFootAlignedPosition(collider, spriteWidth, spriteHeight);
        return new float[] { basePos[0] + offsetX, basePos[1] + offsetY };
    }
    
    /**
     * Calculates scaled sprite dimensions that fit within the collider
     * while maintaining aspect ratio.
     * 
     * @param collider The physics collider
     * @param textureWidth Original texture width
     * @param textureHeight Original texture height
     * @param scale Scale factor (1.0 = fit to collider)
     * @return float array [width, height] for scaled dimensions
     */
    public static float[] getScaledDimensions(Rectangle collider,
                                               float textureWidth,
                                               float textureHeight,
                                               float scale) {
        float textureAspect = textureWidth / textureHeight;
        float colliderAspect = collider.width / collider.height;
        
        float drawWidth, drawHeight;
        
        if (textureAspect > colliderAspect) {
            // Texture is wider - fit to height
            drawHeight = collider.height * scale;
            drawWidth = drawHeight * textureAspect;
        } else {
            // Texture is taller - fit to width
            drawWidth = collider.width * scale;
            drawHeight = drawWidth / textureAspect;
        }
        
        return new float[] { drawWidth, drawHeight };
    }
}
