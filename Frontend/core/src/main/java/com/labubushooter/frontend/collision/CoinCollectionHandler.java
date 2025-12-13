package com.labubushooter.frontend.collision;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.labubushooter.frontend.events.CollisionEvent;
import com.labubushooter.frontend.events.PlayerCoinCollisionEvent;
import com.labubushooter.frontend.objects.Coin;

/**
 * Handles coin collection by the player.
 * Increments score and manages coin pooling.
 */
public class CoinCollectionHandler implements CollisionHandler {
    private final Pool<Coin> coinPool;
    private final Array<Coin> activeCoins;
    
    // These need to be updated in Main's score tracking
    private int coinScore = 0;
    private int coinsCollectedThisSession = 0;

    public CoinCollectionHandler(Pool<Coin> coinPool, Array<Coin> activeCoins) {
        this.coinPool = coinPool;
        this.activeCoins = activeCoins;
    }

    @Override
    public void onCollision(CollisionEvent event) {
        if (event instanceof PlayerCoinCollisionEvent) {
            handle(event);
        }
    }

    @Override
    public void handle(CollisionEvent event) {
        PlayerCoinCollisionEvent collisionEvent = (PlayerCoinCollisionEvent) event;
        Coin coin = collisionEvent.getCoin();
        
        if (!coin.active) return; // Coin already collected
        
        // Increment score
        coinScore++;
        coinsCollectedThisSession++;
        
        Gdx.app.log("Coin", "Collected coin! Total: " + coinScore);
        
        // Remove coin
        activeCoins.removeValue(coin, true);
        coinPool.free(coin);
    }

    @Override
    public boolean canHandle(Class<? extends CollisionEvent> eventType) {
        return eventType == PlayerCoinCollisionEvent.class;
    }

    // Getters for Main to access scores
    public int getCoinScore() {
        return coinScore;
    }

    public int getCoinsCollectedThisSession() {
        return coinsCollectedThisSession;
    }

    // Setters for Main to update scores (when loading/resetting)
    public void setCoinScore(int score) {
        this.coinScore = score;
    }

    public void setCoinsCollectedThisSession(int collected) {
        this.coinsCollectedThisSession = collected;
    }
}
