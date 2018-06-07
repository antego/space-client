package org.antego.dev.util;


import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class PlaneData {
    public int numOfHits;
    public volatile float acceleration;
    public Sprite sprite;
    private final Vector2 spriteOffset;
    private boolean exploding;

    public boolean isExploding() {
        return exploding;
    }

    public void setExploding(boolean exploding) {
        this.exploding = exploding;
    }

    public PlaneData(Vector2 spriteOffset) {
        this.spriteOffset = spriteOffset;
    }

    public Vector2 getSpriteOffset() {
        return spriteOffset;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }
}
