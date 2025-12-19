package com.labubushooter.frontend.observers;

import com.labubushooter.frontend.GameState;

public interface GameStateObserver {
    void onStateChanged(GameState oldState, GameState newState);
}