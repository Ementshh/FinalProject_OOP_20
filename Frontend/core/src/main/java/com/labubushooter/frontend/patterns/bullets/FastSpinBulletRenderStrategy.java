package com.labubushooter.frontend.patterns.bullets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * Rendering strategy for Big Attack boss bullets with fast continuous rotation.
 * 
 * Behavior:
 * - Rotates rapidly around center pivot point
 * - Rotation speed: 360 degrees per second (1 full rotation per second)
 * - Creates a threatening, high-damage visual effect
 * - Rotation accumulates over time and wraps at 360 degrees
 * 
 * Use Case: Boss Big Attack (large upward shot in all phases)
 */
public class FastSpinBulletRenderStrategy implements BulletRenderStrategy {
    
    private final Texture texture;
    private float currentRotation;
    
    // Rotation speed in degrees per second (1 full rotation per second)
    private static final float ROTATION_SPEED = 360f;
    
    /**
     * Creates a fast spin bullet render strategy.
     * 
     * @param texture The bullet texture to render
     */
    public FastSpinBulletRenderStrategy(Texture texture) {
        this.texture = texture;
        this.currentRotation = 0f;
    }
    
    @Override
    public void render(SpriteBatch batch, Rectangle bounds, float delta) {
        // Update rotation based on elapsed time (faster than slow spin)
        currentRotation += ROTATION_SPEED * delta;
        
        // Wrap angle to 0-360 range to prevent overflow
        if (currentRotation >= 360f) {
            currentRotation -= 360f;
        }
        
        // Render with rotation around center point
        batch.draw(
            texture,
            bounds.x, bounds.y,                      // position
            bounds.width / 2, bounds.height / 2,     // origin (center of sprite)
            bounds.width, bounds.height,             // size
            1, 1,                                    // scale (no scaling)
            currentRotation,                         // rotation in degrees
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
        // Reset rotation to initial state
        currentRotation = 0f;
    }
}
