package com.labubushooter.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Rectangle;
import com.labubushooter.frontend.GameState;
import com.labubushooter.frontend.core.GameContext;
import com.labubushooter.frontend.services.PlayerApiService;

/**
 * Screen for username input at game start.
 */
public class UsernameInputScreen extends BaseScreen {
    
    private Rectangle startGameButton;
    private boolean waitingForResponse = false;
    private GamePlayScreen gamePlayScreen;
    
    public UsernameInputScreen(GameContext context) {
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
        startGameButton = new Rectangle(centerX, 200, buttonWidth, buttonHeight);
    }
    
    @Override
    public void show() {
        super.show();
        setupInputProcessor();
    }
    
    private void setupInputProcessor() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (waitingForResponse) return false;
                
                if (character == '\b' && context.usernameInput.length() > 0) {
                    // Backspace
                    context.usernameInput.deleteCharAt(context.usernameInput.length() - 1);
                } else if (character == '\r' || character == '\n') {
                    // Enter key - try to start game
                    if (context.usernameInput.length() > 0) {
                        startLogin();
                    }
                } else if (Character.isLetterOrDigit(character) && 
                           context.usernameInput.length() < GameContext.MAX_USERNAME_LENGTH) {
                    // Add character
                    context.usernameInput.append(character);
                }
                return true;
            }
        });
    }
    
    @Override
    public void hide() {
        super.hide();
        Gdx.input.setInputProcessor(null);
    }
    
    @Override
    public void handleInput(float delta) {
        if (waitingForResponse) return;
        
        // Check for debug mode activation
        if (context.debugManager != null && context.debugManager.checkDebugActivation()) {
            activateDebugMode();
            return;
        }
        
        // Check start button click
        if (isButtonClicked(startGameButton) && context.usernameInput.length() > 0) {
            startLogin();
        }
    }
    
    private void activateDebugMode() {
        context.username = context.debugManager.getDebugUsername();
        context.currentPlayerData = context.debugManager.createDebugPlayerData();
        context.coinsCollectedThisSession = 0;
        context.isNewPlayer = true;
        context.currentLevel = 1;
        if (gamePlayScreen != null) {
            gamePlayScreen.setNeedsLevelLoad(true);
        }
        transitionTo(GameState.PLAYING);
    }
    
    private void startLogin() {
        context.username = context.usernameInput.toString();
        waitingForResponse = true;
        transitionTo(GameState.LOADING_PLAYER_DATA);
        
        context.playerApi.login(context.username, new PlayerApiService.LoginCallback() {
            @Override
            public void onSuccess(PlayerApiService.PlayerData playerData, boolean isNew) {
                Gdx.app.postRunnable(() -> {
                    context.currentPlayerData = playerData;
                    context.isNewPlayer = isNew;
                    context.coinsCollectedThisSession = 0;
                    waitingForResponse = false;
                    
                    if (isNew || playerData.lastStage == 1) {
                        context.currentLevel = 1;
                        if (gamePlayScreen != null) {
                            gamePlayScreen.setNeedsLevelLoad(true);
                        }
                        transitionTo(GameState.PLAYING);
                    } else {
                        transitionTo(GameState.CONTINUE_OR_NEW);
                    }
                });
            }
            
            @Override
            public void onFailure(String error) {
                Gdx.app.postRunnable(() -> {
                    Gdx.app.error("Login", "Failed: " + error);
                    waitingForResponse = false;
                    // Stay on username input screen
                    nextState = null;
                });
            }
        });
    }
    
    @Override
    public void update(float delta) {
        // No update logic needed for this screen
    }
    
    @Override
    public void render(float delta) {
        clearScreenDark();
        
        context.batch.setProjectionMatrix(context.camera.combined);
        context.batch.begin();
        
        // Draw title
        drawCenteredText("LABUBOOM", 500, true);
        
        // Draw prompt
        drawCenteredText("Enter Your Name:", 380, false);
        
        // Draw input box background
        context.batch.setColor(0.3f, 0.3f, 0.3f, 1f);
        context.batch.draw(context.buttonTex, 
            GameContext.VIEWPORT_WIDTH / 2 - 250, 300, 500, 60);
        context.batch.setColor(1, 1, 1, 1);
        
        // Draw username text
        String displayText = context.usernameInput.length() > 0 ? 
            context.usernameInput.toString() : "Username...";
        context.layout.setText(context.smallFont, displayText);
        float textX = GameContext.VIEWPORT_WIDTH / 2 - context.layout.width / 2;
        float textY = 340;
        
        if (context.usernameInput.length() > 0) {
            context.smallFont.draw(context.batch, displayText, textX, textY);
        } else {
            context.smallFont.setColor(0.5f, 0.5f, 0.5f, 1f);
            context.smallFont.draw(context.batch, displayText, textX, textY);
            context.smallFont.setColor(1, 1, 1, 1);
        }
        
        // Draw start button
        drawStartButton();
        
        // Draw debug hint
        context.smallFont.setColor(0.5f, 0.5f, 0.5f, 1f);
        drawCenteredText("Press Right Ctrl + D for Debug Mode", 100, false);
        context.smallFont.setColor(1, 1, 1, 1);
        
        context.batch.end();
    }
    
    private void drawStartButton() {
        boolean canStart = context.usernameInput.length() > 0 && !waitingForResponse;
        
        if (canStart) {
            drawButton("START GAME", startGameButton);
        } else {
            // Draw disabled button
            context.batch.setColor(0.4f, 0.4f, 0.4f, 1f);
            context.batch.draw(context.buttonTex, 
                startGameButton.x, startGameButton.y, 
                startGameButton.width, startGameButton.height);
            context.batch.setColor(1, 1, 1, 1);
            
            context.layout.setText(context.smallFont, "START GAME");
            float btnTextX = startGameButton.x + startGameButton.width / 2 - context.layout.width / 2;
            float btnTextY = startGameButton.y + startGameButton.height / 2 + context.layout.height / 2;
            context.smallFont.setColor(0.6f, 0.6f, 0.6f, 1f);
            context.smallFont.draw(context.batch, "START GAME", btnTextX, btnTextY);
            context.smallFont.setColor(1, 1, 1, 1);
        }
    }
}
