package com.labubushooter.frontend.screens;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.labubushooter.frontend.GameState;
import com.labubushooter.frontend.core.GameContext;

/**
 * Manages screen transitions and lifecycle.
 * Implements State Pattern for screen management.
 */
public class ScreenManager {
    
    private final GameContext context;
    private final Map<GameState, Screen> screens;
    private Screen currentScreen;
    private GameState currentState;
    private GamePlayScreen gamePlayScreen;
    
    public ScreenManager(GameContext context) {
        this.context = context;
        this.screens = new HashMap<>();
    }
    
    /**
     * Get the gameplay screen for rendering in overlay screens.
     */
    public GamePlayScreen getGamePlayScreen() {
        return gamePlayScreen;
    }
    
    /**
     * Set the gameplay screen reference.
     */
    public void setGamePlayScreen(GamePlayScreen screen) {
        this.gamePlayScreen = screen;
    }
    
    /**
     * Register a screen for a specific game state.
     */
    public void registerScreen(GameState state, Screen screen) {
        screens.put(state, screen);
        Gdx.app.log("ScreenManager", "Registered screen for: " + state);
    }
    
    /**
     * Set the current screen by game state.
     */
    public void setScreen(GameState state) {
        if (currentScreen != null) {
            currentScreen.hide();
        }
        
        currentScreen = screens.get(state);
        currentState = state;
        
        if (currentScreen != null) {
            currentScreen.show();
            Gdx.app.log("ScreenManager", "Switched to screen: " + state);
        } else {
            Gdx.app.error("ScreenManager", "No screen registered for state: " + state);
        }
    }
    
    /**
     * Get the current game state.
     */
    public GameState getCurrentState() {
        return currentState;
    }
    
    /**
     * Get the current screen.
     */
    public Screen getCurrentScreen() {
        return currentScreen;
    }
    
    /**
     * Get a specific screen by state.
     */
    public Screen getScreen(GameState state) {
        return screens.get(state);
    }
    
    /**
     * Update and render the current screen.
     */
    public void render(float delta) {
        if (currentScreen == null) return;
        
        // Handle input
        currentScreen.handleInput(delta);
        
        // Update
        currentScreen.update(delta);
        
        // Render
        currentScreen.render(delta);
        
        // Check for state transition AFTER all screen processing
        GameState nextState = currentScreen.getNextState();
        if (nextState != null && nextState != currentState) {
            Gdx.app.log("ScreenManager", "Transition requested: " + currentState + " -> " + nextState);
            currentScreen.clearNextState();
            setScreen(nextState);
        }
    }
    
    /**
     * Resize all screens.
     */
    public void resize(int width, int height) {
        for (Screen screen : screens.values()) {
            screen.resize(width, height);
        }
    }
    
    /**
     * Dispose all screens.
     */
    public void dispose() {
        for (Screen screen : screens.values()) {
            screen.dispose();
        }
        screens.clear();
    }
}
