package com.labubushooter.frontend.patterns.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PistolRenderer implements WeaponRenderer{
    private final Texture texture;
    private final float width;
    private final float height;
    private final float gripX;
    private final float gripY;

    public PistolRenderer(Texture texture) {
        this.texture = texture;

        // Gunakan dimensi asli texture atau set manual
        this.width = texture != null ? texture.getWidth() : 25f;
        this.height = texture != null ? texture.getHeight() : 8f;

        // Grip point di belakang pistol (ujung kiri tengah)
        this.gripX = 2.5f;
        this.gripY = (height / 2f) + 1;
    }

    @Override
    public void render(SpriteBatch batch, float weaponX, float weaponY, float weaponAngle, boolean facingRight) {
        if (texture == null) return;

        // Y-axis flip jika facing left
        float scaleY = facingRight ? 1 : -1;

        batch.draw(
                texture,
                weaponX - gripX,           // x position (offset by grip)
                weaponY - gripY,           // y position (offset by grip)
                gripX,                     // origin x (rotation pivot)
                gripY,                     // origin y (rotation pivot)
                width,                     // width
                height,                    // height
                1,                         // scale x
                scaleY,                    // scale y (flip for left-facing)
                weaponAngle,               // rotation angle
                0, 0,                      // source x, y
                (int) width, (int) height, // source width, height
                false, false               // flip x, y
        );
    }

    @Override
    public Texture getTexture() {
        return texture;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public float getGripX() {
        return gripX;
    }

    @Override
    public float getGripY() {
        return gripY;
    }
}
