package com.labubushooter.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.labubushooter.frontend.GameState;
import com.labubushooter.frontend.core.GameContext;

/**
 * Abstract base class for all screens.
 * Provides common functionality and access to shared resources.
 * Implements Template Method Pattern for screen lifecycle.
 */
public abstract class BaseScreen implements Screen {
    
    protected final GameContext context;
    protected GameState nextState = null;
    
    // Reusable vector for input handling
    protected final Vector3 touchPos = new Vector3();
    
    public BaseScreen(GameContext context) {
        this.context = context;
    }
    
    @Override
    public void show() {
        // Reset camera to center for menu screens
        resetCameraToCenter();
        Gdx.app.log(getClass().getSimpleName(), "Screen shown");
    }
    
    @Override
    public void hide() {
        // Default implementation - can be overridden
        Gdx.app.log(getClass().getSimpleName(), "Screen hidden");
    }
    
    @Override
    public void resize(int width, int height) {
        context.viewport.update(width, height, true);
        // Re-center camera after resize
        resetCameraToCenter();
    }
    
    @Override
    public void dispose() {
        // Default implementation - override if screen has its own resources
    }
    
    @Override
    public GameState getNextState() {
        return nextState;
    }
    
    @Override
    public void clearNextState() {
        nextState = null;
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Reset camera position to viewport center.
     * IMPORTANT: Call this in show() for all menu/UI screens.
     */
    protected void resetCameraToCenter() {
        context.camera.position.set(
            GameContext.VIEWPORT_WIDTH / 2f,
            GameContext.VIEWPORT_HEIGHT / 2f,
            0
        );
        context.camera.update();
    }
    
    /**
     * Get the center X coordinate relative to camera.
     */
    protected float getCenterX() {
        return context.camera.position.x;
    }
    
    /**
     * Get the center Y coordinate relative to camera.
     */
    protected float getCenterY() {
        return context.camera.position.y;
    }
    
    /**
     * Get the left edge of visible area.
     */
    protected float getLeftEdge() {
        return context.camera.position.x - context.viewport.getWorldWidth() / 2f;
    }
    
    /**
     * Get the right edge of visible area.
     */
    protected float getRightEdge() {
        return context.camera.position.x + context.viewport.getWorldWidth() / 2f;
    }
    
    /**
     * Get the top edge of visible area.
     */
    protected float getTopEdge() {
        return context.camera.position.y + context.viewport.getWorldHeight() / 2f;
    }
    
    /**
     * Get the bottom edge of visible area.
     */
    protected float getBottomEdge() {
        return context.camera.position.y - context.viewport.getWorldHeight() / 2f;
    }
    
    /**
     * Clear the screen with a specific color.
     */
    protected void clearScreen(float r, float g, float b, float a) {
        Gdx.gl.glClearColor(r, g, b, a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
    
    /**
     * Clear the screen with default dark color.
     */
    protected void clearScreenDark() {
        clearScreen(0.15f, 0.15f, 0.2f, 1f);
    }
    
    /**
     * Check if a button was clicked.
     */
    protected boolean isButtonClicked(Rectangle button) {
        if (!Gdx.input.justTouched()) return false;
        
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        context.camera.unproject(touchPos);
        
        return button.contains(touchPos.x, touchPos.y);
    }
    
    /**
     * Check if mouse is hovering over a button.
     */
    protected boolean isButtonHovered(Rectangle button) {
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        context.camera.unproject(touchPos);
        return button.contains(touchPos.x, touchPos.y);
    }
    
    /**
     * Draw a button with hover effect.
     */
    protected void drawButton(String text, Rectangle button) {
        boolean hover = isButtonHovered(button);
        Texture btnTex = hover ? context.buttonHoverTex : context.buttonTex;
        
        context.batch.draw(btnTex, button.x, button.y, button.width, button.height);
        
        context.layout.setText(context.smallFont, text);
        float textX = button.x + button.width / 2 - context.layout.width / 2;
        float textY = button.y + button.height / 2 + context.layout.height / 2;
        
        context.smallFont.setColor(0.2f, 0.2f, 0.2f, 1f);
        context.smallFont.draw(context.batch, text, textX, textY);
        context.smallFont.setColor(1, 1, 1, 1);
    }
    
    /**
     * Draw centered text - uses camera center for proper positioning.
     */
    protected void drawCenteredText(String text, float y, boolean useLargeFont) {
        float centerX = getCenterX();
        if (useLargeFont) {
            context.layout.setText(context.font, text);
            float x = centerX - context.layout.width / 2;
            context.font.draw(context.batch, text, x, y);
        } else {
            context.layout.setText(context.smallFont, text);
            float x = centerX - context.layout.width / 2;
            context.smallFont.draw(context.batch, text, x, y);
        }
    }
    
    /**
     * Draw a semi-transparent overlay covering the visible viewport area.
     * This version is camera-aware and covers the current view.
     */
    protected void drawOverlay(float alpha) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        context.shapeRenderer.setProjectionMatrix(context.camera.combined);
        context.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        context.shapeRenderer.setColor(0, 0, 0, alpha);
        
        // Cover the visible area based on camera position
        float left = context.camera.position.x - context.viewport.getWorldWidth() / 2;
        float bottom = context.camera.position.y - context.viewport.getWorldHeight() / 2;
        context.shapeRenderer.rect(left, bottom, 
            context.viewport.getWorldWidth(), 
            context.viewport.getWorldHeight());
        
        context.shapeRenderer.end();
        
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
    
    /**
     * Draw centered text relative to camera position.
     */
    protected void drawCenteredTextAtCamera(String text, float y, boolean useLargeFont) {
        float centerX = context.camera.position.x;
        
        if (useLargeFont) {
            context.layout.setText(context.font, text);
            float x = centerX - context.layout.width / 2;
            context.font.draw(context.batch, text, x, y);
        } else {
            context.layout.setText(context.smallFont, text);
            float x = centerX - context.layout.width / 2;
            context.smallFont.draw(context.batch, text, x, y);
        }
    }
    
    /**
     * Transition to another screen state.
     */
    protected void transitionTo(GameState state) {
        this.nextState = state;
        Gdx.app.log(getClass().getSimpleName(), "Transitioning to: " + state);
    }
}
