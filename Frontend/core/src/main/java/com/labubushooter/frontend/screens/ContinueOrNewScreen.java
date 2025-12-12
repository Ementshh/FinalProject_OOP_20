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
        float buttonWidth = 500f;
        float buttonHeight = 80f;
        float centerX = GameContext.VIEWPORT_WIDTH / 2 - buttonWidth / 2;
        
        continueButton = new Rectangle(centerX, 270, buttonWidth, buttonHeight);
        newGameButton = new Rectangle(centerX, 170, buttonWidth, buttonHeight);
    }
    
    @Override
    public void handleInput(float delta) {
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
        
        context.batch.setProjectionMatrix(context.camera.combined);
        context.batch.begin();
        
        // Draw welcome message
        String welcomeText = "Welcome back, " + context.username + "!";
        drawCenteredText(welcomeText, 450, true);
        
        // Draw last stage info
        String stageText = "Last Stage: " + context.currentPlayerData.lastStage;
        drawCenteredText(stageText, 380, false);
        
        // Draw total coins
        String coinsText = "Total Coins: " + context.currentPlayerData.totalCoins;
        drawCenteredText(coinsText, 350, false);
        
        // Draw buttons
        drawButton("Continue", continueButton);
        drawButton("New Game", newGameButton);
        
        context.batch.end();
    }
}
