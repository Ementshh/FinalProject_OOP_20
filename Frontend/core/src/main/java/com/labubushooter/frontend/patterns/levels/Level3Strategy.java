package com.labubushooter.frontend.patterns.levels;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.labubushooter.frontend.objects.Platform;
import com.labubushooter.frontend.patterns.LevelStrategy;

public class Level3Strategy implements LevelStrategy {
    @Override
    public void loadPlatforms(Array<Platform> platforms, Texture platformTex) {
        // Ground
        platforms.add(new Platform(0, 50, getLevelWidth(), 50, platformTex));
        // Simple platforms
        platforms.add(new Platform(500, 200, 200, 20, platformTex));
        platforms.add(new Platform(800, 300, 200, 20, platformTex));
        platforms.add(new Platform(1200, 200, 200, 20, platformTex));
    }

    @Override
    public float getLevelWidth() { return 2400f; }

    @Override
    public float getPlayerStartX() { return 100f; }

    @Override
    public float getPlayerStartY() { return 300f; }
}