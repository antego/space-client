package org.antego.dev.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import org.antego.dev.PlanesGame;
import org.antego.dev.ScreenshooterApp;

/**
 * Created by anton on 28.01.2016.
 */
public class Screenshooter {

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = ScreenshooterApp.SCREENSHOT_WIDTH;
        config.height = ScreenshooterApp.SCREENSHOT_HEIGHT;
        new LwjglApplication(new ScreenshooterApp(), config);
    }
}
