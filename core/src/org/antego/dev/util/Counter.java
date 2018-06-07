package org.antego.dev.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by anton on 31.01.2016.
 */
public class Counter {
    private final GlyphLayout counterGlyph;
    private final BitmapFont counterFont;
    private int selfCount;
    private int enemyCount;

    public Counter() {
        counterFont = new BitmapFont(Gdx.files.internal("data/arialBig.fnt"));
        counterFont.setColor(Color.WHITE);
        counterFont.getData().setScale((float)Gdx.graphics.getHeight() / 10 / 128);
        counterGlyph = new GlyphLayout(counterFont, "0 : 0");
    }

    public void draw(SpriteBatch batch) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        synchronized (counterGlyph) {
            float w = counterGlyph.width;
            float h = counterGlyph.height;
            counterFont.draw(batch, counterGlyph, screenWidth / 2 - w / 2, screenHeight - h);
        }
    }

    public void incrementSelf() {
        selfCount++;
        synchronized (counterGlyph) {
            counterGlyph.setText(counterFont, selfCount + " : " + enemyCount);
        }
    }

    public void incrementEnemy() {
        enemyCount++;
        synchronized (counterGlyph) {
            counterGlyph.setText(counterFont, selfCount + " : " + enemyCount);
        }
    }
}
