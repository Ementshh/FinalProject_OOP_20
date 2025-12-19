package com.labubushooter.frontend.managers;

import com.labubushooter.frontend.GameState;
import com.labubushooter.frontend.observers.GameStateObserver;
import com.badlogic.gdx.utils.Array;

public class GameManager {
    private static GameManager instance;
    private GameState currentState;
    private Array<GameStateObserver> observers;

    private GameManager() {
        observers = new Array<>();
        currentState = GameState.USERNAME_INPUT;
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void setState(GameState newState) {
        GameState oldState = currentState;
        currentState = newState;
        notifyObservers(oldState, newState);
    }

    public GameState getState() {
        return currentState;
    }

    public void addObserver(GameStateObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(GameStateObserver observer) {
        observers.removeValue(observer, true);
    }

    private void notifyObservers(GameState oldState, GameState newState) {
        for (GameStateObserver observer : observers) {
            observer.onStateChanged(oldState, newState);
        }
    }

    public void reset() {
        currentState = GameState.USERNAME_INPUT;
    }
}