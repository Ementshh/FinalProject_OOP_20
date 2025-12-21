package com.labubushooter.frontend.patterns.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class UnarmedRenderer implements WeaponRenderer {

    public UnarmedRenderer() {
    }

    @Override
    public void render(SpriteBatch batch, float weaponX, float weaponY, float weaponAngle, boolean facingRight) {
        // Render nothing for unarmed
    }

    @Override
    public Texture getTexture() {
        return null;
    }

    @Override
    public float getWidth() {
        return 0;
    }

    @Override
    public float getHeight() {
        return 0;
    }

    @Override
    public float getGripX() {
        return 0;
    }

    @Override
    public float getGripY() {
        return 0;
    }
}
