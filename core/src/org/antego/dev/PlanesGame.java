package org.antego.dev;

import com.badlogic.gdx.Game;

import org.antego.dev.screen.GameScreen;
import org.antego.dev.screen.MenuScreen;
import org.antego.dev.screen.StartGameScreen;

public class PlanesGame extends Game {
	private MenuScreen menuScreen;
	
	@Override
	public void create () {
        menuScreen = new MenuScreen(this, null);
        setScreen(menuScreen);
	}

    public MenuScreen getMenuScreen() {
        return menuScreen;
    }

    public void setMenuScreen(MenuScreen menuScreen) {
        this.menuScreen = menuScreen;
    }
}
