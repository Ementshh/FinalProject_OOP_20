package com.labubushooter.frontend.observer;

public interface GameObserver {
    void onScoreChanged(int newScore);
    void onHealthChanged(float newHealth);
    void onCoinCollected(int totalCoins);
    void onGameOver();
    void onVictory();
}
