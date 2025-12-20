package com.labubushooter.frontend.observer;

import com.badlogic.gdx. Gdx;

public class ScoreObserver implements GameObserver {
    private int displayedScore = 0;
    private int displayedCoins = 0;

    @Override
    public void onScoreChanged(int newScore) {
        this.displayedScore = newScore;
        Gdx.app.log("ScoreUI", "Score Updated: " + displayedScore);
    }

    @Override
    public void onHealthChanged(float newHealth) {
        // Not handled by ScoreObserver
    }

    @Override
    public void onCoinCollected(int totalCoins) {
        this. displayedCoins = totalCoins;
        Gdx. app.log("ScoreUI", "Coins:  " + displayedCoins);
    }

    @Override
    public void onGameOver() {
        Gdx.app.log("ScoreUI", "Final Score: " + displayedScore + " | Coins: " + displayedCoins);
    }

    @Override
    public void onVictory() {
        Gdx.app. log("ScoreUI", "Victory! Score: " + displayedScore + " | Coins: " + displayedCoins);
    }

    public int getDisplayedScore() { return displayedScore; }
    public int getDisplayedCoins() { return displayedCoins; }
}
