package com.labubushooter.frontend.services;

import com.labubushooter.frontend.patterns.IBackgroundRenderStrategy;
import com.labubushooter.frontend.patterns.StaticBackgroundStrategy;
import com.labubushooter.frontend.patterns.ScalingMode;
import com.labubushooter.frontend.patterns.VerticalAlignment;

/**
 * Resolves appropriate background rendering strategy based on level.
 * 
 * Design Patterns:
 * - Factory Pattern: Creates appropriate strategy instances
 * - Strategy Pattern: Returns different strategies for different levels
 * 
 * SOLID Principles:
 * - Single Responsibility: Only responsible for mapping level → rendering strategy
 * - Open/Closed: Can be extended for new levels without modifying existing code
 * - Dependency Inversion: Returns IBackgroundRenderStrategy abstraction
 */
public class BackgroundStrategyResolver {
    
    /**
     * Resolves the appropriate background rendering strategy for a given level.
     * 
     * Strategy Mapping:
     * - Level 1-4: FIT_HEIGHT + CENTER (for 5400x1080 ultra-wide backgrounds)
     * - Level 5: FILL + CENTER (for 1920x1080 16:9 background)
     * 
     * @param level The level number (1-5)
     * @return Appropriate IBackgroundRenderStrategy for the level
     */
    public static IBackgroundRenderStrategy getStrategy(int level) {
        if (level == 5) {
            // Level 5 uses 1920x1080 (16:9) background
            // FILL mode ensures full viewport coverage without gaps
            return new StaticBackgroundStrategy(ScalingMode.FILL, VerticalAlignment.CENTER);
        } else {
            // Levels 1-4 use 5400x1080 ultra-wide backgrounds
            // FIT_HEIGHT ensures full vertical content visible
            return new StaticBackgroundStrategy(ScalingMode.FIT_HEIGHT, VerticalAlignment.CENTER);
        }
    }
}
