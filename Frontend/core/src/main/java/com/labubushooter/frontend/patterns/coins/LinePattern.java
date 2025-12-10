package com.labubushooter.frontend.patterns.coins;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.labubushooter.frontend.objects.Coin;
import com.labubushooter.frontend.patterns.CoinPattern;

public class LinePattern implements CoinPattern {
    private static final float SPACING = 30f;
    
    @Override
    public Array<Coin> spawn(Pool<Coin> coinPool, float spawnX, float spawnY) {
        Array<Coin> coins = new Array<>();
        
        // Random 3-5 coins
        int numberOfCoins = MathUtils.random(3, 5);
        
        // Center the line of coins around spawnX
        float totalWidth = (numberOfCoins - 1) * SPACING;
        float startX = spawnX - (totalWidth / 2);
        
        for (int i = 0; i < numberOfCoins; i++) {
            Coin coin = coinPool.obtain();
            coin.init(startX + (i * SPACING), spawnY);
            coins.add(coin);
        }
        
        return coins;
    }
    
    @Override
    public String getName() {
        return "Line";
    }
}
