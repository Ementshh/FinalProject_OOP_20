package com.labubushooter.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Rectangle;
import com.labubushooter.frontend.GameState;
import com.labubushooter.frontend.core.GameContext;
import com.labubushooter.frontend.services.PlayerApiService;

/**
 * Screen for username input at game start.
 * UI elements are positioned relative to camera center for proper centering.
 */
public class UsernameInputScreen extends BaseScreen {
    
    // UI Constants
    private static final float BUTTON_WIDTH = 500f;
    private static final float BUTTON_HEIGHT = 80f;
    private static final float INPUT_BOX_WIDTH = 500f;
    private static final float INPUT_BOX_HEIGHT = 60f;
    
    // UI Elements - positioned dynamically
    private Rectangle startGameButton;
    private Rectangle inputBox;
    
    // State
    private boolean waitingForResponse = false;
    private GamePlayScreen gamePlayScreen;
    private InputAdapter inputProcessor;
    
    public UsernameInputScreen(GameContext context) {
        super(context);
        initializeUI();
        createInputProcessor();
    }
    
    /**
     * Set reference to GamePlayScreen for triggering level load.
     */
    public void setGamePlayScreen(GamePlayScreen screen) {
        this.gamePlayScreen = screen;
    }
    
    private void initializeUI() {
        // Initialize rectangles - actual positions set in updateUIPositions()
        startGameButton = new Rectangle(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
        inputBox = new Rectangle(0, 0, INPUT_BOX_WIDTH, INPUT_BOX_HEIGHT);
    }
    
    /**
     * Create the input processor once, reuse it.
     */
    private void createInputProcessor() {
        inputProcessor = new InputAdapter() {
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
        };
    }
    
    /**
     * Update UI element positions based on camera center.
     * Call this every frame to ensure proper positioning.
     */
    private void updateUIPositions() {
        float centerX = getCenterX();
        float centerY = getCenterY();
        
        // Input box - centered horizontally, above center vertically
        inputBox.set(
            centerX - INPUT_BOX_WIDTH / 2,
            centerY - 20,
            INPUT_BOX_WIDTH,
            INPUT_BOX_HEIGHT
        );
        
        // Start button - centered horizontally, below input box
        startGameButton.set(
            centerX - BUTTON_WIDTH / 2,
            centerY - 120,
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
    }
    
    @Override
    public void show() {
        super.show(); // This resets camera to center
        
        // CRITICAL: Reset all state when screen is shown
        resetScreenState();
        
        // Set input processor
        Gdx.input.setInputProcessor(inputProcessor);
        
        Gdx.app.log("UsernameInputScreen", "Screen shown, state reset complete");
    }
    
    /**
     * Reset all screen state for fresh start.
     */
    private void resetScreenState() {
        waitingForResponse = false;
        
        // Reset debug manager state
        if (context.debugManager != null) {
            context.debugManager.reset();
        }
        
        // Update UI positions for centered camera
        updateUIPositions();
    }
    
    @Override
    public void hide() {
        super.hide();
        Gdx.input.setInputProcessor(null);
    }
    
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        // Update UI positions after resize
        updateUIPositions();
    }
    
    @Override
    public void handleInput(float delta) {
        if (waitingForResponse) return;
        
        // Update UI positions every frame (camera might have moved)
        updateUIPositions();
        
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
        Gdx.app.log("UsernameInputScreen", "Activating debug mode...");
        
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
        if (waitingForResponse) return; // Prevent double-click
        
        final String username = context.usernameInput.toString().trim();
        if (username.isEmpty()) {
            Gdx.app.log("Login", "Username is empty");
            return;
        }
        
        context.username = username;
        waitingForResponse = true;
        
        Gdx.app.log("UsernameInputScreen", "Starting login for: " + username);
        
        // DON'T transition to loading screen - stay here and show loading indicator
        // The callback will handle the transition after response
        
        context.playerApi.login(username, new PlayerApiService.LoginCallback() {
            @Override
            public void onSuccess(PlayerApiService.PlayerData playerData, boolean isNew) {
                // This is already wrapped in postRunnable by PlayerApiService
                Gdx.app.log("UsernameInputScreen", "Login success! isNew=" + isNew + ", lastStage=" + playerData.lastStage);
                
                context.currentPlayerData = playerData;
                context.isNewPlayer = isNew;
                context.coinsCollectedThisSession = 0;
                waitingForResponse = false;
                
                // Determine next screen based on player status
                if (isNew || playerData.lastStage <= 1) {
                    // New player or player at stage 1 - start game directly at level 1
                    Gdx.app.log("UsernameInputScreen", "New player or stage 1 - starting game at level 1");
                    context.currentLevel = 1;
                    if (gamePlayScreen != null) {
                        gamePlayScreen.setNeedsLevelLoad(true);
                    }
                    transitionTo(GameState.PLAYING);
                } else {
                    // Returning player with progress - show continue/new menu
                    Gdx.app.log("UsernameInputScreen", "Returning player at stage " + playerData.lastStage + " - showing continue/new menu");
                    transitionTo(GameState.CONTINUE_OR_NEW);
                }
            }
            
            @Override
            public void onFailure(String error) {
                Gdx.app.error("UsernameInputScreen", "Login failed: " + error);
                waitingForResponse = false;
                // Stay on username input screen - nextState remains null
                // Error will be shown to user
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
        
        // Ensure UI positions are updated
        updateUIPositions();
        
        context.batch.setProjectionMatrix(context.camera.combined);
        context.batch.begin();
        
        float centerY = getCenterY();
        
        // Draw title - relative to camera center
        drawCenteredText("LABUBOOM", centerY + 200, true);
        
        // Draw prompt
        drawCenteredText("Enter Your Name:", centerY + 80, false);
        
        // Draw input box background
        context.batch.setColor(0.3f, 0.3f, 0.3f, 1f);
        context.batch.draw(context.buttonTex, inputBox.x, inputBox.y, inputBox.width, inputBox.height);
        context.batch.setColor(1, 1, 1, 1);
        
        // Draw username text
        String displayText = context.usernameInput.length() > 0 ? 
            context.usernameInput.toString() : "Username...";
        
        context.layout.setText(context.smallFont, displayText);
        float textX = inputBox.x + inputBox.width / 2 - context.layout.width / 2;
        float textY = inputBox.y + inputBox.height / 2 + context.layout.height / 2;
        
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
        drawCenteredText("Press Right Ctrl + D for Debug Mode", centerY - 200, false);
        context.smallFont.setColor(1, 1, 1, 1);
        
        // Draw waiting indicator with more visibility
        if (waitingForResponse) {
            // Draw semi-transparent overlay
            context.batch.setColor(0f, 0f, 0f, 0.5f);
            float overlayX = context.camera.position.x - context.viewport.getWorldWidth() / 2;
            float overlayY = context.camera.position.y - context.viewport.getWorldHeight() / 2;
            context.batch.draw(context.buttonTex, overlayX, overlayY, 
                context.viewport.getWorldWidth(), context.viewport.getWorldHeight());
            context.batch.setColor(1, 1, 1, 1);
            
            // Draw loading text prominently
            context.font.setColor(1f, 1f, 0f, 1f);
            drawCenteredText("Connecting...", centerY, true);
            context.font.setColor(1, 1, 1, 1);
            
            context.smallFont.setColor(0.8f, 0.8f, 0.8f, 1f);
            drawCenteredText("Please wait", centerY - 60, false);
            context.smallFont.setColor(1, 1, 1, 1);
        }
        
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
