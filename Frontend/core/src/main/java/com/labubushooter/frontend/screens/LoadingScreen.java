package com.labubushooter.frontend.screens;

import com.labubushooter.frontend.core.GameContext;

/**
 * Simple loading screen shown during API calls.
 */
public class LoadingScreen extends BaseScreen {
    
    private float dotTimer = 0f;
    private int dotCount = 0;
    
    public LoadingScreen(GameContext context) {
        super(context);
    }
    
    @Override
    public void handleInput(float delta) {
        // No input handling during loading
    }
    
    @Override
    public void update(float delta) {
        // Animate dots
        dotTimer += delta;
        if (dotTimer >= 0.5f) {
            dotTimer = 0f;
            dotCount = (dotCount + 1) % 4;
        }
    }
    
    @Override
    public void render(float delta) {
        clearScreenDark();
        
        context.batch.setProjectionMatrix(context.camera.combined);
        context.batch.begin();
        
        // Build loading text with animated dots
        StringBuilder loadingText = new StringBuilder("Loading");
        for (int i = 0; i < dotCount; i++) {
            loadingText.append(".");
        }
        
        drawCenteredText(loadingText.toString(), GameContext.VIEWPORT_HEIGHT / 2, true);
        
        context.batch.end();
    }
}
