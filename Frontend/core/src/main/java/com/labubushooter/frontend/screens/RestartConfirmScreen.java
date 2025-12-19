package com.labubushooter.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.labubushooter.frontend.GameState;
import com.labubushooter.frontend.core.GameContext;

/**
 * Confirmation screen for restarting the game.
 */
public class RestartConfirmScreen extends BaseScreen {
    
    private Rectangle yesButton;
    private Rectangle noButton;
    
    // Callback for rendering game in background
    private Runnable gameRenderer;
    
    public RestartConfirmScreen(GameContext context) {
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
        float buttonWidth = 200f;
        float buttonHeight = 80f;
        
        yesButton = new Rectangle(
            GameContext.VIEWPORT_WIDTH / 2 - buttonWidth - 20, 200, 
            buttonWidth, buttonHeight);
        noButton = new Rectangle(
            GameContext.VIEWPORT_WIDTH / 2 + 20, 200, 
            buttonWidth, buttonHeight);
    }
    
    @Override
    public void handleInput(float delta) {
        // ESC to cancel
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            transitionTo(GameState.PAUSED);
            Gdx.app.log("RestartConfirm", "Restart cancelled");
            return;
        }
        
        if (isButtonClicked(yesButton)) {
            confirmRestart();
        } else if (isButtonClicked(noButton)) {
            transitionTo(GameState.PAUSED);
            Gdx.app.log("RestartConfirm", "Restart cancelled");
        }
    }
    
    private void confirmRestart() {
        // Skip backend reset in debug mode
        if (context.debugManager != null && context.debugManager.isDebugModeActive()) {
            context.debugManager.logSkippedAction("Reset progress on backend");
            if (context.currentPlayerData != null) {
                context.currentPlayerData.lastStage = 1;
            }
            context.coinsCollectedThisSession = 0;
            restartGame();
            return;
        }
        
        // Use callback to restart game
        if (context.callback != null) {
            context.callback.restartGame();
            transitionTo(GameState.PLAYING);
            Gdx.app.log("RestartConfirm", "Game restarted via callback");
            return;
        }
        
        // Fallback if no callback
        restartGame();
    }
    
    private void restartGame() {
        context.clearGameObjects();
        context.resetPlayer();
        context.currentLevel = 1;
        transitionTo(GameState.PLAYING);
        Gdx.app.log("RestartConfirm", "Game restarted");
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
        drawCenteredText("Restart?", 400, true);
        drawCenteredText("All progress will be lost!", 340, false);
        
        // Draw buttons
        drawButton("Yes", yesButton);
        drawButton("No", noButton);
        
        context.batch.end();
    }
}
