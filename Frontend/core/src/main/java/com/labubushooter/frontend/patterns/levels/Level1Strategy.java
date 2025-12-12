package com.labubushooter.frontend.patterns.levels;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.labubushooter.frontend.objects.Platform;
import com.labubushooter.frontend.patterns.LevelStrategy;

public class Level1Strategy implements LevelStrategy {
    @Override
    public void loadPlatforms(Array<Platform> platforms, Texture platformTex) {
        // Platforms in visible area (x < 800)
        platforms.add(new Platform(500, 200, 200, 20, platformTex));

        // Platforms in scrolling area (x > 800)
        platforms.add(new Platform(1000, 250, 200, 20, platformTex));
        platforms.add(new Platform(1200, 300, 200, 20, platformTex)); // Debug platform at x=1200
        platforms.add(new Platform(1600, 200, 200, 20, platformTex));
        platforms.add(new Platform(2000, 250, 200, 20, platformTex)); // Debug platform at x=2000
        //platforms.add(new Platform(2200, 180, 150, 20, platformTex));
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