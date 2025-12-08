package com.labubushooter.frontend.patterns.levels;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.labubushooter.frontend.objects.Platform;
import com.labubushooter.frontend.patterns.LevelStrategy;

public class Level3Strategy implements LevelStrategy {
    @Override
    public void loadPlatforms(Array<Platform> platforms, Texture platformTex) {
        // Ground - spans entire level
        platforms.add(new Platform(0, 50, getLevelWidth(), 50, platformTex));

        // Gap jumping challenge in scrolling area
        platforms.add(new Platform(500, 200, 120, 20, platformTex));
        platforms.add(new Platform(800, 280, 120, 20, platformTex));
        platforms.add(new Platform(1100, 200, 120, 20, platformTex));
        platforms.add(new Platform(1350, 320, 150, 20, platformTex));
        platforms.add(new Platform(1650, 250, 180, 20, platformTex));
        platforms.add(new Platform(1950, 180, 200, 20, platformTex));

        // Exit platform
        platforms.add(new Platform(getLevelWidth() - 100, 50, 100, 200, platformTex));
    }

    @Override
    public float getLevelWidth() {
        return 2400f;
    }

    @Override
    public float getPlayerStartX() {
        return 100f;
    }

    @Override
    public float getPlayerStartY() {
        return 300f;
    }
}