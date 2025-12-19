package com.labubushooter.frontend.patterns.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface WeaponRenderer {
    void render(SpriteBatch batch, float weaponX, float weaponY, float weaponAngle, boolean facingRight);
    Texture getTexture();
    float getWidth();
    float getHeight();
    float getGripX();
    float getGripY();
}
