package com.labubushooter.frontend.observer;

import com.badlogic.gdx.utils.Array;

public class GameSubject {
    private Array<GameObserver> observers = new Array<>();
    private int score = 0;
    private float health = 100f;
    private int coins = 0;

    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(GameObserver observer) {
        observers.removeValue(observer, true);
    }

    public void setScore(int score) {
        this. score = score;
        notifyScoreChanged();
    }

    public void addScore(int points) {
        this.score += points;
        notifyScoreChanged();
    }

    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(100, health));
        notifyHealthChanged();
        if (this.health <= 0) {
            notifyGameOver();
        }
    }

    public void takeDamage(float damage) {
        setHealth(this.health - damage);
    }

    public void collectCoin() {
        this.coins++;
        notifyCoinCollected();
    }

    public void triggerVictory() {
        for (GameObserver observer : observers) {
            observer.onVictory();
        }
    }

    private void notifyScoreChanged() {
        for (GameObserver observer : observers) {
            observer.onScoreChanged(score);
        }
    }

    private void notifyHealthChanged() {
        for (GameObserver observer : observers) {
            observer. onHealthChanged(health);
        }
    }

    private void notifyCoinCollected() {
        for (GameObserver observer :  observers) {
            observer.onCoinCollected(coins);
        }
    }

    private void notifyGameOver() {
        for (GameObserver observer :  observers) {
            observer.onGameOver();
        }
    }

    // Getters
    public int getScore() { return score; }
    public float getHealth() { return health; }
    public int getCoins() { return coins; }
}
