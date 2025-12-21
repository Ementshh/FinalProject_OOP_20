package com.labubushooter.frontend.patterns.bullets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * Rendering strategy for Phase 1 boss bullets that rotate to match their trajectory direction.
 * 
 * Behavior:
 * - Calculates rotation angle once during initialization based on velocity
 * - Renders bullet sprite rotated to face the direction of travel
 * - Rotation is static (doesn't change during flight)
 * 
 * Use Case: Boss Phase 1 single shot attack
 */
public class DirectionalBulletRenderStrategy implements BulletRenderStrategy {
    
    private final Texture texture;
    private float rotationAngle;
    
    /**
     * Creates a directional bullet render strategy.
     * 
     * @param texture The bullet texture to render
     * @param velocityX Horizontal velocity component
     * @param velocityY Vertical velocity component
     */
    public DirectionalBulletRenderStrategy(Texture texture, float velocityX, float velocityY) {
        this.texture = texture;
        // Calculate angle from velocity vector
        // atan2 returns radians, convert to degrees for LibGDX
        // atan2(y, x) gives angle from positive X-axis
        this.rotationAngle = (float) Math.toDegrees(Math.atan2(velocityY, velocityX));
    }
    
    @Override
    public void render(SpriteBatch batch, Rectangle bounds, float delta) {
        // Render with rotation around center point
        // Parameters: texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation,
        //             srcX, srcY, srcWidth, srcHeight, flipX, flipY
        batch.draw(
            texture,
            bounds.x, bounds.y,                      // position
            bounds.width / 2, bounds.height / 2,     // origin (center of sprite)
            bounds.width, bounds.height,             // size
            1, 1,                                    // scale (no scaling)
            rotationAngle,                           // rotation in degrees
            0, 0,                                    // source position in texture
            texture.getWidth(), texture.getHeight(), // source size (full texture)
            false, false                             // flip flags
        );
    }
    
    @Override
    public Texture getTexture() {
        return texture;
    }
    
    @Override
    public void reset() {
        // Rotation angle is set in constructor and doesn't change
        // No state to reset for this strategy
    }
}
