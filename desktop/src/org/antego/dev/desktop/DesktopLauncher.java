package org.antego.dev.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import org.antego.dev.util.Constants;
import org.antego.dev.PlanesGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = Constants.APP_WIDTH;
		config.height = Constants.APP_HEIGHT;
		new LwjglApplication(new PlanesGame(), config);
	}
}
