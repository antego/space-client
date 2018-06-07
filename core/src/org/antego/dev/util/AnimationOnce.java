package org.antego.dev.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Created by anton on 30.01.2016.
 */
public class AnimationOnce {
    private final Animation animation;
    private float accumulator;


    public AnimationOnce(float frameDuration, Array<TextureAtlas.AtlasRegion> keyFrames) {
        animation = new Animation(frameDuration, keyFrames);
    }

    public void draw(float delta, SpriteBatch batch, float x, float y) {
        if (accumulator < animation.getFrameDuration() * animation.getKeyFrames().length) {
            TextureRegion region = animation.getKeyFrame(accumulator);
            int scale = Gdx.graphics.getHeight() / (region.getRegionHeight() * 10);
            batch.draw(region, x, y, region.getRegionWidth() * (1 + scale), region.getRegionHeight() * (1 + scale));
            accumulator += delta;
        }
    }
}
