package com.labubushooter.frontend.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class GameObject {
    public Rectangle bounds;
    public Texture texture;

    public GameObject(float x, float y, float w, float h, Texture texture) {
        this.bounds = new Rectangle(x, y, w, h);
        this.texture = texture;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
