package com.labubushooter.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.labubushooter.frontend.GameState;
import com.labubushooter.frontend.core.GameContext;

/**
 * Pause menu screen with dynamic positioning based on camera.
 */
public class PauseScreen extends BaseScreen {
    
    // Button dimensions (constants)
    private static final float BUTTON_WIDTH = 300f;
    private static final float BUTTON_HEIGHT = 60f;
    private static final float BUTTON_SPACING = 20f;
    
    // Buttons - will be positioned dynamically
    private Rectangle continueButton;
    private Rectangle saveButton;
    private Rectangle newGameButton;
    private Rectangle quitButton;
    
    // Callback for rendering game in background
    private Runnable gameRenderer;
    
    public PauseScreen(GameContext context) {
        super(context);
        // Initialize with default positions, will be updated in render
        continueButton = new Rectangle(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
        saveButton = new Rectangle(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
        newGameButton = new Rectangle(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
        quitButton = new Rectangle(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
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
        // Calculate screen center based on camera position
        float centerX = context.camera.position.x;
        float centerY = context.camera.position.y;
        
        // Button X position (centered horizontally)
        float buttonX = centerX - BUTTON_WIDTH / 2;
        
        // Calculate total height of all buttons with spacing
        float totalButtonsHeight = 4 * BUTTON_HEIGHT + 3 * BUTTON_SPACING;
        
        // Start Y position (centered vertically, offset up slightly for title)
        float startY = centerY + totalButtonsHeight / 2 - BUTTON_HEIGHT - 30;
        
        // Position buttons from top to bottom
        continueButton.set(buttonX, startY, BUTTON_WIDTH, BUTTON_HEIGHT);
        saveButton.set(buttonX, startY - (BUTTON_HEIGHT + BUTTON_SPACING), BUTTON_WIDTH, BUTTON_HEIGHT);
        newGameButton.set(buttonX, startY - 2 * (BUTTON_HEIGHT + BUTTON_SPACING), BUTTON_WIDTH, BUTTON_HEIGHT);
        quitButton.set(buttonX, startY - 3 * (BUTTON_HEIGHT + BUTTON_SPACING), BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    @Override
    public void handleInput(float delta) {
        // Update button positions before checking clicks
        updateButtonPositions();
        
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
        // Update button positions based on current camera
        updateButtonPositions();
        
        // Draw game in background if renderer is set
        if (gameRenderer != null) {
            gameRenderer.run();
        } else {
            clearScreenDark();
        }
        
        // Draw overlay covering the visible area
        drawOverlay(0.7f);
        
        context.batch.setProjectionMatrix(context.camera.combined);
        context.batch.begin();
        
        // Calculate positions relative to camera
        float centerY = context.camera.position.y;
        float viewportHeight = context.viewport.getWorldHeight();
        
        // Draw title (positioned above buttons)
        float titleY = centerY + viewportHeight / 2 - 60;
        drawCenteredTextAtCamera("PAUSED", titleY, true);
        
        // Draw buttons with custom styling
        drawButtonWithBackground("Resume", continueButton);
        drawButtonWithBackground("Save Game", saveButton);
        drawButtonWithBackground("New Game", newGameButton);
        drawButtonWithBackground("Quit (Auto Save)", quitButton);
        
        // Draw hint at bottom
        float hintY = centerY - context.viewport.getWorldHeight() / 2 + 40;
        context.smallFont.setColor(0.7f, 0.7f, 0.7f, 1f);
        drawCenteredTextAtCamera("Press ESC to resume", hintY, false);
        context.smallFont.setColor(1, 1, 1, 1);
        
        context.batch.end();
    }
    
    /**
     * Draw button with background.
     */
    private void drawButtonWithBackground(String text, Rectangle button) {
        boolean hover = isButtonHovered(button);
        
        // Draw button background
        if (hover) {
            context.batch.setColor(0.4f, 0.4f, 0.6f, 1f);
        } else {
            context.batch.setColor(0.25f, 0.25f, 0.35f, 1f);
        }
        
        // Use debugTex as simple colored background
        context.batch.draw(context.debugTex, button.x, button.y, button.width, button.height);
        context.batch.setColor(1, 1, 1, 1);
        
        // Draw border effect when hovered
        if (hover) {
            context.batch.setColor(0.6f, 0.6f, 0.8f, 1f);
            context.batch.draw(context.debugTex, button.x, button.y + button.height - 2, button.width, 2);
            context.batch.draw(context.debugTex, button.x, button.y, button.width, 2);
            context.batch.draw(context.debugTex, button.x, button.y, 2, button.height);
            context.batch.draw(context.debugTex, button.x + button.width - 2, button.y, 2, button.height);
            context.batch.setColor(1, 1, 1, 1);
        }
        
        // Draw text centered in button
        context.layout.setText(context.smallFont, text);
        float textX = button.x + button.width / 2 - context.layout.width / 2;
        float textY = button.y + button.height / 2 + context.layout.height / 2;
        context.smallFont.draw(context.batch, text, textX, textY);
    }
}
