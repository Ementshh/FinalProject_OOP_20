package com.labubushooter.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.labubushooter.frontend.GameState;
import com.labubushooter.frontend.core.GameContext;

/**
 * Victory screen shown when player completes the game.
 */
public class VictoryScreen extends BaseScreen {
    
    public VictoryScreen(GameContext context) {
        super(context);
    }
    
    @Override
    public void handleInput(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            restartToUsername();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }
    
    private void restartToUsername() {
        if (context.callback != null) {
            context.callback.restartToUsernameInput();
        } else {
            // Fallback if no callback
            context.usernameInput.setLength(0);
            context.username = "";
            context.clearGameObjects();
            context.resetPlayer();
            context.currentLevel = 1;
        }
        transitionTo(GameState.USERNAME_INPUT);
        Gdx.app.log("Victory", "Returned to username input");
    }
    
    @Override
    public void update(float delta) {
        // No update logic needed
    }
    
    @Override
    public void render(float delta) {
        clearScreen(0.1f, 0.3f, 0.1f, 1f); // Green tint for victory
        
        context.batch.setProjectionMatrix(context.camera.combined);
        context.batch.begin();
        
        float centerX = context.camera.position.x;
        float centerY = context.camera.position.y;
        
        // Draw "VICTORY!" text
        context.font.setColor(1f, 1f, 0.2f, 1f);
        context.layout.setText(context.font, "VICTORY!");
        context.font.draw(context.batch, "VICTORY!", 
            centerX - context.layout.width / 2, centerY + 100);
        context.font.setColor(1, 1, 1, 1);
        
        // Draw "Game Completed!" text
        context.layout.setText(context.smallFont, "Game Completed!");
        context.smallFont.draw(context.batch, "Game Completed!", 
            centerX - context.layout.width / 2, centerY + 50);
        
        // Draw coin score
        String coinsText = "Total Coins Collected: " + context.coinScore;
        context.layout.setText(context.smallFont, coinsText);
        context.smallFont.draw(context.batch, coinsText, 
            centerX - context.layout.width / 2, centerY);
        
        // Draw player stats if available
        if (context.currentPlayerData != null) {
            String totalCoinsText = "All-time Coins: " + context.currentPlayerData.totalCoins;
            context.layout.setText(context.smallFont, totalCoinsText);
            context.smallFont.draw(context.batch, totalCoinsText, 
                centerX - context.layout.width / 2, centerY - 30);
        }
        
        // Draw instructions
        context.layout.setText(context.smallFont, "Press SPACE to Play Again");
        context.smallFont.draw(context.batch, "Press SPACE to Play Again", 
            centerX - context.layout.width / 2, centerY - 80);
        
        context.layout.setText(context.smallFont, "Press ESC to Quit");
        context.smallFont.draw(context.batch, "Press ESC to Quit", 
            centerX - context.layout.width / 2, centerY - 110);
        
        context.batch.end();
    }
}
