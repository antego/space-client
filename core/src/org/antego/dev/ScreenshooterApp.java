package org.antego.dev;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import static org.antego.dev.util.Constants.STARS_DENSITY;
import static org.antego.dev.util.Constants.TIME_STEP;

public class ScreenshooterApp extends ApplicationAdapter {
    public static final int SCREENSHOT_HEIGHT = 1024;
    public static final int SCREENSHOT_WIDTH = 1024;
    SpriteBatch batch;
    ParticleEffect stars = new ParticleEffect();;

    private static final int[] RGBA_OFFSETS = { 0, 1, 2, 3 };
    private static final int[] RGB_OFFSETS = { 0, 1, 2 };

    @Override
    public void create () {
        batch = new SpriteBatch();

        stars.load(new FileHandle("stars.particles"), new FileHandle(""));
        ParticleEmitter emitter = stars.getEmitters().first();
        emitter.getSpawnHeight().setHigh(SCREENSHOT_HEIGHT);
        emitter.getSpawnWidth().setHigh(SCREENSHOT_WIDTH);
        float square = SCREENSHOT_HEIGHT * SCREENSHOT_WIDTH;
        emitter.setMaxParticleCount((int) (square * STARS_DENSITY));
        emitter.setMinParticleCount((int) (square * STARS_DENSITY));
        emitter.setPosition(SCREENSHOT_WIDTH / 2, SCREENSHOT_HEIGHT / 2);
        stars.start();
        stars.update(1);
    }

    int i;
    float accumulator;
    @Override
    public void render () {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        accumulator += Gdx.graphics.getDeltaTime();

        while (accumulator >= TIME_STEP) {
            //stars.update(.033f);
            batch.begin();
            stars.draw(batch);
            batch.end();
            try {
                saveScreenshot("staticStars");
            } catch (IOException e) {
                e.printStackTrace();
            }
            accumulator -= TIME_STEP;
        }
    }

    public static void saveScreenshot(String baseName) throws IOException {
        File createTempFile = new File(baseName, ".png");
        saveScreenshot(createTempFile);
    }

    public static void saveScreenshot(File file) throws IOException {
        saveScreenshot(file, false);
    }

    public static void saveScreenshot(File file, boolean hasAlpha) throws IOException {
        if (Gdx.app.getType() == Application.ApplicationType.Android)
            return;

        byte[] screenshotPixels = ScreenUtils.getFrameBufferPixels(true);

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        saveScreenshot(file, screenshotPixels, width, height, hasAlpha);
    }

    public static void saveScreenshot(File file, byte[] pixels, int width, int height, boolean hasAlpha) throws IOException {
        DataBufferByte dataBuffer = new DataBufferByte(pixels, pixels.length);

        PixelInterleavedSampleModel sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, width, height, 4, 4 * width, getOffsets(hasAlpha));

        WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));

        BufferedImage img = new BufferedImage(getColorModel(hasAlpha), raster, false, null);

        ImageIO.write(img, "png", file);
    }

    private static ColorModel getColorModel(boolean alpha) {
        if (alpha)
            return new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 8 }, true, false, ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        return new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8 }, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
    }

    private static int[] getOffsets(boolean alpha) {
        if (alpha)
            return RGBA_OFFSETS;
        return RGB_OFFSETS;
    }
}
