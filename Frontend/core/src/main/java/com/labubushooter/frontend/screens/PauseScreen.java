package com.labubushooter.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.labubushooter.frontend.GameState;
import com.labubushooter.frontend.core.GameContext;

/**
 * Pause menu screen.
 */
public class PauseScreen extends BaseScreen {
    
    private Rectangle continueButton;
    private Rectangle saveButton;
    private Rectangle newGameButton;
    private Rectangle quitButton;
    
    // Callback for rendering game in background
    private Runnable gameRenderer;
    
    public PauseScreen(GameContext context) {
        super(context);
        initializeUI();
    }
    
    /**
     * Set a callback to render the game in the background.
     */
    public void setGameRenderer(Runnable gameRenderer) {
        this.gameRenderer = gameRenderer;
    }
    
    private void initializeUI() {
        float buttonWidth = 500f;
        float buttonHeight = 80f;
        float centerX = GameContext.VIEWPORT_WIDTH / 2 - buttonWidth / 2;
        
        continueButton = new Rectangle(centerX, 370, buttonWidth, buttonHeight);
        saveButton = new Rectangle(centerX, 270, buttonWidth, buttonHeight);
        newGameButton = new Rectangle(centerX, 170, buttonWidth, buttonHeight);
        quitButton = new Rectangle(centerX, 70, buttonWidth, buttonHeight);
    }
    
    @Override
    public void handleInput(float delta) {
        // Resume with ESC
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            transitionTo(GameState.PLAYING);
            Gdx.app.log("Pause", "RESUMED");
            return;
        }
        
        if (isButtonClicked(continueButton)) {
            transitionTo(GameState.PLAYING);
            Gdx.app.log("Pause", "RESUMED");
        } else if (isButtonClicked(saveButton)) {
            saveGame();
        } else if (isButtonClicked(newGameButton)) {
            transitionTo(GameState.RESTART_CONFIRM);
            Gdx.app.log("Pause", "Showing restart confirmation");
        } else if (isButtonClicked(quitButton)) {
            quitWithSave();
        }
    }
    
    private void saveGame() {
        if (context.debugManager != null && context.debugManager.isDebugModeActive()) {
            context.debugManager.logSkippedAction("Save to backend");
            Gdx.app.log("Pause", "Game saved (debug mode)");
            return;
        }
        
        if (context.callback != null) {
            context.callback.saveProgress();
            Gdx.app.log("Pause", "Game saved successfully");
        }
    }
    
    private void quitWithSave() {
        saveGame();
        Gdx.app.log("Pause", "Quitting game with auto-save");
        
        // Give time for save request
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Gdx.app.postRunnable(() -> Gdx.app.exit());
        }).start();
    }
    
    @Override
    public void update(float delta) {
        // No update logic needed
    }
    
    @Override
    public void render(float delta) {
        // Draw game in background if renderer is set
        if (gameRenderer != null) {
            gameRenderer.run();
        } else {
            clearScreenDark();
        }
        
        // Draw overlay
        drawOverlay(0.6f);
        
        context.batch.setProjectionMatrix(context.camera.combined);
        context.batch.begin();
        
        // Draw title
        drawCenteredText("Game Paused", 500, true);
        
        // Draw buttons
        drawButton("Continue", continueButton);
        drawButton("Save Game", saveButton);
        drawButton("New Game", newGameButton);
        drawButton("Quit (Auto Save)", quitButton);
        
        context.batch.end();
    }
}
