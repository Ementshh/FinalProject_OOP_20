package com.labubushooter.frontend.patterns.levels;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.labubushooter.frontend.objects.Platform;
import com.labubushooter.frontend.patterns.LevelStrategy;

public class Level5Strategy implements LevelStrategy {
    @Override
    public void loadPlatforms(Array<Platform> platforms, Texture platformTex) {
        // Oversized ground - extends beyond level boundaries for ultra-wide screens
        // This ensures no visual gaps regardless of screen resolution
        platforms.add(new Platform(-1000, 50, 4000, 50, platformTex));

        // Boss arena platforms - symmetric layout for combat
        // Left platform
        platforms.add(new Platform(100, 200, 150, 20, platformTex));

        // Right platform
        platforms.add(new Platform(550, 200, 150, 20, platformTex));

        // Center elevated platform
        platforms.add(new Platform(300, 350, 200, 20, platformTex));
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
        return 400f;
    }

    @Override
    public float getBossSpawnY() {
        return 200f;
    }
}