package org.antego.dev.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import org.antego.dev.BodyEditorLoader;
import org.antego.dev.network.WorldParameters;

/**
 * Created by anton on 28.12.2015.
 */
public class WorldUtils {
    public static final float TO_SCREEN_WIDTH = (float)Gdx.graphics.getWidth() / Constants.VIEWPORT_WIDTH;
    public static final float TO_SCREEN_HEIGHT = (float)Gdx.graphics.getHeight() / Constants.VIEWPORT_HEIGHT;
    public static final float TO_WORLD_WIDTH = (float)Constants.VIEWPORT_WIDTH / Gdx.graphics.getWidth();
    public static final float TO_WORLD_HEIGHT = (float)Constants.VIEWPORT_HEIGHT / Gdx.graphics.getHeight();
//    private static TextureAtlas atlas;

    public static World createWorld() {
//        atlas = new TextureAtlas(Gdx.files.internal("bullets.atlas"));
        return new World(Constants.WORLD_GRAVITY, true);
    }

    public static Body createPlane(World world, WorldParameters parameters) {
        Sprite planeSprite = new Sprite(new Texture(Gdx.files.internal("redship.png")));
        float factor = Constants.MAP_HEIGHT / Constants.VIEWPORT_HEIGHT * Gdx.graphics.getHeight() / 10 / planeSprite.getHeight();
        planeSprite.setSize(planeSprite.getWidth() * factor, planeSprite.getHeight() * factor);
        BodyEditorLoader loader = new BodyEditorLoader(Gdx.files.internal("data/plane.json"));
        Vector2 shapeOrigin = loader.getOrigin("plane", planeSprite.getWidth() * TO_WORLD_WIDTH).cpy();
        planeSprite.setOrigin(shapeOrigin.x * TO_SCREEN_WIDTH, shapeOrigin.y * TO_SCREEN_HEIGHT);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(parameters.isLeftPlayerIsHuman() ? Constants.leftPlanePos : Constants.rightPlanePos);
        Body body = world.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = Constants.PLANE_DENSITY;
        fixtureDef.filter.groupIndex = -1;
        loader.attachFixture(body, "plane", fixtureDef, planeSprite.getWidth() * TO_WORLD_WIDTH);
        body.setGravityScale(Constants.PLANE_GRAVITY_SCALE);
        body.setLinearVelocity(parameters.isLeftPlayerIsHuman() ? Constants.velocityVector.cpy() : Constants.velocityVector.cpy().scl(-1));
        //todo test this with ceiling
        body.setFixedRotation(true);
        PlaneData data = new PlaneData(shapeOrigin);
        data.setSprite(planeSprite);
        body.setUserData(data);
        return body;
    }

    public static Body createExternalPlane(World world, WorldParameters parameters) {
        Body plane = createPlane(world, parameters);

        Sprite sprite = new Sprite(new Texture(Gdx.files.internal("blueship.png")));
        float factor = Constants.MAP_HEIGHT / Constants.VIEWPORT_HEIGHT * Gdx.graphics.getHeight() / 10 / sprite.getHeight();
        sprite.setSize(sprite.getWidth() * factor, sprite.getHeight() * factor);
        Vector2 shapeOrigin = ((PlaneData)plane.getUserData()).getSpriteOffset();
        sprite.setOrigin(shapeOrigin.x * TO_SCREEN_WIDTH, shapeOrigin.y * TO_SCREEN_HEIGHT);

        plane.setTransform(parameters.isLeftPlayerIsHuman() ? Constants.rightPlanePos : Constants.leftPlanePos, 0);
        plane.setLinearVelocity(parameters.isLeftPlayerIsHuman() ? Constants.velocityVector.cpy().scl(-1) : Constants.velocityVector.cpy());
        PlaneData data = new PlaneData(shapeOrigin);
        data.setSprite(sprite);
        plane.setUserData(data);
        return plane;
    }

    public static Body createBullet(World world, boolean enemy) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.bullet = true;
        Body body = world.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(Constants.BULLET_RADIUS);
        fixtureDef.shape = shape;
        fixtureDef.density = Constants.BULLET_DENSITY;
        body.createFixture(fixtureDef);
        BulletData data = new BulletData();
        Sprite sprite;
        if (enemy) {
            sprite = new Sprite(new Texture("data/blueBullet.png"));
        } else {
            sprite = new Sprite(new Texture("data/redBullet.png"));
        }
        data.setSprite(sprite);
        data.setEnemy(enemy);
        body.setUserData(data);
        shape.dispose();
        return body;
    }

    public static Vector2 toScreen(Vector2 pos, OrthographicCamera camera) {
        pos.x = pos.x - camera.position.x + camera.viewportWidth / 2;
        pos.y = pos.y - camera.position.y + camera.viewportHeight / 2;
        pos.x = pos.x * TO_SCREEN_WIDTH;
        pos.y = pos.y * TO_SCREEN_HEIGHT;
        return pos;
    }

    public static Vector2 toWorld(Vector2 pos) {
        pos.x = pos.x * TO_WORLD_WIDTH;
        pos.y = pos.y * TO_WORLD_HEIGHT;
        return pos;
    }
}
