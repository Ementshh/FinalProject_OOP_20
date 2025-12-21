package com.labubushooter.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.labubushooter.frontend.GameState;
import com.labubushooter.frontend.core.GameContext;
import com.labubushooter.frontend.services.PlayerApiService;

/**
 * Screen for returning players to continue or start new game.
 */
public class ContinueOrNewScreen extends BaseScreen {
    
    private Rectangle continueButton;
    private Rectangle newGameButton;
    private GamePlayScreen gamePlayScreen;
    
    // UI Constants
    private static final float BUTTON_WIDTH = 500f;
    private static final float BUTTON_HEIGHT = 80f;
    
    public ContinueOrNewScreen(GameContext context) {
        super(context);
        initializeUI();
    }
    
    /**
     * Set reference to GamePlayScreen for triggering level load.
     */
    public void setGamePlayScreen(GamePlayScreen screen) {
        this.gamePlayScreen = screen;
    }
    
    private void initializeUI() {
        // Initialize rectangles - actual positions set in updateUIPositions()
        continueButton = new Rectangle(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
        newGameButton = new Rectangle(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    /**
     * Update UI element positions based on camera center.
     */
    private void updateUIPositions() {
        float centerX = getCenterX();
        float centerY = getCenterY();
        
        continueButton.set(centerX - BUTTON_WIDTH / 2, centerY - 50, BUTTON_WIDTH, BUTTON_HEIGHT);
        newGameButton.set(centerX - BUTTON_WIDTH / 2, centerY - 150, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    @Override
    public void show() {
        super.show();
        updateUIPositions();
        Gdx.app.log("ContinueOrNewScreen", "Screen shown for player: " + context.username);
    }
    
    @Override
    public void handleInput(float delta) {
        // Update UI positions every frame
        updateUIPositions();
        
        if (isButtonClicked(continueButton)) {
            continueGame();
        } else if (isButtonClicked(newGameButton)) {
            startNewGame();
        }
    }
    
    private void continueGame() {
        Gdx.app.log("ContinueOrNew", "Continuing from stage " + context.currentPlayerData.lastStage);
        context.currentLevel = context.currentPlayerData.lastStage;
        if (gamePlayScreen != null) {
            gamePlayScreen.setNeedsLevelLoad(true);
        }
        transitionTo(GameState.PLAYING);
    }
    
    private void startNewGame() {
        // Skip backend reset in debug mode
        if (context.debugManager != null && context.debugManager.isDebugModeActive()) {
            context.debugManager.logSkippedAction("Reset progress on backend");
            context.currentPlayerData.lastStage = 1;
            context.coinsCollectedThisSession = 0;
            context.currentLevel = 1;
            if (gamePlayScreen != null) {
                gamePlayScreen.setNeedsLevelLoad(true);
            }
            transitionTo(GameState.PLAYING);
            return;
        }
        
        // Reset progress on server
        context.playerApi.resetProgress(context.currentPlayerData.playerId, 
            new PlayerApiService.SaveCallback() {
                @Override
                public void onSuccess() {
                    Gdx.app.postRunnable(() -> {
                        context.currentPlayerData.lastStage = 1;
                        context.coinsCollectedThisSession = 0;
                        context.currentLevel = 1;
                        if (gamePlayScreen != null) {
                            gamePlayScreen.setNeedsLevelLoad(true);
                        }
                        transitionTo(GameState.PLAYING);
                        Gdx.app.log("ContinueOrNew", "Starting new game");
                    });
                }
                
                @Override
                public void onFailure(String error) {
                    Gdx.app.error("ContinueOrNew", "Failed to reset progress: " + error);
                }
            });
    }
    
    @Override
    public void update(float delta) {
        // No update logic needed
    }
    
    @Override
    public void render(float delta) {
        clearScreenDark();
        
        // Ensure UI positions are updated
        updateUIPositions();
        
        context.batch.setProjectionMatrix(context.camera.combined);
        context.batch.begin();
        
        float centerY = getCenterY();
        
        // Draw welcome message
        String welcomeText = "Welcome back, " + context.username + "!";
        drawCenteredText(welcomeText, centerY + 150, true);
        
        // Draw last stage info
        String stageText = "Last Stage: " + context.currentPlayerData.lastStage;
        drawCenteredText(stageText, centerY + 80, false);
        
        // Draw total coins
        String coinsText = "Total Coins: " + context.currentPlayerData.totalCoins;
        drawCenteredText(coinsText, centerY + 50, false);
        
        // Draw buttons
        drawButton("Continue", continueButton);
        drawButton("New Game", newGameButton);
        
        context.batch.end();
    }
}
