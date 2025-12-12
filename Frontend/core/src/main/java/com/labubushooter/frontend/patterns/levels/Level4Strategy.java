package com.labubushooter.frontend.patterns.levels;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.labubushooter.frontend.objects.Platform;
import com.labubushooter.frontend.patterns.LevelStrategy;

public class Level4Strategy implements LevelStrategy {
    @Override
    public void loadPlatforms(Array<Platform> platforms, Texture platformTex) {
        // Vertical tower climbing pattern
        // platforms.add(new Platform(500, 150, 50, 200, platformTex)); // Wall
        platforms.add(new Platform(700, 300, 150, 20, platformTex));
        platforms.add(new Platform(1000, 200, 150, 20, platformTex));
        platforms.add(new Platform(1300, 350, 200, 20, platformTex));
        platforms.add(new Platform(1600, 250, 150, 20, platformTex));
        platforms.add(new Platform(1900, 180, 200, 20, platformTex));

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