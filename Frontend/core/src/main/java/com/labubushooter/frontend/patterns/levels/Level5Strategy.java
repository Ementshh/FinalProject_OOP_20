package com.labubushooter.frontend.patterns.levels;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.labubushooter.frontend.objects.Platform;
import com.labubushooter.frontend.patterns.LevelStrategy;

public class Level5Strategy implements LevelStrategy {
    @Override
    public void loadPlatforms(Array<Platform> platforms, Texture platformTex) {
        // Ground - spans entire level (single screen, no scrolling)
        platforms.add(new Platform(0, 50, getLevelWidth(), 50, platformTex));

        // Boss arena - all platforms visible at once
        platforms.add(new Platform(300, 200, 150, 20, platformTex));
        platforms.add(new Platform(700, 250, 150, 20, platformTex));
        platforms.add(new Platform(1100, 300, 200, 20, platformTex));
        platforms.add(new Platform(1500, 200, 150, 20, platformTex));

        // Exit platform
        platforms.add(new Platform(getLevelWidth() - 100, 50, 100, 200, platformTex));
    }

    @Override
    public float getLevelWidth() {
        return 1920f;
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