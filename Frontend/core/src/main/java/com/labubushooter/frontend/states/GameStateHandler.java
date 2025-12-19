package com.labubushooter.frontend.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface GameStateHandler {
    void enter();
    void handleInput();
    void update(float delta);
    void render(SpriteBatch batch);
    void exit();
}