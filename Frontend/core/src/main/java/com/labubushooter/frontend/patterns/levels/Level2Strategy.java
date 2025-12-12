package com.labubushooter.frontend.patterns.levels;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.labubushooter.frontend.objects.Platform;
import com.labubushooter.frontend.patterns.LevelStrategy;

public class Level2Strategy implements LevelStrategy {
    @Override
    public void loadPlatforms(Array<Platform> platforms, Texture platformTex) {
        // Stairway pattern in scrolling area
        platforms.add(new Platform(600, 150, 150, 20, platformTex));
        platforms.add(new Platform(900, 250, 150, 20, platformTex));
        platforms.add(new Platform(1200, 350, 200, 20, platformTex));
        platforms.add(new Platform(1500, 250, 150, 20, platformTex));
        platforms.add(new Platform(1800, 200, 200, 20, platformTex));

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

    @Override
    public float getBossSpawnX() {
        return 0f;
    }

    @Override
    public float getBossSpawnY() {
        return 0f;
    }
}