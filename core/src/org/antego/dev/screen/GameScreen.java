package org.antego.dev.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import org.antego.dev.PlanesGame;
import org.antego.dev.events.AccelEvent;
import org.antego.dev.events.ExitEvent;
import org.antego.dev.events.ShootEvent;
import org.antego.dev.events.GameEvent;
import org.antego.dev.events.RotateEvent;
import org.antego.dev.events.HitEvent;
import org.antego.dev.events.StatusEvent;
import org.antego.dev.network.OnlineSession;
import org.antego.dev.util.AnimationOnce;
import org.antego.dev.util.BulletData;
import org.antego.dev.util.Constants;
import org.antego.dev.util.Counter;
import org.antego.dev.util.PlaneData;
import org.antego.dev.util.WorldUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.antego.dev.util.Constants.*;

public class GameScreen extends InputAdapter implements Screen {
    private final Queue<GameEvent> networkEvents = new ConcurrentLinkedQueue<GameEvent>();
    private final PlanesGame game;
    private final OnlineSession session;
    private final ScheduledExecutorService updatePlaneStateService = Executors.newSingleThreadScheduledExecutor();

    private final World world;
    private final Body plane;
    private final Body enemyPlane;
    private final Box2DDebugRenderer renderer;
    private final OrthographicCamera camera;
//    private final int fontHeight;

    private final Set<Body> bodiesToDestroy = Collections.synchronizedSet(new HashSet<Body>());
    private final Map<Long, Body> bulletMap = new HashMap<Long, Body>();
    private final SpriteBatch batch = new SpriteBatch();
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private final Vector2 bulletPos = new Vector2();
    private final BitmapFont font;
    private final Counter counter = new Counter();
    private final Sprite starsSprite;
    private final Sprite settingsSprite;
    private final Sprite shootSprite;
    private final Rectangle settingsRectangle;
    private final Rectangle shootRectangle;
    private final Label continueLabel;
    private final Label exitLabel;
    private final AnimationOnce explosion;
    private Skin skin;
    private Stage stage = new Stage();
    private int lastPointer;

    private volatile boolean doShoot;
    private volatile boolean changeScreen;
    private volatile boolean showSettings;
    private volatile boolean opponentDisconnected;
    private volatile boolean endGame;
    private long lastShootTime;
    private float accumulator;

    public GameScreen(PlanesGame game, final OnlineSession session) {
        this.game = game;
        this.session = session;

        world = WorldUtils.createWorld();
        plane = WorldUtils.createPlane(world, session.getWorldParameters());
        enemyPlane = WorldUtils.createExternalPlane(world, session.getWorldParameters());

        font = new BitmapFont(Gdx.files.internal("data/arialBig.fnt"));
        font.setColor(Color.WHITE);
        font.getData().setScale((float)Gdx.graphics.getHeight() / 8 / 128);

        camera = new OrthographicCamera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0f);
        camera.update();

        starsSprite  = new Sprite(new Texture("staticStars.png"));
        float starsSide = Math.max(MAP_HEIGHT * WorldUtils.TO_SCREEN_HEIGHT, MAP_WIDTH * WorldUtils.TO_SCREEN_WIDTH);
        starsSprite.setSize(starsSide, starsSide);

        TextureAtlas explosionAtlas = new TextureAtlas("explosions.atlas");
        explosion = new AnimationOnce(1f/30, explosionAtlas.getRegions());

        settingsSprite = new Sprite(new Texture("settings-6-xl.png"));
        float settingsIconSize = Gdx.graphics.getHeight() / 15;
        settingsSprite.setSize(settingsIconSize, settingsIconSize);
        settingsSprite.setPosition(Gdx.graphics.getWidth() - settingsIconSize * 1.5f, Gdx.graphics.getHeight() - settingsIconSize * 1.5f);
        settingsRectangle = new Rectangle(settingsSprite.getX(),
                                            settingsSprite.getY(),
                                            Gdx.graphics.getWidth() - settingsSprite.getX(),
                                            Gdx.graphics.getHeight() - settingsSprite.getY());
        shootSprite = new Sprite(new Texture("cross_gun.png"));
        float shootIconSize = Gdx.graphics.getHeight() / 5;
        shootSprite.setSize(shootIconSize, shootIconSize);
        shootSprite.setPosition(Gdx.graphics.getWidth() - shootIconSize * 1.5f, shootIconSize * 0.5f);
        shootRectangle = new Rectangle(shootSprite.getX() - shootIconSize * 0.5f,
                0,
                Gdx.graphics.getWidth() - shootIconSize * 2,
                shootIconSize * 1.5f);

        renderer = new Box2DDebugRenderer();
        Gdx.input.setInputProcessor(this);
        updatePlaneStateService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                session.getSenderThread().addToQueue(new StatusEvent(plane.getPosition(), plane.getLinearVelocity().cpy(), plane.getAngle()));
            }
        }, 0, UPDATE_PERIOD_MILLIS, TimeUnit.MILLISECONDS);

        world.setContactListener(new ContactListener() {
            private void doContactLogic(Body plane, Body bullet) {
                if (plane == enemyPlane && !((BulletData) bullet.getUserData()).isEnemy()) {
                    bodiesToDestroy.add(bullet);
                    int numOfHits = ++((PlaneData) plane.getUserData()).numOfHits;
                    counter.incrementSelf();
                    session.getSenderThread().addToQueue(new HitEvent());
                    if (numOfHits >= 3) {
                    ((PlaneData) plane.getUserData()).setExploding(true);
                        glyphLayout.setText(font, "You won!");
                        swapInputProcessor();

                    }
                } else if (plane == GameScreen.this.plane && ((BulletData) bullet.getUserData()).isEnemy()) {
                    bodiesToDestroy.add(bullet);
                }
            }

            @Override
            public void beginContact(Contact contact) {
                Body body1 = contact.getFixtureA().getBody();
                Body body2 = contact.getFixtureB().getBody();
                if (body1.getUserData() instanceof BulletData &&
                        body2.getUserData() instanceof PlaneData) {
                    doContactLogic(body2, body1);
                } else if (body2.getUserData() instanceof BulletData &&
                        body1.getUserData() instanceof PlaneData) {
                    doContactLogic(body1, body2);
                }
            }

            @Override
            public void endContact(Contact contact) {
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });

        skin = new Skin(Gdx.files.internal("data/uiskin.json"));

        Table rootTable = new Table();
        rootTable.setFillParent(true);

        Table table = new Table(skin);
        continueLabel = new Label("Continue", skin);
        exitLabel = new Label("Exit", skin);
        continueLabel.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                showSettings = false;
                Gdx.input.setInputProcessor(GameScreen.this);
            }
        });
        exitLabel.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                changeScreen = true;
            }
        });
        table.add(continueLabel).spaceBottom(40);
        table.row();
        table.add(exitLabel);
        rootTable.add(table);
        stage.addActor(rootTable);
    }

    private void swapInputProcessor() {
        endGame = true;
        Gdx.input.setInputProcessor(new EndGameInputProcessor());
    }

    @Override
    public void show() {
        session.registerGameScreen(this);
    }

    @Override
    public void render(float delta) {
        //Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Fixed timestep
        accumulator += delta;

        while (accumulator >= TIME_STEP) {
            destroyBullets();

            GameEvent gameEvent = networkEvents.poll();
            if (gameEvent != null && !endGame) {
                applyEvent(gameEvent);
            }
            checkBoundsForBullets();
            checkBounds(plane);
            checkBounds(enemyPlane);
            accelerate(plane);
            accelerate(enemyPlane);
            rotateAndTruncateVelocity(plane);
            rotateAndTruncateVelocity(enemyPlane);
            if (doShoot) {
                doShoot();
                doShoot = false;
            }
            world.step(TIME_STEP, 6, 2);
            accumulator -= TIME_STEP;
        }
        batch.begin();
        float starsX = (VIEWPORT_WIDTH / 2 - camera.position.x) * WorldUtils.TO_SCREEN_WIDTH;
        float starsY = (VIEWPORT_HEIGHT / 2 - camera.position.y) * WorldUtils.TO_SCREEN_HEIGHT;
        starsSprite.setPosition(starsX, starsY);
        starsSprite.draw(batch);
        Iterator<Body> iter = bulletMap.values().iterator();
        while (iter.hasNext()) {
            Body bullet = iter.next();
            Sprite sprite = ((BulletData) bullet.getUserData()).getSprite();
            bulletPos.x = bullet.getPosition().x;
            bulletPos.y = bullet.getPosition().y;
            WorldUtils.toScreen(bulletPos, camera);
            bulletPos.x = bulletPos.x - sprite.getWidth() / 2;
            bulletPos.y = bulletPos.y - sprite.getHeight() / 2;
            sprite.setPosition(bulletPos.x, bulletPos.y);
            sprite.draw(batch);
        }
        drawSpriteOnBody(plane, delta, batch);
        if (!showSettings && !endGame) {
            settingsSprite.draw(batch);
            shootSprite.draw(batch);
        }
        drawSpriteOnBody(enemyPlane, delta, batch);
        if (!glyphLayout.toString().isEmpty()) {
            float w = glyphLayout.width;
            float h = glyphLayout.height;
            font.draw(batch, glyphLayout, Gdx.graphics.getWidth() / 2 - w / 2, Gdx.graphics.getHeight() / 2 + h / 2);
        }
        if (showSettings) {
            stage.draw();
        }
        counter.draw(batch);
        batch.end();
        updateCameraPosition();
//        renderer.render(world, camera.combined);
        if (changeScreen) {
            dispose();
            game.setScreen(new StartGameScreen(game, null));
        }
    }


    private void updateCameraPosition() {
        float planeX = plane.getPosition().x;
        float planeY = plane.getPosition().y;
        float camX = camera.position.x;
        float camY = camera.position.y;
        float camWidth = camera.viewportWidth;
        float camHeight = camera.viewportHeight;

        if (planeX > camX + camWidth / 2 - VIEWPORT_BUFFER && camX + camWidth / 2 < MAP_WIDTH) {
            float newPos = planeX - camWidth / 2 + VIEWPORT_BUFFER;
            camera.position.x = newPos + camWidth / 2 <= MAP_WIDTH ? newPos : MAP_WIDTH - camWidth / 2;
        } else if (planeX < camX - camWidth / 2 + VIEWPORT_BUFFER && camX - camWidth / 2 > 0) {
            float newPos = planeX + camWidth / 2 - VIEWPORT_BUFFER;
            camera.position.x = newPos - camWidth / 2 >= 0 ? newPos : camWidth / 2;
        }
        if (planeY > camY + camHeight / 2 - VIEWPORT_BUFFER && camY + camHeight / 2 < MAP_HEIGHT) {
            float newPos = planeY - camHeight / 2 + VIEWPORT_BUFFER;
            camera.position.y = newPos + camHeight / 2 <= MAP_HEIGHT ? newPos : MAP_HEIGHT - camHeight / 2;
        } else if (planeY < camY - camHeight / 2 + VIEWPORT_BUFFER && camY - camHeight / 2 > 0) {
            float newPos = planeY + camHeight / 2 - VIEWPORT_BUFFER;
            camera.position.y = newPos - camHeight / 2 >= 0 ? newPos : camHeight / 2;
        }
        camera.update();
    }
    //eliminate redundant object creation in render loop
    private Vector2 posToConvert = new Vector2();
    private void drawSpriteOnBody(Body plane, float delta, SpriteBatch batch) {
        PlaneData planeData = (PlaneData)plane.getUserData();
        Sprite sprite = planeData.getSprite();
        Vector2 spriteOffset = planeData.getSpriteOffset().cpy();
        spriteOffset.x = spriteOffset.x * WorldUtils.TO_SCREEN_WIDTH;
        spriteOffset.y = spriteOffset.y * WorldUtils.TO_SCREEN_HEIGHT;
        sprite.rotate(plane.getLinearVelocity().angle() - sprite.getRotation());
        posToConvert.x = plane.getPosition().x;
        posToConvert.y = plane.getPosition().y;
        WorldUtils.toScreen(posToConvert, camera);
        posToConvert.sub(spriteOffset);
        sprite.setPosition(posToConvert.x, posToConvert.y);
        if (!planeData.isExploding()) {
            sprite.draw(batch);
        } else {
            explosion.draw(delta, batch, posToConvert.x, posToConvert.y);
        }
    }

    private void checkBoundsForBullets() {
        for (Body bullet : bulletMap.values()) {
            checkBounds(bullet);
        }
    }

    private void destroyBullets() {
        Iterator<Map.Entry<Long, Body>> iter = bulletMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Long, Body> entry = iter.next();
            Body body = entry.getValue();
            if(System.nanoTime() - entry.getKey() > BULLET_TTL || bodiesToDestroy.contains(body)) {
                world.destroyBody(body);
                iter.remove();
                bodiesToDestroy.remove(body);
            }
        }
    }

    private void doShoot() {
        Body bullet = WorldUtils.createBullet(world, false);
        Sprite sprite = ((PlaneData)plane.getUserData()).getSprite();
        float originOffset = ((PlaneData)plane.getUserData()).getSpriteOffset().x / WorldUtils.TO_WORLD_WIDTH / sprite.getWidth();
        float bulletOffset = (1 - originOffset) * WorldUtils.TO_WORLD_WIDTH * sprite.getWidth();
        bullet.setTransform(plane.getPosition().add(new Vector2(bulletOffset + 0.2f, 0).rotateRad(plane.getAngle())), 0);
        Vector2 tanVelo = new Vector2(0, plane.getAngularVelocity());
        //todo angular velocity
        Vector2 bulletVelocity = Constants.BULLET_VELOCITY.cpy().add(tanVelo).rotateRad(plane.getAngle());
        bullet.setLinearVelocity(bulletVelocity);
        lastShootTime = System.nanoTime();
        bulletMap.put(lastShootTime, bullet);
        session.getSenderThread().addToQueue(new ShootEvent(bulletVelocity, bullet.getPosition()));
    }

    private void checkBounds(Body plane) {
        float planeX = plane.getPosition().x;
        float planeY = plane.getPosition().y;
        float angle = plane.getAngle();

        if (planeX < -VIEWPORT_BUFFER) {
            plane.setTransform(planeX + MAP_WIDTH + 2 * VIEWPORT_BUFFER, planeY, angle);
        } else if (planeX > MAP_WIDTH + VIEWPORT_BUFFER) {
            plane.setTransform(planeX - MAP_WIDTH - 2 * VIEWPORT_BUFFER, planeY, angle);
        }

        if (planeY < -VIEWPORT_BUFFER) {
            plane.setTransform(planeX, planeY + MAP_HEIGHT + 2 * VIEWPORT_BUFFER, angle);
        } else if (planeY > MAP_HEIGHT + VIEWPORT_BUFFER) {
            plane.setTransform(planeX, planeY - MAP_HEIGHT - 2 * VIEWPORT_BUFFER, angle);
        }
    }

    private void rotateAndTruncateVelocity(Body plane) {
        float curAngle = (float) (Math.toDegrees(plane.getAngle()) % 360);
        curAngle = curAngle <= 0 ? curAngle + 360 : curAngle;
        Vector2 vel = plane.getLinearVelocity().cpy();
        float deltaAngle = curAngle - vel.angle();
        vel.rotate(deltaAngle);
        plane.setLinearVelocity(vel);
    }

    private void accelerate(Body plane) {
        Vector2 velocity = plane.getLinearVelocity();
        velocity.scl(1 + TIME_STEP * ((PlaneData) plane.getUserData()).acceleration / velocity.len());
        if (velocity.len() < MIN_VELOCITY) {
            velocity.scl(MIN_VELOCITY / velocity.len());
        } else if (velocity.len() > MAX_VELOCITY) {
            velocity.scl(MAX_VELOCITY / velocity.len());
        }
        plane.setLinearVelocity(velocity);
    }

    private void applyEvent(GameEvent gameEvent) {
        //todo visitor pattern
        if (gameEvent instanceof StatusEvent) {
            enemyPlane.setTransform(((StatusEvent) gameEvent).getPosition(), ((StatusEvent) gameEvent).getAngle());
            enemyPlane.setLinearVelocity(((StatusEvent) gameEvent).getVelocity());
        } else if (gameEvent instanceof RotateEvent) {
            enemyPlane.setAngularVelocity(((RotateEvent) gameEvent).getAngularVelocity());
        } else if (gameEvent instanceof ShootEvent) {
            Body bullet = WorldUtils.createBullet(world, true);
            bullet.setTransform(((ShootEvent) gameEvent).getPosition(), 0f);
            bullet.setLinearVelocity(((ShootEvent) gameEvent).getVelocity());
            bulletMap.put(System.nanoTime(), bullet);
        } else if (gameEvent instanceof HitEvent) {
            int hits = ++((PlaneData)plane.getUserData()).numOfHits;
            counter.incrementEnemy();
            if (hits >= 3) {
                glyphLayout.setText(font, "You lose!");
                swapInputProcessor();
                ((PlaneData) plane.getUserData()).setExploding(true);
            }
        } else if (gameEvent instanceof AccelEvent) {
            ((PlaneData)enemyPlane.getUserData()).acceleration = ((AccelEvent) gameEvent).getAcceleration();
        } else if (gameEvent instanceof ExitEvent) {
            opponentDisconnected = true;
            session.cancel();
            swapInputProcessor();
            glyphLayout.setText(font, "enemy disconnected");
        }
    }

    public void addGameEvent(GameEvent event) {
        networkEvents.add(event);
    }

    @Override
    public boolean keyDown(int keycode) {
        Float rotateSpeed = null;
        Float acceleration = null;
        if (keycode == Input.Keys.RIGHT) {
            rotateSpeed = -angularSpeed;
        } else if (keycode == Input.Keys.LEFT) {
            rotateSpeed = angularSpeed;
        } else if (keycode == Input.Keys.UP) {
            acceleration = ACCELERATION;
        } else if (keycode == Input.Keys.DOWN) {
            acceleration = -ACCELERATION;
        } else if (keycode == Input.Keys.SPACE && System.nanoTime() - lastShootTime > SHOOT_PERIOD) {
            doShoot = true;
        }

        if (rotateSpeed != null) {
            session.getSenderThread().addToQueue(new RotateEvent(rotateSpeed));
            plane.setAngularVelocity(rotateSpeed);
        } else if (acceleration != null) {
            session.getSenderThread().addToQueue(new AccelEvent(acceleration));
            ((PlaneData)plane.getUserData()).acceleration = acceleration;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        Float rotateSpeed = null;
        Float acceleration = null;
        if (keycode == Input.Keys.RIGHT) {
            rotateSpeed = 0f;
        } else if (keycode == Input.Keys.LEFT) {
            rotateSpeed = 0f;
        } else if (keycode == Input.Keys.UP) {
            acceleration = 0f;
        } else if (keycode == Input.Keys.DOWN) {
            acceleration = 0f;
        }

        if (rotateSpeed != null) {
            session.getSenderThread().addToQueue(new RotateEvent(rotateSpeed));
            plane.setAngularVelocity(rotateSpeed);
        } else if (acceleration != null) {
            session.getSenderThread().addToQueue(new AccelEvent(acceleration));
            ((PlaneData)plane.getUserData()).acceleration = acceleration;
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        screenY = Gdx.graphics.getHeight() - screenY;
        Float rotateSpeed = null;
        if (settingsRectangle.contains(screenX, screenY)) {
            showSettings = true;
            Gdx.input.setInputProcessor(stage);
        } else if (shootRectangle.contains(screenX, screenY)) {
            if (System.nanoTime() - lastShootTime > SHOOT_PERIOD) {
                doShoot = true;
            }
        } else if (screenX < Gdx.graphics.getWidth() / 2) {
            lastPointer = pointer;
            rotateSpeed = angularSpeed;
        } else if (screenX > Gdx.graphics.getWidth() / 2) {
            lastPointer = pointer;
            rotateSpeed = -angularSpeed;
        }

        if (rotateSpeed != null) {
            session.getSenderThread().addToQueue(new RotateEvent(rotateSpeed));
            plane.setAngularVelocity(rotateSpeed);
        }
        //todo other controls
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        screenY = Gdx.graphics.getHeight() - screenY;
        Float rotateSpeed = null;
        if (!settingsRectangle.contains(screenX, screenY) &&
            !shootRectangle.contains(screenX, screenY)) {
            rotateSpeed = 0f;
        }
        if (rotateSpeed != null && pointer == lastPointer) {
            session.getSenderThread().addToQueue(new RotateEvent(rotateSpeed));
            plane.setAngularVelocity(rotateSpeed);
        }
        return false;
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        updatePlaneStateService.shutdown();
        session.closeSocket();
        world.dispose();
        Gdx.app.log(LOG_TAG, "socket closed in GameScreen");
    }

    private class EndGameInputProcessor extends InputAdapter {
        private long createTime = System.nanoTime();

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (System.nanoTime() - createTime > END_GAME_SCREEN_DURATION) {
                changeScreen = true;
            }
            return false;
        }
    }
}
