package com.labubushooter.frontend.patterns.factories;

/**
 * Enum defining different enemy types.
 * Used by EnemyFactory to create appropriate enemy instances.
 * 
 * Design Pattern: Part of Factory Method Pattern
 * - Encapsulates enemy type information
 * - Allows for type-safe enemy creation
 */
public enum EnemyType {
    /** Basic common enemy with melee attack */
    COMMON(50f, 100f, 10f, "Common Enemy"),
    
    /** Fast enemy with lower health */
    FAST(30f, 180f, 8f, "Fast Enemy"),
    
    /** Tank enemy with high health but slow */
    TANK(100f, 60f, 15f, "Tank Enemy"),
    
    /** Mini boss enemy */
    MINI_BOSS(500f, 80f, 25f, "Mini Boss"),
    
    /** Final boss enemy */
    FINAL_BOSS(1000f, 50f, 50f, "Final Boss");
    
    private final float baseHealth;
    private final float baseSpeed;
    private final float baseDamage;
    private final String displayName;
    
    EnemyType(float baseHealth, float baseSpeed, float baseDamage, String displayName) {
        this.baseHealth = baseHealth;
        this.baseSpeed = baseSpeed;
        this.baseDamage = baseDamage;
        this.displayName = displayName;
    }
    
    public float getBaseHealth() {
        return baseHealth;
    }
    
    public float getBaseSpeed() {
        return baseSpeed;
    }
    
    public float getBaseDamage() {
        return baseDamage;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets scaled health based on level.
     * Health increases by 20% per level.
     * 
     * @param level Current game level
     * @return Scaled health value
     */
    public float getScaledHealth(int level) {
        return baseHealth * (1f + (level - 1) * 0.2f);
    }
    
    /**
     * Gets scaled speed based on level.
     * Speed increases by 10% per level.
     * 
     * @param level Current game level
     * @return Scaled speed value
     */
    public float getScaledSpeed(int level) {
        return baseSpeed * (1f + (level - 1) * 0.1f);
    }
    
    /**
     * Gets scaled damage based on level.
     * Damage increases by 15% per level.
     * 
     * @param level Current game level
     * @return Scaled damage value
     */
    public float getScaledDamage(int level) {
        return baseDamage * (1f + (level - 1) * 0.15f);
    }
}
