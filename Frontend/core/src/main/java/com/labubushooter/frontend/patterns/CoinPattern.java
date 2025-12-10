package com.labubushooter.frontend.patterns;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.labubushooter.frontend.objects.Coin;

public interface CoinPattern {
    Array<Coin> spawn(Pool<Coin> coinPool, float spawnX, float spawnY);
    String getName();
}
