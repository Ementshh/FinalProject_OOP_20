package com.labubushooter.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.labubushooter.frontend.GameState;
import com.labubushooter.frontend.core.GameContext;

/**
 * Confirmation screen for restarting the game.
 * Uses dynamic positioning based on camera.
 */
public class RestartConfirmScreen extends BaseScreen {
    
    private static final float BUTTON_WIDTH = 150f;
    private static final float BUTTON_HEIGHT = 60f;
    private static final float BUTTON_GAP = 40f;
    
    private Rectangle yesButton;
    private Rectangle noButton;
    
    // Callback for rendering game in background
    private Runnable gameRenderer;
    
    public RestartConfirmScreen(GameContext context) {
        super(context);
        yesButton = new Rectangle(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
        noButton = new Rectangle(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    /**
     * Set a callback to render the game in the background.
     */
    public void setGameRenderer(Runnable gameRenderer) {
        this.gameRenderer = gameRenderer;
    }
    
    /**
     * Update button positions based on current camera position.
     */
    private void updateButtonPositions() {
        float centerX = context.camera.position.x;
        float centerY = context.camera.position.y;
        
        // Position buttons side by side, centered
        float totalWidth = 2 * BUTTON_WIDTH + BUTTON_GAP;
        float startX = centerX - totalWidth / 2;
        float buttonY = centerY - 50;
        
        yesButton.set(startX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        noButton.set(startX + BUTTON_WIDTH + BUTTON_GAP, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    @Override
    public void handleInput(float delta) {
        updateButtonPositions();
        
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
        updateButtonPositions();
        
        // Draw game in background if renderer is set
        if (gameRenderer != null) {
            gameRenderer.run();
        } else {
            clearScreenDark();
        }
        
        // Draw overlay
        drawOverlay(0.8f);
        
        context.batch.setProjectionMatrix(context.camera.combined);
        context.batch.begin();
        
        float centerX = context.camera.position.x;
        float centerY = context.camera.position.y;
        
        // Draw dialog box background
        float dialogWidth = 450f;
        float dialogHeight = 200f;
        float dialogX = centerX - dialogWidth / 2;
        float dialogY = centerY - dialogHeight / 2;
        
        context.batch.setColor(0.15f, 0.15f, 0.2f, 0.95f);
        context.batch.draw(context.debugTex, dialogX, dialogY, dialogWidth, dialogHeight);
        context.batch.setColor(1, 1, 1, 1);
        
        // Draw border
        context.batch.setColor(0.4f, 0.4f, 0.5f, 1f);
        context.batch.draw(context.debugTex, dialogX, dialogY + dialogHeight - 3, dialogWidth, 3);
        context.batch.draw(context.debugTex, dialogX, dialogY, dialogWidth, 3);
        context.batch.draw(context.debugTex, dialogX, dialogY, 3, dialogHeight);
        context.batch.draw(context.debugTex, dialogX + dialogWidth - 3, dialogY, 3, dialogHeight);
        context.batch.setColor(1, 1, 1, 1);
        
        // Draw title
        float titleY = centerY + 60;
        drawCenteredTextAtCamera("Start New Game?", titleY, true);
        
        // Draw warning
        float warningY = centerY + 10;
        context.smallFont.setColor(1f, 0.6f, 0.6f, 1f);
        drawCenteredTextAtCamera("All progress will be lost!", warningY, false);
        context.smallFont.setColor(1, 1, 1, 1);
        
        // Draw buttons
        drawButtonWithBackground("Yes", yesButton);
        drawButtonWithBackground("No", noButton);
        
        context.batch.end();
    }
    
    /**
     * Draw button with background.
     */
    private void drawButtonWithBackground(String text, Rectangle button) {
        boolean hover = isButtonHovered(button);
        
        if (hover) {
            context.batch.setColor(0.4f, 0.4f, 0.6f, 1f);
        } else {
            context.batch.setColor(0.25f, 0.25f, 0.35f, 1f);
        }
        
        context.batch.draw(context.debugTex, button.x, button.y, button.width, button.height);
        context.batch.setColor(1, 1, 1, 1);
        
        // Draw text centered
        context.layout.setText(context.smallFont, text);
        float textX = button.x + button.width / 2 - context.layout.width / 2;
        float textY = button.y + button.height / 2 + context.layout.height / 2;
        context.smallFont.draw(context.batch, text, textX, textY);
    }
}
