package com.labubushooter.frontend.services;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;

/**
 * Centralized asset management with lazy loading and automatic disposal.
 * 
 * Design Patterns:
 * - Singleton: Single point of asset access across the application
 * - Lazy Loading: Assets loaded on first request or explicit initialization
 * - Object Pool/Flyweight: Reuses loaded assets instead of creating duplicates
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles asset lifecycle (load, cache, dispose)
 * - Dependency Inversion: Clients depend on AssetManager abstraction
 * - Open/Closed: New asset types can be added without modifying existing code
 */
public class AssetManager implements Disposable {
    
    private static AssetManager instance;
    
    // ==================== ASSET CACHES ====================
    private final Map<String, Texture> textureCache;
    private final Map<String, Texture> generatedTextureCache;
    private final Map<String, BitmapFont> fontCache;
    
    // Loading state
    private boolean initialized = false;
    
    // ==================== TEXTURE KEYS (File-based) ====================
    public static final String PLAYER = "player";
    public static final String PLATFORM = "platform";
    public static final String GROUND = "ground";
    public static final String GROUND_BASE = "ground_base";
    public static final String EXIT_DOOR = "door";
    public static final String BULLET = "bullet";
    public static final String ENEMY = "enemy";
    public static final String MINI_BOSS = "miniboss";
    public static final String BOSS = "boss";
    public static final String PISTOL = "pistol";
    public static final String BACKGROUND_LEVEL1 = "background_level1";
    public static final String BACKGROUND_LEVEL2_TO_4 = "background_level2to4";
    public static final String BACKGROUND_LEVEL5 = "background_level5";
    
    // ==================== TEXTURE KEYS (Generated) ====================
    public static final String MAC10 = "mac10";
    public static final String DEBUG_LINE = "debug_line";
    public static final String LEVEL_INDICATOR = "level_indicator";
    public static final String ENEMY_BULLET = "enemy_bullet";
    public static final String FLASH_WHITE = "flash_white";
    public static final String FLASH_RED = "flash_red";
    public static final String FLASH_YELLOW = "flash_yellow";
    public static final String BUTTON = "button";
    public static final String BUTTON_HOVER = "button_hover";
    
    // ==================== FONT KEYS ====================
    public static final String FONT_DEFAULT = "font_default";
    public static final String FONT_SMALL = "font_small";
    
    // ==================== CONSTRUCTOR ====================
    
    private AssetManager() {
        textureCache = new HashMap<>();
        generatedTextureCache = new HashMap<>();
        fontCache = new HashMap<>();
    }
    
    /**
     * Get singleton instance.
     * Thread-safe lazy initialization.
     * 
     * @return The singleton AssetManager instance
     */
    public static synchronized AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }
    
    // ==================== INITIALIZATION ====================
    
    /**
     * Initialize all game assets.
     * Call once at game start in Main.create().
     * Loads all textures and fonts into cache.
     */
    public void initialize() {
        if (initialized) {
            Gdx.app.log("AssetManager", "Already initialized, skipping");
            return;
        }
        
        Gdx.app.log("AssetManager", "Initializing assets...");
        
        // Load file-based textures
        loadFileTextures();
        
        // Generate procedural color textures
        generateColorTextures();
        
        // Load fonts
        loadFonts();
        
        initialized = true;
        Gdx.app.log("AssetManager", "Assets initialized successfully. " +
                   "Textures: " + (textureCache.size() + generatedTextureCache.size()) + 
                   ", Fonts: " + fontCache.size());
    }
    
    // ==================== TEXTURE LOADING ====================
    
    /**
     * Load all file-based textures.
     * Uses Nearest filtering for pixel art style.
     */
    private void loadFileTextures() {
        loadTexture(PLAYER, "player.png");
        loadTexture(PLATFORM, "ground.png");
        loadTexture(GROUND_BASE, "ground_base.png");
        loadTexture(EXIT_DOOR, "door.png");
        loadTexture(BULLET, "bullet.png");
        loadTexture(ENEMY, "enemy.png");
        loadTexture(MINI_BOSS, "miniboss.png");
        loadTexture(BOSS, "boss.png");
        loadTexture(PISTOL, "pistol.png");
        // Note: No generic background.png - using level-specific backgrounds instead
        loadTexture(BACKGROUND_LEVEL1, "bglevel1.png");
        loadTexture(BACKGROUND_LEVEL2_TO_4, "bglevel2to4.png");
        loadTexture(BACKGROUND_LEVEL5, "bglevel5.png");
    }
    
    /**
     * Load a texture from file with nearest filtering.
     * 
     * @param key Asset key for retrieval
     * @param filename File path relative to assets folder
     */
    private void loadTexture(String key, String filename) {
        try {
            Texture texture = new Texture(Gdx.files.internal(filename));
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            textureCache.put(key, texture);
            Gdx.app.log("AssetManager", "Loaded texture: " + filename);
        } catch (Exception e) {
            Gdx.app.error("AssetManager", "Failed to load texture: " + filename, e);
        }
    }
    
    /**
     * Generate all procedural color textures.
     */
    private void generateColorTextures() {
        generatedTextureCache.put(MAC10, createColorTexture(30, 15, Color.LIME));
        generatedTextureCache.put(DEBUG_LINE, createColorTexture(10, 600, Color.RED));
        generatedTextureCache.put(LEVEL_INDICATOR, createColorTexture(30, 30, Color.YELLOW));
        generatedTextureCache.put(ENEMY_BULLET, createColorTexture(8, 8, Color.ORANGE));
        generatedTextureCache.put(FLASH_WHITE, createColorTexture(60, 90, Color.WHITE));
        generatedTextureCache.put(FLASH_RED, createColorTexture(60, 100, Color.RED));
        generatedTextureCache.put(FLASH_YELLOW, createColorTexture(60, 90, Color.YELLOW));
        generatedTextureCache.put(BUTTON, createColorTexture(500, 80, new Color(0.7f, 0.7f, 0.7f, 1f)));
        generatedTextureCache.put(BUTTON_HOVER, createColorTexture(500, 80, new Color(0.9f, 0.9f, 0.9f, 1f)));
        
        Gdx.app.log("AssetManager", "Generated " + generatedTextureCache.size() + " color textures");
    }
    
    /**
     * Create a solid color texture.
     * 
     * @param width Texture width in pixels
     * @param height Texture height in pixels
     * @param color Fill color
     * @return Generated texture
     */
    public Texture createColorTexture(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
    
    // ==================== FONT LOADING ====================
    
    /**
     * Load and configure fonts.
     */
    private void loadFonts() {
        // Default font (large)
        BitmapFont defaultFont = new BitmapFont();
        defaultFont.getData().setScale(3f);
        fontCache.put(FONT_DEFAULT, defaultFont);
        
        // Small font
        BitmapFont smallFont = new BitmapFont();
        smallFont.getData().setScale(1.5f);
        fontCache.put(FONT_SMALL, smallFont);
        
        Gdx.app.log("AssetManager", "Loaded " + fontCache.size() + " fonts");
    }
    
    // ==================== GETTERS ====================
    
    /**
     * Get a texture by key.
     * Checks both file-loaded and generated textures.
     * 
     * @param key Texture key constant (e.g., AssetManager.PLAYER)
     * @return Texture or null if not found
     */
    public Texture getTexture(String key) {
        if (!initialized) {
            Gdx.app.error("AssetManager", "Assets not initialized! Call initialize() first.");
            return null;
        }
        
        // Check file textures first
        Texture texture = textureCache.get(key);
        if (texture != null) {
            return texture;
        }
        
        // Check generated textures
        texture = generatedTextureCache.get(key);
        if (texture != null) {
            return texture;
        }
        
        Gdx.app.error("AssetManager", "Texture not found: " + key);
        return null;
    }
    
    /**
     * Get a font by key.
     * 
     * @param key Font key constant (e.g., AssetManager.FONT_DEFAULT)
     * @return BitmapFont or null if not found
     */
    public BitmapFont getFont(String key) {
        if (!initialized) {
            Gdx.app.error("AssetManager", "Assets not initialized! Call initialize() first.");
            return null;
        }
        
        BitmapFont font = fontCache.get(key);
        if (font == null) {
            Gdx.app.error("AssetManager", "Font not found: " + key);
        }
        return font;
    }
    
    /**
     * Convenience method for default font.
     * 
     * @return Default (large) font
     */
    public BitmapFont getDefaultFont() {
        return getFont(FONT_DEFAULT);
    }
    
    /**
     * Convenience method for small font.
     * 
     * @return Small font
     */
    public BitmapFont getSmallFont() {
        return getFont(FONT_SMALL);
    }
    
    // ==================== STATE QUERIES ====================
    
    /**
     * Check if assets have been initialized.
     * 
     * @return true if initialize() has been called
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Get total number of cached textures.
     * 
     * @return Total texture count
     */
    public int getTextureCount() {
        return textureCache.size() + generatedTextureCache.size();
    }
    
    /**
     * Get total number of cached fonts.
     * 
     * @return Total font count
     */
    public int getFontCount() {
        return fontCache.size();
    }
    
    // ==================== LIFECYCLE ====================
    
    /**
     * Dispose all loaded assets.
     * Call on game exit in Main.dispose().
     */
    @Override
    public void dispose() {
        Gdx.app.log("AssetManager", "Disposing assets...");
        
        // Dispose file textures
        for (Texture texture : textureCache.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        textureCache.clear();
        
        // Dispose generated textures
        for (Texture texture : generatedTextureCache.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        generatedTextureCache.clear();
        
        // Dispose fonts
        for (BitmapFont font : fontCache.values()) {
            if (font != null) {
                font.dispose();
            }
        }
        fontCache.clear();
        
        initialized = false;
        Gdx.app.log("AssetManager", "Assets disposed successfully");
    }
    
    /**
     * Reset singleton instance (for testing or full restart).
     */
    public static void reset() {
        if (instance != null) {
            instance.dispose();
            instance = null;
        }
    }
}
