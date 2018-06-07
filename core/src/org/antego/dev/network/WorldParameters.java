package org.antego.dev.network;

/**
 * Created by anton on 02.01.2016.
 */
public class WorldParameters {
    private boolean leftPlayerIsHuman;

    public WorldParameters(boolean leftPlayerIsHuman) {
        this.leftPlayerIsHuman = leftPlayerIsHuman;
    }

    public boolean isLeftPlayerIsHuman() {
        return leftPlayerIsHuman;
    }

    public void setLeftPlayerIsHuman(boolean leftPlayerIsHuman) {
        this.leftPlayerIsHuman = leftPlayerIsHuman;
    }
}
