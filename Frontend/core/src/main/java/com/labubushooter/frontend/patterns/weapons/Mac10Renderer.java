package com.labubushooter.frontend.patterns.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Mac10Renderer implements WeaponRenderer{
    private final Texture texture;
    private final float width;
    private final float height;
    private final float gripX;
    private final float gripY;

    public Mac10Renderer(Texture texture) {
        this.texture = texture;
        this.width = texture != null ? texture.getWidth() : 30f;
        this.height = texture != null ? texture.getHeight() : 10f;
        this.gripX = 5f;
        this.gripY = height / 2f;
    }

    @Override
    public void render(SpriteBatch batch, float weaponX, float weaponY, float weaponAngle, boolean facingRight) {
        if (texture == null) return;

        float scaleY = facingRight ? 1 : -1;

        batch.draw(
                texture,
                weaponX - gripX,
                weaponY - gripY,
                gripX,
                gripY,
                width,
                height,
                1,
                scaleY,
                weaponAngle,
                0, 0,
                (int) width, (int) height,
                false, false
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
