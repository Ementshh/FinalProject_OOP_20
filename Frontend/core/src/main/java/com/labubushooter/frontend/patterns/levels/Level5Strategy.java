package com.labubushooter.frontend.patterns.levels;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.labubushooter.frontend.objects.Platform;
import com.labubushooter.frontend.patterns.LevelStrategy;

public class Level5Strategy implements LevelStrategy {
    @Override
    public void loadPlatforms(Array<Platform> platforms, Texture platformTex) {
        // Boss arena platforms - symmetric layout for combat
        // Left platform
        platforms.add(new Platform(200, 200, 200, 20, platformTex));

        // Right platform
        platforms.add(new Platform(700, 200, 200, 20, platformTex));

        // Center elevated platform (centered at x=400)
        platforms.add(new Platform(450, 330, 200, 20, platformTex));

        // Exit platform (appears after boss defeat)
        // platforms.add(new Platform(getLevelWidth() - 100, 50, 100, 200,
        // platformTex));
    }

    @Override
    public float getLevelWidth() {
        return 800f;
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
        return 450f;
    }

    @Override
    public float getBossSpawnY() {
        return 200f;
    }
}