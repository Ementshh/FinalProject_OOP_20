package com.labubushooter.frontend.patterns;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.labubushooter.frontend.objects.Platform;

public interface LevelStrategy {
    void loadPlatforms(Array<Platform> platforms, Texture platformTex);

    float getLevelWidth();

    float getPlayerStartX();

    float getPlayerStartY();

    float getBossSpawnX();

    float getBossSpawnY();
}