package com.labubushooter.frontend.services;

import com.badlogic.gdx.graphics.Texture;

/**
 * Resolves background texture keys based on level number.
 * 
 * Design Patterns:
 * - Strategy Pattern: Maps level numbers to appropriate background textures
 * - Factory Pattern: Provides texture resolution based on level input
 * 
 * SOLID Principles:
 * - Single Responsibility: Only responsible for mapping level → texture key
 * - Open/Closed: Can be extended for new levels without modifying existing code
 * - Dependency Inversion: Depends on AssetManager abstraction
 */
public class BackgroundTextureResolver {
    
    /**
     * Resolves the texture key for a given level number.
     * 
     * Mapping:
     * - Level 1 → BACKGROUND_LEVEL1
     * - Levels 2-4 → BACKGROUND_LEVEL2_TO_4
     * - Level 5 → BACKGROUND_LEVEL5
     * - Default/fallback → BACKGROUND_LEVEL1
     * 
     * @param level The level number (1-5)
     * @return The texture key string for the corresponding background
     */
    public static String resolveTextureKey(int level) {
        if (level == 1) {
            return AssetManager.BACKGROUND_LEVEL1;
        } else if (level >= 2 && level <= 4) {
            return AssetManager.BACKGROUND_LEVEL2_TO_4;
        } else if (level == 5) {
            return AssetManager.BACKGROUND_LEVEL5;
        }
        // Fallback to level 1 background for invalid levels
        return AssetManager.BACKGROUND_LEVEL1;
    }
    
    /**
     * Convenience method that directly returns the Texture for a given level.
     * 
     * @param level The level number (1-5)
     * @param assetManager The AssetManager instance to retrieve textures from
     * @return The Texture for the corresponding background, or null if not found
     */
    public static Texture getTexture(int level, AssetManager assetManager) {
        String key = resolveTextureKey(level);
        return assetManager.getTexture(key);
    }
}

