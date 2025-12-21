package com.labubushooter.frontend.patterns.bullets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.labubushooter.frontend.services.AssetManager;

/**
 * Factory for creating bullet rendering strategies based on bullet type.
 * 
 * Design Pattern: Factory Pattern + Singleton
 * - Encapsulates strategy creation logic
 * - Provides centralized access to rendering strategies
 * - Maps bullet types to appropriate strategy implementations
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles strategy creation
 * - Open/Closed: New bullet types can be added by extending the enum and switch
 * - Dependency Inversion: Returns interface type, not concrete implementations
 */
public class BulletRenderStrategyFactory {
    
    private static BulletRenderStrategyFactory instance;
    private final AssetManager assetManager;
    
    /**
     * Enum defining all available bullet types.
     * Each type maps to a specific rendering strategy.
     */
    public enum BulletType {
        /** Phase 1 single shot - rotates to match trajectory direction */
        PHASE1_SINGLE,
        
        /** Phase 2/3 multi-shot - slow continuous rotation */
        PHASE23_MULTI,
        
        /** Big Attack - fast continuous rotation */
        BIG_ATTACK
    }
    
    private BulletRenderStrategyFactory() {
        this.assetManager = AssetManager.getInstance();
    }
    
    /**
     * Gets the singleton factory instance.
     * 
     * @return The factory instance
     */
    public static synchronized BulletRenderStrategyFactory getInstance() {
        if (instance == null) {
            instance = new BulletRenderStrategyFactory();
        }
        return instance;
    }
    
    /**
     * Creates a rendering strategy for the specified bullet type.
     * 
     * @param type The bullet type
     * @param velocityX Horizontal velocity (used for directional bullets)
     * @param velocityY Vertical velocity (used for directional bullets)
     * @return A rendering strategy, or fallback strategy if texture loading fails
     */
    public BulletRenderStrategy createStrategy(BulletType type, float velocityX, float velocityY) {
        switch (type) {
            case PHASE1_SINGLE:
                Texture phase1Tex = assetManager.getTexture(AssetManager.BOSS_PHASE1_BULLET);
                if (phase1Tex != null) {
                    return new DirectionalBulletRenderStrategy(phase1Tex, velocityX, velocityY);
                } else {
                    Gdx.app.error("BulletRenderStrategyFactory", 
                        "Failed to load BOSS_PHASE1_BULLET texture, using fallback");
                    return createFallbackStrategy();
                }
                
            case PHASE23_MULTI:
                Texture phase23Tex = assetManager.getTexture(AssetManager.BOSS_PHASE23_BULLET);
                if (phase23Tex != null) {
                    return new SlowSpinBulletRenderStrategy(phase23Tex);
                } else {
                    Gdx.app.error("BulletRenderStrategyFactory", 
                        "Failed to load BOSS_PHASE23_BULLET texture, using fallback");
                    return createFallbackStrategy();
                }
                
            case BIG_ATTACK:
                Texture bigTex = assetManager.getTexture(AssetManager.BOSS_BIG_BULLET);
                if (bigTex != null) {
                    return new FastSpinBulletRenderStrategy(bigTex);
                } else {
                    Gdx.app.error("BulletRenderStrategyFactory", 
                        "Failed to load BOSS_BIG_BULLET texture, using fallback");
                    return createFallbackStrategy();
                }
                
            default:
                Gdx.app.error("BulletRenderStrategyFactory", 
                    "Unknown bullet type: " + type + ", using fallback");
                return createFallbackStrategy();
        }
    }
    
    /**
     * Creates a fallback strategy using the default enemy bullet texture.
     * Used when boss bullet textures fail to load.
     * 
     * @return A fallback rendering strategy
     */
    private BulletRenderStrategy createFallbackStrategy() {
        Texture defaultTex = assetManager.getTexture(AssetManager.ENEMY_BULLET);
        if (defaultTex != null) {
            return new SlowSpinBulletRenderStrategy(defaultTex);
        } else {
            // Last resort: return a no-op strategy
            Gdx.app.error("BulletRenderStrategyFactory", 
                "Even fallback texture failed to load!");
            return new BulletRenderStrategy() {
                @Override
                public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch, 
                                 com.badlogic.gdx.math.Rectangle bounds, float delta) {
                    // No-op: don't render anything
                }
                
                @Override
                public Texture getTexture() {
                    return null;
                }
                
                @Override
                public void reset() {
                    // No-op
                }
            };
        }
    }
}
