package org.antego.dev.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by anton on 28.12.2015.
 */
public class Constants {
    public static final int APP_WIDTH = 1500;
    public static final int APP_HEIGHT = 900;
    public static final String LOG_TAG = "Planes log";
    public static final Vector2 WORLD_GRAVITY = new Vector2(0, 0);
    public static final float TIME_STEP = 1 / 300f;

    public static final int VIEWPORT_WIDTH;
    public static final int VIEWPORT_HEIGHT;
    public static final int MAP_WIDTH = 40;
    public static final int MAP_HEIGHT = 24;
    public static final float VIEWPORT_BUFFER = 1.5f;
    public static final float PLANE_DENSITY = 0.5f;

    public static final float PLANE_GRAVITY_SCALE = 1f;
    public static final Vector2 leftPlanePos = new Vector2(2, 15);
    public static final Vector2 rightPlanePos = new Vector2(18, 15);
    public static final Vector2 velocityVector = new Vector2(6, 0);
    public static final float MAX_VELOCITY = 9;
    public static final float MIN_VELOCITY = 9;
    public static final float ACCELERATION = 2f;
    public static final float angularSpeed = 2 * (float)Math.PI / 2; //оборот за 3 секунды

    public static final int HOST_PORT = 9998;
    public static final String HOST_ADDRESS = "127.0.0.1";

    public static final float BULLET_DENSITY = 1f;
    public static final long SHOOT_PERIOD = 500*1000*1000;
    public static final Vector2 BULLET_VELOCITY = new Vector2(18, 0);
    public static final long BULLET_TTL = 1500*1000*1000;
    public static final float BULLET_RADIUS = 0.1f;
    public static final float STARS_DENSITY = 1 / 800f;
    public static final int UPDATE_PERIOD_MILLIS = 200;
    public static final long END_GAME_SCREEN_DURATION = 2000 * 1000 * 1000;

    static {
        float aspectRatio = (float)Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
        if (aspectRatio > MAP_WIDTH / MAP_HEIGHT) {
            VIEWPORT_WIDTH = MAP_WIDTH;
            VIEWPORT_HEIGHT = Gdx.graphics.getHeight() * MAP_WIDTH / Gdx.graphics.getWidth();
        } else {
            VIEWPORT_WIDTH = Gdx.graphics.getWidth() * MAP_HEIGHT / Gdx.graphics.getHeight();
            VIEWPORT_HEIGHT = MAP_HEIGHT;
        }
    }
}
