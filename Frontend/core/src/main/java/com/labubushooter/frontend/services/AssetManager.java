package com.labubushooter.frontend.services;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
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
    public static final String PLAYER_WALK_FRAME1 = "player_walk_frame1";
    public static final String PLAYER_WALK_FRAME2 = "player_walk_frame2";
    public static final String PLAYER_CROUCH = "player_crouch";
    public static final String PLATFORM = "platform";
    public static final String GROUND = "ground";
    public static final String GROUND_BASE = "ground_base";
    public static final String EXIT_DOOR = "door";
    public static final String BULLET = "bullet";
    public static final String ENEMY = "enemy";
    public static final String ENEMY_FRAME1 = "enemy_frame1";
    public static final String ENEMY_FRAME2 = "enemy_frame2";
    public static final String MINI_BOSS_WALK_FRAME1 = "miniboss_walk_frame1";
    public static final String MINI_BOSS_WALK_FRAME2 = "miniboss_walk_frame2";
    public static final String MINI_BOSS_CROUCH = "miniboss_crouch";
    public static final String MINI_BOSS_DASHPREP = "miniboss_dashprep";
    public static final String MINI_BOSS_DASH = "miniboss_dash";
    public static final String BOSS = "boss";
    
    // Boss animation textures - Phase 1
    public static final String BOSS_PHASE1_WALK1 = "boss_phase1_walk1";
    public static final String BOSS_PHASE1_WALK2 = "boss_phase1_walk2";
    public static final String BOSS_PHASE1_JUMP = "boss_phase1_jump";
    public static final String BOSS_PHASE1_BIGATTACK = "boss_phase1_bigattack";
    
    // Boss animation textures - Phase 2
    public static final String BOSS_PHASE2_WALK1 = "boss_phase2_walk1";
    public static final String BOSS_PHASE2_WALK2 = "boss_phase2_walk2";
    public static final String BOSS_PHASE2_JUMP = "boss_phase2_jump";
    public static final String BOSS_PHASE2_BIGATTACK = "boss_phase2_bigattack";
    
    // Boss animation textures - Phase 3
    public static final String BOSS_PHASE3_WALK1 = "boss_phase3_walk1";
    public static final String BOSS_PHASE3_WALK2 = "boss_phase3_walk2";
    public static final String BOSS_PHASE3_JUMP = "boss_phase3_jump";
    public static final String BOSS_PHASE3_BIGATTACK = "boss_phase3_bigattack";
    
    // Boss bullet textures
    public static final String BOSS_PHASE1_BULLET = "boss_phase1_bullet";
    public static final String BOSS_PHASE23_BULLET = "boss_phase23_bullet";
    public static final String BOSS_BIG_BULLET = "boss_big_bullet";
    public static final String PISTOL = "pistol";
    public static final String MAC10 = "mac10";
    public static final String BACKGROUND_LEVEL1 = "background_level1";
    public static final String BACKGROUND_LEVEL2_TO_4 = "background_level2to4";
    public static final String BACKGROUND_LEVEL5 = "background_level5";

    // ==================== TEXTURE KEYS (Generated) ====================
    public static final String DEBUG_LINE = "debug_line";
    public static final String LEVEL_INDICATOR = "level_indicator";
    public static final String LEVEL_INDICATOR_1 = "level_indicator_1";
    public static final String LEVEL_INDICATOR_2 = "level_indicator_2";
    public static final String LEVEL_INDICATOR_3 = "level_indicator_3";
    public static final String LEVEL_INDICATOR_4 = "level_indicator_4";
    public static final String LEVEL_INDICATOR_5 = "level_indicator_5";
    public static final String ENEMY_BULLET = "enemy_bullet";
    public static final String FLASH_WHITE = "flash_white";
    public static final String FLASH_RED = "flash_red";
    public static final String FLASH_YELLOW = "flash_yellow";
    public static final String BUTTON = "button";
    public static final String BUTTON_HOVER = "button_hover";
    public static final String TRANSPARENT = "transparent";

    // Pickups (File-based)
    public static final String AMMO_9MM = "ammo_9mm";
    public static final String AMMO_45CAL = "ammo_45cal";
    public static final String HEALTH_POTION = "health_potion";
    public static final String AMMO_PACK = "ammo_pack";

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
        loadTexture(PLAYER_WALK_FRAME1, "player_walk_frame1.png");
        loadTexture(PLAYER_WALK_FRAME2, "player_walk_frame2.png");
        loadTexture(PLAYER_CROUCH, "player_crouch.png");
        loadTexture(PLATFORM, "ground.png");
        loadTexture(GROUND_BASE, "ground_base.png");
        loadTexture(EXIT_DOOR, "door.png");
        loadTexture(BULLET, "bullet.png");
        loadTexture(ENEMY, "enemy.png");
        loadTexture(ENEMY_FRAME1, "enemyframe1.png");
        loadTexture(ENEMY_FRAME2, "enemyframe2.png");
        loadTexture(MINI_BOSS_WALK_FRAME1, "miniboss_walk_frame1.png");
        loadTexture(MINI_BOSS_WALK_FRAME2, "miniboss_walk_frame2.png");
        loadTexture(MINI_BOSS_CROUCH, "miniboss_crouch.png");
        loadTexture(MINI_BOSS_DASHPREP, "miniboss_dashprep.png");
        loadTexture(MINI_BOSS_DASH, "miniboss_dash.png");
        loadTexture(BOSS, "boss.png");
        
        // Load boss animation textures - Phase 1
        loadTexture(BOSS_PHASE1_WALK1, "boss_phase1_walk1.png");
        loadTexture(BOSS_PHASE1_WALK2, "boss_phase1_walk2.png");
        loadTexture(BOSS_PHASE1_JUMP, "boss_phase1_jump.png");
        loadTexture(BOSS_PHASE1_BIGATTACK, "boss_phase1_bigattack.png");
        
        // Load boss animation textures - Phase 2
        loadTexture(BOSS_PHASE2_WALK1, "boss_phase2_walk1.png");
        loadTexture(BOSS_PHASE2_WALK2, "boss_phase2_walk2.png");
        loadTexture(BOSS_PHASE2_JUMP, "boss_phase2_jump.png");
        loadTexture(BOSS_PHASE2_BIGATTACK, "boss_phase2_bigattack.png");
        
        // Load boss animation textures - Phase 3
        loadTexture(BOSS_PHASE3_WALK1, "boss_phase3_walk1.png");
        loadTexture(BOSS_PHASE3_WALK2, "boss_phase3_walk2.png");
        loadTexture(BOSS_PHASE3_JUMP, "boss_phase3_jump.png");
        loadTexture(BOSS_PHASE3_BIGATTACK, "boss_phase3_bigattack.png");
        
        // Load boss bullet textures
        loadTexture(BOSS_PHASE1_BULLET, "boss_phase1_bullet.png");
        loadTexture(BOSS_PHASE23_BULLET, "boss_phase23_bullet.png");
        loadTexture(BOSS_BIG_BULLET, "boss_big_bullet.png");
        loadTexture(PISTOL, "pistol.png");
        loadTexture(MAC10, "mac10.png");
        loadTexture(BACKGROUND_LEVEL1, "bglevel1.png");
        loadTexture(BACKGROUND_LEVEL2_TO_4, "bglevel2to4.png");
        loadTexture(BACKGROUND_LEVEL5, "bglevel5.png");

        // Load pickup textures
        loadTexture(HEALTH_POTION, "healthpotion.png");
        loadTexture(AMMO_PACK, "ammopack.png");
        // Use ammopack.png for both ammo types (share the same texture instance)
        Texture ammoPackTex = textureCache.get(AMMO_PACK);
        if (ammoPackTex != null) {
            textureCache.put(AMMO_9MM, ammoPackTex);
            textureCache.put(AMMO_45CAL, ammoPackTex);
        }
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
        generatedTextureCache.put(DEBUG_LINE, createColorTexture(10, 600, Color.RED));
        generatedTextureCache.put(LEVEL_INDICATOR, createColorTexture(30, 30, Color.YELLOW));

        // Level indicator textures with colored background and number
        generatedTextureCache.put(LEVEL_INDICATOR_1, createLevelIndicatorTexture(1));
        generatedTextureCache.put(LEVEL_INDICATOR_2, createLevelIndicatorTexture(2));
        generatedTextureCache.put(LEVEL_INDICATOR_3, createLevelIndicatorTexture(3));
        generatedTextureCache.put(LEVEL_INDICATOR_4, createLevelIndicatorTexture(4));
        generatedTextureCache.put(LEVEL_INDICATOR_5, createLevelIndicatorTexture(5));

        generatedTextureCache.put(ENEMY_BULLET, createColorTexture(8, 8, Color.ORANGE));
        generatedTextureCache.put(FLASH_WHITE, createColorTexture(60, 90, Color.WHITE));
        generatedTextureCache.put(FLASH_RED, createColorTexture(60, 100, Color.RED));
        generatedTextureCache.put(FLASH_YELLOW, createColorTexture(60, 90, Color.YELLOW));
        generatedTextureCache.put(BUTTON, createColorTexture(500, 80, new Color(0.7f, 0.7f, 0.7f, 1f)));
        generatedTextureCache.put(BUTTON_HOVER, createColorTexture(500, 80, new Color(0.9f, 0.9f, 0.9f, 1f)));
        generatedTextureCache.put(TRANSPARENT, createColorTexture(1, 1, new Color(0, 0, 0, 0)));

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

    /**
     * Creates a level indicator texture with colored circle background and level number.
     * Colors: Level 1,2,4 = Yellow (#fce803), Level 3 = Green (#379624), Level 5 = Purple (#882dc7)
     *
     * @param level The level number (1-5)
     * @return Texture with circle and number
     */
    private Texture createLevelIndicatorTexture(int level) {
        int size = 50; // Circle diameter
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        // Determine background color based on level
        Color bgColor;
        switch (level) {
            case 3:
                // Green for mini boss level: #379624
                bgColor = new Color(0x379624FF);
                break;
            case 5:
                // Purple for final boss level: #882dc7
                bgColor = new Color(0x882dc7FF);
                break;
            default:
                // Yellow for normal levels (1, 2, 4): #fce803
                bgColor = new Color(0xfce803FF);
                break;
        }

        // Draw filled circle
        int centerX = size / 2;
        int centerY = size / 2;
        int radius = size / 2 - 2;

        // Fill circle
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int dx = x - centerX;
                int dy = y - centerY;
                if (dx * dx + dy * dy <= radius * radius) {
                    pixmap.drawPixel(x, y, Color.rgba8888(bgColor));
                }
            }
        }

        // Draw number in black
        pixmap.setColor(Color.BLACK);
        drawDigit(pixmap, level, centerX, centerY);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Draws a simple digit on the pixmap.
     *
     * @param pixmap The pixmap to draw on
     * @param digit The digit to draw (1-5)
     * @param centerX Center X position
     * @param centerY Center Y position
     */
    private void drawDigit(Pixmap pixmap, int digit, int centerX, int centerY) {
        int thickness = 3;

        switch (digit) {
            case 1:
                // Vertical line with small top hook and bottom base
                for (int t = 0; t < thickness; t++) {
                    for (int y = centerY - 12; y <= centerY + 12; y++) {
                        pixmap.drawPixel(centerX + t, y);
                    }
                    for (int x = centerX - 5; x <= centerX; x++) {
                        pixmap.drawPixel(x + t, centerY - 12);
                        pixmap.drawPixel(x + t, centerY - 11);
                    }
                    for (int x = centerX - 6; x <= centerX + 6; x++) {
                        pixmap.drawPixel(x, centerY + 12 + t);
                    }
                }
                break;

            case 2:
                for (int t = 0; t < thickness; t++) {
                    for (int x = centerX - 6; x <= centerX + 6; x++) {
                        pixmap.drawPixel(x, centerY - 12 + t);
                    }
                    for (int y = centerY - 12; y <= centerY; y++) {
                        pixmap.drawPixel(centerX + 6 + t, y);
                    }
                    for (int x = centerX - 6; x <= centerX + 6; x++) {
                        pixmap.drawPixel(x, centerY + t);
                    }
                    for (int y = centerY; y <= centerY + 12; y++) {
                        pixmap.drawPixel(centerX - 6 + t, y);
                    }
                    for (int x = centerX - 6; x <= centerX + 6; x++) {
                        pixmap.drawPixel(x, centerY + 12 + t);
                    }
                }
                break;

            case 3:
                for (int t = 0; t < thickness; t++) {
                    for (int x = centerX - 6; x <= centerX + 6; x++) {
                        pixmap.drawPixel(x, centerY - 12 + t);
                    }
                    for (int x = centerX - 4; x <= centerX + 6; x++) {
                        pixmap.drawPixel(x, centerY + t);
                    }
                    for (int x = centerX - 6; x <= centerX + 6; x++) {
                        pixmap.drawPixel(x, centerY + 12 + t);
                    }
                    for (int y = centerY - 12; y <= centerY + 12; y++) {
                        pixmap.drawPixel(centerX + 6 + t, y);
                    }
                }
                break;

            case 4:
                for (int t = 0; t < thickness; t++) {
                    for (int y = centerY - 12; y <= centerY; y++) {
                        pixmap.drawPixel(centerX - 6 + t, y);
                    }
                    for (int x = centerX - 6; x <= centerX + 6; x++) {
                        pixmap.drawPixel(x, centerY + t);
                    }
                    for (int y = centerY - 12; y <= centerY + 12; y++) {
                        pixmap.drawPixel(centerX + 6 + t, y);
                    }
                }
                break;

            case 5:
                for (int t = 0; t < thickness; t++) {
                    for (int x = centerX - 6; x <= centerX + 6; x++) {
                        pixmap.drawPixel(x, centerY - 12 + t);
                    }
                    for (int y = centerY - 12; y <= centerY; y++) {
                        pixmap.drawPixel(centerX - 6 + t, y);
                    }
                    for (int x = centerX - 6; x <= centerX + 6; x++) {
                        pixmap.drawPixel(x, centerY + t);
                    }
                    for (int y = centerY; y <= centerY + 12; y++) {
                        pixmap.drawPixel(centerX + 6 + t, y);
                    }
                    for (int x = centerX - 6; x <= centerX + 6; x++) {
                        pixmap.drawPixel(x, centerY + 12 + t);
                    }
                }
                break;
        }
    }

    // ==================== FONT LOADING ====================

    /**
     * Load and configure fonts.
     * Uses FreeTypeFontGenerator to create high-resolution bitmap fonts.
     */
    private void loadFonts() {
        FreeTypeFontGenerator generator = null;
        try {
            // Try to load the custom font
            generator = new FreeTypeFontGenerator(Gdx.files.internal("game_default_font.otf"));
            FreeTypeFontParameter parameter = new FreeTypeFontParameter();

            // Configure default font (Large)
            parameter.size = 48; // High resolution size
            parameter.minFilter = Texture.TextureFilter.Linear;
            parameter.magFilter = Texture.TextureFilter.Linear;
            parameter.color = Color.WHITE;
            parameter.borderWidth = 2;
            parameter.borderColor = Color.BLACK;
            parameter.shadowOffsetX = 3;
            parameter.shadowOffsetY = 3;
            parameter.shadowColor = new Color(0, 0, 0, 0.5f);

            BitmapFont defaultFont = generator.generateFont(parameter);
            // Scale down for display if needed, but keep high res texture
            defaultFont.getData().setScale(0.5f);
            fontCache.put(FONT_DEFAULT, defaultFont);

            // Configure small font
            parameter.size = 24; // Smaller size
            parameter.borderWidth = 1;
            parameter.shadowOffsetX = 1;
            parameter.shadowOffsetY = 1;

            BitmapFont smallFont = generator.generateFont(parameter);
            // Scale down for display
            smallFont.getData().setScale(0.8f);
            fontCache.put(FONT_SMALL, smallFont);

            Gdx.app.log("AssetManager", "Loaded high-resolution fonts from game_default_font.otf");

        } catch (Exception e) {
            Gdx.app.error("AssetManager", "Failed to load custom font, falling back to default", e);

            // Fallback to default LibGDX font
            BitmapFont defaultFont = new BitmapFont();
            defaultFont.getData().setScale(2f);
            fontCache.put(FONT_DEFAULT, defaultFont);

            BitmapFont smallFont = new BitmapFont();
            smallFont.getData().setScale(1.2f);
            fontCache.put(FONT_SMALL, smallFont);
        } finally {
            if (generator != null) {
                generator.dispose();
            }
        }

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
