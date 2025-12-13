package com.labubushooter.frontend.events;

import com.labubushooter.frontend.objects.Coin;
import com.labubushooter.frontend.objects.Player;

/**
 * Event fired when the player collects a coin.
 */
public class PlayerCoinCollisionEvent extends CollisionEvent {
    private final Player player;
    private final Coin coin;

    public PlayerCoinCollisionEvent(Player player, Coin coin) {
        super();
        this.player = player;
        this.coin = coin;
    }

    public Player getPlayer() {
        return player;
    }

    public Coin getCoin() {
        return coin;
    }
}
