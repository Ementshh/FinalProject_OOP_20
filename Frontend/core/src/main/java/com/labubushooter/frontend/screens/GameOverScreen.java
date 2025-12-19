package com.labubushooter.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.labubushooter.frontend.GameState;
import com.labubushooter.frontend.core.GameContext;

/**
 * Game over screen shown when player dies.
 */
public class GameOverScreen extends BaseScreen {
    
    public GameOverScreen(GameContext context) {
        super(context);
    }
    
    @Override
    public void handleInput(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            restartToUsername();
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
        Gdx.app.log("GameOver", "Returned to username input");
    }
    
    @Override
    public void update(float delta) {
        // No update logic needed
    }
    
    @Override
    public void render(float delta) {
        clearScreen(0.1f, 0.1f, 0.1f, 1f);
        
        context.batch.setProjectionMatrix(context.camera.combined);
        context.batch.begin();
        
        // Draw "GAME OVER" text
        float centerY = context.camera.position.y;
        
        context.font.setColor(1f, 0.2f, 0.2f, 1f);
        context.layout.setText(context.font, "GAME OVER");
        float gameOverX = context.camera.position.x - context.layout.width / 2;
        context.font.draw(context.batch, "GAME OVER", gameOverX, centerY + 50);
        context.font.setColor(1, 1, 1, 1);
        
        // Draw restart instruction
        context.layout.setText(context.smallFont, "Press SPACE to restart");
        float restartX = context.camera.position.x - context.layout.width / 2;
        context.smallFont.draw(context.batch, "Press SPACE to restart", restartX, centerY - 20);
        
        context.batch.end();
    }
}
