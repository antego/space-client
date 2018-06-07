package org.antego.dev.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import org.antego.dev.PlanesGame;

/**
 * Created by anton on 24.01.2016.
 */
public class MenuScreen extends InputAdapter implements Screen {
    private enum ScreenType {QUICK_GAME, FRIEND_GAME, NONE}

    private SpriteBatch batch = new SpriteBatch();
    private Skin skin;
    private Stage stage = new Stage();
    private PlanesGame game;
    private final ParticleEffect stars;
    private volatile ScreenType changeTo;

    public MenuScreen(final PlanesGame game, ParticleEffect stars) {
        this.game = game;
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));

        Table rootTable = new Table();
        rootTable.setFillParent(true);

        Table table = new Table(skin);
//        table.debug();
        final Label quickGameLabel = new Label("New Game", skin);
        final Label quitLabel = new Label("Quit", skin);
        quickGameLabel.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                changeTo = ScreenType.QUICK_GAME;
            }
        });
        quitLabel.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                changeTo = ScreenType.NONE;
            }
        });
        table.add(quickGameLabel).spaceBottom(40);
        table.row();
        table.add(quitLabel);
        rootTable.add(table);
        stage.addActor(rootTable);

        if (stars != null) {
            this.stars = stars;
        } else {
            this.stars = new ParticleEffect();
//            this.stars.load(new FileHandle("menuStars.particles"), new FileHandle(""));
            this.stars.load(Gdx.files.internal("menuStars.particles"), Gdx.files.internal(""));
            int pixelWidth = Gdx.graphics.getWidth();
            int pixelHeight = Gdx.graphics.getHeight();
            ParticleEmitter emitter = this.stars.getEmitters().first();
            emitter.setPosition(pixelWidth / 2, pixelHeight / 2);
            this.stars.start();
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if(changeTo != null) {
            switch (changeTo) {
                case QUICK_GAME:
                    game.setScreen(new StartGameScreen(game, stars));
                    dispose();
                    break;
                case FRIEND_GAME:
                    game.setScreen(new StartGameScreen(game, stars));
                    dispose();
                    break;
                case NONE:
                    dispose();
                    Gdx.app.exit();
            }
        }
        batch.begin();
        stars.draw(batch, delta);
        stage.draw();
        batch.end();

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

    }
}
