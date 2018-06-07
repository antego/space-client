package org.antego.dev.util;

import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Created by anton on 11.01.2016.
 */
public class BulletData {
    private Sprite sprite;
    private boolean enemy;

    public boolean isEnemy() {
        return enemy;
    }

    public void setEnemy(boolean enemy) {
        this.enemy = enemy;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }
}
