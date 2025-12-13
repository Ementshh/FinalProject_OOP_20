package com.labubushooter.frontend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.labubushooter.frontend.services.PlayerApiService.PlayerData;

/**
 * Debug Manager untuk skip menu dan backend checking
 * Aktifkan dengan: Right Ctrl + D
 */
public class DebugManager {
    
    private static final String DEBUG_USERNAME = "DEBUG_PLAYER";
    private static final int DEBUG_STARTING_COINS = 9999;
    private static final int DEBUG_STARTING_STAGE = 1;
    
    private boolean debugModeActive = false;
    private boolean debugKeyWasPressed = false;
    
    /**
     * Check if debug mode should be activated (Right Ctrl + D)
     * @return true if debug mode was just activated
     */
    public boolean checkDebugActivation() {
        boolean rightCtrlHeld = Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
        boolean dPressed = Gdx.input.isKeyPressed(Input.Keys.D);
        
        if (rightCtrlHeld && dPressed && !debugKeyWasPressed) {
            debugKeyWasPressed = true;
            if (!debugModeActive) {
                activateDebugMode();
                return true;
            }
        }
        
        if (!dPressed) {
            debugKeyWasPressed = false;
        }
        
        return false;
    }
    
    /**
     * Activate debug mode
     */
    private void activateDebugMode() {
        debugModeActive = true;
        printDebugInfo();
    }
    
    /**
     * Print debug mode activation info to console
     */
    private void printDebugInfo() {
        Gdx.app.log("Debug", "════════════════════════════════════════");
        Gdx.app.log("Debug", "       DEBUG MODE ACTIVATED!");
        Gdx.app.log("Debug", "════════════════════════════════════════");
        Gdx.app.log("Debug", "Username: " + DEBUG_USERNAME);
        Gdx.app.log("Debug", "Starting Coins: " + DEBUG_STARTING_COINS);
        Gdx.app.log("Debug", "Starting Stage: " + DEBUG_STARTING_STAGE);
        Gdx.app.log("Debug", "Backend: SKIPPED");
        Gdx.app.log("Debug", "════════════════════════════════════════");
        Gdx.app.log("Debug", "Debug Commands:");
        Gdx.app.log("Debug", "  F3 - Skip to Level 3 (Mini Boss)");
        Gdx.app.log("Debug", "  F5 - Skip to Level 5 (Final Boss)");
        Gdx.app.log("Debug", "  K  - Insta-kill Boss");
        Gdx.app.log("Debug", "  1/2/3 - Switch weapons");
        Gdx.app.log("Debug", "════════════════════════════════════════");
    }
    
    /**
     * Check if debug mode is currently active
     */
    public boolean isDebugModeActive() {
        return debugModeActive;
    }
    
    /**
     * Get debug username
     */
    public String getDebugUsername() {
        return DEBUG_USERNAME;
    }
    
    /**
     * Create dummy player data for debug mode
     */
    public PlayerData createDebugPlayerData() {
        PlayerData data = new PlayerData();
        data.playerId = "debug-" + System.currentTimeMillis();
        data.username = DEBUG_USERNAME;
        data.totalCoins = DEBUG_STARTING_COINS;
        data.lastStage = DEBUG_STARTING_STAGE;
        return data;
    }
    
    /**
     * Get starting level for debug mode
     */
    public int getDebugStartingLevel() {
        return DEBUG_STARTING_STAGE;
    }
    
    /**
     * Log a debug-mode specific message
     */
    public void logSkippedAction(String action) {
        if (debugModeActive) {
            Gdx.app.log("Debug", "[SKIPPED] " + action);
        }
    }
    
    /**
     * Reset debug mode (for game restart)
     */
    public void reset() {
        // Don't reset debugModeActive - keep it active once enabled
        debugKeyWasPressed = false;
    }
}
